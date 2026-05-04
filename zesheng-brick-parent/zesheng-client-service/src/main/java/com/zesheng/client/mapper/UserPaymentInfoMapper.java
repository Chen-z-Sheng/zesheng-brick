package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.UserPaymentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收款信息Mapper
 */
@Mapper
public interface UserPaymentInfoMapper extends BaseMapper<UserPaymentInfo> {
}
