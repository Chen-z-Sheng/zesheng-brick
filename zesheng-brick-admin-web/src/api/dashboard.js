import request from '@/utils/request'

/**
 * 首页仪表盘：业务趋势 + 本机运行指标
 */
export function getDashboardOverview() {
  return request({
    url: '/admin/dashboard/overview',
    method: 'get',
  })
}

/** 仅运行环境（CPU/内存/JVM/磁盘），用于定时轮询 */
export function getDashboardServerRuntime() {
  return request({
    url: '/admin/dashboard/server-runtime',
    method: 'get',
  })
}
