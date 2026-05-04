package com.zesheng.client.controller;

import com.zesheng.client.entity.LogisticsCompany;
import com.zesheng.client.service.ILogisticsCompanyService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("logistics-company")
@Tag(name = "物流公司", description = "小程序端-物流公司列表")
public class LogisticsCompanyController {

    @Autowired
    private ILogisticsCompanyService logisticsCompanyService;

    @GetMapping("/list")
    @Operation(summary = "获取物流公司列表")
    public R<List<LogisticsCompany>> getLogisticsCompanies(@RequestParam(value = "name", required = false) String name) {
        return R.ok(logisticsCompanyService.getLogisticsCompanies(name));
    }
}
