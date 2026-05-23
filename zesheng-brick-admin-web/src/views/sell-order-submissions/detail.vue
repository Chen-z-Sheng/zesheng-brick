<template>
  <div class="sell-order-detail">
    <el-page-header title="返回" @back="goBack" class="mb-4">
      <template #content>
        <span>行情报单详情 · #{{ recordId }}</span>
      </template>
      <template #extra>
        <el-button
          v-if="detail?.userId"
          type="success"
          plain
          @click="paymentDialogVisible = true"
        >
          打款信息
        </el-button>
      </template>
    </el-page-header>

    <UserPaymentInfoDialog
      v-model="paymentDialogVisible"
      :settled-amount="form.settledAmount"
      amount-label="回款金额"
      :user-id="detail?.userId"
      @update:settled-amount="applySettledAmountFromDialog"
    />

    <el-card v-loading="loading" shadow="never">
      <template v-if="detail">
        <div class="detail-grid">
          <section class="panel">
            <div class="section-title">用户提交信息</div>
            <el-form :model="form" label-width="100px" class="edit-form">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="提交人">
                    <el-input :model-value="detail.displayName || '-'" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="寄件人姓名">
                    <el-input v-model="form.senderName" placeholder="寄件人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="寄件人手机号">
                    <el-input v-model="form.senderPhone" placeholder="寄件人手机号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="物流公司">
                    <el-input v-model="form.logisticsCompany" placeholder="物流公司" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="寄件单号">
                    <div class="express-no-editor">
                      <div
                        v-for="(item, index) in logisticsNoList"
                        :key="index"
                        class="express-no-row"
                      >
                        <el-input v-model="item.no" placeholder="寄件单号" />
                        <el-button
                          v-if="logisticsNoList.length > 1"
                          type="danger"
                          link
                          @click="removeLogisticsNo(index)"
                        >
                          删除
                        </el-button>
                      </div>
                      <el-button type="primary" link @click="addLogisticsNo">+ 添加快递单号</el-button>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="是否寄存">
                    <el-select v-model="form.storage" placeholder="请选择" class="w-full">
                      <el-option :value="0" label="否" />
                      <el-option :value="1" label="是" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="寄存日期">
                    <el-date-picker
                      v-model="form.storageDate"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="选择日期时间"
                      class="w-full"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="用户备注">
                    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="用户备注" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="状态">
                    <el-select v-model="form.status" placeholder="请选择" class="w-full">
                      <el-option :value="0" label="草稿" />
                      <el-option :value="1" label="已提交" />
                      <el-option :value="2" label="运输中" />
                      <el-option :value="3" label="入库中" />
                      <el-option :value="4" label="已打款" />
                      <el-option :value="5" label="异常" />
                      <el-option :value="6" label="已退货" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回款金额">
                    <el-input
                      v-model="settledAmountInput"
                      type="text"
                      inputmode="decimal"
                      placeholder="回款金额"
                      class="w-full"
                      @input="onSettledAmountInput"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="管理员备注">
                    <el-input v-model="form.adminInternalNote" type="textarea" :rows="2" placeholder="内部备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <div class="meta">
              <span>创建时间：{{ formatTime(detail.createdAt) }}</span>
              <span>更新时间：{{ formatTime(detail.updatedAt) }}</span>
            </div>
          </section>

          <section class="panel">
            <div class="section-title">商品明细</div>
            <el-table :data="form.items" border size="small" class="items-table">
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column label="商品名称" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.productName" placeholder="商品名称" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="回收单价" width="110" align="right">
                <template #default="{ row }">
                  <span class="readonly-price">¥ {{ formatMoney(row.price) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="90">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :precision="0" size="small" class="w-full" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center" fixed="right">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="total-row">
              <span class="total-label">总回款：</span>
              <span class="total-value">¥ {{ formatMoney(totalAmount) }}</span>
            </div>
            <el-button type="primary" plain size="small" class="mt-2" @click="addItem">添加一行</el-button>
          </section>

          <section v-if="detail" class="panel full-width">
            <div class="section-title">物流轨迹</div>
            <div v-if="logisticsLoading" class="logistics-muted">正在查询物流…</div>
            <template v-else-if="logisticsTraceList.length">
              <div v-for="(trace, traceIdx) in logisticsTraceList" :key="traceIdx" class="logistics-multi-block">
                <div class="logistics-multi-title">{{ trace.trackingNo || `快递单号 ${traceIdx + 1}` }}</div>
                <div v-if="trace.success" class="logistics-head">
                  <el-tag type="primary" size="small">{{ trace.stateText }}</el-tag>
                  <span v-if="trace.lastTime" class="logistics-time">{{ trace.lastTime }}</span>
                </div>
                <p v-if="trace.success" class="logistics-latest">{{ trace.lastInfo || '—' }}</p>
                <el-alert v-else type="warning" :closable="false" show-icon :title="trace.errorMessage || '暂无物流'" />
                <el-timeline v-if="trace.success && trace.traces?.length" class="logistics-timeline">
                  <el-timeline-item
                    v-for="(row, idx) in trace.traces"
                    :key="idx"
                    :timestamp="row.time"
                    placement="top"
                  >
                    {{ row.context }}
                  </el-timeline-item>
                </el-timeline>
              </div>
            </template>
          </section>

          <section class="panel full-width">
            <div class="section-title">回款凭证</div>
            <div class="settled-proofs">
              <div v-for="(url, idx) in (detail.settledProofUrls || [])" :key="idx" class="proof-item">
                <el-image :src="url" fit="contain" class="proof-img" :preview-src-list="detail.settledProofUrls" />
                <el-button type="danger" link size="small" class="remove-proof" @click="removeProof(idx)">删除</el-button>
              </div>
              <div class="upload-wrap">
                <el-upload
                  :http-request="uploadSettledProof"
                  :show-file-list="false"
                  accept="image/*"
                  multiple
                >
                  <el-button type="primary" plain size="small">上传图片（可多张）</el-button>
                </el-upload>
              </div>
            </div>
          </section>
        </div>

        <div class="actions-footer">
          <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="记录不存在" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getSellOrderSubmission,
  getSellOrderSubmissionLogisticsTrace,
  updateSellOrderSubmission,
  uploadSettledProof as apiUploadSettledProof,
  removeSettledProof
} from '@/api/sell-order-submissions'
import { getPricesByDate } from '@/api/recycle-market'
import UserPaymentInfoDialog from '@/components/UserPaymentInfoDialog/index.vue'
import {
  createEmptyExpressItem,
  normalizeExpressNoList,
  collectExpressNos,
} from '@/utils/expressNo'

const route = useRoute()
const router = useRouter()
const id = ref(route.query.id)
const detail = ref(null)
const loading = ref(false)
const saving = ref(false)
const logisticsTraceList = ref([])
const logisticsNoList = ref([createEmptyExpressItem()])
const logisticsLoading = ref(false)
const paymentDialogVisible = ref(false)
const pricesByDate = ref([])
const form = ref({
  senderName: '',
  senderPhone: '',
  logisticsCompany: '',
  storage: 0,
  storageDate: null,
  remark: '',
  status: 1,
  settledAmount: null,
  adminInternalNote: '',
  items: []
})

const recordId = computed(() => detail.value?.id ?? id.value ?? '')

const settledAmountInput = ref('')

function formatSettledAmountForDisplay(val) {
  if (val == null || val === '') return ''
  const n = Number(val)
  if (Number.isNaN(n)) return ''
  return Number.isInteger(n) ? String(n) : String(n)
}

function applySettledAmountFromDialog(v) {
  form.value.settledAmount = v
  settledAmountInput.value = formatSettledAmountForDisplay(v)
}

function onSettledAmountInput(val) {
  const filtered = (val || '').replace(/[^\d.]/g, '').replace(/(\..*)\./g, '$1')
  settledAmountInput.value = filtered
  if (filtered === '') {
    form.value.settledAmount = null
    return
  }
  const num = parseFloat(filtered)
  form.value.settledAmount = (Number.isNaN(num) || num < 0) ? null : num
}

const totalAmount = computed(() => {
  const items = form.value.items || []
  return items.reduce((sum, it) => sum + (Number(it.price) || 0) * (Number(it.quantity) || 0), 0)
})

function yesterdayStr() {
  const d = new Date()
  d.setDate(d.getDate() - 1)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function matchRecyclePrice(productName) {
  if (!productName || !pricesByDate.value.length) return null
  const name = String(productName).trim()
  const vo = pricesByDate.value.find(
    (p) => p.level2Name && (p.level2Name === name || p.level2Name.includes(name) || name.includes(p.level2Name))
  )
  return vo ? Number(vo.recyclePrice) : null
}

function addLogisticsNo() {
  logisticsNoList.value.push(createEmptyExpressItem())
}

function removeLogisticsNo(index) {
  if (logisticsNoList.value.length <= 1) {
    logisticsNoList.value = [createEmptyExpressItem()]
    return
  }
  logisticsNoList.value.splice(index, 1)
}

function normalizeTraceList(raw) {
  if (Array.isArray(raw)) return raw
  if (raw && typeof raw === 'object') return [raw]
  return []
}

function syncFormFromDetail() {
  if (!detail.value) return
  const d = detail.value
  const rawItems = d.itemsJson || []
  // 寄存日期：后端为 DATE，用 datetime 选择器时需补全为 YYYY-MM-DD HH:mm:ss 供组件显示，保存时再只传日期部分
  const storageDateStr = d.storageDate ? String(d.storageDate).trim() : ''
  const storageDateForPicker = storageDateStr.length >= 10 ? (storageDateStr.slice(0, 10) + ' 00:00:00') : null
  logisticsNoList.value = normalizeExpressNoList({ logisticsNos: d.logisticsNos })
  form.value = {
    senderName: d.senderName ?? '',
    senderPhone: d.senderPhone ?? '',
    logisticsCompany: d.logisticsCompany ?? '',
    storage: d.storage !== undefined && d.storage !== null ? d.storage : 0,
    storageDate: storageDateForPicker,
    remark: d.remark ?? '',
    status: d.status ?? 1,
    settledAmount: d.settledAmount != null ? Number(d.settledAmount) : null,
    adminInternalNote: d.adminInternalNote ?? '',
    items: rawItems.map((it) => {
      const price = it.price != null ? Number(it.price) : 0
      const quantity = it.quantity != null ? Number(it.quantity) : 1
      const productName = it.productName ?? ''
      const matched = matchRecyclePrice(productName)
      return {
        productName,
        price: matched != null ? matched : price,
        quantity
      }
    })
  }
  if (form.value.items.length === 0) {
    form.value.items.push({ productName: '', price: 0, quantity: 1 })
  }
  settledAmountInput.value = formatSettledAmountForDisplay(form.value.settledAmount)
}

function fillRecyclePrices() {
  form.value.items.forEach((row) => {
    const matched = matchRecyclePrice(row.productName)
    if (matched != null) row.price = matched
  })
}

function addItem() {
  form.value.items.push({ productName: '', price: 0, quantity: 1 })
}

function removeItem(index) {
  form.value.items.splice(index, 1)
}

function formatMoney(v) {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
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

async function loadLogistics() {
  if (!id.value) return
  logisticsLoading.value = true
  logisticsTraceList.value = []
  try {
    const res = await getSellOrderSubmissionLogisticsTrace(id.value)
    logisticsTraceList.value = normalizeTraceList(res)
  } catch {
    logisticsTraceList.value = []
  } finally {
    logisticsLoading.value = false
  }
}

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const yesterday = yesterdayStr()
    const [data, prices] = await Promise.all([
      getSellOrderSubmission(id.value),
      getPricesByDate(yesterday).catch(() => [])
    ])
    detail.value = data
    pricesByDate.value = Array.isArray(prices) ? prices : []
    syncFormFromDetail()
    fillRecyclePrices()
    await loadLogistics()
  } catch (err) {
    console.error(err)
    ElMessage.error('加载失败：' + (err?.message || '服务器异常'))
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!id.value) return
  const validItems = form.value.items.filter((it) => it.productName && String(it.productName).trim())
  if (validItems.length === 0) {
    ElMessage.warning('请至少保留一行有效商品')
    return
  }
  saving.value = true
  try {
    const payload = {
      senderName: form.value.senderName,
      senderPhone: form.value.senderPhone,
      logisticsCompany: form.value.logisticsCompany,
      logisticsNos: collectExpressNos(logisticsNoList.value),
      storage: form.value.storage,
      storageDate: form.value.storageDate ? String(form.value.storageDate).slice(0, 10) : null,
      remark: form.value.remark,
      status: form.value.status,
      settledAmount: form.value.settledAmount != null ? Number(form.value.settledAmount) : null,
      adminInternalNote: form.value.adminInternalNote,
      items: validItems.map((it) => ({
        productName: String(it.productName).trim(),
        price: Number(it.price) || 0,
        quantity: Math.max(1, Number(it.quantity) || 1)
      }))
    }
    await updateSellOrderSubmission(id.value, payload)
    ElMessage.success('保存成功')
    goBack()
  } catch (err) {
    ElMessage.error('保存失败：' + (err?.message || '服务器异常'))
  } finally {
    saving.value = false
  }
}

async function uploadSettledProof({ file }) {
  if (!id.value) return
  try {
    const res = await apiUploadSettledProof(id.value, file)
    if (res && res.settledProofUrls) {
      detail.value.settledProofUrls = res.settledProofUrls
      ElMessage.success('上传成功')
    }
  } catch (err) {
    ElMessage.error('上传失败：' + (err?.message || '服务器异常'))
  }
}

async function removeProof(index) {
  if (!id.value) return
  try {
    const res = await removeSettledProof(id.value, { index })
    const data = (res && res.data !== undefined) ? res.data : res
    if (data && data.settledProofUrls) detail.value.settledProofUrls = data.settledProofUrls
    ElMessage.success('已删除')
  } catch (err) {
    ElMessage.error('删除失败：' + (err?.message || '服务器异常'))
  }
}

function goBack() {
  router.push({ path: '/vab/sell-order-submissions' })
}

watch(() => route.query.id, (newId) => {
  id.value = newId
  if (newId) load()
}, { immediate: false })
onMounted(() => {
  id.value = route.query.id
  if (id.value) load()
})
</script>

<style scoped>
.sell-order-detail {
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 120px);
}
.mb-4 {
  margin-bottom: 16px;
}
.mt-2 {
  margin-top: 8px;
}
.w-full {
  width: 100%;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.panel {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}
.panel.full-width {
  grid-column: 1 / -1;
}
.section-title {
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 15px;
}
.edit-form {
  margin-bottom: 12px;
}
.meta {
  font-size: 12px;
  color: #666;
}
.meta span + span {
  margin-left: 16px;
}
.items-table {
  margin-bottom: 12px;
}
.readonly-price {
  color: #333;
}
.total-row {
  margin-bottom: 12px;
  text-align: right;
  font-size: 15px;
}
.total-label {
  color: #666;
}
.total-value {
  font-weight: 600;
  color: #303133;
}
.actions-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
.settled-proofs {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px;
}
.proof-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.proof-img {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  border: 1px solid #eee;
}
.upload-wrap {
  display: flex;
  align-items: center;
}
.remove-proof {
  margin-top: 4px;
}
.logistics-muted {
  color: #909399;
  font-size: 13px;
}
.logistics-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.logistics-time {
  font-size: 13px;
  color: #909399;
}
.logistics-latest {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
}
.logistics-timeline {
  margin-top: 8px;
  padding-left: 4px;
}
.express-no-editor {
  width: 100%;
}
.express-no-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.express-no-row .el-input {
  flex: 1;
}
.logistics-multi-block + .logistics-multi-block {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
.logistics-multi-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 600;
}
@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
