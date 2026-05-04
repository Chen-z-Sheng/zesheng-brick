package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理端进程所在机器/JVM 运行指标（供首页展示，部署到服务器即可看当前主机大致负载）
 */
@Data
@Builder
@Schema(description = "服务运行环境指标")
public class DashboardServerRuntimeVO {

    @Schema(description = "系统 CPU 使用率 0~100，不可用时为 null")
    private Double systemCpuLoadPercent;

    @Schema(description = "物理内存使用率 0~100，不可用时为 null")
    private Double physicalMemoryUsedPercent;

    @Schema(description = "JVM 堆内存使用率 0~100")
    private Double jvmHeapUsedPercent;

    @Schema(description = "应用工作目录所在磁盘使用率 0~100")
    private Double diskUsedPercent;

    @Schema(description = "磁盘路径说明，如 user.dir")
    private String diskPathNote;

    @Schema(description = "JVM 已运行毫秒数")
    private Long jvmUptimeMs;
}
