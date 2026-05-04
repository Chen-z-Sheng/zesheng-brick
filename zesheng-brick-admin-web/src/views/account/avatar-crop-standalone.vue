<template>
  <div class="avatar-crop-standalone-root crop-island">
    <header class="acr-head">
      <el-button text type="primary" @click="onCancel">返回</el-button>
      <h1 class="acr-title">裁剪头像</h1>
      <span class="acr-placeholder"></span>
    </header>

    <div class="acr-grid">
      <div class="acr-main">
        <VueCropper
          v-if="workspaceUrl"
          ref="cropperRef"
          :key="workspaceUrl"
          :img="workspaceUrl"
          :wrapper="cropperWrapper"
          :crop-layout="cropperFrame"
          :center-box="true"
          mode="cover"
          output-type="jpeg"
          :output-size="0.92"
          :full="true"
          @real-time="handlePreview"
        />

        <div class="acr-toolbar">
          <el-radio-group v-model="cropShape" size="small">
            <el-radio-button label="round">圆形导出</el-radio-button>
            <el-radio-button label="square">方形导出</el-radio-button>
          </el-radio-group>
          <el-button-group>
            <el-button size="small" @click="onRotateLeft">左旋</el-button>
            <el-button size="small" @click="onRotateRight">右旋</el-button>
          </el-button-group>
        </div>

        <div class="acr-actions">
          <el-button @click="onCancel">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="onConfirm">确定并上传</el-button>
        </div>
      </div>

      <aside class="acr-side">
        <p class="acr-preview-label">预览</p>
        <div
          class="acr-preview-host"
          :class="{ 'acr-preview-host--round': cropShape === 'round', 'acr-preview-host--square': cropShape === 'square' }"
        >
          <section v-if="previews.url" class="acr-realtime-box" :style="previewBoxStyle">
            <img class="acr-realtime-img" :src="previews.url" alt="" :style="previews.img" />
          </section>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onBeforeUnmount } from "vue"
import { useRouter } from "vue-router"
import { useStore } from "vuex"
import { ElMessage } from "element-plus"
import { VueCropper } from "cropper-next-vue"
import "cropper-next-vue/style.css"
import { circleClipJpegBlob } from "@/utils/circleClipAvatarBlob"
import { uploadAdminAvatar } from "@/api/oss"
import { updateSelfProfile } from "@/api/user"

defineOptions({
  name: "AvatarCropStandalone",
})

const cropperWrapper = Object.freeze({ width: 480, height: 480 })
const cropperFrame = Object.freeze({ width: 320, height: 320 })

const PREVIEW_TARGET_PX = 120

const router = useRouter()
const store = useStore()

const cropperRef = ref(null)
const uploading = ref(false)
const cropShape = ref("round")

const workspaceUrl = computed(() => store.state.avatarCrop.workspaceObjectUrl)

const previews = reactive({
  w: 0,
  h: 0,
  url: "",
  img: {
    width: "0px",
    height: "0px",
    transform: "",
  },
})

const previewBoxStyle = computed(() => {
  const w = previews.w
  if (!w) return { display: "none" }
  return {
    width: `${previews.w}px`,
    height: `${previews.h}px`,
    overflow: "hidden",
    margin: "0",
    zoom: PREVIEW_TARGET_PX / w,
  }
})

function handlePreview(payload) {
  previews.w = payload.w
  previews.h = payload.h
  previews.url = payload.url
  previews.img = payload.img
}

function resetPreview() {
  previews.w = 0
  previews.h = 0
  previews.url = ""
  previews.img = { width: "0px", height: "0px", transform: "" }
}

function onRotateLeft() {
  cropperRef.value?.rotateLeft?.()
}

function onRotateRight() {
  cropperRef.value?.rotateRight?.()
}

function getErrMsg(err) {
  if (!err) return "操作失败"
  const d = err.response?.data
  const m = d?.message ?? d?.msg
  if (m) return String(m)
  if (typeof err === "string") return err
  if (err.message) return err.message
  return "操作失败，请稍后重试"
}

/** 从 R.data 中取上传地址；固定 OSS 路径覆盖上传时需带版本参数避免浏览器沿用旧缓存 */
function resolveUploadedAvatarUrl(res) {
  const payload = res?.data
  if (!payload || typeof payload !== "object") return ""
  const raw = payload.fileUrl || payload.url || ""
  if (!raw || typeof raw !== "string") return ""
  const u = raw.trim()
  if (!u) return ""
  const sep = u.includes("?") ? "&" : "?"
  return `${u}${sep}v=${Date.now()}`
}

function onCancel() {
  store.commit("avatarCrop/clearWorkspace")
  resetPreview()
  router.back()
}

async function onConfirm() {
  const cropper = cropperRef.value
  if (!cropper?.getCropBlob) {
    ElMessage.warning("裁剪组件未就绪")
    return
  }
  uploading.value = true
  let blob = null
  try {
    blob = await cropper.getCropBlob()
    if (cropShape.value === "round") {
      blob = await circleClipJpegBlob(blob)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(getErrMsg(e))
    uploading.value = false
    return
  }

  try {
    const file = new File([blob], "avatar.jpg", { type: "image/jpeg" })
    const res = await uploadAdminAvatar(file)
    const url = resolveUploadedAvatarUrl(res)
    if (!url) {
      throw new Error("上传成功但未返回头像地址")
    }
    await updateSelfProfile({ avatarUrl: url })
    await store.dispatch("user/getUserInfo")
    ElMessage.success("头像已更新")
    await router.replace({ path: "/personal-center" })
  } catch (e) {
    console.error(e)
    ElMessage.error(getErrMsg(e))
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  if (!workspaceUrl.value) {
    ElMessage.warning("请先选择图片")
    router.replace({ path: "/personal-center" })
  }
})

onBeforeUnmount(() => {
  // 浏览器后退等未点「取消」时也释放 blob，避免泄漏
  store.commit("avatarCrop/clearWorkspace")
})
</script>

<style lang="scss" scoped>
.avatar-crop-standalone-root {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 16px 20px 32px;
  background: #f2f3f5;
}

.acr-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 960px;
  margin: 0 auto 12px;
}

.acr-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.acr-placeholder {
  width: 48px;
}

.acr-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: center;
  align-items: flex-start;
  max-width: 960px;
  margin: 0 auto;
}

.acr-main {
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.acr-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 16px;
}

.acr-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.acr-side {
  padding: 16px;
  background: #fafafa;
  border: 1px solid #e5e6eb;
  border-radius: 12px;
  min-width: 160px;
}

.acr-preview-label {
  margin: 0 0 12px;
  font-size: 13px;
  color: #4e5969;
}

.acr-preview-host {
  width: 120px;
  height: 120px;
  overflow: hidden;
  border: 1px solid #e5e6eb;
  background: #fff;
}

.acr-preview-host--round {
  border-radius: 50%;
}

.acr-preview-host--square {
  border-radius: 12px;
}

.acr-realtime-img {
  display: block;
}
</style>

<!-- 非 scoped：仅作用于本页根 class，抵消 layouts/vab 全局 * transition 与 img object-fit -->
<style lang="scss">
.avatar-crop-standalone-root.crop-island,
.avatar-crop-standalone-root.crop-island * {
  transition: none !important;
}

.avatar-crop-standalone-root.crop-island img {
  object-fit: fill !important;
}
</style>
