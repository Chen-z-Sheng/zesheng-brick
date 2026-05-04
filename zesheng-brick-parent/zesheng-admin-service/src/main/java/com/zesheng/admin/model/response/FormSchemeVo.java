package com.zesheng.admin.model.response;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "FormSchemeVo", description = "方案查询响应参数")
public class FormSchemeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "方案ID", example = "1")
    private Long id;
    
    @Schema(description = "方案名称", example = "xxx方案")
    private String name;

    @Schema(description = "关联下单地址ID")
    private Long addressId;

    @Schema(description = "下单地址（只读展示）")
    private String deliveryAddressText;

    @Schema(description = "方案说明", example = "老板结账慢")
    private String description;

    @Schema(description = "状态：0=禁用 1=启用 2=草稿", example = "0")
    private StatusEnum status;

    @Schema(description = "每单结算金额", example = "66")
    private BigDecimal unitPrice;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    private LocalDateTime updatedAt;
}
