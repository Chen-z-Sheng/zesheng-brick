package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.UserSender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户寄件人信息 Mapper
 */
@Mapper
public interface UserSenderMapper extends BaseMapper<UserSender> {

    /**
     * 按使用次数字段降序（填写报单次数多的排前面）
     */
    @Select("SELECT s.* FROM client_user_senders s WHERE s.user_id = #{userId} ORDER BY s.use_count DESC, s.id ASC")
    List<UserSender> listByUserIdOrderByUseCountDesc(@Param("userId") Long userId);

    /**
     * 提交报单时对匹配的寄件人 use_count +1
     */
    @Update("UPDATE client_user_senders SET use_count = use_count + 1 WHERE user_id = #{userId} AND name = #{name} AND phone = #{phone}")
    int incrementUseCount(@Param("userId") Long userId, @Param("name") String name, @Param("phone") String phone);
}
