package com.zesheng.common.constant;

/**
 * 服务间调用约定请求头（C 端 → 管理端 /pub/recycle-market 等）
 */
public final class InternalApiHeaders {

    /**
     * 与配置项 zesheng.internal.recycle-market-api-key 对应，须与客户端 RestTemplate 携带值一致
     */
    public static final String RECYCLE_MARKET_INTERNAL_KEY = "X-Zesheng-Internal-Key";

    private InternalApiHeaders() {
    }
}
