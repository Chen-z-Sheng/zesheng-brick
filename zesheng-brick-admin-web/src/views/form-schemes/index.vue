<template>
  <div class="scheme-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="q"
        clearable
        placeholder="搜索方案名称"
        class="w-64"
        @keyup.enter="onSearch"
        @clear="onClearAndSearch"
        @input="debounceSearch"
      />
      <el-select
        v-model="status"
        clearable
        placeholder="状态"
        class="w-40"
        @change="onSearch"
      >
        <el-option
          :value="1"
          label="启用"
        />
        <el-option
          :value="2"
          label="草稿"
        />
        <el-option
          :value="0"
          label="停用"
        />
      </el-select>

      <div class="grow"></div>

      <el-button
        v-permissions="['admin:form-scheme:delete']"
        type="danger"
        @click="doBatchRemove"
        :disabled="selectedIds.length === 0"
      >批量删除</el-button>
      <el-button
        v-permissions="['admin:form-scheme:add']"
        type="primary"
        @click="goCreate"
      >新建方案</el-button>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="list"
      border
      header-row-class-name="table-header"
      size="small"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="ID"
        width="120"
        align="center"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          {{ row.id ?? '无ID' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="name"
        label="方案名称"
        min-width="220"
        show-overflow-tooltip
      />
      <el-table-column
        prop="description"
        label="方案说明"
        min-width="240"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <span v-if="row.description">{{ row.description }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="unitPrice"
        label="结算金额"
        width="120"
        align="right"
      >
        <template #default="{ row }">
          <span v-if="row.unitPrice != null">¥ {{ Number(row.unitPrice).toFixed(2) }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="status"
        label="状态"
        width="80"
        align="center"
      >
        <template #default="{ row }">
          <el-select
            :model-value="row.status != null ? Number(row.status) : undefined"
            size="small"
            :class="['status-select', 'status-' + (row.status ?? '')]"
            placeholder="请选择"
            @change="(val) => handleStatusChange(row, val)"
          >
            <el-option :value="1" label="启用" />
            <el-option :value="2" label="草稿" />
            <el-option :value="0" label="停用" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column
        prop="createdAt"
        label="创建时间"
        width="180"
        align="center"
      >
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column
        prop="updatedAt"
        label="修改时间"
        width="180"
        align="center"
      >
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>

      <el-table-column
        label="操作"
        width="200"
        fixed="right"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-permissions="['admin:form-scheme:update']"
            size="small"
            type="primary"
            link
            @click="goEdit(row.id)"
          >
            编辑
          </el-button>
          <el-popconfirm
            v-permissions="['admin:form-scheme:delete']"
            title="确认删除此方案？"
            @confirm="doRemove(row.id)"
          >
            <template #reference>
              <el-button
                size="small"
                type="danger"
                link
              >删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无方案"
      class="mt-4"
    >
      <template #extra>
        <el-button
          v-permissions="['admin:form-scheme:add']"
          type="primary"
          @click="goCreate"
        >去新建</el-button>
      </template>
    </el-empty>

    <!-- 分页 -->
    <div
      class="pager"
      v-if="pagination.total > 0"
    >
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
import { ref, onMounted, onActivated, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getSchemes, deleteSingleScheme, batchDeleteScheme, updateScheme } from "@/api/form-schemes";

const router = useRouter();

const q = ref("");
const status = ref(null); // 0/1/2，可选
const list = ref([]);
const loading = ref(false);
const selectedIds = ref([]);
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
});

// 定时器标识
const searchTimer = ref(null);

async function load () {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      name: q.value || undefined,
      status: status.value ?? undefined,
    };
    const resp = await getSchemes(params);
    const data = unwrapPage(resp);
    list.value = Array.isArray(data?.records) ? data.records : [];

    if (data?.pageMeta) {
      pagination.value = {
        page: Number(data.pageMeta.page) || pagination.value.page,
        pageSize: Number(data.pageMeta.pageSize) || pagination.value.pageSize,
        total: Number(data.pageMeta.total) || 0,
      };
    } else {
      // 后端不返回 pageMeta 时，至少把 total 置为当前列表长度
      pagination.value.total = list.value.length;
    }
  } catch (err) {
    console.error(err);
    ElMessage.error("加载方案失败：" + (err?.message || "服务器异常"));
    list.value = [];
    pagination.value.total = 0;
  } finally {
    loading.value = false;
  }
}

