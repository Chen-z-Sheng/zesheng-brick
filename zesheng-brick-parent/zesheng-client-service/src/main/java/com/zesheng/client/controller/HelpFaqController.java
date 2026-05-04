package com.zesheng.client.controller;

import com.zesheng.client.entity.HelpFaq;
import com.zesheng.client.service.IHelpFaqService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 帮助中心FAQ（公开接口，无需登录）
 */
@Tag(name = "帮助中心", description = "常见问题FAQ")
@RestController
@RequestMapping("/help-faq")
public class HelpFaqController {

    private final IHelpFaqService helpFaqService;

    public HelpFaqController(IHelpFaqService helpFaqService) {
        this.helpFaqService = helpFaqService;
    }

    @Operation(summary = "获取FAQ列表")
    @GetMapping("/list")
    public R<List<HelpFaq>> list() {
        return R.success(helpFaqService.listEnabled());
    }
}
