/** 送入裁剪页前的最长边像素上限 */
export const MAX_CROP_LONG_EDGE = 1280

export function loadImageFromUrl(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error("图片加载失败"))
    img.src = url
  })
}

/**
 * 生成裁剪工作副本 Object URL：超长边则缩小到 MAX_CROP_LONG_EDGE 再导出 JPEG。
 */
export async function createCropWorkspaceObjectUrl(file) {
  const rawUrl = URL.createObjectURL(file)
  try {
    const img = await loadImageFromUrl(rawUrl)
    const w = img.naturalWidth || img.width
    const h = img.naturalHeight || img.height
    if (!w || !h) {
      throw new Error("无法读取图片尺寸")
    }
    const maxEdge = Math.max(w, h)
    if (maxEdge <= MAX_CROP_LONG_EDGE) {
      return rawUrl
    }
    const scale = MAX_CROP_LONG_EDGE / maxEdge
    const tw = Math.max(1, Math.round(w * scale))
    const th = Math.max(1, Math.round(h * scale))
    const canvas = document.createElement("canvas")
    canvas.width = tw
    canvas.height = th
    const ctx = canvas.getContext("2d")
    if (!ctx) {
      throw new Error("浏览器无法创建画布")
    }
    if ("imageSmoothingQuality" in ctx) {
      ctx.imageSmoothingQuality = "high"
    }
    ctx.drawImage(img, 0, 0, tw, th)
    URL.revokeObjectURL(rawUrl)
    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob(
        (b) => {
          if (b) resolve(b)
          else reject(new Error("图片编码失败"))
        },
        "image/jpeg",
        0.88
      )
    })
    return URL.createObjectURL(blob)
  } catch (e) {
    URL.revokeObjectURL(rawUrl)
    throw e
  }
}
