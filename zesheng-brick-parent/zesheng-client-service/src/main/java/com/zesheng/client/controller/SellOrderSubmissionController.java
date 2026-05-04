package com.zesheng.client.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.SellOrderSubmission;
import com.zesheng.client.service.ILogisticsTraceService;
import com.zesheng.client.service.ISellOrderSubmissionService;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.exception.AuthException;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * C端-行情报单提交
 */
@Slf4j
@RestController
@RequestMapping("/sell-order-submissions")
@Tag(name = "C端-行情报单", description = "行情报单提交")
@RequiredArgsConstructor
public class SellOrderSubmissionController {

    private final ISellOrderSubmissionService sellOrderSubmissionService;
    private final ILogisticsTraceService logisticsTraceService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "提交行情报单")
    public R<Long> submit(@RequestBody Map<String, Object> dataJson, HttpServletRequest request) {
        Long userId = getUserId(request);
        return sellOrderSubmissionService.submit(userId, dataJson);
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的行情报单分页", description = "statusTab: all|shipped|storing|completed|exception")
    public R<PageResult<SellOrderSubmission>> myPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String statusTab,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        IPage<SellOrderSubmission> page = sellOrderSubmissionService.pageMy(userId, pageNum, pageSize, statusTab);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "我的行情报单详情")
    public R<SellOrderSubmission> getMyById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        SellOrderSubmission entity = sellOrderSubmissionService.getMyById(userId, id);
        if (entity == null) {
            return R.error("记录不存在或无权查看");
        }
        return R.success(entity);
    }

    @GetMapping("/{id}/logistics-trace")
    @Operation(summary = "我的行情报单物流轨迹（快递100）")
    public R<LogisticsTraceVo> logisticsTrace(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.success(logisticsTraceService.traceSellOrderForUser(userId, id));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new AuthException("未授权访问");
    }
}
