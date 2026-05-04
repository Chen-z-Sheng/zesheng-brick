package com.zesheng.admin.service;

import com.zesheng.admin.entity.DeliveryAddress;
import com.zesheng.admin.model.request.DeliveryAddressSaveRequest;
import com.zesheng.admin.model.request.DeliveryAddressUpdateRequest;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 下单地址服务
 */
public interface IDeliveryAddressService {

    /**
     * 列表，status 为空返回全部（管理端），为 1 仅启用（方案下拉用）
     */
    R<List<DeliveryAddress>> list(Integer status);

    R<DeliveryAddress> save(DeliveryAddressSaveRequest request);

    R<DeliveryAddress> update(Long id, DeliveryAddressUpdateRequest request);

    R<Integer> delete(Long id);
}
