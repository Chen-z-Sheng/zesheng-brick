package com.zesheng.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 分页元信息
 */
@Data
@AllArgsConstructor
@Schema(description = "分页元信息")
public class PageMeta {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer pages;

    // 静态工厂方法：自动计算总页数
    public static PageMeta of(Long total, Integer page, Integer pageSize) {
        // 1. 空值/非法值校验（避免空指针或除0异常）
        if (page == null || page < 1) {
            page = 1; // 页码默认1
        }
        if (pageSize == null || pageSize < 1 || pageSize > 1000) {
            pageSize = 10; // 页大小默认10，限制最大1000
        }
        if (total == null) {
            total = 0L;
        }

        // 2. 计算总页数
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        return new PageMeta(total, page, pageSize, pages);
    }
}