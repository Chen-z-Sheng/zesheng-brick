/**
 * 将方形头像 Blob 转为圆形 JPEG（白底圆内切）
 */
export async function circleClipJpegBlob(blob) {
  let bmp
  try {
    bmp = await createImageBitmap(blob)
  } catch {
    return Promise.reject(new Error("浏览器无法处理裁剪结果，请换一张图或选方形导出"))
  }
  const s = Math.min(bmp.width, bmp.height)
  const canvas = document.createElement("canvas")
  canvas.width = s
  canvas.height = s
  const ctx = canvas.getContext("2d")
  if (!ctx) {
    bmp.close?.()
    return Promise.reject(new Error("浏览器不支持画布"))
  }
  ctx.fillStyle = "#ffffff"
  ctx.fillRect(0, 0, s, s)
  ctx.beginPath()
  ctx.arc(s / 2, s / 2, s / 2, 0, Math.PI * 2)
  ctx.closePath()
  ctx.clip()
  const ox = (bmp.width - s) / 2
  const oy = (bmp.height - s) / 2
  ctx.drawImage(bmp, ox, oy, s, s, 0, 0, s, s)
  bmp.close?.()
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (b) => {
        if (!b) reject(new Error("圆形导出失败"))
        else resolve(b)
      },
      "image/jpeg",
      0.92
    )
  })
}
