<template>
  <div class="index-container" v-loading="overviewLoading">
    <el-row :gutter="20">
      <el-col :lg="6" :md="12" :sm="24" :xl="6" :xs="24">
        <el-card shadow="never">
          <template #header>
            <span>小程序用户</span>
          </template>
          <div class="chart-container">
            <div class="chart-header">
              <div class="chart-title">
                <span>近7日新增注册</span>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ overview.clientUserTotal }}</div>
                  <div class="stat-label">累计用户</div>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <vab-chart autoresize :option="userChartOption" />
            </div>
            <div class="chart-footer">
              <div class="trend-info">
                <div class="trend-item">
                  <el-icon :class="userTrendIconClass">
                    <component :is="userTrendIcon" />
                  </el-icon>
                  <span class="trend-text">{{ userTrendText }}</span>
                </div>
                <div class="trend-item">
                  <el-icon class="trend-icon">
                    <User />
                  </el-icon>
                  <span class="trend-text">今日新增 {{ overview.clientUserRegisterToday }} 人</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :lg="6" :md="12" :sm="24" :xl="6" :xs="24">
        <el-card shadow="never">
          <template #header>
            <span>固结报单</span>
          </template>
          <div class="chart-container">
            <div class="chart-header">
              <div class="chart-title">
                <span>近7日提交趋势</span>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ overview.formSubmissionToday }}</div>
                  <div class="stat-label">今日提交</div>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <vab-chart autoresize :option="formChartOption" />
            </div>
            <div class="chart-footer">
              <div class="trend-info">
                <div class="trend-item">
                  <el-icon :class="formTrendIconClass">
                    <component :is="formTrendIcon" />
                  </el-icon>
                  <span class="trend-text">{{ formTrendText }}</span>
                </div>
                <div class="trend-item">
                  <el-icon class="trend-icon">
                    <Document />
                  </el-icon>
                  <span class="trend-text">近7日共 {{ formWeekTotal }} 单</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :lg="6" :md="12" :sm="24" :xl="6" :xs="24">
        <el-card shadow="never">
          <template #header>
            <span>行情报单</span>
          </template>
          <div class="chart-container">
            <div class="chart-header">
              <div class="chart-title">
                <span>近7日提交趋势</span>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ overview.sellOrderToday }}</div>
                  <div class="stat-label">今日提交</div>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <vab-chart autoresize :option="sellChartOption" />
            </div>
            <div class="chart-footer">
              <div class="trend-info">
                <div class="trend-item">
                  <el-icon :class="sellTrendIconClass">
                    <component :is="sellTrendIcon" />
                  </el-icon>
                  <span class="trend-text">{{ sellTrendText }}</span>
                </div>
                <div class="trend-item">
                  <el-icon class="trend-icon">
                    <TrendCharts />
                  </el-icon>
                  <span class="trend-text">近7日共 {{ sellWeekTotal }} 单</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :lg="6" :md="12" :sm="24" :xl="6" :xs="24">
        <el-card shadow="never">
          <template #header>
            <span>运行环境</span>
          </template>
          <div class="chart-container">
            <div class="chart-header">
              <div class="chart-title">
                <span>当前主机 / JVM</span>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ jvmHeapDisplay }}</div>
                  <div class="stat-label">JVM 堆占用</div>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <vab-chart autoresize :option="serverChartOption" />
            </div>
            <div class="chart-footer">
              <div class="trend-info">
                <div class="trend-item">
                  <el-icon class="trend-up">
                    <Cpu />
                  </el-icon>
                  <span class="trend-text">{{ serverCpuText }}</span>
                </div>
                <div class="trend-item">
                  <el-icon class="trend-icon">
                    <Timer />
                  </el-icon>
                  <span class="trend-text">已运行 {{ uptimeText }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :lg="24" :md="24" :sm="24" :xl="24" :xs="24">
        <el-card class="card" shadow="never">
          <template #header>
            <span>依赖信息</span>
          </template>
          <div class="rspack-info">
            <div class="rspack-item">
              <div class="rspack-name">Rspack版本</div>
              <div class="rspack-version">{{ devDependencies['rspack'] }}</div>
            </div>
          </div>

          <div class="dependency-content">
            <div class="dependency-grid">
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Vue版本</div>
                  <div class="dependency-version">{{ dependencies['vue'] }}</div>
                </div>
              </div>
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Vuex版本</div>
                  <div class="dependency-version">{{ dependencies['vuex'] }}</div>
                </div>
              </div>
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Vue Router</div>
                  <div class="dependency-version">{{ dependencies['vue-router'] }}</div>
                </div>
              </div>
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Element Plus</div>
                  <div class="dependency-version">{{ dependencies['element-plus'] }}</div>
                </div>
              </div>
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Axios版本</div>
                  <div class="dependency-version">{{ dependencies['axios'] }}</div>
                </div>
              </div>
              <div class="dependency-item">
                <div class="dependency-info">
                  <div class="dependency-name">Sass版本</div>
                  <div class="dependency-version">{{ devDependencies['sass'] }}</div>
                </div>
              </div>
            </div>

            <div class="system-info">
              <div class="info-header">
                <vab-icon :icon="['fas', 'info-circle']" />
                <span>系统信息</span>
              </div>
              <div class="info-content">
                <div class="info-item">
                  <span class="info-label">构建时间:</span>
                  <span class="info-value">{{ updateTime || '未知' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">运行环境:</span>
                  <span class="info-value">{{ nodeEnv }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">用户代理:</span>
                  <span class="info-value">{{ userAgent }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, onMounted, onUnmounted, reactive, ref } from 'vue'

const VabChart = defineAsyncComponent(() => import('@/plugins/echarts'))
import { dependencies, devDependencies } from '../../../package.json'
import { getDashboardOverview, getDashboardServerRuntime } from '@/api/dashboard'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  ArrowUp,
  User,
  Document,
  Monitor,
  TrendCharts,
  Cpu,
  Timer,
} from '@element-plus/icons-vue'

/** 仅「运行环境」卡片轮询；业务三张卡只在进入页面时请求 overview 一次 */
const SERVER_POLL_MS = 5000

const updateTime = process.env.VUE_APP_UPDATE_TIME
const nodeEnv = process.env.NODE_ENV
const userAgent = typeof navigator !== 'undefined' ? navigator.userAgent : ''

let serverPollTimer = null

const overviewLoading = ref(false)
const overview = reactive({
  formSubmissionDaily: [],
  formSubmissionToday: 0,
  formSubmissionYesterday: 0,
  clientUserTotal: 0,
  clientUserRegisterDaily: [],
  clientUserRegisterToday: 0,
  clientUserRegisterYesterday: 0,
  sellOrderDaily: [],
  sellOrderToday: 0,
  sellOrderYesterday: 0,
  serverRuntime: null,
})

const formChartOption = ref(buildLineOption('提交数', []))
const userChartOption = ref(buildBarOption('新增用户', []))
const sellChartOption = ref(buildLineOption('提交数', []))
const serverChartOption = ref(buildServerBarOption([]))

function buildLineOption(seriesName, pairs) {
  const labels = pairs.map((p) => p.label)
  const vals = pairs.map((p) => Number(p.count) || 0)
  return {
    color: ['#409EFF', '#8B5CF6'],
    backgroundColor: 'transparent',
    grid: { top: '12%', left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: labels,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399', fontSize: 11 },
      },
    ],
    yAxis: [
      {
        type: 'value',
        axisTick: { show: false },
        axisLine: { show: false },
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { color: '#F5F7FA', type: 'dashed' } },
      },
    ],
    series: [
      {
        name: seriesName,
        type: 'line',
        data: vals,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#409EFF' },
        itemStyle: { color: '#409EFF', borderWidth: 2, borderColor: '#fff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
              { offset: 1, color: 'rgba(139, 92, 246, 0.06)' },
            ],
          },
        },
      },
    ],
  }
}

