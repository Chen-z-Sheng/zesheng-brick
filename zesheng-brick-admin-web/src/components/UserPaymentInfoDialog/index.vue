<template>
  <el-dialog
    v-model="visible"
    title="用户收款信息"
    width="720px"
    class="user-payment-dialog"
    destroy-on-close
    append-to-body
    @closed="onClosed"
  >
    <div v-loading="loading" class="dialog-body">
      <template v-if="!loading && loaded">
        <div class="amount-card">
          <div class="amount-head">
            <span class="amount-title">{{ amountLabel }}</span>
            <span class="amount-tip">与详情页表单同步，需仍在详情点击保存才会入库</span>
          </div>
          <el-input
            :model-value="amountInputStr"
            type="text"
            inputmode="decimal"
            :placeholder="amountPlaceholder"
            class="amount-input"
            clearable
            @input="onSettledAmountInput"
            @clear="onAmountClear"
          >
            <template #prefix>
              <span class="amount-prefix">¥</span>
            </template>
          </el-input>
        </div>

        <template v-if="hasAnyContent">
          <p class="hint">核对以下信息后向用户打款，收款码可点击放大预览。</p>
          <div class="info-card">
          <div class="field-row">
            <span class="label">真实姓名</span>
            <span class="value">{{ displayText(info.realName) }}</span>
            <el-button
              v-if="info.realName"
              type="primary"
              link
              size="small"
              @click="copyText(info.realName)"
            >
              复制
            </el-button>
          </div>
          <div class="field-row">
            <span class="label">支付宝账号</span>
            <span class="value mono">{{ displayText(info.alipayAccount) }}</span>
            <el-button
              v-if="info.alipayAccount"
              type="primary"
              link
              size="small"
              @click="copyText(info.alipayAccount)"
            >
              复制
            </el-button>
          </div>
        </div>

        <div class="bank-card">
          <div class="bank-title">银行卡</div>
          <div class="field-row">
            <span class="label">卡号</span>
            <span class="value mono">{{ displayText(info.bankCardNo) }}</span>
            <el-button
              v-if="info.bankCardNo"
              type="primary"
              link
              size="small"
              @click="copyText(info.bankCardNo)"
            >
              复制
            </el-button>
          </div>
          <div class="field-row">
            <span class="label">开户银行</span>
            <span class="value">{{ displayText(info.bankName) }}</span>
          </div>
          <div class="field-row">
            <span class="label">开户支行</span>
            <span class="value">{{ displayText(info.bankBranch) }}</span>
          </div>
        </div>

        <div class="qr-section">
          <div class="qr-box">
            <div class="qr-label">微信收款码</div>
            <div v-if="info.wechatQrcode" class="qr-img-wrap">
              <el-image
                :src="info.wechatQrcode"
                fit="contain"
                class="qr-img"
                :preview-src-list="[info.wechatQrcode]"
                preview-teleported
              />
            </div>
            <el-empty v-else description="未上传" :image-size="56" />
          </div>
          <div class="qr-box">
            <div class="qr-label">支付宝收款码</div>
            <div v-if="info.alipayQrcode" class="qr-img-wrap">
              <el-image
                :src="info.alipayQrcode"
                fit="contain"
                class="qr-img"
                :preview-src-list="[info.alipayQrcode]"
                preview-teleported
              />
            </div>
            <el-empty v-else description="未上传" :image-size="56" />
          </div>
        </div>
        </template>

        <el-empty
          v-if="!hasAnyContent"
          class="payment-empty"
          :description="emptyDescription"
        />
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPaymentInfoByUserId } from '@/api/user-payment-info'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userId: {
    type: [Number, String],
    default: null
  },
  /** 详情页结算/回款金额，与表单双向同步（不写库） */
  settledAmount: {
    type: [Number, String],
    default: null
  },
  /** 字段文案：固结报单用「结算金额」，行情报单用「回款金额」 */
  amountLabel: {
    type: String,
    default: '结算金额'
  }
})

const emit = defineEmits(['update:modelValue', 'update:settledAmount'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const loaded = ref(false)
const info = ref(null)
/** 弹窗内金额输入字符串，打开时从 settledAmount 同步 */
const amountInputStr = ref('')

const hasAnyContent = computed(() => {
  const row = info.value
  if (!row) return false
  return Boolean(
    row.realName ||
      row.alipayAccount ||
      row.wechatQrcode ||
      row.alipayQrcode ||
      row.bankCardNo ||
      row.bankName ||
      row.bankBranch
  )
})

const emptyDescription = computed(() =>
  props.userId ? '该用户尚未填写收款信息' : '无法加载：缺少用户 ID'
)

const amountPlaceholder = computed(() => '与详情页一致，可直接修改')

function formatAmountInputFromProp(val) {
  if (val == null || val === '') return ''
  const n = Number(val)
  if (Number.isNaN(n)) return ''
  return Number.isInteger(n) ? String(n) : String(n)
}

function syncAmountFromProp() {
  amountInputStr.value = formatAmountInputFromProp(props.settledAmount)
}

function onSettledAmountInput(val) {
  const filtered = (val || '').replace(/[^\d.]/g, '').replace(/(\..*)\./g, '$1')
  amountInputStr.value = filtered
  if (filtered === '') {
    emit('update:settledAmount', null)
    return
  }
  const num = parseFloat(filtered)
  emit('update:settledAmount', (Number.isNaN(num) || num < 0) ? null : num)
}

function onAmountClear() {
  amountInputStr.value = ''
  emit('update:settledAmount', null)
}

function displayText(v) {
  if (v == null || String(v).trim() === '') return '—'
  return String(v).trim()
}

async function copyText(text) {
  const t = text != null ? String(text) : ''
  if (!t) return
  try {
    await navigator.clipboard.writeText(t)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动选择复制')
  }
}

async function fetchInfo() {
  if (!props.userId) {
    info.value = null
    loaded.value = true
    return
  }
  loading.value = true
  loaded.value = false
  info.value = null
  try {
    const raw = await getUserPaymentInfoByUserId(props.userId)
    info.value = raw || null
  } catch {
    info.value = null
    ElMessage.error('收款信息加载失败')
  } finally {
    loading.value = false
    loaded.value = true
  }
}

function onClosed() {
  info.value = null
  loaded.value = false
  amountInputStr.value = ''
}

watch(
  () => [props.modelValue, props.userId],
  ([open]) => {
    if (open) {
      syncAmountFromProp()
      fetchInfo()
    }
  }
)
</script>

<style scoped>
.user-payment-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}
.dialog-body {
  min-height: 120px;
}
.amount-card {
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 50%, #fff 100%);
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 18px;
}
.amount-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 12px;
  margin-bottom: 10px;
}
.amount-title {
  font-weight: 600;
  font-size: 15px;
  color: #065f46;
}
.amount-tip {
  font-size: 12px;
  color: #6b7280;
}
.amount-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}
.amount-prefix {
  color: #059669;
  font-weight: 600;
  margin-right: 2px;
}
.payment-empty {
  margin-top: 8px;
}
.hint {
  margin: 0 0 16px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.info-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.bank-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}
.bank-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
  color: #303133;
}
.field-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.field-row:last-child {
  margin-bottom: 0;
}
.label {
  flex: 0 0 88px;
  font-size: 13px;
  color: #909399;
}
.value {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}
.value.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
.qr-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.qr-box {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}
.qr-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.qr-img-wrap {
  display: flex;
  justify-content: center;
}
.qr-img {
  width: 180px;
  height: 180px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: zoom-in;
}
@media (max-width: 640px) {
  .qr-section {
    grid-template-columns: 1fr;
  }
}
</style>
