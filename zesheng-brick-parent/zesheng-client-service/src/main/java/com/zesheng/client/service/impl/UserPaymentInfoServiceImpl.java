package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.client.entity.UserPaymentInfo;
import com.zesheng.client.mapper.UserPaymentInfoMapper;
import com.zesheng.client.model.request.UserPaymentInfoSaveRequest;
import com.zesheng.client.model.request.UserPaymentInfoUpdateRequest;
import com.zesheng.client.service.IUserPaymentInfoService;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserPaymentInfoServiceImpl implements IUserPaymentInfoService {

    @Resource
    private UserPaymentInfoMapper userPaymentInfoMapper;

    @Override
    public IPage<UserPaymentInfo> page(PageAndSortQueryRequest queryDto) {
        Page<UserPaymentInfo> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        LambdaQueryWrapper<UserPaymentInfo> wrapper = new LambdaQueryWrapper<>();
        String orderBy = queryDto.getOrderBy();
        if (orderBy != null && !orderBy.isBlank()) {
            boolean asc = "ASC".equalsIgnoreCase(queryDto.getOrder());
            wrapper.orderBy(true, asc, "id".equals(orderBy) ? UserPaymentInfo::getId : UserPaymentInfo::getUpdatedAt);
        } else {
            wrapper.orderByDesc(UserPaymentInfo::getUpdatedAt);
        }
        return userPaymentInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UserPaymentInfo> list() {
        return userPaymentInfoMapper.selectList(
                new LambdaQueryWrapper<UserPaymentInfo>().orderByDesc(UserPaymentInfo::getUpdatedAt));
    }

    @Override
    public UserPaymentInfo getById(Long id) {
        return userPaymentInfoMapper.selectById(id);
    }

    @Override
    public UserPaymentInfo getByUserId(Long userId) {
        return userPaymentInfoMapper.selectOne(
                new LambdaQueryWrapper<UserPaymentInfo>().eq(UserPaymentInfo::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserPaymentInfo> save(UserPaymentInfoSaveRequest request) {
        UserPaymentInfo exist = getByUserId(request.getUserId());
        if (exist != null) {
            return R.error(ResultCodeEnum.USER_PAYMENT_INFO_EXIST);
        }
        UserPaymentInfo entity = new UserPaymentInfo();
        entity.setUserId(request.getUserId());
        entity.setRealName(request.getRealName());
        entity.setAlipayAccount(request.getAlipayAccount());
        entity.setWechatQrcode(request.getWechatQrcode());
        entity.setAlipayQrcode(request.getAlipayQrcode());
        entity.setBankCardNo(request.getBankCardNo());
        entity.setBankName(request.getBankName());
        entity.setBankBranch(request.getBankBranch());
        userPaymentInfoMapper.insert(entity);
        return R.success(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserPaymentInfo> update(Long id, UserPaymentInfoUpdateRequest request) {
        UserPaymentInfo entity = userPaymentInfoMapper.selectById(id);
        if (entity == null) {
            return R.error(ResultCodeEnum.USER_PAYMENT_INFO_NOT_FOUND);
        }
        if (request.getRealName() != null) {
            entity.setRealName(request.getRealName());
        }
        if (request.getAlipayAccount() != null) {
            entity.setAlipayAccount(request.getAlipayAccount());
        }
        if (request.getWechatQrcode() != null) {
            entity.setWechatQrcode(request.getWechatQrcode());
        }
        if (request.getAlipayQrcode() != null) {
            entity.setAlipayQrcode(request.getAlipayQrcode());
        }
        if (request.getBankCardNo() != null) {
            entity.setBankCardNo(request.getBankCardNo());
        }
        if (request.getBankName() != null) {
            entity.setBankName(request.getBankName());
        }
        if (request.getBankBranch() != null) {
            entity.setBankBranch(request.getBankBranch());
        }
        userPaymentInfoMapper.updateById(entity);
        return R.success(userPaymentInfoMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        UserPaymentInfo entity = userPaymentInfoMapper.selectById(id);
        if (entity == null) {
            return R.error(ResultCodeEnum.USER_PAYMENT_INFO_NOT_FOUND);
        }
        int rows = userPaymentInfoMapper.deleteById(id);
        return R.success(rows);
    }
}
