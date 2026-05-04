<template>
  <div class="address-page">
    <div class="toolbar">
      <el-button
        v-permissions="['admin:delivery-address:add']"
        type="primary"
        @click="openDialog()"
      >
        新增地址
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      border
      header-row-class-name="table-header"
      size="small"
    >
      <el-table-column label="ID" width="80" align="center" prop="id" />
      <el-table-column prop="name" label="地址名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="fullAddress" label="完整地址" min-width="280" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-permissions="['admin:delivery-address:update']"
            size="small"
            type="primary"
            link
            @click="openDialog(row)"
          >
            编辑
          </el-button>
          <el-popconfirm
            v-permissions="['admin:delivery-address:delete']"
            title="确认删除该地址？方案中已选此地址的将变为未选。"
            @confirm="doRemove(row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无下单地址"
      class="mt-4"
    >
      <template #extra>
        <el-button
          v-permissions="['admin:delivery-address:add']"
          type="primary"
          @click="openDialog()"
        >
          新增地址
        </el-button>
      </template>
    </el-empty>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑地址' : '新增地址'"
      width="480px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="地址名称" prop="name">
          <el-input v-model="form.name" placeholder="如：华东仓" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="完整地址" prop="fullAddress">
          <el-input
            v-model="form.fullAddress"
            type="textarea"
            :rows="3"
            placeholder="省市区+详细地址"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  getDeliveryAddressList,
  createDeliveryAddress,
  updateDeliveryAddress,
  deleteDeliveryAddress,
} from "@/api/delivery-addresses";

const loading = ref(false);
const list = ref([]);
const dialogVisible = ref(false);
const editId = ref(null);
const saving = ref(false);
const formRef = ref();

const form = ref({
  name: "",
  fullAddress: "",
  sortOrder: 0,
  status: 1,
});

const rules = {
  name: [{ required: true, message: "请输入地址名称", trigger: "blur" }],
  fullAddress: [{ required: true, message: "请输入完整地址", trigger: "blur" }],
};

function formatTime(val) {
  if (!val) return "-";
  const d = new Date(val);
  return d.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function load() {
  loading.value = true;
  try {
    const data = await getDeliveryAddressList();
    list.value = Array.isArray(data) ? data : [];
  } catch (e) {
    console.error(e);
    ElMessage.error("加载列表失败");
    list.value = [];
  } finally {
    loading.value = false;
  }
}

function openDialog(row) {
  editId.value = row ? row.id : null;
  if (row) {
    form.value = {
      name: row.name ?? "",
      fullAddress: row.fullAddress ?? "",
      sortOrder: row.sortOrder ?? 0,
      status: row.status ?? 1,
    };
  } else {
    form.value = {
      name: "",
      fullAddress: "",
      sortOrder: 0,
      status: 1,
    };
  }
  dialogVisible.value = true;
}

function resetForm() {
  form.value = { name: "", fullAddress: "", sortOrder: 0, status: 1 };
  editId.value = null;
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    saving.value = true;
    try {
      if (editId.value) {
        await updateDeliveryAddress(editId.value, form.value);
        ElMessage.success("修改成功");
      } else {
        await createDeliveryAddress(form.value);
        ElMessage.success("新增成功");
      }
      dialogVisible.value = false;
      load();
    } catch (e) {
      console.error(e);
      ElMessage.error(e?.message || "操作失败");
    } finally {
      saving.value = false;
    }
  });
}

async function doRemove(id) {
  try {
    await deleteDeliveryAddress(id);
    ElMessage.success("删除成功");
    load();
  } catch (e) {
    console.error(e);
    ElMessage.error(e?.message || "删除失败");
  }
}

onMounted(() => {
  load();
});
</script>

<style scoped lang="scss">
.address-page {
  padding: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.mt-4 {
  margin-top: 24px;
}
</style>
