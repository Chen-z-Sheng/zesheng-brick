/**
 * 方案等模块通用状态配置：0=停用 1=启用 2=草稿
 */
export const STATUS_SCHEME = [
    { value: 0, label: '停用', tagType: 'info', color: '#909399' },
    { value: 1, label: '启用', tagType: 'success', color: '#67c23a' },
    { value: 2, label: '草稿', tagType: 'warning', color: '#e6a23c' },
]

export function getStatusConfig(status) {
    return STATUS_SCHEME.find((item) => item.value === status) || { label: '-', tagType: 'info', color: '#909399' }
}

