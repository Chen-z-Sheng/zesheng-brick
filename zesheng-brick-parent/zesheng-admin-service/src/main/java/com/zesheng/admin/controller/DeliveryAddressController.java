package com.zesheng.admin.controller;

import com.zesheng.admin.entity.DeliveryAddress;
import com.zesheng.admin.model.request.DeliveryAddressSaveRequest;
import com.zesheng.admin.model.request.DeliveryAddressUpdateRequest;
import com.zesheng.admin.service.IDeliveryAddressService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("delivery-addresses")
@Tag(name = "管理端-下单地址", description = "下单地址增删改查")
@RequiredArgsConstructor
public class DeliveryAddressController {

    private final IDeliveryAddressService deliveryAddressService;

    @GetMapping
    @Operation(summary = "列表，不传 status 返回全部，传 1 仅启用（方案下拉用）")
    @PreAuthorize("hasAuthority('admin:delivery-address:list') or hasAuthority('admin:form-scheme:list')")
    public R<List<DeliveryAddress>> list(
            @RequestParam(required = false) @Min(value = 0, message = "状态取值非法") @Max(value = 1, message = "状态取值非法") Integer status) {
        return deliveryAddressService.list(status);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('admin:delivery-address:add')")
    public R<DeliveryAddress> save(@Validated @RequestBody DeliveryAddressSaveRequest request) {
        return deliveryAddressService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('admin:delivery-address:update')")
    public R<DeliveryAddress> update(
            @PathVariable Long id,
            @Validated @RequestBody DeliveryAddressUpdateRequest request) {
        return deliveryAddressService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('admin:delivery-address:delete')")
    public R<Integer> delete(@PathVariable Long id) {
        return deliveryAddressService.delete(id);
    }
}
