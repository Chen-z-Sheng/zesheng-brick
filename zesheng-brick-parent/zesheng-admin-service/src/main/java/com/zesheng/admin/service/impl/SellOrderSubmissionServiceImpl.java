package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.ClientUser;
import com.zesheng.admin.entity.SellOrderSubmission;
import com.zesheng.admin.entity.UserPaymentInfo;
import com.zesheng.admin.mapper.ClientUserMapper;
import com.zesheng.admin.mapper.SellOrderSubmissionMapper;
import com.zesheng.admin.mapper.UserPaymentInfoMapper;
import com.zesheng.admin.model.request.SellOrderSubmissionPageRequest;
import com.zesheng.admin.model.request.SellOrderSubmissionUpdateRequest;
import com.zesheng.admin.service.ISellOrderSubmissionService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.ExpressNoSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 行情报单提交记录 Service 实现（单表 items_json）
 */
@Service
public class SellOrderSubmissionServiceImpl implements ISellOrderSubmissionService {

    @Resource
    private SellOrderSubmissionMapper sellOrderSubmissionMapper;
    @Resource
    private UserPaymentInfoMapper userPaymentInfoMapper;
    @Resource
    private ClientUserMapper clientUserMapper;

    @Override
    public IPage<SellOrderSubmission> page(SellOrderSubmissionPageRequest request) {
        Page<SellOrderSubmission> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<SellOrderSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SellOrderSubmission::getDeletedAt);
        // 用户：优先按真实姓名或昵称模糊，否则按用户ID精确
        if (request.getUserKeyword() != null && !request.getUserKeyword().isBlank()) {
            String keyword = request.getUserKeyword().trim();
            List<Long> fromPayment = userPaymentInfoMapper.selectList(
                    new LambdaQueryWrapper<UserPaymentInfo>().like(UserPaymentInfo::getRealName, keyword))
                    .stream().map(UserPaymentInfo::getUserId).distinct().collect(Collectors.toList());
            List<Long> fromNick = clientUserMapper.selectList(
                    new LambdaQueryWrapper<ClientUser>().like(ClientUser::getNickName, keyword))
                    .stream().map(ClientUser::getId).distinct().collect(Collectors.toList());
            List<Long> userIds = Stream.concat(fromPayment.stream(), fromNick.stream()).distinct().collect(Collectors.toList());
            if (userIds.isEmpty()) {
                wrapper.eq(SellOrderSubmission::getUserId, -1L);
            } else {
                wrapper.in(SellOrderSubmission::getUserId, userIds);
            }
        } else if (request.getUserId() != null) {
            wrapper.eq(SellOrderSubmission::getUserId, request.getUserId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SellOrderSubmission::getStatus, request.getStatus());
        }
        String orderBy = request.getOrderBy();
        boolean asc = "ASC".equalsIgnoreCase(request.getOrder());
        if (orderBy != null && !orderBy.isBlank()) {
            if ("createdAt".equals(orderBy)) {
                wrapper.orderBy(true, asc, SellOrderSubmission::getCreatedAt);
            } else if ("updatedAt".equals(orderBy)) {
                wrapper.orderBy(true, asc, SellOrderSubmission::getUpdatedAt);
            } else {
                wrapper.orderByDesc(SellOrderSubmission::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(SellOrderSubmission::getCreatedAt);
        }
        IPage<SellOrderSubmission> result = sellOrderSubmissionMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillDisplayName);
        result.getRecords().forEach(this::enrichLogisticsNos);
        return result;
    }

    @Override
    public SellOrderSubmission getById(Long id) {
        SellOrderSubmission entity = sellOrderSubmissionMapper.selectById(id);
        if (entity != null) {
            fillDisplayName(entity);
            enrichLogisticsNos(entity);
        }
        return entity;
    }

    private void enrichLogisticsNos(SellOrderSubmission entity) {
        if (entity == null) {
            return;
        }
        entity.setLogisticsNos(ExpressNoSupport.splitStored(entity.getLogisticsNo()));
    }

