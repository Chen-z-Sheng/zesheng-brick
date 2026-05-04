package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.client.entity.UserFeedback;
import com.zesheng.client.mapper.UserFeedbackMapper;
import com.zesheng.client.service.IUserFeedbackService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户问题反馈 Service 实现
 */
@Service
public class UserFeedbackServiceImpl implements IUserFeedbackService {

    @Resource
    private UserFeedbackMapper userFeedbackMapper;

    @Override
    public UserFeedback submit(Long userId, String feedbackType, String content, List<String> imageUrls) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        if (!StringUtils.hasText(feedbackType)) {
            throw new IllegalArgumentException("反馈类型不能为空");
        }
        if (!StringUtils.hasText(content) || content.trim().length() < 5) {
            throw new IllegalArgumentException("反馈内容至少5个字");
        }
        UserFeedback entity = new UserFeedback();
        entity.setUserId(userId);
        entity.setFeedbackType(feedbackType.trim());
        entity.setContent(content.trim());
        entity.setImageUrls(imageUrls == null ? new ArrayList<>() : imageUrls);
        entity.setStatus(0);
        userFeedbackMapper.insert(entity);
        return entity;
    }

    @Override
    public IPage<UserFeedback> pageMy(Long userId, int pageNum, int pageSize) {
        Page<UserFeedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFeedback::getUserId, userId)
                .isNull(UserFeedback::getDeletedAt)
                .orderByDesc(UserFeedback::getCreatedAt);
        return userFeedbackMapper.selectPage(page, wrapper);
    }

    @Override
    public UserFeedback getMyById(Long userId, Long id) {
        if (id == null || userId == null) {
            return null;
        }
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFeedback::getId, id)
                .eq(UserFeedback::getUserId, userId)
                .isNull(UserFeedback::getDeletedAt)
                .last("limit 1");
        return userFeedbackMapper.selectOne(wrapper);
    }
}
