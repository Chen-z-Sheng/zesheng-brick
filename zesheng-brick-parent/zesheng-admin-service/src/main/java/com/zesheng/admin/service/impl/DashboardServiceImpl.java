package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.admin.entity.ClientUser;
import com.zesheng.admin.entity.FormSubmission;
import com.zesheng.admin.entity.SellOrderSubmission;
import com.zesheng.admin.mapper.ClientUserMapper;
import com.zesheng.admin.mapper.FormSubmissionMapper;
import com.zesheng.admin.mapper.SellOrderSubmissionMapper;
import com.zesheng.admin.model.response.DashboardDayCountItem;
import com.zesheng.admin.model.response.DashboardOverviewResponse;
import com.zesheng.admin.model.response.DashboardServerRuntimeVO;
import com.zesheng.admin.service.IDashboardService;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页统计：业务表按日聚合 + 本机运行指标
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private static final int RECENT_DAYS = 7;
    private static final Path LINUX_MEMINFO = Path.of("/proc/meminfo");

    private final FormSubmissionMapper formSubmissionMapper;
    private final ClientUserMapper clientUserMapper;
    private final SellOrderSubmissionMapper sellOrderSubmissionMapper;

    @Override
    public DashboardOverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(RECENT_DAYS - 1);
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM-dd");

        List<DashboardDayCountItem> formDaily = new ArrayList<>();
        for (LocalDate d = startDay; !d.isAfter(today); d = d.plusDays(1)) {
            formDaily.add(new DashboardDayCountItem(d.format(labelFmt), countFormForDay(d)));
        }

        List<DashboardDayCountItem> userDaily = new ArrayList<>();
        for (LocalDate d = startDay; !d.isAfter(today); d = d.plusDays(1)) {
            userDaily.add(new DashboardDayCountItem(d.format(labelFmt), countClientUserForDay(d)));
        }

        List<DashboardDayCountItem> sellDaily = new ArrayList<>();
        for (LocalDate d = startDay; !d.isAfter(today); d = d.plusDays(1)) {
            sellDaily.add(new DashboardDayCountItem(d.format(labelFmt), countSellOrderForDay(d)));
        }

        return DashboardOverviewResponse.builder()
                .formSubmissionDaily(formDaily)
                .formSubmissionToday(countFormForDay(today))
                .formSubmissionYesterday(countFormForDay(today.minusDays(1)))
                .clientUserTotal(clientUserMapper.selectCount(null))
                .clientUserRegisterDaily(userDaily)
                .clientUserRegisterToday(countClientUserForDay(today))
                .clientUserRegisterYesterday(countClientUserForDay(today.minusDays(1)))
                .sellOrderDaily(sellDaily)
                .sellOrderToday(countSellOrderForDay(today))
                .sellOrderYesterday(countSellOrderForDay(today.minusDays(1)))
                .serverRuntime(buildServerRuntime())
                .build();
    }

    @Override
    public DashboardServerRuntimeVO getServerRuntime() {
        return buildServerRuntime();
    }

    private long countFormForDay(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return formSubmissionMapper.selectCount(
                new LambdaQueryWrapper<FormSubmission>()
                        .ge(FormSubmission::getCreatedAt, start)
                        .lt(FormSubmission::getCreatedAt, end));
    }

    private long countClientUserForDay(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return clientUserMapper.selectCount(
                new LambdaQueryWrapper<ClientUser>()
                        .ge(ClientUser::getCreatedAt, start)
                        .lt(ClientUser::getCreatedAt, end));
    }

    private long countSellOrderForDay(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return sellOrderSubmissionMapper.selectCount(
                new LambdaQueryWrapper<SellOrderSubmission>()
                        .ge(SellOrderSubmission::getCreatedAt, start)
                        .lt(SellOrderSubmission::getCreatedAt, end));
    }

    private DashboardServerRuntimeVO buildServerRuntime() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long heapUsed = heap.getUsed();
        long heapMax = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        double heapPct = heapMax > 0 ? (heapUsed * 100.0 / heapMax) : 0.0;

        Double cpuPct = null;
        Double memPct = readPhysicalMemoryUsedPercent();
        try {
            OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double load = os.getSystemCpuLoad();
            if (load >= 0) {
                cpuPct = round2(load * 100.0);
            }
            if (memPct == null) {
                memPct = readPhysicalMemoryUsedPercentFromJmx(os);
            }
        } catch (Throwable e) {
            log.debug("读取 OSBean 指标失败: {}", e.getMessage());
        }

        Double diskPct = null;
        String diskNote = null;
        try {
            Path base = Path.of("").toAbsolutePath();
            File root = base.getRoot() != null ? base.getRoot().toFile() : base.toFile();
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            diskNote = root.getAbsolutePath();
            if (total > 0) {
                diskPct = round2((total - free) * 100.0 / total);
            }
        } catch (Throwable e) {
            log.debug("读取磁盘使用率失败: {}", e.getMessage());
        }

        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();

        return DashboardServerRuntimeVO.builder()
                .systemCpuLoadPercent(cpuPct)
                .physicalMemoryUsedPercent(memPct)
                .jvmHeapUsedPercent(round2(heapPct))
                .diskUsedPercent(diskPct)
                .diskPathNote(diskNote)
                .jvmUptimeMs(rt.getUptime())
                .build();
    }

    /**
     * 物理内存使用率：Linux 优先读 /proc/meminfo 的 MemAvailable（与 free、云监控口径一致）
     */
    private static Double readPhysicalMemoryUsedPercent() {
        if (!Files.isReadable(LINUX_MEMINFO)) {
            return null;
        }
        long memTotalKb = -1;
        long memAvailableKb = -1;
        long memFreeKb = -1;
        long cachedKb = 0;
        long buffersKb = 0;
        try (BufferedReader reader = Files.newBufferedReader(LINUX_MEMINFO, StandardCharsets.US_ASCII)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    memTotalKb = parseMeminfoValueKb(line);
                } else if (line.startsWith("MemAvailable:")) {
                    memAvailableKb = parseMeminfoValueKb(line);
                } else if (line.startsWith("MemFree:")) {
                    memFreeKb = parseMeminfoValueKb(line);
                } else if (line.startsWith("Cached:")) {
                    cachedKb = parseMeminfoValueKb(line);
                } else if (line.startsWith("Buffers:")) {
                    buffersKb = parseMeminfoValueKb(line);
                }
                if (memTotalKb >= 0 && memAvailableKb >= 0) {
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }
        if (memTotalKb <= 0) {
            return null;
        }
        if (memAvailableKb < 0) {
            // 旧内核无 MemAvailable 时近似估算
            if (memFreeKb < 0) {
                return null;
            }
            memAvailableKb = memFreeKb + cachedKb + buffersKb;
        }
        long usedKb = memTotalKb - memAvailableKb;
        if (usedKb < 0) {
            usedKb = 0;
        }
        return round2(usedKb * 100.0 / memTotalKb);
    }

    private static Double readPhysicalMemoryUsedPercentFromJmx(OperatingSystemMXBean os) {
        long totalMem = os.getTotalPhysicalMemorySize();
        long freeMem = os.getFreePhysicalMemorySize();
        if (totalMem <= 0) {
            return null;
        }
        return round2((totalMem - freeMem) * 100.0 / totalMem);
    }

    private static long parseMeminfoValueKb(String line) {
        int colon = line.indexOf(':');
        if (colon < 0) {
            return -1;
        }
        String num = line.substring(colon + 1).trim();
        int space = num.indexOf(' ');
        if (space > 0) {
            num = num.substring(0, space);
        }
        try {
            return Long.parseLong(num);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
