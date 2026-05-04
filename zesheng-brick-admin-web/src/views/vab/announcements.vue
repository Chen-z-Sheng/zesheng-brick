<template>
    <div class="announcements-container">
        <el-card shadow="never">
            <div class="toolbar">
                <el-input
                    v-model="searchKey"
                    clearable
                    placeholder="搜索标题"
                    class="w-64"
                    @input="onSearchInputDebounce"
                    @clear="onSearchClear"
                />
                <el-button v-permissions="['admin:announcement:add']" type="primary" @click="openCreateDialog">新增公告</el-button>
            </div>

            <el-table v-loading="loading" :data="list" border header-row-class-name="table-header" size="small">
                <el-table-column label="ID" width="70" align="center" prop="id" />
                <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
                <el-table-column label="状态" width="140" align="center">
                    <template #default="{ row }">
                        <div class="status-cell">
                            <el-tag :type="row.status === 1 ? 'success' : 'info'" class="status-tag">
                                {{ row.status === 1 ? '启用' : '未启用' }}
                            </el-tag>
                            <el-button
                                v-if="row.status !== 1"
                                v-permissions="['admin:announcement:update']"
                                type="primary"
                                link
                                class="status-enable-button"
                                :loading="enableLoadingId === row.id"
                                @click="enableRow(row)"
                            >
                                启用
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180" align="center">
                    <template #default="{ row }">
                        {{ formatDateTime(row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="更新时间" width="180" align="center">
                    <template #default="{ row }">
                        {{ formatDateTime(row.updatedAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right" align="center">
                    <template #default="{ row }">
                        <el-button
                            v-permissions="['admin:announcement:update']"
                            size="small"
                            type="primary"
                            link
                            @click="openEditDialog(row)"
                        >
                            编辑
                        </el-button>
                        <el-popconfirm v-permissions="['admin:announcement:delete']" title="确认删除该公告？" @confirm="doRemove(row)">
                            <template #reference>
                                <el-button size="small" type="danger" link>删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="!loading && list.length === 0" description="暂无公告，点击新增添加" class="mt-4">
                <template #extra>
                    <el-button v-permissions="['admin:announcement:add']" type="primary" @click="openCreateDialog">新增公告</el-button>
                </template>
            </el-empty>

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
        </el-card>

        <el-dialog
            v-model="dialogVisible"
            :title="isEdit ? '编辑公告' : '新增公告'"
            width="800px"
            destroy-on-close
            class="announcements-dialog"
            @close="resetForm"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="128" show-word-limit />
                </el-form-item>
                <el-form-item label="内容" prop="content" class="editor-form-item">
                    <div class="editor-wrap" v-show="dialogVisible">
                        <div ref="toolbarContainerRef" class="editor-toolbar"></div>
                        <div ref="editorContainerRef" class="editor-body"></div>
                    </div>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :label="1">启用</el-radio>
                        <el-radio :label="0">未启用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="submit">{{ isEdit ? '保存' : '发布' }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, shallowRef, onBeforeUnmount, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createEditor, createToolbar } from '@wangeditor/editor'
import dayjs from 'dayjs'
import '@wangeditor/editor/dist/css/style.css'
import {
    getAnnouncementPage,
    createAnnouncement,
    updateAnnouncement,
    enableAnnouncement,
    deleteAnnouncement,
    uploadAnnouncementImage
} from '@/api/announcement'

const searchKey = ref('')
const DEBOUNCE_MS = 600
let searchDebounceTimer = null
const list = ref([])
const loading = ref(false)
const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0
})
const dialogVisible = ref(false)
const submitLoading = ref(false)
const enableLoadingId = ref(null)
const editingId = ref(null)
const formRef = ref(null)
const toolbarContainerRef = ref(null)
const editorContainerRef = ref(null)
const editorInstanceRef = shallowRef(null)
const toolbarInstanceRef = shallowRef(null)

const createDefaultForm = () => ({
    title: '',
    content: '',
    status: 0
})

const form = ref(createDefaultForm())

const rules = {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const isEdit = computed(() => editingId.value !== null)

function formatDateTime(value) {
    if (!value) return '-'
    const date = dayjs(value)
    return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : value
}

function getEditorConfig() {
    return {
        placeholder: '请输入公告内容',
        MENU_CONF: {
            uploadImage: {
                async customUpload(file, insertFn) {
                    try {
                        const data = await uploadAnnouncementImage(file)
                        const url = data && data.url ? data.url : (data && typeof data === 'string' ? data : null)
                        if (url) {
                            insertFn(url, file.name, url)
                        } else {
                            ElMessage.error('上传失败，未返回图片地址')
                        }
                    } catch (err) {
                        ElMessage.error('图片上传失败：' + (err?.message || '网络异常'))
                    }
                }
            }
        }
    }
}

function initEditor() {
    if (!editorContainerRef.value || !toolbarContainerRef.value) return
    const editorConfig = getEditorConfig()
    const editor = createEditor({
        selector: editorContainerRef.value,
        config: editorConfig,
        html: form.value.content || '',
        mode: 'default'
    })
    const toolbar = createToolbar({
        editor,
        selector: toolbarContainerRef.value,
        config: {},
        mode: 'default'
    })
    editorInstanceRef.value = editor
    toolbarInstanceRef.value = toolbar
}

function destroyEditor() {
    try {
        if (toolbarInstanceRef.value && typeof toolbarInstanceRef.value.destroy === 'function') {
            toolbarInstanceRef.value.destroy()
        }
    } catch (err) {
        console.warn('toolbar destroy', err)
    }
    toolbarInstanceRef.value = null
    try {
        if (editorInstanceRef.value && typeof editorInstanceRef.value.destroy === 'function') {
            editorInstanceRef.value.destroy()
        }
    } catch (err) {
        console.warn('editor destroy', err)
    }
    editorInstanceRef.value = null
}

watch(dialogVisible, async (visible) => {
    if (visible) {
        await nextTick()
        initEditor()
    } else {
        destroyEditor()
    }
})

function unwrapPage(res) {
    const data = res && res.data !== undefined ? res.data : res
    return data
}

function onSearchInputDebounce() {
    if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
        searchDebounceTimer = null
        pagination.value.page = 1
        load()
    }, DEBOUNCE_MS)
}

function onSearchClear() {
    if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer)
        searchDebounceTimer = null
    }
    pagination.value.page = 1
    load()
}

async function load() {
    loading.value = true
    try {
        const params = {
            pageNum: pagination.value.page,
            pageSize: pagination.value.pageSize,
            orderBy: 'updatedAt',
            order: 'DESC',
            titleKeyword: searchKey.value?.trim() || undefined
        }
        const res = await getAnnouncementPage(params)
        const data = unwrapPage(res)
        list.value = Array.isArray(data?.records) ? data.records : []
        if (data?.pageMeta) {
            pagination.value.total = Number(data.pageMeta.total) || 0
            pagination.value.page = Number(data.pageMeta.page) || pagination.value.page
            pagination.value.pageSize = Number(data.pageMeta.pageSize) || pagination.value.pageSize
        } else {
            pagination.value.total = list.value.length
        }
    } catch (err) {
        console.error(err)
        ElMessage.error('加载公告失败：' + (err?.message || '服务器异常'))
        list.value = []
        pagination.value.total = 0
    } finally {
        loading.value = false
    }
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

function openCreateDialog() {
    editingId.value = null
    form.value = createDefaultForm()
    dialogVisible.value = true
}

function openEditDialog(row) {
    editingId.value = row.id
    form.value = {
        title: row.title || '',
        content: row.content || '',
        status: row.status === 1 ? 1 : 0
    }
    dialogVisible.value = true
}

function resetForm() {
    destroyEditor()
    editingId.value = null
    form.value = createDefaultForm()
    formRef.value?.resetFields?.()
}

function syncEditorContent() {
    if (editorInstanceRef.value) {
        form.value.content = editorInstanceRef.value.getHtml()
    }
}

async function submit() {
    if (!formRef.value) return
    syncEditorContent()
    await formRef.value.validate(async (valid) => {
        if (!valid) return
        submitLoading.value = true
        try {
            const payload = {
                title: form.value.title.trim(),
                content: form.value.content || '',
                status: form.value.status === 1 ? 1 : 0
            }
            if (isEdit.value) {
                await updateAnnouncement(editingId.value, payload)
                ElMessage.success('保存成功')
            } else {
                await createAnnouncement(payload)
                ElMessage.success('发布成功')
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

async function enableRow(row) {
    enableLoadingId.value = row.id
    try {
        await enableAnnouncement(row.id)
        ElMessage.success('启用成功')
        load()
    } catch (err) {
        console.error(err)
        ElMessage.error(err?.message || '启用失败')
    } finally {
        enableLoadingId.value = null
    }
}

async function doRemove(row) {
    try {
        await deleteAnnouncement(row.id)
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

onBeforeUnmount(() => {
    destroyEditor()
})

onUnmounted(() => {
    if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer)
        searchDebounceTimer = null
    }
})
</script>

<style lang="scss" scoped>
.announcements-container {
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

.pager {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}

.status-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-height: 32px;
}

.status-tag {
    height: 24px;
    line-height: 22px;
    display: inline-flex;
    align-items: center;
}

.status-enable-button {
    height: 24px;
    line-height: 24px;
    padding: 0;
    display: inline-flex;
    align-items: center;
    vertical-align: middle;
}

.status-enable-button:deep(.el-button) {
    height: 24px;
}

.status-enable-button:deep(.el-button--primary.is-link) {
    height: 24px;
    line-height: 24px;
    padding: 0;
}

.editor-form-item {
    :deep(.el-form-item__content) {
        line-height: 1;
    }
}

.editor-wrap {
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    overflow: hidden;

    .editor-toolbar {
        border-bottom: 1px solid var(--el-border-color);
    }

    .editor-body {
        min-height: 320px;

        :deep(.w-e-scroll) {
            min-height: 320px;
            box-sizing: border-box;
        }

        :deep(.w-e-text-container [data-slate-editor]) {
            min-height: 288px;
            padding: 16px;
            line-height: 1.75;
        }

        :deep(.w-e-text-placeholder) {
            top: 16px;
            left: 16px;
            line-height: 1.75;
        }

        :deep(.w-e-text-container [data-slate-editor] p) {
            margin: 0;
        }
    }
}
</style>

<style lang="scss">
.announcements-dialog.el-dialog .el-dialog__body {
    padding-top: 24px;
}
</style>
