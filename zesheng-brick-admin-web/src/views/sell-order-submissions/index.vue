<template>
  <div class="sell-order-submissions-page">
    <div class="toolbar">
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
      <el-table-column prop="displayName" label="提交人" width="100" show-overflow-tooltip>
        <template #default="{ row }">{{ row.displayName || '-' }}</template>
      </el-table-column>
      <el-table-column label="商品名称 / 数量" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ itemsSummary(row.itemsJson) }}
        </template>
      </el-table-column>
      <el-table-column label="物流公司 / 单号" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.logisticsCompany && row.logisticsNos?.length ? `${row.logisticsCompany} ${formatExpressNosDisplay({ logisticsNos: row.logisticsNos })}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="storage" label="是否寄存" width="90" align="center">
        <template #default="{ row }">{{ row.storage === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="用户备注" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.remark">{{ row.remark }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回款金额" width="100" align="right">
        <template #default="{ row }">
          {{ row.settledAmount != null ? '¥ ' + formatMoney(row.settledAmount) : '-' }}
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
            v-permissions="['admin:sell-order-submission:list']"
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
      description="暂无行情报单记录"
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
import { getSellOrderSubmissions } from '@/api/sell-order-submissions'
import { formatExpressNosDisplay } from '@/utils/expressNo'
const router = useRouter()

const DEBOUNCE_MS = 600

const list = ref([])
const loading = ref(false)
const query = ref({
  userKeyword: '',
  status: null
})
let keywordDebounceTimer = null
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0
})

const STATUS_MAP = { 0: '草稿', 1: '已提交', 2: '运输中', 3: '入库中', 4: '已打款', 5: '异常', 6: '已退货' }

function statusLabel(code) {
  return STATUS_MAP[code] ?? '-'
}

function statusTagType(code) {
  const types = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'success', 5: 'danger', 6: 'info' }
  return types[code] ?? 'info'
}

function formatMoney(v) {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
}

function itemsSummary(itemsJson) {
  if (!Array.isArray(itemsJson) || !itemsJson.length) return '-'
  return itemsJson.map((i) => `${i.productName || ''} × ${i.quantity || 1}`).filter(Boolean).join('； ') || '-'
}

function goDetail(id) {
  router.push({ path: '/vab/sell-order-submissions/detail', query: { id: String(id) } })
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

async function load() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      orderBy: 'createdAt',
      order: 'DESC',
      userKeyword: query.value.userKeyword?.trim() || undefined,
      status: query.value.status ?? undefined
    }
    const res = await getSellOrderSubmissions(params)
    list.value = Array.isArray(res?.records) ? res.records : []
    if (res?.pageMeta) {
      pagination.value.total = Number(res.pageMeta.total) || 0
      pagination.value.page = Number(res.pageMeta.page) || pagination.value.page
      pagination.value.pageSize = Number(res.pageMeta.pageSize) || pagination.value.pageSize
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

onMounted(() => {
  load()
})
</script>

<style scoped>
.sell-order-submissions-page {
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
