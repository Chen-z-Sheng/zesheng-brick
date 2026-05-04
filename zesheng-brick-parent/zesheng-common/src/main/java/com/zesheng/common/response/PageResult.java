package com.zesheng.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类（适配MyBatis-Plus的IPage）
 */
@Data
public class PageResult<T> implements Serializable {
    private PageMeta pageMeta;
    private List<T> records;

    public static <T> R<PageResult<T>> success(IPage<T> iPage) {
        PageResult<T> pageResult = new PageResult<>();

        PageMeta pageMeta = PageMeta.of(
                iPage.getTotal(),
                (int) iPage.getCurrent(),
                (int) iPage.getSize()
        );
        pageResult.setPageMeta(pageMeta);
        pageResult.setRecords(iPage.getRecords());

        return R.success(pageResult);
    }
}