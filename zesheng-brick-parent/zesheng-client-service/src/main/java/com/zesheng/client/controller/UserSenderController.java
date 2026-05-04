package com.zesheng.client.controller;

import com.zesheng.client.entity.UserSender;
import com.zesheng.client.service.IUserSenderService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端-寄件人信息（行情报单用，仅用户端）
 */
@RestController
@RequestMapping("/user-senders")
@Tag(name = "C端-寄件人", description = "寄件人列表、新增、修改、删除")
@RequiredArgsConstructor
public class UserSenderController {

    private final IUserSenderService userSenderService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "当前用户寄件人列表")
    public R<List<UserSender>> list(HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.success(userSenderService.listByUserId(userId));
    }

    @PostMapping
    @Operation(summary = "新增寄件人")
    public R<UserSender> add(
            @RequestBody UserSenderBody body,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return userSenderService.add(userId, body.getName(), body.getPhone());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "修改寄件人")
    public R<UserSender> update(
            @PathVariable Long id,
            @RequestBody UserSenderBody body,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return userSenderService.update(id, userId, body.getName(), body.getPhone());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除寄件人")
    public R<Integer> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        return userSenderService.delete(id, userId);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("未授权访问");
    }

    @lombok.Data
    public static class UserSenderBody {
        private String name;
        private String phone;
    }
}
