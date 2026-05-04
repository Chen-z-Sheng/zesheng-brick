<template>
    <div class="help-faq-container">
        <el-card shadow="never">
            <div class="toolbar">
                <el-input
                    v-model="searchKey"
                    clearable
                    placeholder="搜索问题或答案"
                    class="w-64"
                    @keyup.enter="load"
                    @input="onSearchInputDebounce"
                    @clear="onSearchClear"
                />
                <el-button v-permissions="['admin:help-faq:add']" type="primary" @click="openDialog()">新增FAQ</el-button>
            </div>

            <el-table v-loading="loading" :data="filteredList" border header-row-class-name="table-header" size="small">
                <el-table-column label="ID" width="70" align="center" prop="id" />
                <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
                <el-table-column prop="answer" label="答案" min-width="280" show-overflow-tooltip />
                <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
                <el-table-column prop="status" label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right" align="center">
                    <template #default="{ row }">
                        <el-button v-permissions="['admin:help-faq:update']" size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
                        <el-popconfirm v-permissions="['admin:help-faq:delete']" title="确认删除该FAQ？" @confirm="doRemove(row)">
                            <template #reference>
                                <el-button size="small" type="danger" link>删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="!loading && filteredList.length === 0" description="暂无FAQ，点击新增添加" class="mt-4">
                <template #extra>
                    <el-button v-permissions="['admin:help-faq:add']" type="primary" @click="openDialog()">新增FAQ</el-button>
                </template>
            </el-empty>
        </el-card>

        <el-dialog
            v-model="dialogVisible"
            :title="editId ? '编辑FAQ' : '新增FAQ'"
            width="560px"
            destroy-on-close
            class="help-faq-dialog"
            @close="resetForm"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="问题" prop="question">
                    <el-input v-model="form.question" type="textarea" :rows="2" placeholder="请输入问题" maxlength="256" show-word-limit />
                </el-form-item>
                <el-form-item label="答案" prop="answer">
                    <el-input v-model="form.answer" type="textarea" :rows="4" placeholder="请输入答案" />
                </el-form-item>
                <el-form-item label="排序号" prop="sortOrder">
                    <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :label="1">启用</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="submit">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHelpFaqList, createHelpFaq, updateHelpFaq, deleteHelpFaq } from '@/api/help-faq'

const searchKey = ref('')
const filterKey = ref('')
const DEBOUNCE_MS = 600
let searchDebounceTimer = null
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = ref({
    question: '',
    answer: '',
    sortOrder: 0,
    status: 1
})

const rules = {
    question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
    answer: [{ required: true, message: '请输入答案', trigger: 'blur' }],
    sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredList = computed(() => {
    const key = (filterKey.value || '').trim().toLowerCase()
    if (!key) return list.value
    return list.value.filter(
        (item) =>
            (item.question || '').toLowerCase().includes(key) || (item.answer || '').toLowerCase().includes(key)
    )
})

function onSearchInputDebounce() {
    if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
        searchDebounceTimer = null
        filterKey.value = searchKey.value
    }, DEBOUNCE_MS)
}

function onSearchClear() {
    if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer)
        searchDebounceTimer = null
    }
    filterKey.value = searchKey.value
}

async function load() {
    loading.value = true
    try {
        const data = await getHelpFaqList()
        list.value = Array.isArray(data) ? data : []
    } catch (err) {
        console.error(err)
        ElMessage.error('加载FAQ失败：' + (err?.message || '服务器异常'))
        list.value = []
    } finally {
        loading.value = false
    }
}

function openDialog(row) {
    editId.value = row ? row.id : null
    if (row) {
        form.value = {
            question: row.question || '',
            answer: row.answer || '',
            sortOrder: row.sortOrder ?? 0,
            status: row.status ?? 1
        }
    } else {
        form.value = {
            question: '',
            answer: '',
            sortOrder: list.value.length > 0 ? Math.max(...list.value.map((x) => x.sortOrder || 0)) + 1 : 0,
            status: 1
        }
    }
    dialogVisible.value = true
}

function resetForm() {
    form.value = { question: '', answer: '', sortOrder: 0, status: 1 }
    editId.value = null
    formRef.value?.resetFields?.()
}

async function submit() {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (!valid) return
        submitLoading.value = true
        try {
            if (editId.value) {
                await updateHelpFaq(editId.value, {
                    question: form.value.question.trim(),
                    answer: form.value.answer.trim(),
                    sortOrder: form.value.sortOrder,
                    status: form.value.status
                })
                ElMessage.success('保存成功')
            } else {
                await createHelpFaq({
                    question: form.value.question.trim(),
                    answer: form.value.answer.trim(),
                    sortOrder: form.value.sortOrder,
                    status: form.value.status
                })
                ElMessage.success('新增成功')
            }
            dialogVisible.value = false
            load()
        } catch (err) {
            console.error(err)
            ElMessage.error(err?.message || '操作失败')
        } finally {
            submitLoading.value = false
        }
    })
}

async function doRemove(row) {
    try {
        await deleteHelpFaq(row.id)
        ElMessage.success('删除成功')
        load()
    } catch (err) {
        console.error(err)
        ElMessage.error('删除失败：' + (err?.message || '操作异常'))
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

<style lang="scss" scoped>
.help-faq-container {
    padding: 20px;
}

.toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    gap: 12px;
}

.w-64 {
    width: 260px;
}

.mt-4 {
    margin-top: 16px;
}
</style>

<style lang="scss">
.help-faq-dialog.el-dialog .el-dialog__body {
    padding-top: 24px;
}
</style>