    /**
     * 填充展示用姓名：优先打款信息真实姓名，否则 client_user 昵称，否则「用户+ID」
     */
    private void fillDisplayName(SellOrderSubmission entity) {
        if (entity == null || entity.getUserId() == null) {
            return;
        }
        Long userId = entity.getUserId();
        UserPaymentInfo payment = userPaymentInfoMapper.selectOne(
                new LambdaQueryWrapper<UserPaymentInfo>().eq(UserPaymentInfo::getUserId, userId));
        if (payment != null && payment.getRealName() != null && !payment.getRealName().isBlank()) {
            entity.setDisplayName(payment.getRealName().trim());
            return;
        }
        ClientUser user = clientUserMapper.selectById(userId);
        if (user != null && user.getNickName() != null && !user.getNickName().isBlank()) {
            entity.setDisplayName(user.getNickName().trim());
            return;
        }
        entity.setDisplayName("用户" + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<SellOrderSubmission> update(Long id, SellOrderSubmissionUpdateRequest request) {
        SellOrderSubmission entity = sellOrderSubmissionMapper.selectById(id);
        if (entity == null) {
            return R.error("记录不存在");
        }
        if (request.getSenderName() != null) {
            entity.setSenderName(request.getSenderName());
        }
        if (request.getSenderPhone() != null) {
            entity.setSenderPhone(request.getSenderPhone());
        }
        if (request.getLogisticsCompany() != null) {
            entity.setLogisticsCompany(request.getLogisticsCompany());
        }
        if (request.getLogisticsNos() != null) {
            List<String> nos = ExpressNoSupport.normalizeInputList(request.getLogisticsNos());
            entity.setLogisticsNo(ExpressNoSupport.joinStored(nos));
        }
        if (request.getStorage() != null) {
            entity.setStorage(request.getStorage());
        }
        if (request.getStorageDate() != null) {
            entity.setStorageDate(request.getStorageDate());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getSettledAmount() != null) {
            entity.setSettledAmount(request.getSettledAmount());
        }
        if (request.getAdminInternalNote() != null) {
            entity.setAdminInternalNote(request.getAdminInternalNote());
        }
        if (request.getItems() != null) {
            List<Map<String, Object>> itemsJson = new ArrayList<>();
            for (SellOrderSubmissionUpdateRequest.SellOrderItemDto dto : request.getItems()) {
                if (dto == null) continue;
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productName", dto.getProductName() != null ? dto.getProductName() : "");
                row.put("price", dto.getPrice() != null ? dto.getPrice() : java.math.BigDecimal.ZERO);
                row.put("quantity", dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1);
                itemsJson.add(row);
            }
            entity.setItemsJson(itemsJson);
        }
        sellOrderSubmissionMapper.updateById(entity);
        return R.success(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<SellOrderSubmission> appendSettledProofUrl(Long id, String url) {
        SellOrderSubmission entity = sellOrderSubmissionMapper.selectById(id);
        if (entity == null) {
            return R.error("记录不存在");
        }
        List<String> urls = entity.getSettledProofUrls();
        if (urls == null) {
            urls = new ArrayList<>();
        } else {
            urls = new ArrayList<>(urls);
        }
        urls.add(url);
        entity.setSettledProofUrls(urls);
        sellOrderSubmissionMapper.updateById(entity);
        return R.success(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<SellOrderSubmission> removeSettledProofUrl(Long id, String url, Integer index) {
        SellOrderSubmission entity = sellOrderSubmissionMapper.selectById(id);
        if (entity == null) {
            return R.error("记录不存在");
        }
        List<String> urls = entity.getSettledProofUrls();
        if (urls == null || urls.isEmpty()) {
            return R.success(getById(id));
        }
        boolean removed = false;
        if (index != null && index >= 0 && index < urls.size()) {
            urls.remove(index.intValue());
            removed = true;
        } else if (url != null && !url.isBlank()) {
            removed = urls.remove(url.trim());
        }
        if (removed) {
            entity.setSettledProofUrls(urls);
            sellOrderSubmissionMapper.updateById(entity);
        }
        return R.success(getById(id));
    }
}
