package com.zesheng.admin.service;

import com.zesheng.admin.model.response.DashboardOverviewResponse;
import com.zesheng.admin.model.response.DashboardServerRuntimeVO;

/**
 * 管理端首页仪表盘
 */
public interface IDashboardService {

    DashboardOverviewResponse getOverview();

    /**
     * 仅采集本进程所在主机/JVM 指标，供前端高频轮询，避免重复扫库
     */
    DashboardServerRuntimeVO getServerRuntime();
}
