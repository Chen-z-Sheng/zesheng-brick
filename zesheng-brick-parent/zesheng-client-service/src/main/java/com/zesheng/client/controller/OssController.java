package com.zesheng.client.controller;

import com.zesheng.client.model.request.OssUploadRequest;
import com.zesheng.client.model.request.PresignedUrlRequest;
import com.zesheng.client.model.response.OssUploadResponse;
import com.zesheng.client.service.IOssService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * OSS文件上传控制器
 * 提供文件上传、删除、预签名URL生成等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
@Tag(name = "OSS文件管理", description = "OSS文件上传、下载、删除等操作")
public class OssController {

    private final IOssService ossService;
    private final JwtUtil jwtUtil;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "支持简单上传和分片上传，自动根据文件大小选择上传方式")
    public R<OssUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory,
            @RequestParam(value = "uploadId", required = false) String uploadId,
            @RequestParam(value = "userId", required = false) Long requestUserId,
            HttpServletRequest httpRequest) {

        try {
            Long jwtUserId = getUserIdFromRequest(httpRequest);
            Long userId = resolveUserIdForUpload(jwtUserId, requestUserId, directory);

            String fileUrl = ossService.uploadFileWithMultipart(
                    file,
                    directory,
                    userId,
                    uploadId,
                    progress -> log.info("上传进度: {}%", String.format("%.2f", progress))
            );

            OssUploadResponse response = OssUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .uploadId(uploadId)
                    .fileSize(file.getSize())
                    .fileName(file.getOriginalFilename())
                    .build();

            return R.success(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据文件URL删除OSS上的文件")
    public R<Boolean> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            boolean result = ossService.deleteFile(fileUrl);
            return R.success(result);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return R.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名URL
     */
    @PostMapping("/presigned-url")
    @Operation(summary = "生成预签名URL", description = "生成用于前端直接上传的预签名URL")
    public R<Map<String, String>> generatePresignedUrl(
            @RequestBody PresignedUrlRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getUserIdFromRequest(httpRequest);
            String presignedUrl = ossService.generatePresignedUrl(
                    request.getFileName(),
                    request.getDirectory(),
                    userId,
                    request.getExpireSeconds()
            );

            Map<String, String> result = new HashMap<>();
            result.put("presignedUrl", presignedUrl);
            result.put("fileName", request.getFileName());

            return R.success(result);
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            return R.error("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 查询上传进度
     */
    @GetMapping("/upload-progress")
    @Operation(summary = "查询上传进度", description = "查询分片上传的进度信息")
    public R<IOssService.UploadProgress> checkUploadProgress(
            @RequestParam("uploadId") String uploadId) {

        try {
            IOssService.UploadProgress progress = ossService.checkUploadProgress(uploadId);
            if (progress == null) {
                return R.error("未找到上传任务");
            }
            return R.success(progress);
        } catch (Exception e) {
            log.error("查询上传进度失败", e);
            return R.error("查询上传进度失败: " + e.getMessage());
        }
    }

    /**
     * 取消上传
     */
    @PostMapping("/cancel-upload")
    @Operation(summary = "取消上传", description = "取消正在进行的分片上传任务")
    public R<Void> cancelUpload(@RequestParam("uploadId") String uploadId) {
        try {
            ossService.cancelUpload(uploadId);
            return R.success();
        } catch (Exception e) {
            log.error("取消上传失败", e);
            return R.error("取消上传失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID（来自 JWT）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("未授权访问");
    }

    /**
     * 解析用于 OSS 文件命名的用户 ID。打款信息目录（微信/支付宝收款码）下优先使用客户端传入的 userId 命名，需与 JWT 一致。
     */
    private Long resolveUserIdForUpload(Long jwtUserId, Long requestUserId, String directory) {
        boolean isPaymentQrcodeDir = "wechat-qrcode".equals(directory) || "alipay-qrcodes".equals(directory);
        if (isPaymentQrcodeDir && requestUserId != null && (jwtUserId == null || requestUserId.equals(jwtUserId))) {
            return requestUserId;
        }
        return jwtUserId;
    }
}
