package com.zesheng.client.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.UserFeedback;

import java.util.List;

/**
 * 用户问题反馈 Service
 */
public interface IUserFeedbackService {

    /**
     * 提交问题反馈
     */
    UserFeedback submit(Long userId, String feedbackType, String content, List<String> imageUrls);

    /**
     * 我的反馈分页
     */
    IPage<UserFeedback> pageMy(Long userId, int pageNum, int pageSize);

    /**
     * 我的反馈详情
     */
    UserFeedback getMyById(Long userId, Long id);
}
