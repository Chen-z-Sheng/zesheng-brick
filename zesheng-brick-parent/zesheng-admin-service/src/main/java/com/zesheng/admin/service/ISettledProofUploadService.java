package com.zesheng.admin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 回款凭证上传（固结/行情），路径：settled-proof/consolidated|market/年/月/随机名.png，大图自动压缩
 */
public interface ISettledProofUploadService {

    /**
     * 上传回款凭证图片
     *
     * @param file   图片文件
     * @param subDir 子目录：consolidated（固结）或 market（行情）
     * @return 公网访问 URL
     */
    String upload(MultipartFile file, String subDir);
}
