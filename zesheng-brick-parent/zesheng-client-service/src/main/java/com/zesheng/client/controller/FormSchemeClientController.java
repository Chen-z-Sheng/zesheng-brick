package com.zesheng.client.controller;

import com.zesheng.client.mapper.FormSchemeClientMapper;
import com.zesheng.client.model.response.FormSchemeListItemVo;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端-方案列表（固结报单选方案用）
 */
@RestController
@RequestMapping("/form-schemes")
@Tag(name = "C端-方案", description = "小程序固结报单-方案列表")
@RequiredArgsConstructor
public class FormSchemeClientController {

    private final FormSchemeClientMapper formSchemeClientMapper;

    @GetMapping
    @Operation(summary = "方案列表（仅启用，可关键词搜索）")
    public R<List<FormSchemeListItemVo>> list(@RequestParam(required = false) String keyword) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<FormSchemeListItemVo> list = formSchemeClientMapper.listByKeyword(kw);
        return R.success(list);
    }
}