function unwrapPage(res) {
  if (res && res.data !== undefined) {
    return res.data;
  }
  return res;
}

onMounted(() => {
  load();
});

onActivated(() => {
  load();
});

function onSearch () {
  pagination.value.page = 1;
  load();
}

// 防抖搜索（0.6s）
function debounceSearch () {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value);
  }
  searchTimer.value = setTimeout(() => {
    searchTimer.value = null;
    onSearch();
  }, 600);
}

// 点击输入框内 x 清除时立即查询
function onClearAndSearch () {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value);
    searchTimer.value = null;
  }
  onSearch();
}

function onPageChange (page) {
  pagination.value.page = page;
  load();
}

function onPageSizeChange (size) {
  pagination.value.pageSize = size;
  pagination.value.page = 1;
  load();
}

function goCreate () {
  router.push({ path: "/form-schemes/edit", query: { mode: "new" } });
}

function goEdit (id) {
  router.push({
    path: "/form-schemes/edit",
    query: { id: String(id) },
  });
}

// 多选框选中变化
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id).filter(id => id != null);
}

// 单个删除
async function doRemove (id) {
  try {
    await deleteSingleScheme(id);
    ElMessage.success("删除成功！");
    // 如果当前页删到空了，自动回到上一页
    if (list.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page -= 1;
    }
    load();
  } catch (err) {
    console.error(err);
    ElMessage.error("删除失败：" + (err?.message || "操作异常"));
  }
}

// 批量删除
async function doBatchRemove() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请先选择要删除的方案");
    return;
  }
  try {
    const res = await batchDeleteScheme(selectedIds.value);
    ElMessage.success(`成功删除 ${res.successCount || 0} 个方案，失败 ${res.failedCount || 0} 个`);
    // 显示失败原因
    if (res.failedIds && Object.keys(res.failedIds).length > 0) {
      const failedReasons = Object.entries(res.failedIds).map(([id, reason]) => `ID ${id}: ${reason}`).join('\n');
      ElMessage.warning(`删除失败详情：\n${failedReasons}`);
    }
    // 清空选中
    selectedIds.value = [];
    // 如果当前页删到空了，自动回到上一页
    if (list.value.length === selectedIds.value.length && pagination.value.page > 1) {
      pagination.value.page -= 1;
    }
    load();
  } catch (err) {
    console.error(err);
    ElMessage.error("批量删除失败：" + (err?.message || "操作异常"));
  }
}

// 列表内直接修改状态
async function handleStatusChange(row, newStatus) {
  const statusLabels = { 0: '停用', 1: '启用', 2: '草稿' };
  const label = statusLabels[newStatus] ?? '';
  try {
    await ElMessageBox.confirm(
      `确认将方案「${row.name}」改为${label}状态？`,
      '状态修改确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    );
    await updateScheme(row.id, { status: newStatus });
    ElMessage.success(`状态已改为${label}`);
    load();
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err);
      ElMessage.error('状态修改失败：' + (err?.message || '操作异常'));
    }
  }
}

function formatTime (v) {
  if (!v) return "";
  try {
    const d = typeof v === "string" ? new Date(v) : v;
    if (Number.isNaN(d.getTime())) return String(v);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const h = String(d.getHours()).padStart(2, "0");
    const mi = String(d.getMinutes()).padStart(2, "0");
    return `${y}-${m}-${day} ${h}:${mi}`;
  } catch {
    return String(v);
  }
}

onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value);
    searchTimer.value = null;
  }
});
</script>

<style scoped>
.scheme-page {
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
.grow {
  flex: 1;
}
.w-64 {
  width: 260px;
}
.w-40 {
  width: 180px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.tpl-team {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}
.text-muted {
  color: #bbb;
}
.mt-4 {
  margin-top: 16px;
}

/* 状态选择器颜色区分 */
.status-select.status-0 :deep(.el-input__wrapper) {
  background-color: #f4f4f5;
  color: #909399;
}
.status-select.status-1 :deep(.el-input__wrapper) {
  background-color: #f0f9eb;
  color: #67c23a;
}
.status-select.status-2 :deep(.el-input__wrapper) {
  background-color: #fdf6ec;
  color: #e6a23c;
}
</style>
