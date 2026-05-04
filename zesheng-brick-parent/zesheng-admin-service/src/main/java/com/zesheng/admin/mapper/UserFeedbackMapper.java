package com.zesheng.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.admin.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户问题反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}
