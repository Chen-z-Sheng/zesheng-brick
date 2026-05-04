package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.UserFeedback;
import com.zesheng.admin.model.request.UserFeedbackPageRequest;
import com.zesheng.common.response.R;

/**
 * 用户问题反馈 Service
 */
public interface IUserFeedbackService {

    /**
     * 分页查询反馈列表
     */
    IPage<UserFeedback> page(UserFeedbackPageRequest request);

    /**
     * 反馈详情
     */
    UserFeedback getById(Long id);

    /**
     * 回复用户反馈
     */
    R<UserFeedback> reply(Long id, Long adminUserId, String replyContent);
}
