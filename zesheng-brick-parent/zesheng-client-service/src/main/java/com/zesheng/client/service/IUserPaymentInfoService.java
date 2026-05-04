package com.zesheng.client.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.UserPaymentInfo;
import com.zesheng.client.model.request.UserPaymentInfoSaveRequest;
import com.zesheng.client.model.request.UserPaymentInfoUpdateRequest;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 用户收款信息Service接口
 */
public interface IUserPaymentInfoService {

    IPage<UserPaymentInfo> page(PageAndSortQueryRequest queryDto);

    List<UserPaymentInfo> list();

    UserPaymentInfo getById(Long id);

    UserPaymentInfo getByUserId(Long userId);

    R<UserPaymentInfo> save(UserPaymentInfoSaveRequest request);

    R<UserPaymentInfo> update(Long id, UserPaymentInfoUpdateRequest request);

    R<Integer> deleteById(Long id);
}
