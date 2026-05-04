package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户打款/收款信息（client_user_payment_info），管理端用于展示真实姓名与收款账户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user_payment_info")
@Schema(description = "用户收款信息（管理端查询）")
public class UserPaymentInfo extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "关联用户ID")
    private Long userId;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField("alipay_account")
    @Schema(description = "支付宝账号")
    private String alipayAccount;

    @TableField("wechat_qrcode")
    @Schema(description = "微信收款码图片URL")
    private String wechatQrcode;

    @TableField("alipay_qrcode")
    @Schema(description = "支付宝收款码图片URL")
    private String alipayQrcode;

    @TableField("bank_card_no")
    @Schema(description = "银行卡号")
    private String bankCardNo;

    @TableField("bank_name")
    @Schema(description = "开户银行")
    private String bankName;

    @TableField("bank_branch")
    @Schema(description = "开户支行")
    private String bankBranch;
}
