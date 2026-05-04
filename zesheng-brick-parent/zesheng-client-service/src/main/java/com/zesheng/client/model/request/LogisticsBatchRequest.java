package com.zesheng.client.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量拉取订单列表物流摘要
 */
@Data
public class LogisticsBatchRequest {

    @NotEmpty
    @Size(max = 30)
    @Valid
    private List<LogisticsBatchItem> items;

    @Data
    public static class LogisticsBatchItem {

        @NotBlank
        private String type;

        @NotNull
        private Long id;
    }
}
