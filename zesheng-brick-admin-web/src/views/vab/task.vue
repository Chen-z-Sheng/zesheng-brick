<template>
    <div class="task-container">
        <el-card shadow="never">
            <div class="toolbar">
                <el-input
                    v-model="searchText"
                    clearable
                    placeholder="搜索标题或内容"
                    class="search-input"
                    @keyup.enter="loadTaskList"
                    @clear="loadTaskList"
                    @input="onSearchTextInput"
                />
                <el-select v-model="filterStatus" placeholder="状态筛选" class="status-select" @change="loadTaskList">
                    <el-option label="全部" :value="null" />
                    <el-option label="待处理" :value="0" />
                    <el-option label="已处理" :value="1" />
                </el-select>
                <el-button v-permissions="['sys:todo-task:add']" type="primary" @click="openDialog()">
                    新增待办
                </el-button>
            </div>

            <el-table v-loading="loading" :data="taskList" border size="small">
                <el-table-column prop="id" label="ID" width="80" align="center" />
                <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
                <el-table-column label="内容" min-width="280">
                    <template #default="{ row }">
                        <span class="content-text">{{ row.content || '-' }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                            {{ row.status === 1 ? '已处理' : '待处理' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="更新时间" width="180" align="center">
                    <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="260" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button v-permissions="['sys:todo-task:update']" type="primary" link @click="openDialog(row)">
                            编辑
                        </el-button>
                        <el-button
                            v-permissions="['sys:todo-task:update']"
                            type="primary"
                            link
                            @click="switchStatus(row)"
                        >
                            {{ row.status === 1 ? '标记待处理' : '标记已处理' }}
                        </el-button>
                        <el-popconfirm
                            v-permissions="['sys:todo-task:delete']"
                            title="确认删除该待办吗？"
                            @confirm="removeTask(row)"
                        >
                            <template #reference>
                                <el-button type="danger" link>删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="!loading && taskList.length === 0" description="暂无待办任务" />
        </el-card>

        <el-dialog
            v-model="dialogVisible"
            :title="editId ? '编辑待办' : '新增待办'"
            width="560px"
            destroy-on-close
            @close="resetForm"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <el-input
                        v-model="form.content"
                        type="textarea"
                        :rows="4"
                        placeholder="可记录功能点、Bug、优化项等"
                    />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :value="0">待处理</el-radio>
                        <el-radio :value="1">已处理</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTodoTask, deleteTodoTask, getTodoTaskList, updateTodoTask, updateTodoTaskStatus } from '@/api/todo-task'

const loading = ref(false)
const submitLoading = ref(false)
const taskList = ref([])
const searchText = ref('')
const filterStatus = ref(null)
const DEBOUNCE_MS = 600
let searchDebounceTimer = null
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = ref({
    title: '',
    content: '',
    status: 0
})

const rules = computed(() => ({
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

function formatDateTime(dateValue) {
    if (!dateValue) {
        return '-'
    }
    const date = new Date(dateValue)
    if (Number.isNaN(date.getTime())) {
        return '-'
    }
    const pad = (number) => String(number).padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function loadTaskList() {
    loading.value = true
    try {
        const params = {}
        if (filterStatus.value !== null && filterStatus.value !== undefined) {
            params.status = filterStatus.value
        }
        if (searchText.value && searchText.value.trim()) {
            params.keyword = searchText.value.trim()
        }
        const data = await getTodoTaskList(params)
        taskList.value = Array.isArray(data) ? data : []
    } catch (error) {
        console.error(error)
        ElMessage.error(error?.message || '待办列表加载失败')
        taskList.value = []
    } finally {
        loading.value = false
    }
}

function onSearchTextInput() {
    if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
        searchDebounceTimer = null
        loadTaskList()
    }, DEBOUNCE_MS)
}

function openDialog(row) {
    editId.value = row?.id ?? null
    form.value = {
        title: row?.title || '',
        content: row?.content || '',
        status: row?.status === 1 ? 1 : 0
    }
    dialogVisible.value = true
}

function resetForm() {
    editId.value = null
    form.value = {
        title: '',
        content: '',
        status: 0
    }
    formRef.value?.resetFields?.()
}

async function submitForm() {
    if (!formRef.value) {
        return
    }
    await formRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        submitLoading.value = true
        try {
            const payload = {
                title: form.value.title.trim(),
                content: form.value.content?.trim() || null,
                status: form.value.status
            }
            if (editId.value) {
                await updateTodoTask(editId.value, payload)
                ElMessage.success('更新成功')
            } else {
                await createTodoTask(payload)
                ElMessage.success('新增成功')
            }
            dialogVisible.value = false
            await loadTaskList()
        } catch (error) {
            console.error(error)
            ElMessage.error(error?.message || '保存失败')
        } finally {
            submitLoading.value = false
        }
    })
}

async function switchStatus(row) {
    try {
        const targetStatus = row.status === 1 ? 0 : 1
        await updateTodoTaskStatus(row.id, targetStatus)
        ElMessage.success('状态更新成功')
        await loadTaskList()
    } catch (error) {
        console.error(error)
        ElMessage.error(error?.message || '状态更新失败')
    }
}

async function removeTask(row) {
    try {
        await deleteTodoTask(row.id)
        ElMessage.success('删除成功')
        await loadTaskList()
    } catch (error) {
        console.error(error)
        ElMessage.error(error?.message || '删除失败')
    }
}

onMounted(() => {
    loadTaskList()
})

onUnmounted(() => {
    if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer)
        searchDebounceTimer = null
    }
})
</script>

<style lang="scss" scoped>
.task-container {
    padding: 20px;
}

.toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
}

.search-input {
    width: 260px;
}

.status-select {
    width: 130px;
}

.content-text {
    white-space: pre-wrap;
    word-break: break-word;
}
</style>