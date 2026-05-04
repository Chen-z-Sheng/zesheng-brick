<template>
  <div class="personal-center-page">
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>
      <div v-loading="detailLoading" class="profile-row">
        <div class="avatar-col">
          <img :src="displayAvatar" alt="头像" class="preview-avatar" />
          <el-button type="primary" plain @click="openFilePicker">修改头像</el-button>
          <p class="hint">
            选择图片后进入裁剪页面，完成裁剪并上传即可更新头像。若图片较大，会先缩小后再裁剪（最长边不超过 {{ maxCropEdgePx }} 像素）。
          </p>
          <input
            ref="fileInputRef"
            type="file"
            class="hidden-file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            @change="onNativeFileChange"
          />
        </div>
        <div class="readonly-block">
          <p><span class="label">用户名</span>{{ detail.username || "-" }}</p>
          <p><span class="label">用户 ID</span>{{ detail.id ?? "-" }}</p>
          <p><span class="label">角色</span>{{ detail.roleCode || "-" }}</p>
          <p><span class="label">账号状态</span>{{ formatStatus(detail.status) }}</p>
          <p><span class="label">最近登录</span>{{ formatDateTime(detail.lastLoginAt) }}</p>
          <p><span class="label">登录 IP</span>{{ detail.lastLoginIp || "-" }}</p>
        </div>
      </div>

      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="88px" class="profile-form">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" clearable placeholder="选填" maxlength="32" class="input-md" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="profileSaving" @click="submitProfile">保存手机号</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="pwd-form">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="off" class="input-md" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="new-password" class="input-md" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="new-password" class="input-md" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdSaving" @click="submitPassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useStore } from "vuex"
import { ElLoading, ElMessage } from "element-plus"
import { getUserInfo, updateSelfProfile, changeOwnPassword } from "@/api/user"
import { createCropWorkspaceObjectUrl, MAX_CROP_LONG_EDGE } from "@/utils/cropWorkspaceImage"

defineOptions({
  name: "PersonalCenter",
})

// 与裁剪预处理逻辑共用上限，便于文案与行为一致
const maxCropEdgePx = MAX_CROP_LONG_EDGE

const router = useRouter()
const store = useStore()

const detailLoading = ref(false)
const detail = ref({})

const defaultAvatar = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"

const displayAvatar = computed(() => {
  const u = detail.value.avatarUrl
  return u && String(u).trim() ? String(u).trim() : defaultAvatar
})

const profileForm = reactive({
  phone: "",
})

const profileRules = {
  phone: [{ max: 32, message: "手机号过长", trigger: "blur" }],
}

const profileFormRef = ref(null)
const profileSaving = ref(false)

const fileInputRef = ref(null)

const MAX_PICK_BYTES = 8 * 1024 * 1024

const pwdFormRef = ref(null)
const pwdSaving = ref(false)
const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
})

const pwdRules = {
  oldPassword: [{ required: true, message: "请输入当前密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 64, message: "密码长度为 6～64 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (_rule, val, cb) => {
        if (val !== pwdForm.newPassword) {
          cb(new Error("两次输入的新密码不一致"))
        } else {
          cb()
        }
      },
      trigger: "blur",
    },
  ],
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

function validatePickFile(file) {
  if (!file) return "未选择文件"
  const okType = ["image/jpeg", "image/png", "image/webp", "image/gif"].includes(file.type)
  if (!okType) return "仅支持 jpg、png、webp、gif 图片"
  if (file.size > MAX_PICK_BYTES) return "原图不能超过 8MB，请先压缩或换一张"
  return ""
}

function openFilePicker() {
  fileInputRef.value?.click?.()
}

async function onNativeFileChange(ev) {
  const input = ev.target
  const file = input?.files?.[0]
  if (input) input.value = ""
  if (!file) return

  const msg = validatePickFile(file)
  if (msg) {
    ElMessage.error(msg)
    return
  }

  store.commit("avatarCrop/clearWorkspace")

  const loading = ElLoading.service({
    lock: true,
    text: "正在准备图片…",
    background: "rgba(0, 0, 0, 0.35)",
  })
  try {
    const url = await createCropWorkspaceObjectUrl(file)
    store.commit("avatarCrop/setWorkspaceUrl", url)
    await router.push({ name: "AvatarCropStandalone" })
  } catch (e) {
    console.error(e)
    ElMessage.error(getErrMsg(e) || "图片处理失败，请换一张")
  } finally {
    loading.close()
  }
}

function formatDateTime(val) {
  if (!val) return "-"
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return "-"
  const pad = (n) => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatStatus(status) {
  if (status === undefined || status === null) return "-"
  if (status === 1 || status === "ENABLE") return "启用"
  if (status === 0 || status === "DISABLE") return "禁用"
  return String(status)
}

async function loadDetail() {
  const userId = store.getters["user/userId"]
  if (!userId) {
    ElMessage.warning("未获取到用户信息")
    return
  }
  detailLoading.value = true
  try {
    const res = await getUserInfo(userId)
    const u = res.data
    detail.value = u || {}
    profileForm.phone = u?.phone || ""
  } catch (e) {
    console.error(e)
    ElMessage.error(getErrMsg(e) || "加载资料失败")
  } finally {
    detailLoading.value = false
  }
}

async function submitProfile() {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    profileSaving.value = true
    try {
      await updateSelfProfile({
        phone: profileForm.phone,
      })
      ElMessage.success("手机号已保存")
      await store.dispatch("user/getUserInfo")
      await loadDetail()
    } catch (e) {
      console.error(e)
      ElMessage.error(getErrMsg(e))
    } finally {
      profileSaving.value = false
    }
  })
}

async function submitPassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await changeOwnPassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword,
      })
      ElMessage.success("密码已修改，请妥善保管")
      pwdForm.oldPassword = ""
      pwdForm.newPassword = ""
      pwdForm.confirmPassword = ""
      pwdFormRef.value.resetFields()
    } catch (e) {
      console.error(e)
      ElMessage.error(getErrMsg(e))
    } finally {
      pwdSaving.value = false
    }
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.personal-center-page {
  padding: 20px;
  max-width: 720px;
}

.section-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
}

.profile-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  align-items: flex-start;
}

.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  flex-shrink: 0;
}

.preview-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--el-border-color-lighter);
}

.hidden-file {
  position: fixed;
  left: -2000px;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.readonly-block {
  flex: 1;
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-regular);

  .label {
    display: inline-block;
    width: 88px;
    color: var(--el-text-color-secondary);
  }
}

.profile-form,
.pwd-form {
  max-width: 560px;
}

.input-md {
  max-width: 320px;
}

.hint {
  margin: 0;
  max-width: 320px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
