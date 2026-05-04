package com.zesheng.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量删除 ID 列表")
public class BatchDeleteRequest {

    @NotEmpty(message = "待删除ID列表不能为空")
    @Size(max = 500, message = "单次删除条数不能超过500")
    @Schema(description = "主键 ID 列表")
    private List<Long> ids;
}
