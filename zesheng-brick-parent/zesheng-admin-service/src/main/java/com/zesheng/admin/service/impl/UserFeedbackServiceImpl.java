package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.ClientUser;
import com.zesheng.admin.entity.UserFeedback;
import com.zesheng.admin.mapper.ClientUserMapper;
import com.zesheng.admin.mapper.UserFeedbackMapper;
import com.zesheng.admin.model.request.UserFeedbackPageRequest;
import com.zesheng.admin.service.IUserFeedbackService;
import com.zesheng.common.response.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 用户问题反馈 Service 实现
 */
@Service
public class UserFeedbackServiceImpl implements IUserFeedbackService {

    @Resource
    private UserFeedbackMapper userFeedbackMapper;

    @Resource
    private ClientUserMapper clientUserMapper;

    @Override
    public IPage<UserFeedback> page(UserFeedbackPageRequest request) {
        Page<UserFeedback> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(UserFeedback::getDeletedAt);
        if (request.getUserId() != null) {
            wrapper.eq(UserFeedback::getUserId, request.getUserId());
        }
        if (request.getFeedbackType() != null && !request.getFeedbackType().isBlank()) {
            wrapper.eq(UserFeedback::getFeedbackType, request.getFeedbackType().trim());
        }
        String orderBy = request.getOrderBy();
        boolean asc = "ASC".equalsIgnoreCase(request.getOrder());
        if ("createdAt".equals(orderBy)) {
            wrapper.orderBy(true, asc, UserFeedback::getCreatedAt);
        } else {
            wrapper.orderByDesc(UserFeedback::getCreatedAt);
        }
        IPage<UserFeedback> result = userFeedbackMapper.selectPage(page, wrapper);
        fillDisplayName(result.getRecords());
        return result;
    }

    @Override
    public UserFeedback getById(Long id) {
        UserFeedback entity = userFeedbackMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        List<UserFeedback> single = new ArrayList<>();
        single.add(entity);
        fillDisplayName(single);
        return entity;
    }

    @Override
    public R<UserFeedback> reply(Long id, Long adminUserId, String replyContent) {
        if (id == null) {
            return R.error("反馈ID不能为空");
        }
        if (!StringUtils.hasText(replyContent)) {
            return R.error("回复内容不能为空");
        }
        UserFeedback entity = userFeedbackMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            return R.error("反馈记录不存在");
        }
        entity.setReplyContent(replyContent.trim());
        entity.setRepliedAt(LocalDateTime.now());
        entity.setReplyBy(adminUserId);
        entity.setStatus(1);
        userFeedbackMapper.updateById(entity);
        return R.success(getById(id));
    }

    private void fillDisplayName(List<UserFeedback> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (UserFeedback item : records) {
            if (item.getUserId() == null) {
                item.setDisplayName("-");
                continue;
            }
            ClientUser user = clientUserMapper.selectById(item.getUserId());
            if (user != null && user.getNickName() != null && !user.getNickName().isBlank()) {
                item.setDisplayName(user.getNickName().trim());
            } else {
                item.setDisplayName("用户" + item.getUserId());
            }
        }
    }
}
