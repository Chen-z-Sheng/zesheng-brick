package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.admin.entity.UserPaymentInfo;
import com.zesheng.admin.mapper.UserPaymentInfoMapper;
import com.zesheng.admin.service.IUserPaymentInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户收款信息查询实现
 */
@Service
public class UserPaymentInfoServiceImpl implements IUserPaymentInfoService {

    @Resource
    private UserPaymentInfoMapper userPaymentInfoMapper;

    @Override
    public UserPaymentInfo getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return userPaymentInfoMapper.selectOne(
                new LambdaQueryWrapper<UserPaymentInfo>().eq(UserPaymentInfo::getUserId, userId));
    }
}
