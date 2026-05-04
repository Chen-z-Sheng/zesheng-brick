package com.zesheng.client.controller;

import com.zesheng.client.model.request.LogisticsBatchRequest;
import com.zesheng.client.model.response.OrderStatsVo;
import com.zesheng.client.service.IFormSubmissionService;
import com.zesheng.client.service.ILogisticsTraceService;
import com.zesheng.client.service.ISellOrderSubmissionService;
import com.zesheng.common.dto.logistics.LogisticsSummaryVo;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C端-我的订单（统计）
 */
@Slf4j
@RestController
@RequestMapping("/my")
@Tag(name = "C端-我的订单", description = "订单统计、固结/行情分页见 form-submissions、sell-order-submissions")
@RequiredArgsConstructor
public class MyOrderController {

    private final IFormSubmissionService formSubmissionService;
    private final ISellOrderSubmissionService sellOrderSubmissionService;
    private final ILogisticsTraceService logisticsTraceService;
    private final JwtUtil jwtUtil;

    @GetMapping("/order-stats")
    @Operation(summary = "订单统计（固结+行情，按 tab 汇总）")
    public R<OrderStatsVo> orderStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        long total = formSubmissionService.countMy(userId, "all") + sellOrderSubmissionService.countMy(userId, "all");
        long shipped = formSubmissionService.countMy(userId, "shipped") + sellOrderSubmissionService.countMy(userId, "shipped");
        long signed = formSubmissionService.countMy(userId, "signed") + sellOrderSubmissionService.countMy(userId, "signed");
        long transit = formSubmissionService.countMy(userId, "transit") + sellOrderSubmissionService.countMy(userId, "transit");
        long storing = formSubmissionService.countMy(userId, "storing") + sellOrderSubmissionService.countMy(userId, "storing");
        long completed = formSubmissionService.countMy(userId, "completed") + sellOrderSubmissionService.countMy(userId, "completed");
        long exception = formSubmissionService.countMy(userId, "exception") + sellOrderSubmissionService.countMy(userId, "exception");
        return R.success(new OrderStatsVo(total, shipped, signed, transit, storing, completed, exception));
    }

    @PostMapping("/logistics-summaries")
    @Operation(summary = "批量查询订单物流摘要（用于我的订单列表）")
    public R<Map<String, LogisticsSummaryVo>> logisticsSummaries(
            @Valid @RequestBody LogisticsBatchRequest body,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        List<ILogisticsTraceService.LogisticsBatchItem> items = body.getItems().stream()
                .map(i -> new ILogisticsTraceService.LogisticsBatchItem(i.getType(), i.getId()))
                .collect(Collectors.toList());
        return R.success(logisticsTraceService.batchSummariesForUser(userId, items));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("未授权访问");
    }
}
