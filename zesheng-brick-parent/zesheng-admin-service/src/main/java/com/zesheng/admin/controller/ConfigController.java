package com.zesheng.admin.controller;

import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.ConfigEntity;
import com.zesheng.sys.model.request.ConfigSaveRequest;
import com.zesheng.sys.model.request.ConfigUpdateRequest;
import com.zesheng.sys.service.IConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-系统配置
 */
@Validated
@RestController
@RequestMapping("config")
@Tag(name = "管理端-系统配置", description = "系统配置增删改查")
@RequiredArgsConstructor
public class ConfigController {

    private final IConfigService configService;

    @GetMapping
    @Operation(summary = "查询配置全量列表")
    @PreAuthorize("hasAuthority('admin:config:list')")
    public R<List<ConfigEntity>> list() {
        return R.success(configService.list());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询配置列表")
    @PreAuthorize("hasAuthority('admin:config:list')")
    public R<PageResult<ConfigEntity>> page(@Validated PageAndSortQueryRequest queryDto) {
        return R.success(PageResult.success(configService.pageConfig(queryDto)).getData());
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询配置详情")
    @PreAuthorize("hasAuthority('admin:config:list')")
    public R<ConfigEntity> getById(@PathVariable Long id) {
        ConfigEntity entity = configService.getById(id);
        if (entity == null) {
            return R.error(com.zesheng.common.enums.ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        return R.success(entity);
    }

    @GetMapping("/by-key/{configKey}")
    @Operation(summary = "按配置键查询")
    @PreAuthorize("hasAuthority('admin:config:list')")
    public R<ConfigEntity> getByKey(
            @PathVariable @Pattern(regexp = "^[\\w.-]{1,128}$", message = "配置键格式不合法") String configKey) {
        ConfigEntity entity = configService.getByKey(configKey);
        if (entity == null) {
            return R.error(com.zesheng.common.enums.ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        return R.success(entity);
    }

    @PostMapping
    @Operation(summary = "新增配置")
    @PreAuthorize("hasAuthority('admin:config:add')")
    public R<ConfigEntity> save(@Validated @RequestBody ConfigSaveRequest request) {
        return configService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新配置")
    @PreAuthorize("hasAuthority('admin:config:update')")
    public R<ConfigEntity> update(@PathVariable Long id, @Validated @RequestBody ConfigUpdateRequest request) {
        return configService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    @PreAuthorize("hasAuthority('admin:config:delete')")
    public R<Integer> deleteById(@PathVariable Long id) {
        return configService.deleteById(id);
    }
}
