package com.zesheng.client.controller;

import com.zesheng.client.service.PublicSysConfigService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "C端-公开系统配置", description = "小程序等匿名可读的受控配置项")
@Validated
@RestController
@RequestMapping("/public/sys-config")
public class PublicSysConfigController {

    private final PublicSysConfigService publicSysConfigService;

    public PublicSysConfigController(PublicSysConfigService publicSysConfigService) {
        this.publicSysConfigService = publicSysConfigService;
    }

    @Operation(summary = "按配置键获取配置值（白名单）")
    @GetMapping("/by-key/{configKey}")
    public R<String> getByKey(
            @PathVariable @Pattern(regexp = "^[a-z0-9_]{1,64}$", message = "配置键格式不合法") String configKey) {
        return publicSysConfigService.getPublicValueByKey(configKey);
    }
}
