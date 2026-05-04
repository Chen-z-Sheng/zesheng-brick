package com.zesheng.common.kuaidi100;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 快递100 API 配置（key、customer 等在开放平台企业中心获取）
 */
@Data
@ConfigurationProperties(prefix = "kuaidi100")
public class Kuaidi100Properties {

    /**
     * 是否启用调用
     */
    private boolean enabled = false;

    private String key = "";

    private String customer = "";

    /**
     * 订阅推送回调校验用 salt，无则空串
     */
    private String subscribeSalt = "";
}
