const { get } = require('../utils/request');

/**
 * 一级分类（含二级树）
 */
const getCategories = () => get('/recycle-market/categories');

/**
 * 二级分类列表
 */
const getSubCategories = (level1Id) => get('/recycle-market/sub-categories', { level1Id });

/**
 * 三级分类列表
 */
const getThirdCategories = (level2Id) => get('/recycle-market/third-categories', { level2Id });

/**
 * 商品列表（带行情价格）
 * @param {number} level1Id - 一级分类ID
 * @param {number} level2Id - 二级分类ID（可选，与level1Id二选一或同时传）
 * @param {number} level3Id - 三级分类ID（可选）
 * @param {string} priceDate - 行情日期 YYYY-MM-DD（可选，默认当天）
 */
const getProducts = (level1Id, level2Id, level3Id, priceDate) => {
    const params = {};
    if (level1Id != null) params.level1Id = level1Id;
    if (level2Id != null) params.level2Id = level2Id;
    if (level3Id != null) params.level3Id = level3Id;
    if (priceDate) params.priceDate = priceDate;
    return get('/recycle-market/products', params);
};

/**
 * 商品详情（priceDate 为空时传当天日期，避免后端收到 null 报错）
 */
const getProductDetail = (level3Id, priceDate) => {
    const params = {};
    if (level3Id != null) params.level3Id = level3Id;
    const date = priceDate || new Date().toISOString().slice(0, 10);
    if (date) params.priceDate = date;
    return get('/recycle-market/product/detail', params);
};

/**
 * 历史行情
 */
const getPriceHistory = (level3Id, limit = 30) => {
    const params = { limit };
    if (level3Id != null) params.level3Id = level3Id;
    return get('/recycle-market/price-history', params);
};

module.exports = {
    getCategories,
    getSubCategories,
    getThirdCategories,
    getProducts,
    getProductDetail,
    getPriceHistory
};
