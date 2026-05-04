package com.zesheng.client.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.FormSubmission;
import com.zesheng.common.response.R;

import java.util.Map;

/**
 * C端-表单提交记录
 */
public interface IFormSubmissionService {

    /**
     * 保存/提交固结报单
     *
     * @param userId   提交人ID（从JWT取）
     * @param schemeId 方案ID
     * @param quantity 下单数量
     * @param status   状态 0=草稿 1=已提交
     * @param dataJson 固结字段（加赠、快递单号、订单编号等）
     * @return 提交记录ID
     */
    R<Long> save(Long userId, Long schemeId, Integer quantity, Integer status, Map<String, Object> dataJson);

    /**
     * 当前用户固结报单分页（排除草稿）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param statusTab all=全部 shipped=已寄出(1,2) storing=待入库(3) completed=已完成(4) exception=异常(5,6)
     * @return 分页结果
     */
    IPage<FormSubmission> pageMy(Long userId, int pageNum, int pageSize, String statusTab);

    /**
     * 当前用户固结报单指定 tab 数量（排除草稿）
     */
    long countMy(Long userId, String statusTab);

    /**
     * 当前用户固结报单详情（仅本人可查）
     */
    FormSubmission getMyById(Long userId, Long id);
}
