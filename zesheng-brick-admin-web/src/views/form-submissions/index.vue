<template>
  <div class="form-submissions-page">
    <div class="toolbar">
      <el-input
        v-model="query.schemeNameKeyword"
        clearable
        placeholder="方案名称"
        class="toolbar-scheme"
        @input="onKeywordInput"
        @clear="onClearAndSearch"
      />
      <el-input
        v-model="query.userKeyword"
        clearable
        placeholder="真实姓名/昵称"
        class="w-32"
        @input="onKeywordInput"
        @clear="onClearAndSearch"
      />
      <el-select
        v-model="query.status"
        clearable
        placeholder="状态"
        class="toolbar-status"
        @change="onSearch"
      >
        <el-option :value="0" label="草稿" />
        <el-option :value="1" label="已提交" />
        <el-option :value="2" label="运输中" />
        <el-option :value="3" label="入库中" />
        <el-option :value="4" label="已打款" />
        <el-option :value="5" label="异常" />
        <el-option :value="6" label="已退货" />
      </el-select>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      border
      header-row-class-name="table-header"
      size="small"
    >
      <el-table-column label="ID" width="80" align="center" prop="id" />
      <el-table-column prop="schemeName" label="方案" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.schemeName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="displayName" label="提交人" width="100" show-overflow-tooltip>
        <template #default="{ row }">{{ row.displayName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" align="center" />
      <el-table-column label="应结金额" width="100" align="right">
        <template #default="{ row }">
          <span v-if="row.unitPrice != null && row.quantity != null">¥ {{ ((Number(row.unitPrice) || 0) * (Number(row.quantity) || 0)).toFixed(2) }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="settledAmount" label="结算金额" width="100" align="right">
        <template #default="{ row }">
          <span v-if="row.settledAmount != null">¥ {{ Number(row.settledAmount).toFixed(2) }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="扩展信息" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.dataJson && typeof row.dataJson === 'object'">
            {{ dataJsonSummary(row.dataJson) }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="adminInternalNote" label="管理员备注" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.adminInternalNote">{{ row.adminInternalNote }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="168" align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-permissions="['admin:form-submission:list']"
            size="small"
            type="primary"
            link
            @click="goDetail(row.id)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无固结报单记录"
      class="mt-4"
    />

    <div v-if="pagination.total > 0" class="pager">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFormSubmissions } from '@/api/form-submissions'

const DEBOUNCE_MS = 600

const router = useRouter()
const list = ref([])
const loading = ref(false)
const query = ref({
  schemeNameKeyword: '',
  userKeyword: '',
  status: null
})
let keywordDebounceTimer = null
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0
})

const STATUS_MAP = {
  0: '草稿',
  1: '已提交',
  2: '运输中',
  3: '入库中',
  4: '已打款',
  5: '异常',
  6: '已退货'
}

function statusLabel(code) {
  return STATUS_MAP[code] ?? '-'
}

function statusTagType(code) {
  const types = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'success', 5: 'danger', 6: 'info' }
  return types[code] ?? 'info'
}

function dataJsonSummary(data) {
  if (!data || typeof data !== 'object') return '-'
  const parts = []
  if (data.expressNo) parts.push(`快递: ${data.expressNo}`)
  if (data.remark) parts.push(data.remark)
  return parts.length ? parts.join(' | ') : JSON.stringify(data).slice(0, 60)
}

function formatTime(v) {
  if (!v) return ''
  try {
    const d = typeof v === 'string' ? new Date(v) : v
    if (Number.isNaN(d.getTime())) return String(v)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const mi = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${mi}`
  } catch {
    return String(v)
  }
}

function unwrapPage(res) {
  const data = (res && res.data !== undefined) ? res.data : res
  return data
}

async function load() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      orderBy: 'createdAt',
      order: 'DESC',
      schemeNameKeyword: query.value.schemeNameKeyword?.trim() || undefined,
      userKeyword: query.value.userKeyword?.trim() || undefined,
      status: query.value.status ?? undefined
    }
    const res = await getFormSubmissions(params)
    const data = unwrapPage(res)
    list.value = Array.isArray(data?.records) ? data.records : []
    if (data?.pageMeta) {
      pagination.value.total = Number(data.pageMeta.total) || 0
      pagination.value.page = Number(data.pageMeta.page) || pagination.value.page
      pagination.value.pageSize = Number(data.pageMeta.pageSize) || pagination.value.pageSize
    } else {
      pagination.value.total = list.value.length
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('加载失败：' + (err?.message || '服务器异常'))
    list.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.value.page = 1
  load()
}

// 方案/用户输入防抖：有内容且停止输入 0.6s 后自动查询
function onKeywordInput() {
  if (keywordDebounceTimer) clearTimeout(keywordDebounceTimer)
  keywordDebounceTimer = setTimeout(() => {
    keywordDebounceTimer = null
    onSearch()
  }, DEBOUNCE_MS)
}

// 点击输入框内 x 清除时立即查询（只清当前框，防抖取消）
function onClearAndSearch() {
  if (keywordDebounceTimer) {
    clearTimeout(keywordDebounceTimer)
    keywordDebounceTimer = null
  }
  onSearch()
}

function onPageChange(page) {
  pagination.value.page = page
  load()
}

function onPageSizeChange(size) {
  pagination.value.pageSize = size
  pagination.value.page = 1
  load()
}

function goDetail(id) {
  router.push({ path: '/vab/form-submissions/detail', query: { id: String(id) } })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.form-submissions-page {
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 120px);
}
.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}
.w-32 {
  width: 140px;
}
.toolbar-scheme {
  width: 200px;
}
.toolbar-status {
  width: 100px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.text-muted {
  color: #bbb;
}
.mt-4 {
  margin-top: 16px;
}
</style>
