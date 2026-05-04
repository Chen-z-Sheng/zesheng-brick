import request from '@/utils/request'

/**
 * 上传管理员头像至 OSS（固定键 avatars/admin/{userId}.jpg 覆盖写入，后端压缩为 JPG）
 * @param {File|Blob} file 图片文件
 * @returns {Promise<{ data?: { fileUrl?: string } }>}
 */
export function uploadAdminAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/oss/avatar',
    method: 'post',
    data: formData,
  })
}

/**
 * @deprecated 管理端请使用 uploadAdminAvatar；旧路径未实现
 */
export function uploadPhoto(params) {
  if (!params?.file) {
    return Promise.reject(new Error("缺少 file 参数"))
  }
  return uploadAdminAvatar(params.file)
}

/**
 * 获取OSS指定文件夹下的文件列表
 * @param {string} prefix 文件夹前缀（如：photos/20260111/）
 * @param {number} [expires=3600] 签名URL有效期（秒），默认3600
 * @returns {Promise<any>} 文件列表（包含name和url）
 */
export function listOssFiles(prefix, expires = 3600) {
  return request({
    url: '/admin/oss/list',
    method: 'get',
    params: { prefix, expires },
  });
}