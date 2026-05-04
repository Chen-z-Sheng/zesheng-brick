package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户寄件人信息（行情报单用，仅 C 端）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user_senders")
@Schema(description = "用户寄件人信息")
public class UserSender extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "寄件人姓名")
    private String name;

    @Schema(description = "寄件人手机号")
    private String phone;

    @Schema(description = "被填写报单使用次数，用于列表排序")
    private Integer useCount = 0;
}
