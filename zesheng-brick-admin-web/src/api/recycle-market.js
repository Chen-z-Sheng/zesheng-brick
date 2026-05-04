import request from '@/utils/request'

const BASE = '/admin/recycle-market'

/** 一级分类 */
export function getLevel1List() {
    return request.get(`${BASE}/level1`).then(res => res.data)
}

export function createLevel1(data) {
    return request.post(`${BASE}/level1`, data).then(res => res.data)
}

export function updateLevel1(id, data) {
    return request.patch(`${BASE}/level1/${id}`, data).then(res => res.data)
}

export function deleteLevel1(id) {
    return request.delete(`${BASE}/level1/${id}`).then(res => res.data)
}

/** 二级分类 */
export function getLevel2List(level1Id) {
    return request.get(`${BASE}/level2`, { params: { level1Id } }).then(res => res.data)
}

export function createLevel2(data) {
    return request.post(`${BASE}/level2`, data).then(res => res.data)
}

export function updateLevel2(id, data) {
    return request.patch(`${BASE}/level2/${id}`, data).then(res => res.data)
}

export function deleteLevel2(id) {
    return request.delete(`${BASE}/level2/${id}`).then(res => res.data)
}

/** 三级分类 */
export function getLevel3List(level2Id) {
    return request.get(`${BASE}/level3`, { params: { level2Id } }).then(res => res.data)
}

export function createLevel3(data) {
    return request.post(`${BASE}/level3`, data).then(res => res.data)
}

export function updateLevel3(id, data) {
    return request.patch(`${BASE}/level3/${id}`, data).then(res => res.data)
}

export function deleteLevel3(id) {
    return request.delete(`${BASE}/level3/${id}`).then(res => res.data)
}

/** 行情 */
export function getLatestPriceDate() {
    return request.get(`${BASE}/price/latest-date`).then(res => res.data)
}

/** 按日期查询当日所有品类回收价格（行情报单详情自动填充单价用） */
export function getPricesByDate(date) {
    return request.get(`${BASE}/price/by-date`, { params: { date } }).then(res => res.data)
}

export function getPricePage(params) {
    return request.get(`${BASE}/price/page`, { params }).then(res => res.data)
}

export function getPriceById(id) {
    return request.get(`${BASE}/price/detail/${id}`).then(res => res.data)
}

export function createPrice(data) {
    return request.post(`${BASE}/price`, data).then(res => res.data)
}

export function updatePrice(id, data) {
    return request.patch(`${BASE}/price/detail/${id}`, data).then(res => res.data)
}

export function deletePrice(id) {
    return request.delete(`${BASE}/price/detail/${id}`).then(res => res.data)
}
