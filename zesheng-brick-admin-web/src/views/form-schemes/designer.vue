<template>
  <div class="designer-page">
    <div class="topbar">
      <el-page-header @back="goBack">
        <template #content>
          <span>{{ isNew ? "新建方案" : "编辑方案" }}</span>
        </template>
      </el-page-header>

      <div class="grow" />

      <el-button
        :loading="saving"
        type="primary"
        @click="onSubmit"
      > 保存 </el-button>
      <el-button @click="goBack">返回</el-button>
    </div>

    <div class="form-wrap">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="96px"
        class="scheme-form"
      >
        <div class="form-row form-row-main">
          <el-form-item
            label="方案名称"
            prop="name"
            class="form-item-name"
          >
            <el-input
              v-model="form.name"
              placeholder="例如：11.11京东珀莱雅222本845回900"
              maxlength="80"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="结算金额"
            prop="unitPrice"
            class="form-item-amount"
          >
            <div class="amount-input-wrap">
              <span class="amount-prefix">¥</span>
              <el-input
                v-model="unitPriceInput"
                type="text"
                inputmode="decimal"
                class="amount-input"
                @input="onUnitPriceInput"
              />
              <span class="amount-suffix">元/单</span>
            </div>
          </el-form-item>
        </div>

        <el-form-item label="下单地址" prop="addressId">
          <el-select
            v-model="form.addressId"
            placeholder="请选择下单地址（报单时只读展示）"
            clearable
            filterable
            class="w-72"
          >
            <el-option
              v-for="addr in addressOptions"
              :key="addr.id"
              :label="addr.name + ' - ' + (addr.fullAddress || '')"
              :value="addr.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="方案说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="可选，给自己/团队看的备注说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          label="状态"
          prop="status"
        >
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">草稿</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onActivated, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getScheme, createScheme, updateScheme } from "@/api/form-schemes";
import { getDeliveryAddressList } from "@/api/delivery-addresses";

const route = useRoute();
const router = useRouter();

const id = ref(route.query.id ? String(route.query.id) : null);
const isNew = computed(() => route.query.mode === "new" || !id.value);

const formRef = ref();
const saving = ref(false);

const form = ref({
  name: "",
  addressId: null,
  unitPrice: null,
  description: "",
  status: 1, // 默认启用
});

const addressOptions = ref([]);

const unitPriceInput = ref("");

function formatUnitPriceForDisplay (val) {
  if (val == null || val === "") return "";
  const n = Number(val);
  if (Number.isNaN(n)) return "";
  return Number.isInteger(n) ? String(n) : String(n);
}

function onUnitPriceInput (val) {
  const filtered = (val || "").replace(/[^\d.]/g, "").replace(/(\..*)\./g, "$1");
  unitPriceInput.value = filtered;
  if (filtered === "") {
    form.value.unitPrice = null;
    return;
  }
  const num = parseFloat(filtered);
  form.value.unitPrice = Number.isNaN(num) || num < 0 ? null : num;
}

function resetForm () {
  form.value = {
    name: "",
    addressId: null,
    unitPrice: null,
    description: "",
    status: 1,
  };
  unitPriceInput.value = "";
}

async function loadAddressOptions() {
  try {
    const list = await getDeliveryAddressList({ status: 1 });
    addressOptions.value = Array.isArray(list) ? list : [];
  } catch (e) {
    console.error(e);
  }
}

const rules = {
  name: [{ required: true, message: "请输入方案名称", trigger: "blur" }],
  unitPrice: [
    { required: true, message: "请输入结算金额", trigger: "blur" },
    { type: "number", min: 0, message: "结算金额不能为负数", trigger: "blur" },
  ],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

// 加载方案详情（编辑模式）
async function loadDetail () {
  if (isNew.value || !id.value) return;
  try {
    const detail = await getScheme(id.value);
    form.value = {
      name: detail.name ?? "",
      addressId: detail.addressId ?? null,
      unitPrice: detail.unitPrice != null ? Number(detail.unitPrice) : null,
      description: detail.description ?? "",
      status: typeof detail.status === "number" ? detail.status : 1,
    };
    unitPriceInput.value = formatUnitPriceForDisplay(form.value.unitPrice);
  } catch (e) {
    console.error(e);
    ElMessage.error("加载方案失败：" + (e.message || "接口异常"));
  }
}

loadAddressOptions();

// keep-alive 场景：再次进入编辑页时重新拉取详情，避免回显旧数据
onActivated(async () => {
  if (isNew.value) {
    resetForm();
    return;
  }
  await loadDetail();
});

// 同一路由不同 query（切换不同方案/新建）时同步刷新
watch(
  () => route.query.id,
  async (newId) => {
    id.value = newId ? String(newId) : null;
    if (isNew.value) {
      resetForm();
      return;
    }
    await loadDetail();
  },
  { immediate: true }
);

function onSubmit () {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;

    saving.value = true;
    try {
      const payload = {
        name: form.value.name,
        addressId: form.value.addressId || null,
        unitPrice: form.value.unitPrice != null && form.value.unitPrice !== "" ? Number(form.value.unitPrice) : null,
        description: form.value.description || null,
        status: form.value.status,
      };

      if (isNew.value) {
        await createScheme(payload);
        ElMessage.success("方案创建成功");
        goBack();
      } else if (id.value) {
        await updateScheme(id.value, payload);
        ElMessage.success("方案更新成功");
        goBack();
      }
    } catch (e) {
      console.error(e);
      ElMessage.error("保存失败：" + (e.message || "接口异常"));
    } finally {
      saving.value = false;
    }
  });
}

function goBack () {
  router.back();
}
</script>

<style scoped lang="scss">
.designer-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
}
.topbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.grow {
  flex: 1;
}
.w-72 {
  width: 360px;
}
.form-wrap {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px 28px;
  box-sizing: border-box;
}
.form-row {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.form-row-main {
  margin-bottom: 18px;
}
.form-row-main .form-item-name {
  flex: 1;
  min-width: 0;
  max-width: 420px;
}
.form-row-main .form-item-amount {
  flex-shrink: 0;
  width: 200px;
}
.amount-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 32px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}
.amount-input-wrap .amount-prefix {
  font-size: 14px;
  font-weight: 500;
  color: #e6a23c;
}
.amount-input-wrap .amount-suffix {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  flex-shrink: 0;
}
.amount-input-wrap :deep(.el-input),
.amount-input-wrap :deep(.el-input-number) {
  width: 100px;
}
.amount-input-wrap :deep(.el-input__wrapper) {
  box-shadow: none;
  background: transparent;
  padding: 0 4px;
}
</style>
