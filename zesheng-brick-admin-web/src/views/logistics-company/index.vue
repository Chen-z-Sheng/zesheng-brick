<template>
  <div class="logistics-page">
    <div class="toolbar">
      <el-input
        v-model="searchForm.name"
        class="search-input"
        placeholder="公司名称关键词"
        clearable
        @keyup.enter="onSearch"
        @input="onNameInput"
        @clear="onClearAndSearch"
      >
        <template #append>
          <el-button @click="onSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-button
        v-permissions="['admin:logistics-company:add']"
        type="primary"
        @click="openDialog()"
      >
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      border
      header-row-class-name="table-header"
      size="small"
    >
      <el-table-column prop="id" label="ID" width="72" align="center" />
      <el-table-column prop="name" label="公司名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="code" label="公司代码" width="100" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-switch
            v-permissions="['admin:logistics-company:update']"
            v-model="row.status"
            @change="onStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="168" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="168" align="center">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-permissions="['admin:logistics-company:update']"
            type="primary"
            link
            size="small"
            @click="openDialog(row)"
          >
            编辑
          </el-button>
          <el-popconfirm
            v-permissions="['admin:logistics-company:delete']"
            title="确定删除该公司？引用处需自行检查。"
            @confirm="doRemove(row.id)"
          >
            <template #reference>
              <el-button type="danger" link size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无物流公司"
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

    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑物流公司' : '新增物流公司'"
      width="480px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="form.name" placeholder="必填" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="公司代码" prop="code">
          <el-input v-model="form.code" placeholder="可选" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="9999"
            controls-position="right"
            class="sort-input-number"
          />
        </el-form-item>
        <el-form-item label="启用" prop="status">
          <el-switch v-model="form.status" />
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import {
  getLogisticsCompanyPage,
  createLogisticsCompany,
  updateLogisticsCompany,
  deleteLogisticsCompany,
} from '@/api/logistics-company'

const loading = ref(false)
const DEBOUNCE_MS = 600
const list = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const saving = ref(false)
const formRef = ref()

const searchForm = reactive({
  name: '',
})
let searchDebounceTimer = null

const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
})

const form = ref({
  name: '',
  code: '',
  sort: 0,
  status: true,
})

const rules = {
  name: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
}

function formatTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function unwrapPage(res) {
  return res && res.data !== undefined ? res.data : res
}

async function load() {
  loading.value = true
  try {
    const name = searchForm.name?.trim()
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      ...(name ? { name } : {}),
    }
    const res = await getLogisticsCompanyPage(params)
    const data = unwrapPage(res)
    list.value = Array.isArray(data?.records) ? data.records : []
    if (data?.pageMeta) {
      pagination.value.total = Number(data.pageMeta.total) || 0
      pagination.value.page = Number(data.pageMeta.page) || pagination.value.page
      pagination.value.pageSize = Number(data.pageMeta.pageSize) || pagination.value.pageSize
    } else {
      pagination.value.total = list.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载列表失败')
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

function onNameInput() {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    searchDebounceTimer = null
    onSearch()
  }, DEBOUNCE_MS)
}

function onClearAndSearch() {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
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

function openDialog(row) {
  editId.value = row ? row.id : null
  if (row) {
    form.value = {
      name: row.name ?? '',
      code: row.code ?? '',
      sort: row.sort ?? 0,
      status: row.status !== false,
    }
  } else {
    form.value = {
      name: '',
      code: '',
      sort: 0,
      status: true,
    }
  }
  dialogVisible.value = true
}

function resetForm() {
  form.value = { name: '', code: '', sort: 0, status: true }
  editId.value = null
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        name: form.value.name?.trim(),
        code: form.value.code?.trim() || undefined,
        sort: form.value.sort ?? 0,
        status: form.value.status !== false,
      }
      if (editId.value) {
        await updateLogisticsCompany(editId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await createLogisticsCompany(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      load()
    } catch (e) {
      console.error(e)
      ElMessage.error(e?.message || '操作失败')
    } finally {
      saving.value = false
    }
  })
}

async function onStatusChange(row) {
  try {
    await updateLogisticsCompany(row.id, { status: row.status })
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = !row.status
    ElMessage.error('状态更新失败')
  }
}

async function doRemove(id) {
  try {
    await deleteLogisticsCompany(id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(() => {
  load()
})

onUnmounted(() => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
})
</script>

<style scoped lang="scss">
.logistics-page {
  padding: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.search-input {
  width: 300px;
  max-width: 100%;
}
.mt-4 {
  margin-top: 24px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 排序无需占满一行，右侧步进器宽度适中即可 */
.sort-input-number {
  width: 132px;
}
</style>
