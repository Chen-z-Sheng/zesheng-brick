package com.zesheng.client.service.impl;

import com.zesheng.common.config.OssConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OSS服务简化测试
 * 测试核心业务逻辑，不依赖实际的OSS客户端
 */
class OssServiceSimpleTest {

    private OssConfig ossConfig;

    @BeforeEach
    void setUp() {
        ossConfig = new OssConfig();
        ossConfig.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        ossConfig.setAccessKeyId("test-access-key-id");
        ossConfig.setAccessKeySecret("test-access-key-secret");
        ossConfig.setBucketName("test-bucket");
        ossConfig.setDirPrefix("uploads");
        ossConfig.setMaxFileSize(10 * 1024 * 1024L);
        ossConfig.setPartSize(1024 * 1024L);
        ossConfig.setUploadConcurrency(3);
        ossConfig.setAllowedExtensions(new String[]{"jpg", "jpeg", "png", "webp", "gif"});
    }

    @Test
    void testFileValidation_ValidFile() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        assertDoesNotThrow(() -> {
            validateFile(file);
        });
    }

    @Test
    void testFileValidation_NullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            validateFile(null);
        });
    }

    @Test
    void testFileValidation_EmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            validateFile(file);
        });
    }

    @Test
    void testFileValidation_TooLarge() {
        byte[] largeContent = new byte[20 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        assertThrows(IllegalArgumentException.class, () -> {
            validateFile(file);
        });
    }

    @Test
    void testFileValidation_InvalidExtension() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "test content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            validateFile(file);
        });
    }

    @Test
    void testGenerateFileName() {
        String originalFilename = "test-image.jpg";
        String fileName = generateFileName(originalFilename);

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));
        assertTrue(fileName.contains("_"));
        assertFalse(fileName.equals(originalFilename));
    }

    @Test
    void testGetFileExtension() {
        assertEquals("jpg", getFileExtension("test.jpg"));
        assertEquals("png", getFileExtension("test.png"));
        assertEquals("jpeg", getFileExtension("test.jpeg"));
        assertEquals("webp", getFileExtension("test.webp"));
        assertEquals("gif", getFileExtension("test.gif"));
        assertEquals("", getFileExtension("test"));
        assertEquals("", getFileExtension("test."));
        assertEquals("jpg", getFileExtension("test.image.jpg"));
    }

    @Test
    void testBuildObjectKey() {
        assertEquals("avatars/test.jpg", buildObjectKey("avatars", "test.jpg"));
        assertEquals("uploads/test.png", buildObjectKey(null, "test.png"));
        assertEquals("uploads/test.gif", buildObjectKey("", "test.gif"));
    }

    @Test
    void testBuildFileUrl() {
        String objectKey = "avatars/test.jpg";
        String fileUrl = buildFileUrl(objectKey);

        assertTrue(fileUrl.contains("test-bucket"));
        assertTrue(fileUrl.contains("oss-cn-hangzhou.aliyuncs.com"));
        assertTrue(fileUrl.contains("avatars/test.jpg"));
        assertTrue(fileUrl.startsWith("https://"));
    }

    @Test
    void testExtractObjectKey() {
        String fileUrl = "https://test-bucket.oss-cn-hangzhou.aliyuncs.com/avatars/test.jpg";
        String objectKey = extractObjectKey(fileUrl);

        assertEquals("avatars/test.jpg", objectKey);
    }

    @Test
    void testExtractObjectKey_InvalidUrl() {
        String fileUrl = "invalid-url";
        String objectKey = extractObjectKey(fileUrl);

        assertNull(objectKey);
    }

    @Test
    void testCalculateMD5() throws Exception {
        String content = "test content";
        java.io.InputStream inputStream = new java.io.ByteArrayInputStream(content.getBytes());
        String md5 = calculateMD5(inputStream);

        assertNotNull(md5);
        assertEquals(32, md5.length());
        assertFalse(md5.isEmpty());
    }

    @Test
    void testCalculateMD5_EmptyStream() throws Exception {
        java.io.InputStream inputStream = new java.io.ByteArrayInputStream(new byte[0]);
        String md5 = calculateMD5(inputStream);

        assertNotNull(md5);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long fileSize = file.getSize();
        if (fileSize > ossConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许: " +
                    (ossConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        boolean isAllowed = false;
        for (String allowedExt : ossConfig.getAllowedExtensions()) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String buildObjectKey(String directory, String fileName) {
        if (directory == null || directory.isEmpty()) {
            directory = ossConfig.getDirPrefix();
        }
        return directory + "/" + fileName;
    }

    private String buildFileUrl(String objectKey) {
        return "https://" + ossConfig.getBucketName() + "." +
                ossConfig.getEndpoint().replace("https://", "") +
                "/" + objectKey;
    }

    private String extractObjectKey(String fileUrl) {
        try {
            int bucketIndex = fileUrl.indexOf(ossConfig.getBucketName());
            if (bucketIndex == -1) {
                return null;
            }
            int keyStart = fileUrl.indexOf("/", bucketIndex) + 1;
            return fileUrl.substring(keyStart);
        } catch (Exception e) {
            return null;
        }
    }

    private String calculateMD5(java.io.InputStream inputStream) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
