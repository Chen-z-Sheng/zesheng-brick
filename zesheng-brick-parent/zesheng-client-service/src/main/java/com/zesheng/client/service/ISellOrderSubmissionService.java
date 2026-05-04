package com.zesheng.client.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.SellOrderSubmission;
import com.zesheng.common.response.R;

import java.util.Map;

/**
 * C端-行情报单提交
 */
public interface ISellOrderSubmissionService {

    /**
     * 提交行情报单
     *
     * @param userId  提交人ID（从JWT取）
     * @param dataJson 报单数据：items/sender/logistics/storage/remark
     * @return 提交记录ID
     */
    R<Long> submit(Long userId, Map<String, Object> dataJson);

    /**
     * 当前用户行情报单分页（排除草稿）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param statusTab all=全部 shipped=已寄出(1,2) storing=待入库(3) completed=已完成(4) exception=异常(5,6)
     * @return 分页结果
     */
    IPage<SellOrderSubmission> pageMy(Long userId, int pageNum, int pageSize, String statusTab);

    /**
     * 当前用户行情报单指定 tab 数量（排除草稿）
     */
    long countMy(Long userId, String statusTab);

    /**
     * 当前用户行情报单详情（仅本人可查）
     */
    SellOrderSubmission getMyById(Long userId, Long id);
}
