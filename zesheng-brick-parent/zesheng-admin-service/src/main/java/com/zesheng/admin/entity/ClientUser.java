package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端用户（client_user），用于管理端展示提交人昵称等。
 * 多模块共用同一库时，管理端直接读 C 端表属常见做法；若后续拆库可改为通过 Feign/HTTP 调 client 接口。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user")
public class ClientUser extends BaseEntity {

    @TableField("nick_name")
    private String nickName;
}
