package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zesheng.admin.entity.ClientUser;
import com.zesheng.admin.entity.FormScheme;
import com.zesheng.admin.entity.FormSubmission;
import com.zesheng.admin.entity.UserPaymentInfo;
import com.zesheng.admin.mapper.ClientUserMapper;
import com.zesheng.admin.mapper.FormSchemeMapper;
import com.zesheng.admin.mapper.FormSubmissionMapper;
import com.zesheng.admin.mapper.UserPaymentInfoMapper;
import com.zesheng.admin.model.request.FormSubmissionPageRequest;
import com.zesheng.admin.model.request.FormSubmissionUpdateRequest;
import com.zesheng.admin.service.IFormSubmissionService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.ExpressNoSupport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 固结报单提交记录 Service 实现
 */
@Slf4j
@Service
public class FormSubmissionServiceImpl implements IFormSubmissionService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ConcurrentHashMap<Long, Object> APPEND_PROOF_LOCKS = new ConcurrentHashMap<>();

    @Resource
    private FormSubmissionMapper formSubmissionMapper;
    @Resource
    private FormSchemeMapper formSchemeMapper;
    @Resource
    private ClientUserMapper clientUserMapper;
    @Resource
    private UserPaymentInfoMapper userPaymentInfoMapper;

    @Override
    public IPage<FormSubmission> page(FormSubmissionPageRequest request) {
        Page<FormSubmission> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<FormSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(FormSubmission::getDeletedAt);
        // 方案：优先按名称关键字模糊，否则按方案ID精确
        if (request.getSchemeNameKeyword() != null && !request.getSchemeNameKeyword().isBlank()) {
            String keyword = request.getSchemeNameKeyword().trim();
            List<FormScheme> schemes = formSchemeMapper.selectList(
                    new LambdaQueryWrapper<FormScheme>().like(FormScheme::getName, keyword));
            List<Long> schemeIds = schemes == null || schemes.isEmpty()
                    ? Collections.emptyList()
                    : schemes.stream().map(FormScheme::getId).distinct().collect(Collectors.toList());
            if (schemeIds.isEmpty()) {
                wrapper.eq(FormSubmission::getSchemeId, -1L);
            } else {
                wrapper.in(FormSubmission::getSchemeId, schemeIds);
            }
        } else if (request.getSchemeId() != null) {
            wrapper.eq(FormSubmission::getSchemeId, request.getSchemeId());
        }
        // 用户：优先按打款信息真实姓名模糊，否则按用户ID精确
        if (request.getUserKeyword() != null && !request.getUserKeyword().isBlank()) {
            String keyword = request.getUserKeyword().trim();
            List<UserPaymentInfo> payments = userPaymentInfoMapper.selectList(
                    new LambdaQueryWrapper<UserPaymentInfo>().like(UserPaymentInfo::getRealName, keyword));
            List<Long> userIds = payments == null || payments.isEmpty()
                    ? Collections.emptyList()
                    : payments.stream().map(UserPaymentInfo::getUserId).distinct().collect(Collectors.toList());
            if (userIds.isEmpty()) {
                wrapper.eq(FormSubmission::getUserId, -1L);
            } else {
                wrapper.in(FormSubmission::getUserId, userIds);
            }
        } else if (request.getUserId() != null) {
            wrapper.eq(FormSubmission::getUserId, request.getUserId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(FormSubmission::getStatus, request.getStatus());
        }
        String orderBy = request.getOrderBy();
        boolean asc = "ASC".equalsIgnoreCase(request.getOrder());
        if (orderBy != null && !orderBy.isBlank()) {
            switch (orderBy) {
                case "createdAt":
                    wrapper.orderBy(true, asc, FormSubmission::getCreatedAt);
                    break;
                case "updatedAt":
                    wrapper.orderBy(true, asc, FormSubmission::getUpdatedAt);
                    break;
                case "status":
                    wrapper.orderBy(true, asc, FormSubmission::getStatus);
                    break;
                default:
                    wrapper.orderByDesc(FormSubmission::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(FormSubmission::getCreatedAt);
        }
        IPage<FormSubmission> result = formSubmissionMapper.selectPage(page, wrapper);
        result.getRecords().forEach(r -> {
            fillDisplayName(r);
            fillSchemeName(r);
            fillSettledProofUrls(r);
        });
        return result;
    }

    @Override
    public FormSubmission getById(Long id) {
        FormSubmission entity = formSubmissionMapper.selectById(id);
        if (entity != null) {
            fillDisplayName(entity);
            fillSchemeName(entity);
            fillSettledProofUrls(entity);
        }
        return entity;
    }

    /**
     * 填充提交人展示名：优先打款信息真实姓名，否则小程序昵称，否则「用户+ID」
     */
    private void fillDisplayName(FormSubmission entity) {
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

    /**
     * 根据 schemeId 填充方案名称与每单结算金额（用于列表应结金额=unitPrice*quantity）
     */
    private void fillSchemeName(FormSubmission entity) {
        if (entity == null || entity.getSchemeId() == null) {
            return;
        }
        FormScheme scheme = formSchemeMapper.selectById(entity.getSchemeId());
        if (scheme != null) {
            if (scheme.getName() != null) {
                entity.setSchemeName(scheme.getName());
            }
            entity.setUnitPrice(scheme.getUnitPrice());
        }
    }

    /**
     * 回款凭证列表：优先用 settled_proof_urls（JSON 列），为空时从 settled_proof_url（兼容旧数据）解析
     */
    private void fillSettledProofUrls(FormSubmission entity) {
        if (entity == null) {
            return;
        }
        List<String> fromJson = entity.getSettledProofUrls();
        if (fromJson != null && !fromJson.isEmpty()) {
            return;
        }
        String raw = entity.getSettledProofUrl();
        if (raw == null || raw.isBlank()) {
            entity.setSettledProofUrls(new ArrayList<>());
            return;
        }
        String trim = raw.trim();
        try {
            if (trim.startsWith("[")) {
                List<String> list = OBJECT_MAPPER.readValue(trim, new TypeReference<List<String>>() {});
                entity.setSettledProofUrls(list != null ? list : new ArrayList<>());
            } else {
                List<String> single = new ArrayList<>();
                single.add(trim);
                entity.setSettledProofUrls(single);
            }
        } catch (Exception e) {
            log.warn("解析回款凭证 URL 失败，按单条处理: {}", trim, e);
            List<String> single = new ArrayList<>();
            single.add(trim);
            entity.setSettledProofUrls(single);
        }
    }

    /**
     * 将 URL 列表写入 settled_proof_urls（JSON 列）
     */
    private void persistSettledProofUrls(FormSubmission entity, List<String> urls) {
        if (urls == null) {
            entity.setSettledProofUrls(new ArrayList<>());
        } else {
            entity.setSettledProofUrls(urls);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<FormSubmission> update(Long id, FormSubmissionUpdateRequest request) {
        FormSubmission entity = formSubmissionMapper.selectById(id);
        if (entity == null) {
            return R.error("提交记录不存在");
        }
        if (request.getQuantity() != null) {
            entity.setQuantity(request.getQuantity());
        }
        if (request.getSettledAmount() != null) {
            entity.setSettledAmount(request.getSettledAmount());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getSettledAt() != null) {
            entity.setSettledAt(request.getSettledAt());
        }
        if (request.getSettledProofUrl() != null) {
            entity.setSettledProofUrl(request.getSettledProofUrl());
        }
        if (request.getAdminInternalNote() != null) {
            entity.setAdminInternalNote(request.getAdminInternalNote());
        }
        if (request.getDataJson() != null) {
            Map<String, Object> dataJson = request.getDataJson();
            List<String> expressNos = ExpressNoSupport.readExpressNosFromFormJson(dataJson);
            ExpressNoSupport.writeExpressNosToFormJson(dataJson, expressNos);
            entity.setDataJson(dataJson);
        }
        formSubmissionMapper.updateById(entity);
        return R.success(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<FormSubmission> appendSettledProofUrl(Long id, String url) {
        Object lock = APPEND_PROOF_LOCKS.computeIfAbsent(id, k -> new Object());
        synchronized (lock) {
            FormSubmission entity = formSubmissionMapper.selectById(id);
            if (entity == null) {
                return R.error("提交记录不存在");
            }
            fillSettledProofUrls(entity);
            List<String> urls = entity.getSettledProofUrls();
            if (urls == null) {
                urls = new ArrayList<>();
            } else {
                urls = new ArrayList<>(urls);
            }
            String urlTrim = url != null ? url.trim() : "";
            boolean alreadyExists = urls.stream().anyMatch(u -> u != null && u.trim().equals(urlTrim));
            if (!alreadyExists) {
                urls.add(url);
                persistSettledProofUrls(entity, urls);
                formSubmissionMapper.updateById(entity);
            }
            return R.success(getById(id));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<FormSubmission> removeSettledProofUrl(Long id, String url, Integer index) {
        FormSubmission entity = formSubmissionMapper.selectById(id);
        if (entity == null) {
            return R.error("提交记录不存在");
        }
        fillSettledProofUrls(entity);
        List<String> urls = entity.getSettledProofUrls();
        if (urls == null || urls.isEmpty()) {
            return R.success(getById(id));
        }
        boolean removed = false;
        if (index != null && index >= 0 && index < urls.size()) {
            urls.remove(index.intValue());
            removed = true;
        } else if (url != null && !url.isBlank()) {
            String target = url.trim();
            removed = urls.removeIf(u -> u != null && (u.trim().equals(target)
                    || normalizeUrlForCompare(u).equals(normalizeUrlForCompare(target))));
        }
        if (removed) {
            persistSettledProofUrls(entity, urls);
            formSubmissionMapper.updateById(entity);
        }
        return R.success(getById(id));
    }

    /** 用于删除时 URL 比对：取 path 最后一段，避免协议/域名/前缀差异导致删不掉 */
    private static String normalizeUrlForCompare(String u) {
        if (u == null || u.isBlank()) return "";
        String t = u.trim();
        int lastSlash = t.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < t.length() - 1) return t.substring(lastSlash + 1);
        return t;
    }
}
