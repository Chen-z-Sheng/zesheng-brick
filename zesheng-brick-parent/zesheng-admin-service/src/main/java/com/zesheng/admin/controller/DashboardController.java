package com.zesheng.admin.controller;

import com.zesheng.admin.model.response.DashboardOverviewResponse;
import com.zesheng.admin.model.response.DashboardServerRuntimeVO;
import com.zesheng.admin.service.IDashboardService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端首页仪表盘
 */
@RestController
@RequestMapping("dashboard")
@Tag(name = "管理端-仪表盘", description = "首页业务统计与运行环境指标")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "首页概览", description = "近7日业务趋势、累计注册用户数、当前进程所在机器资源占用")
    public R<DashboardOverviewResponse> overview() {
        return R.success(dashboardService.getOverview());
    }

    @GetMapping("/server-runtime")
    @Operation(summary = "运行环境指标", description = "仅 JVM/主机采样，供首页定时刷新，不做业务库查询")
    public R<DashboardServerRuntimeVO> serverRuntime() {
        return R.success(dashboardService.getServerRuntime());
    }
}
