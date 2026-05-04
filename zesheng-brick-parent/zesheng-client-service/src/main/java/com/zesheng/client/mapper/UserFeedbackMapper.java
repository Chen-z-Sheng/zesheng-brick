package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户问题反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}
