<template>
  <div class="form-submission-detail">
    <el-page-header title="返回" @back="goBack" class="mb-4">
      <template #content>
        <span>固结报单详情 · #{{ recordId }}</span>
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
      amount-label="结算金额"
      :user-id="detail?.userId"
      @update:settled-amount="applySettledAmountFromDialog"
    />

    <el-card v-loading="loading" shadow="never">
      <template v-if="detail">
        <div class="detail-grid">
          <section class="panel">
            <div class="section-title">提交信息</div>
            <el-form :model="form" label-width="100px" class="edit-form">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="方案">
                    <el-input :model-value="detail.schemeName ?? '-'" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="提交人">
                    <el-input :model-value="detail.displayName ?? '-'" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="数量">
                    <el-input-number
                      v-model="form.quantity"
                      :min="1"
                      class="w-full"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="应结金额">
                    <el-input :model-value="expectedSettledAmountText" disabled class="w-full" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="结算金额">
                    <el-input
                      v-model="settledAmountInput"
                      type="text"
                      inputmode="decimal"
                      class="w-full"
                      @input="onSettledAmountInput"
                    />
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
                  <el-form-item label="结算时间">
                    <el-date-picker
                      v-model="form.settledAt"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="选择时间"
                      class="w-full"
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
            <div class="section-title">扩展数据</div>
            <el-form :model="form.dataJson" label-width="100px" class="edit-form">
              <el-form-item label="加赠">
                <el-input v-model="form.dataJson.giftDesc" placeholder="加赠" clearable />
              </el-form-item>
              <el-form-item label="快递单号">
                <div class="express-no-editor">
                  <div
                    v-for="(item, index) in expressNoList"
                    :key="index"
                    class="express-no-row"
                  >
                    <el-input v-model="item.no" placeholder="快递单号" clearable />
                    <el-button
                      v-if="expressNoList.length > 1"
                      type="danger"
                      link
                      @click="removeExpressNo(index)"
                    >
                      删除
                    </el-button>
                  </div>
                  <el-button type="primary" link @click="addExpressNo">+ 添加快递单号</el-button>
                </div>
              </el-form-item>
              <el-form-item label="主品单号">
                <el-input v-model="form.dataJson.orderNoMain" placeholder="主品单号" clearable />
              </el-form-item>
              <el-form-item label="加赠单号">
                <el-input v-model="form.dataJson.orderNoGift" placeholder="加赠单号" clearable />
              </el-form-item>
              <el-form-item label="签收日期">
                <el-date-picker
                  v-model="form.dataJson.signDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择签收日期"
                  clearable
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="用户备注">
                <el-input v-model="form.dataJson.remark" type="textarea" :rows="2" placeholder="用户备注" clearable />
              </el-form-item>
              <el-form-item v-if="imageUrls.length" label="凭证图片">
                <div class="data-json-images">
                  <el-image
                    v-for="(u, i) in imageUrls"
                    :key="i"
                    :src="u"
                    fit="contain"
                    class="data-json-img"
                    :preview-src-list="imageUrls"
                  />
                </div>
              </el-form-item>
            </el-form>
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
  getFormSubmission,
  getFormSubmissionLogisticsTrace,
  updateFormSubmission,
  uploadSettledProof as apiUploadSettledProof,
  removeSettledProof as apiRemoveSettledProof
} from '@/api/form-submissions'
import UserPaymentInfoDialog from '@/components/UserPaymentInfoDialog/index.vue'
import {
  createEmptyExpressItem,
  normalizeExpressNoList,
  collectExpressNos,
} from '@/utils/expressNo'

// 后端 R 包装：取 res.data 为实体
function unwrap(res) {
  return (res && res.data !== undefined) ? res.data : res
}

const route = useRoute()
const router = useRouter()
const id = ref(route.query.id)
const detail = ref(null)
const loading = ref(false)
const saving = ref(false)
const logisticsTraceList = ref([])
const expressNoList = ref([createEmptyExpressItem()])
const logisticsLoading = ref(false)
const paymentDialogVisible = ref(false)
const form = ref({
  quantity: 1,
  settledAmount: null,
  status: 1,
  settledAt: null,
  adminInternalNote: '',
  dataJson: {
    giftDesc: '',
    orderNoMain: '',
    orderNoGift: '',
    signDate: '',
    remark: ''
  }
})

const recordId = computed(() => detail.value?.id ?? id.value ?? '')

// 应结金额 = 每单结算金额 × 数量（只读展示）
const expectedSettledAmountText = computed(() => {
  const unitPrice = detail.value?.unitPrice != null ? Number(detail.value.unitPrice) : null
  const qty = form.value.quantity != null ? Number(form.value.quantity) : 0
  if (unitPrice == null) return '-'
  const val = unitPrice * (qty || 0)
  return '¥ ' + val.toFixed(2)
})

const settledAmountInput = ref('')

