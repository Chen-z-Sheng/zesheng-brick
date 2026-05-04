<template>
  <div class="preferences-page">
    <el-alert type="info" show-icon :closable="false" class="hint-alert">
      <template #title>
        <span>控制台偏好说明</span>
      </template>
      <div class="alert-body">
        <p>「主题风格」切换的是 Element Plus 主色、顶部进度条、侧栏选中背景等（通过 CSS 变量生效）。完整换肤包可参考仓库内 themes 目录说明。</p>
        <p>与侧边栏「组件 → 系统配置」中的业务键值参数不是同一类设置。</p>
      </div>
    </el-alert>

    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="card-title">外观与布局</span>
      </template>
      <p class="muted">与顶部导航栏「主题配置」抽屉共用同一套本地存储，在此修改后刷新页面仍会保留。</p>

      <div class="field-block">
        <div class="field-label">主题风格</div>
        <el-radio-group v-model="theme.name">
          <el-radio-button label="default">默认</el-radio-button>
          <el-radio-button label="green">绿荫草场</el-radio-button>
          <el-radio-button label="glory">荣耀典藏</el-radio-button>
        </el-radio-group>
      </div>

      <div class="field-block">
        <div class="field-label">布局</div>
        <el-radio-group v-model="theme.layout">
          <el-radio-button label="vertical">纵向</el-radio-button>
          <el-radio-button label="horizontal">横向</el-radio-button>
        </el-radio-group>
      </div>

      <div class="field-block">
        <div class="field-label">固定头部</div>
        <el-switch v-model="theme.header" active-value="fixed" inactive-value="noFixed" />
      </div>

      <div class="field-block">
        <div class="field-label">多标签页</div>
        <el-switch v-model="theme.tabsBar" active-value="true" inactive-value="false" />
      </div>

      <div class="field-block">
        <div class="field-label">右侧主题配置入口</div>
        <p class="muted">与顶部导航栏画笔图标打开的是同一套存储，可任选一处修改。</p>
      </div>

      <el-button type="primary" @click="saveTheme">保存设置</el-button>
    </el-card>

    <el-card v-if="canOpenBizConfig" shadow="never" class="section-card">
      <template #header>
        <span class="card-title">业务参数</span>
      </template>
      <p class="muted">数据库中的键值型业务配置（如站点文案、开关等），需要相应菜单权限。</p>
      <el-button type="primary" link @click="goBizConfig">打开「系统配置」页面</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useStore } from "vuex"
import { ElMessage } from "element-plus"
import { layout as defaultLayout } from "@/config"
import { applyThemeBodyClassFromStorage } from "@/utils/applyThemeBodyClass"

defineOptions({
  name: "Preferences",
})

const THEME_KEY = "vue-admin-better-theme"

const store = useStore()
const router = useRouter()

const permissions = computed(() => store.getters["user/permissions"] || [])
const canOpenBizConfig = computed(() => permissions.value.includes("admin:config:list"))

const theme = reactive({
  name: "default",
  layout: "",
  header: "fixed",
  tabsBar: "true",
})

function handleIsMobile() {
  return document.body.getBoundingClientRect().width - 1 < 992
}

function saveTheme() {
  const { name, layout, header, tabsBar } = theme
  localStorage.setItem(
    THEME_KEY,
    `{
            "name":"${name}",
            "layout":"${layout}",
            "header":"${header}",
            "tabsBar":"${tabsBar}"
          }`
  )
  if (!handleIsMobile()) {
    store.dispatch("settings/changeLayout", theme.layout)
  }
  store.dispatch("settings/changeHeader", theme.header)
  store.dispatch("settings/changeTabsBar", theme.tabsBar)
  applyThemeBodyClassFromStorage()
  ElMessage.success("已保存")
}

function goBizConfig() {
  router.push("/vab/settings")
}

/** 与顶部主题抽屉的 Switch 一致，统一成字符串 true/false */
function normalizeTabsBar(v) {
  if (v === true || v === "true") return "true"
  return "false"
}

onMounted(() => {
  const raw = localStorage.getItem(THEME_KEY)
  const layoutGx = store.getters["settings/layout"] || defaultLayout
  const headerGx = store.getters["settings/header"] || "fixed"
  const tabsGx = normalizeTabsBar(store.getters["settings/tabsBar"])

  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      theme.name = parsed.name || "default"
      theme.layout = parsed.layout || layoutGx
      theme.header = parsed.header || headerGx
      theme.tabsBar =
        parsed.tabsBar !== undefined && parsed.tabsBar !== null
          ? normalizeTabsBar(parsed.tabsBar)
          : tabsGx
    } catch {
      theme.layout = layoutGx
      theme.header = headerGx
      theme.tabsBar = tabsGx
    }
  } else {
    theme.layout = layoutGx
    theme.header = headerGx
    theme.tabsBar = tabsGx
  }
})
</script>

<style lang="scss" scoped>
.preferences-page {
  padding: 20px;
  max-width: 640px;
}

.hint-alert {
  margin-bottom: 16px;
}

.section-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
}

.muted {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0 0 16px;
}

.field-block {
  margin-bottom: 16px;
}

.field-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.alert-body {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);

  p {
    margin: 6px 0 0;
  }
}
</style>
