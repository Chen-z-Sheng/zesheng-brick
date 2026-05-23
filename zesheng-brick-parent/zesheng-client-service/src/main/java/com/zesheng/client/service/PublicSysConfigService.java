package com.zesheng.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.client.entity.SysConfig;
import com.zesheng.client.mapper.SysConfigMapper;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.response.R;
import com.zesheng.common.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * C 端可读系统配置（仅白名单 key，避免任意键遍历）；热点键短 TTL 缓存降低数据库压力
 */
@Service
public class PublicSysConfigService {

    private static final Set<String> PUBLIC_CONFIG_KEYS = Set.of(
            "cheap_express_mini_program",
            "admin_wechat_account",
            "business_wechat_account");

    private static final String CACHE_KEY_PREFIX = "sys:pub-config:v1:";
    private static final long CACHE_TTL_SECONDS = 180L;

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private RedisUtil redisUtil;

    public R<String> getPublicValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey) || !PUBLIC_CONFIG_KEYS.contains(configKey.trim())) {
            return R.error(ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        String key = configKey.trim();
        String cacheKey = CACHE_KEY_PREFIX + key;
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof String str && StringUtils.hasText(str)) {
            return R.success(str.trim());
        }

        SysConfig row = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (row == null || !StringUtils.hasText(row.getValue())) {
            return R.error(ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        String value = row.getValue().trim();
        redisUtil.set(cacheKey, value, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return R.success(value);
    }
}
