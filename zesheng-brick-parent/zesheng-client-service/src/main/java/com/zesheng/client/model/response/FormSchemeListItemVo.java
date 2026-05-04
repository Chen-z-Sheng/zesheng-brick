package com.zesheng.client.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "C端-方案列表项")
public class FormSchemeListItemVo {

    @Schema(description = "方案ID")
    private Long id;

    @Schema(description = "方案名称")
    private String name;

    @Schema(description = "每单结算金额")
    private BigDecimal unitPrice;

    @Schema(description = "下单地址（只读展示）")
    private String deliveryAddressText;
}
