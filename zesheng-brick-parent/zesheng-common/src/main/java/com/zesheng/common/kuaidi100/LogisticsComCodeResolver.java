package com.zesheng.common.kuaidi100;

import java.util.Optional;

/**
 * 根据用户填写的物流公司名称解析快递100所需的 com 编码
 */
@FunctionalInterface
public interface LogisticsComCodeResolver {

    Optional<String> resolveByCompanyName(String logisticsCompanyName);
}
