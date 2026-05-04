package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.admin.entity.DeliveryAddress;
import com.zesheng.admin.mapper.DeliveryAddressMapper;
import com.zesheng.admin.model.request.DeliveryAddressSaveRequest;
import com.zesheng.admin.model.request.DeliveryAddressUpdateRequest;
import com.zesheng.admin.service.IDeliveryAddressService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAddressServiceImpl implements IDeliveryAddressService {

    private final DeliveryAddressMapper deliveryAddressMapper;

    @Override
    @Transactional(readOnly = true)
    public R<List<DeliveryAddress>> list(Integer status) {
        LambdaQueryWrapper<DeliveryAddress> wrapper = new LambdaQueryWrapper<DeliveryAddress>()
                .orderByAsc(DeliveryAddress::getSortOrder)
                .orderByAsc(DeliveryAddress::getId);
        if (status != null) {
            wrapper.eq(DeliveryAddress::getStatus, status);
        }
        List<DeliveryAddress> list = deliveryAddressMapper.selectList(wrapper);
        return R.success(list);
    }

    @Override
    public R<DeliveryAddress> save(DeliveryAddressSaveRequest request) {
        DeliveryAddress entity = BeanCopyUtils.copyIgnoreNull(request, DeliveryAddress.class);
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        deliveryAddressMapper.insert(entity);
        return R.success(entity);
    }

    @Override
    public R<DeliveryAddress> update(Long id, DeliveryAddressUpdateRequest request) {
        Assert.notNull(id, "地址ID不能为空");
        DeliveryAddress exist = deliveryAddressMapper.selectById(id);
        if (exist == null) {
            return R.error("地址不存在");
        }
        BeanCopyUtils.copyIgnoreNullToExist(request, exist);
        deliveryAddressMapper.updateById(exist);
        return R.success(exist);
    }

    @Override
    public R<Integer> delete(Long id) {
        Assert.notNull(id, "地址ID不能为空");
        DeliveryAddress exist = deliveryAddressMapper.selectById(id);
        if (exist == null) {
            return R.error("地址不存在");
        }
        int n = deliveryAddressMapper.deleteById(id);
        return R.success(n);
    }
}
