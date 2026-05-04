package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user")
@Schema(description = "C端用户")
public class User extends BaseEntity {

    private String openid;

    @TableField("donut_user_id")
    private String donutUserId;

    @TableField("session_key")
    private String sessionKey;

    @TableField("nick_name")
    private String nickName;

    @TableField("avatar_url")
    private String avatarUrl;

    private String phone;

    @TableField("invite_code")
    private String inviteCode;

    @TableField("inviter_user_id")
    private Long inviterUserId;

    @TableField("invite_path")
    private String invitePath;

    private StatusEnum status = StatusEnum.ENABLE;
}
