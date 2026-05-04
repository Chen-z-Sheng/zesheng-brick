package com.zesheng.client.controller;

import com.zesheng.client.service.IOssService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页轮播图（公开接口，读取 OSS index-banner 目录）
 */
@Tag(name = "首页轮播", description = "小程序首页轮播图列表")
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private static final String BANNER_PREFIX = "index-banner";

    private final IOssService ossService;

    @Operation(summary = "获取首页轮播图列表")
    @GetMapping("/list")
    public R<List<String>> list() {
        List<String> urls = ossService.listObjectUrls(BANNER_PREFIX);
        return R.success(urls);
    }
}