function formatSettledAmountForDisplay(val) {
  if (val == null || val === '') return ''
  const n = Number(val)
  if (Number.isNaN(n)) return ''
  return Number.isInteger(n) ? String(n) : String(n)
}

/** 打款弹窗改金额时写回详情表单（不落库） */
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


const imageUrls = computed(() => {
  const raw = form.value.dataJson?.imageUrls
  if (Array.isArray(raw)) return raw.filter(Boolean)
  return []
})

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

function addExpressNo() {
  expressNoList.value.push(createEmptyExpressItem())
}

function removeExpressNo(index) {
  if (expressNoList.value.length <= 1) {
    expressNoList.value = [createEmptyExpressItem()]
    return
  }
  expressNoList.value.splice(index, 1)
}

function normalizeTraceList(raw) {
  if (Array.isArray(raw)) return raw
  if (raw && typeof raw === 'object') return [raw]
  return []
}

function syncFormFromDetail() {
  if (!detail.value) return
  const d = detail.value
  let settledAtVal = null
  if (d.settledAt) {
    try {
      const date = typeof d.settledAt === 'string' ? new Date(d.settledAt) : d.settledAt
      if (!Number.isNaN(date.getTime())) {
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const h = String(date.getHours()).padStart(2, '0')
        const mi = String(date.getMinutes()).padStart(2, '0')
        const s = String(date.getSeconds()).padStart(2, '0')
        settledAtVal = `${y}-${m}-${day} ${h}:${mi}:${s}`
      }
    } catch (_) {}
  }
  const dataJson = d.dataJson && typeof d.dataJson === 'object' ? { ...d.dataJson } : {}
  expressNoList.value = normalizeExpressNoList({ expressNos: dataJson.expressNos })
  form.value = {
    quantity: d.quantity != null ? d.quantity : 1,
    settledAmount: d.settledAmount != null ? Number(d.settledAmount) : null,
    status: d.status ?? 1,
    settledAt: settledAtVal,
    adminInternalNote: d.adminInternalNote ?? '',
    dataJson: {
      giftDesc: dataJson.giftDesc ?? '',
      orderNoMain: dataJson.orderNoMain ?? '',
      orderNoGift: dataJson.orderNoGift ?? '',
      signDate: dataJson.signDate ?? '',
      remark: dataJson.remark ?? '',
      imageUrls: Array.isArray(dataJson.imageUrls) ? [...dataJson.imageUrls] : []
    }
  }
  settledAmountInput.value = formatSettledAmountForDisplay(form.value.settledAmount)
}

async function loadLogistics() {
  if (!id.value) return
  logisticsLoading.value = true
  logisticsTraceList.value = []
  try {
    const res = await getFormSubmissionLogisticsTrace(id.value)
    logisticsTraceList.value = normalizeTraceList(unwrap(res))
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
    const res = await getFormSubmission(id.value)
    detail.value = unwrap(res)
    syncFormFromDetail()
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
  saving.value = true
  try {
    const expressNos = collectExpressNos(expressNoList.value)
    const payload = {
      quantity: form.value.quantity,
      settledAmount: form.value.settledAmount != null ? form.value.settledAmount : null,
      status: form.value.status,
      settledAt: form.value.settledAt || null,
      adminInternalNote: form.value.adminInternalNote,
    dataJson: {
      giftDesc: form.value.dataJson.giftDesc ?? '',
      expressNos,
      orderNoMain: form.value.dataJson.orderNoMain ?? '',
        orderNoGift: form.value.dataJson.orderNoGift ?? '',
        signDate: form.value.dataJson.signDate ?? '',
        remark: form.value.dataJson.remark ?? '',
        imageUrls: Array.isArray(form.value.dataJson.imageUrls) ? form.value.dataJson.imageUrls : []
      }
    }
    const res = await updateFormSubmission(id.value, payload)
    const entity = unwrap(res)
    if (entity) detail.value = entity
    syncFormFromDetail()
    ElMessage.success('保存成功')
    router.push({ path: '/vab/form-submissions' })
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
    const entity = unwrap(res)
    if (entity && entity.settledProofUrls) {
      detail.value.settledProofUrls = entity.settledProofUrls
    }
    ElMessage.success('上传成功')
  } catch (err) {
    ElMessage.error('上传失败：' + (err?.message || '服务器异常'))
  }
}

async function removeProof(index) {
  if (!id.value) return
  try {
    const res = await apiRemoveSettledProof(id.value, { index })
    const entity = unwrap(res)
    if (entity && entity.settledProofUrls) {
      detail.value.settledProofUrls = entity.settledProofUrls
    }
    ElMessage.success('已删除')
  } catch (err) {
    ElMessage.error('删除失败：' + (err?.message || '服务器异常'))
  }
}

function goBack() {
  router.push({ path: '/vab/form-submissions' })
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
.form-submission-detail {
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 120px);
}
.mb-4 {
  margin-bottom: 16px;
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
.data-json-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.data-json-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
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