function buildBarOption(seriesName, pairs) {
  const labels = pairs.map((p) => p.label)
  const vals = pairs.map((p) => Number(p.count) || 0)
  return {
    color: ['#409EFF'],
    backgroundColor: 'transparent',
    grid: { top: '12%', left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: [
      {
        type: 'category',
        data: labels,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399', fontSize: 11 },
      },
    ],
    yAxis: [
      {
        type: 'value',
        axisTick: { show: false },
        axisLine: { show: false },
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { color: '#F5F7FA', type: 'dashed' } },
      },
    ],
    series: [
      {
        name: seriesName,
        type: 'bar',
        barWidth: '55%',
        data: vals,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#409EFF' },
              { offset: 1, color: '#8B5CF6' },
            ],
          },
          borderRadius: [6, 6, 0, 0],
        },
      },
    ],
  }
}

function buildServerBarOption(metrics) {
  const labels = metrics.map((m) => m.label)
  const vals = metrics.map((m) => m.value)
  return {
    color: ['#409EFF', '#8B5CF6', '#A855F7', '#C084FC'],
    backgroundColor: 'transparent',
    grid: { top: '12%', left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: [
      {
        type: 'category',
        data: labels,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399', fontSize: 11 },
      },
    ],
    yAxis: [
      {
        type: 'value',
        max: 100,
        axisTick: { show: false },
        axisLine: { show: false },
        axisLabel: { color: '#909399', fontSize: 11, formatter: '{value}%' },
        splitLine: { lineStyle: { color: '#F5F7FA', type: 'dashed' } },
      },
    ],
    series: [
      {
        name: '使用率',
        type: 'bar',
        data: vals,
        barWidth: '50%',
        itemStyle: {
          color: (params) => {
            const colors = ['#409EFF', '#8B5CF6', '#A855F7', '#C084FC']
            return colors[params.dataIndex % colors.length]
          },
          borderRadius: [4, 4, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          formatter: (p) => (p.value != null ? `${p.value}%` : '-'),
          color: '#2c3e50',
          fontSize: 11,
        },
      },
    ],
  }
}

function sumDailyPairs(pairs) {
  if (!Array.isArray(pairs)) return 0
  return pairs.reduce((s, p) => s + (Number(p.count) || 0), 0)
}

function compareText(today, yesterday, unit) {
  if (yesterday === 0 && today === 0) return '与昨日持平（均为0）'
  if (yesterday === 0) return `昨日为 0，今日 ${today}${unit}`
  const pct = ((today - yesterday) / yesterday) * 100
  const sign = pct > 0 ? '+' : ''
  return `较昨日 ${sign}${pct.toFixed(1)}%`
}

const formWeekTotal = computed(() => sumDailyPairs(overview.formSubmissionDaily))
const sellWeekTotal = computed(() => sumDailyPairs(overview.sellOrderDaily))

const formCompare = computed(() =>
  compareText(overview.formSubmissionToday, overview.formSubmissionYesterday, '单')
)
const formTrendIcon = computed(() => {
  if (overview.formSubmissionToday > overview.formSubmissionYesterday) return ArrowUp
  if (overview.formSubmissionToday < overview.formSubmissionYesterday) return ArrowDown
  return Monitor
})
const formTrendIconClass = computed(() =>
  overview.formSubmissionToday >= overview.formSubmissionYesterday ? 'trend-up' : 'trend-down'
)
const formTrendText = computed(() => formCompare.value)

const userTrendText = computed(() =>
  compareText(overview.clientUserRegisterToday, overview.clientUserRegisterYesterday, '人')
)
const userTrendIcon = computed(() => {
  if (overview.clientUserRegisterToday > overview.clientUserRegisterYesterday) return ArrowUp
  if (overview.clientUserRegisterToday < overview.clientUserRegisterYesterday) return ArrowDown
  return User
})
const userTrendIconClass = computed(() =>
  overview.clientUserRegisterToday >= overview.clientUserRegisterYesterday ? 'trend-up' : 'trend-down'
)

const sellTrendText = computed(() =>
  compareText(overview.sellOrderToday, overview.sellOrderYesterday, '单')
)
const sellTrendIcon = computed(() => {
  if (overview.sellOrderToday > overview.sellOrderYesterday) return ArrowUp
  if (overview.sellOrderToday < overview.sellOrderYesterday) return ArrowDown
  return TrendCharts
})
const sellTrendIconClass = computed(() =>
  overview.sellOrderToday >= overview.sellOrderYesterday ? 'trend-up' : 'trend-down'
)

const jvmHeapDisplay = computed(() => {
  const v = overview.serverRuntime?.jvmHeapUsedPercent
  if (v == null || Number.isNaN(v)) return '—'
  return `${Number(v).toFixed(1)}%`
})

const uptimeText = computed(() => {
  const ms = overview.serverRuntime?.jvmUptimeMs
  if (ms == null || ms < 0) return '—'
  const s = Math.floor(ms / 1000)
  const d = Math.floor(s / 86400)
  const h = Math.floor((s % 86400) / 3600)
  const m = Math.floor((s % 3600) / 60)
  if (d > 0) return `${d}天${h}小时`
  if (h > 0) return `${h}小时${m}分`
  return `${m}分钟`
})

const serverCpuText = computed(() => {
  const cpu = overview.serverRuntime?.systemCpuLoadPercent
  if (cpu == null) return 'CPU：—'
  return `CPU ${Number(cpu).toFixed(1)}%`
})

function pctOrZero(v) {
  if (v == null || Number.isNaN(Number(v))) return 0
  const n = Number(v)
  return Math.min(100, Math.max(0, Math.round(n * 100) / 100))
}

function applyServerRuntime(rt) {
  overview.serverRuntime = rt ?? null
  const metrics = [
    { label: 'CPU', value: pctOrZero(rt?.systemCpuLoadPercent) },
    { label: '物理内存', value: pctOrZero(rt?.physicalMemoryUsedPercent) },
    { label: 'JVM堆', value: pctOrZero(rt?.jvmHeapUsedPercent) },
    { label: '磁盘', value: pctOrZero(rt?.diskUsedPercent) },
  ]
  serverChartOption.value = buildServerBarOption(metrics)
}

function applyOverviewPayload(payload) {
  if (!payload) return
  overview.formSubmissionDaily = payload.formSubmissionDaily || []
  overview.formSubmissionToday = Number(payload.formSubmissionToday) || 0
  overview.formSubmissionYesterday = Number(payload.formSubmissionYesterday) || 0
  overview.clientUserTotal = Number(payload.clientUserTotal) || 0
  overview.clientUserRegisterDaily = payload.clientUserRegisterDaily || []
  overview.clientUserRegisterToday = Number(payload.clientUserRegisterToday) || 0
  overview.clientUserRegisterYesterday = Number(payload.clientUserRegisterYesterday) || 0
  overview.sellOrderDaily = payload.sellOrderDaily || []
  overview.sellOrderToday = Number(payload.sellOrderToday) || 0
  overview.sellOrderYesterday = Number(payload.sellOrderYesterday) || 0

  formChartOption.value = buildLineOption('提交数', overview.formSubmissionDaily)
  userChartOption.value = buildBarOption('新增用户', overview.clientUserRegisterDaily)
  sellChartOption.value = buildLineOption('提交数', overview.sellOrderDaily)

  applyServerRuntime(payload.serverRuntime ?? null)
}

async function pollServerRuntime() {
  try {
    const res = await getDashboardServerRuntime()
    const rt = res && res.data !== undefined ? res.data : res
    applyServerRuntime(rt)
  } catch {
    // 轮询失败静默，避免重复弹窗；鉴权失败由全局请求拦截处理
  }
}

function clearServerPoll() {
  if (serverPollTimer != null) {
    clearInterval(serverPollTimer)
    serverPollTimer = null
  }
}

function startServerPoll() {
  clearServerPoll()
  serverPollTimer = window.setInterval(pollServerRuntime, SERVER_POLL_MS)
}

function onPageVisibilityChange() {
  if (typeof document === 'undefined') return
  if (document.hidden) {
    clearServerPoll()
  } else {
    pollServerRuntime()
    startServerPoll()
  }
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    const res = await getDashboardOverview()
    const payload = res && res.data !== undefined ? res.data : res
    applyOverviewPayload(payload)
  } catch (e) {
    console.error(e)
    ElMessage.error('首页数据加载失败，请检查网络或重新登录')
  } finally {
    overviewLoading.value = false
  }
}

onMounted(async () => {
  await loadOverview()
  pollServerRuntime()
  startServerPoll()
  if (typeof document !== 'undefined') {
    document.addEventListener('visibilitychange', onPageVisibilityChange)
  }
})

onUnmounted(() => {
  clearServerPoll()
  if (typeof document !== 'undefined') {
    document.removeEventListener('visibilitychange', onPageVisibilityChange)
  }
})
</script>

<style lang="scss" scoped>
.index-container {
  padding: 0 !important;
  margin: 0 !important;
  background: #f5f7f8 !important;

  :deep() {
    .el-card__body {
      .echarts {
        width: 100%;
        height: 115px;
      }
    }
  }

  .card {
    height: auto !important;
    display: flex;
    flex-direction: column;

    :deep() {
      .el-card__body {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .echarts {
          width: 100%;
          height: 305px;
        }
      }
    }

    .dependency-content {
      .dependency-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 15px;
        margin-bottom: 25px;
        flex-shrink: 0;

        .dependency-item {
          display: flex;
          align-items: center;
          padding: 15px;
          background: #f8f9fa;
          border: 1px solid #e9ecef;
          border-radius: 8px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            background: #ffffff;
          }

          .dependency-info {
            flex: 1;
            text-align: center;

            .dependency-name {
              font-size: 0.9rem;
              color: #6c757d;
              margin-bottom: 6px;
              font-weight: 500;
            }

            .dependency-version {
              font-size: 1.1rem;
              color: #2c3e50;
              font-weight: 600;
            }
          }
        }
      }

      .system-info {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: 8px;
        padding: 20px;
        flex-shrink: 0;

        .info-header {
          display: flex;
          align-items: center;
          margin-bottom: 15px;
          padding-bottom: 10px;
          border-bottom: 2px solid #dee2e6;

          .vab-icon {
            color: #409eff;
            margin-right: 3px;
            font-size: 1.1rem;
          }

          span {
            font-size: 1.1rem;
            font-weight: 600;
            color: #2c3e50;
          }
        }

        .info-content {
          .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #f1f3f4;

            &:last-child {
              border-bottom: none;
            }

            .info-label {
              color: #6c757d;
              font-weight: 500;
              font-size: 0.9rem;
            }

            .info-value {
              color: #2c3e50;
              font-weight: 600;
              font-size: 0.9rem;
              max-width: 60%;
              text-align: right;
              word-break: break-all;
            }
          }
        }
      }
    }
  }

  .rspack-info {
    margin-bottom: 12px;
    .rspack-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 12px;
      background: #f8f9fa;
      border-radius: 8px;
      font-size: 0.95rem;
    }
    .rspack-name {
      color: #6c757d;
    }
    .rspack-version {
      font-weight: 600;
      color: #2c3e50;
    }
  }

  .chart-container {
    height: 200px;
    display: flex;
    flex-direction: column;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #f0f0f0;
      flex-shrink: 0;

      .chart-title {
        display: flex;
        align-items: center;
        font-size: 1rem;
        font-weight: 600;
        color: #2c3e50;
      }

      .chart-stats {
        .stat-item {
          text-align: right;

          .stat-value {
            font-size: 1.5rem;
            font-weight: 700;
            color: #2c3e50;
            line-height: 1;
          }

          .stat-label {
            font-size: 0.8rem;
            color: #7f8c8d;
            margin-top: 2px;
          }
        }
      }
    }

    .chart-content {
      flex: 1;
      margin-bottom: 15px;
      min-height: 0;

      .echarts {
        height: 100% !important;
        min-height: 120px;
      }
    }

    .chart-footer {
      flex-shrink: 0;

      .trend-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 8px;

        .trend-item {
          display: flex;
          align-items: center;
          font-size: 0.85rem;
          min-width: 0;

          .trend-up {
            color: #8b5cf6;
            margin-right: 3px;
            font-size: 0.8rem;
            flex-shrink: 0;
          }

          .trend-down {
            color: #f56c6c;
            margin-right: 3px;
            font-size: 0.8rem;
            flex-shrink: 0;
          }

          .trend-icon {
            color: #409eff;
            margin-right: 3px;
            font-size: 0.8rem;
            flex-shrink: 0;
          }

          .trend-text {
            color: #7f8c8d;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }

  .el-card {
    height: 280px;
    display: flex;
    flex-direction: column;

    :deep() {
      .el-card__body {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }
    }
  }
}
</style>
