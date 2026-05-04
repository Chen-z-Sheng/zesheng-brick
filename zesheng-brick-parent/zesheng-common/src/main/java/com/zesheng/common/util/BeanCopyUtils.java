package com.zesheng.common.util;

import cn.hutool.core.bean.BeanUtil;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.BizException;

public class BeanCopyUtils {
    /**
     * 拷贝对象，忽略null值
     * @param source 源对象
     * @param targetClass 目标类
     * @return 拷贝后的目标对象
     */
    public static <T> T copyIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T target;
        try {
            // 创建目标类的空实例
            target = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "创建目标类实例失败，请确保目标类有无参构造方法", e);
        }
        // 核心：拷贝属性，第三个参数true=忽略null值
        BeanUtil.copyProperties(source, target, true);
        return target;
    }

    /**
     * 拷贝对象到已有实例，忽略null值（更新场景专用）
     * @param source 源对象
     * @param target 已存在的目标对象
     */
    public static void copyIgnoreNullToExist(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        // 把source的非空属性复制到已有target对象中
        BeanUtil.copyProperties(source, target, true);
    }
}