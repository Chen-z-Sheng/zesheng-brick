package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端-用户收款信息表
 * 表名：client_user_payment_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user_payment_info")
@Schema(description = "用户收款信息")
public class UserPaymentInfo extends BaseEntity {

    /** 关联用户ID */
    @TableField("user_id")
    private Long userId;

    /** 真实姓名 */
    @TableField("real_name")
    private String realName;

    /** 支付宝账号 */
    @TableField("alipay_account")
    private String alipayAccount;

    /** 微信收款码图片URL */
    @TableField("wechat_qrcode")
    private String wechatQrcode;

    /** 支付宝收款码图片URL */
    @TableField("alipay_qrcode")
    private String alipayQrcode;

    /** 银行卡号 */
    @TableField("bank_card_no")
    private String bankCardNo;

    /** 开户银行 */
    @TableField("bank_name")
    private String bankName;

    /** 开户支行 */
    @TableField("bank_branch")
    private String bankBranch;
}
