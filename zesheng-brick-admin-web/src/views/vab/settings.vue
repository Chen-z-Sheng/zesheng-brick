<template>
    <div class="settings-container">
        <el-card shadow="never">
            <div class="toolbar">
                <el-input
                    v-model="searchKey"
                    clearable
                    placeholder="搜索配置键"
                    class="w-64"
                    @keyup.enter="onSearchInput"
                    @input="onSearchInputDebounce"
                    @clear="onSearchClear"
                />
                <el-button v-permissions="['admin:config:add']" type="primary" @click="openDialog()">新增配置</el-button>
            </div>

            <el-table
                v-loading="loading"
                :data="filteredList"
                border
                header-row-class-name="table-header"
                size="small"
            >
                <el-table-column label="ID" width="70" align="center" prop="id" show-overflow-tooltip />
                <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
                <el-table-column label="配置值" min-width="200" show-overflow-tooltip>
                    <template #default="{ row }">
                        <span class="value-cell">{{ formatValue(row.value) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="值类型" width="100" align="center">
                    <template #default="{ row }">{{ formatValueType(row.valueType) }}</template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip>
                    <template #default="{ row }">
                        <span v-if="row.remark">{{ row.remark }}</span>
                        <span v-else class="text-muted">-</span>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" width="165" align="center">
                    <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="更新时间" width="165" align="center">
                    <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right" align="center">
                    <template #default="{ row }">
                        <el-button v-permissions="['admin:config:update']" size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
                        <el-popconfirm v-permissions="['admin:config:delete']" title="确认删除该配置？" @confirm="doRemove(row)">
                            <template #reference>
                                <el-button size="small" type="danger" link>删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="!loading && filteredList.length === 0" description="暂无配置" class="mt-4">
                <template #extra>
                    <el-button v-permissions="['admin:config:add']" type="primary" @click="openDialog()">新增配置</el-button>
                </template>
            </el-empty>
        </el-card>

        <el-dialog
            v-model="dialogVisible"
            :title="editId ? '编辑配置' : '新增配置'"
            width="520px"
            destroy-on-close
            class="config-dialog"
            @close="resetForm"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="config-form">
                <el-form-item label="配置键" prop="configKey">
                    <el-input v-model="form.configKey" placeholder="如 site.name、home.banner" :disabled="!!editId" />
                </el-form-item>
                <el-form-item label="值类型" prop="valueType">
                    <el-select v-model="form.valueType" placeholder="请选择" class="w-full" @change="onValueTypeChange">
                        <el-option value="json" label="json" />
                        <el-option value="string" label="string" />
                        <el-option value="number" label="number" />
                        <el-option value="boolean" label="boolean" />
                    </el-select>
                </el-form-item>
                <el-form-item label="配置值" prop="value">
                    <el-select
                        v-if="form.valueType === 'boolean'"
                        v-model="form.value"
                        placeholder="请选择"
                        class="w-full"
                    >
                        <el-option label="true" value="true" />
                        <el-option label="false" value="false" />
                    </el-select>
                    <div v-else class="value-editor-wrap">
                        <div v-if="form.valueType === 'json'" class="value-editor-toolbar">
                            <span class="value-editor-hint">Tab 缩进 · 后回车自动补一行</span>
                            <el-button type="primary" link size="small" @click="formatJsonValue">格式化</el-button>
                        </div>
                        <el-input
                            ref="valueInputRef"
                            v-model="form.value"
                            type="textarea"
                            :rows="6"
                            :placeholder="valuePlaceholder"
                            class="value-textarea"
                            @keydown.tab.prevent="onValueTab"
                            @keydown.enter="onValueEnter"
                        />
                    </div>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, createConfig, updateConfig, deleteConfig } from '@/api/config'

const TAB_STR = '  '
/** 新增 json 类型时的默认骨架 */
const JSON_DEFAULT = '{\n  "": ""\n}'

const valueInputRef = ref(null)

function onValueTab(e) {
    const ta = e.target
    if (!ta || ta.tagName !== 'TEXTAREA') return
    const start = ta.selectionStart
    const end = ta.selectionEnd
    const val = form.value.value
    const newVal = val.slice(0, start) + TAB_STR + val.slice(end)
    form.value.value = newVal
    nextTick(() => {
        ta.selectionStart = ta.selectionEnd = start + TAB_STR.length
        ta.focus()
    })
}

function formatJsonValue() {
    const raw = (form.value.value || '').trim()
    if (!raw) return
    try {
        const parsed = JSON.parse(raw)
        form.value.value = JSON.stringify(parsed, null, 2)
        ElMessage.success('已格式化')
    } catch (err) {
        ElMessage.warning('当前内容不是合法 JSON，无法格式化')
    }
}

function onValueEnter(e) {
    if (form.value.valueType !== 'json') return
    const ta = e.target
    if (!ta || ta.tagName !== 'TEXTAREA') return
    const val = form.value.value || ''
    const start = ta.selectionStart
    const end = ta.selectionEnd

    // 「,」后回车：补一行与默认骨架相同格式的空键值对（"": ""，仅冒号后一个空格）
    const beforeCursor = val.slice(0, start).replace(/\s*$/, '')
    if (beforeCursor.endsWith(',')) {
        e.preventDefault()
        const lineStart = val.lastIndexOf('\n', start - 1) + 1
        const linePrefix = val.slice(lineStart, start)
        const indentMatch = linePrefix.match(/^(\s*)/)
        // 与当前行同级（下一个键），不要再加一层缩进；加一层会与下面「{ 后回车」逻辑叠成双倍
        const lineIndent = indentMatch ? indentMatch[1] : ''
        const insert = `\n${lineIndent}"": ""`
        form.value.value = val.slice(0, start) + insert + val.slice(end)
        const cursorPos = start + 1 + lineIndent.length + 1
        nextTick(() => {
            ta.selectionStart = ta.selectionEnd = cursorPos
            ta.focus()
        })
        return
    }

    if (val.trim() === '{}') {
        e.preventDefault()
        form.value.value = '{\n  \n}'
        nextTick(() => {
            ta.selectionStart = ta.selectionEnd = 4
            ta.focus()
        })
        return
    }

    const textBefore = val.slice(0, start)
    const lineStart = textBefore.lastIndexOf('\n') + 1
    const nextNewline = val.indexOf('\n', lineStart)
    const lineEnd = nextNewline === -1 ? val.length : nextNewline
    const currentLine = val.slice(lineStart, lineEnd)
    const indentMatch = currentLine.match(/^(\s*)/)
    const currentIndent = indentMatch ? indentMatch[1] : ''
    const fromLineStartToCursor = textBefore.slice(lineStart)
    const isRightAfterBrace = /^\s*\{\s*$/.test(fromLineStartToCursor)
    const newIndent = isRightAfterBrace ? currentIndent + '  ' : currentIndent
    const insert = '\n' + newIndent
    e.preventDefault()
    form.value.value = val.slice(0, start) + insert + val.slice(end)
    nextTick(() => {
        ta.selectionStart = ta.selectionEnd = start + insert.length
        ta.focus()
    })
}

function formatDate(dateVal) {
    if (!dateVal) return '-'
    const d = new Date(dateVal)
    if (isNaN(d.getTime())) return '-'
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatValue(val) {
    if (val === undefined || val === null) return '-'
    if (typeof val === 'object') return JSON.stringify(val)
    return String(val)
}

function formatValueType(vt) {
    if (!vt) return '-'
    if (vt === 'bool') return 'boolean'
    return vt
}

/** 记录上一次值类型，便于类型切换时清空不适用的配置值 */
const lastValueType = ref('json')

function applyDefaultValueForType(t) {
    if (t === 'json') {
        form.value.value = JSON_DEFAULT
    } else {
        form.value.value = ''
    }
}

function onValueTypeChange(newType) {
    const oldType = lastValueType.value
    lastValueType.value = newType

    if (oldType === 'boolean' && newType !== 'boolean') {
        applyDefaultValueForType(newType)
        return
    }

    if (oldType === 'json' && newType !== 'json') {
        if (newType === 'boolean') {
            form.value.value = 'true'
        } else {
            form.value.value = ''
        }
        return
    }

    if (newType === 'boolean') {
        const v = (form.value.value || '').trim().toLowerCase()
        if (v !== 'true' && v !== 'false') {
            form.value.value = 'true'
        }
    } else if (newType === 'json') {
        const cur = (form.value.value || '').trim()
        if (!cur) {
            form.value.value = JSON_DEFAULT
        }
    }
}

const DEBOUNCE_MS = 600
const searchKey = ref('')
const filterKey = ref('')
let searchDebounceTimer = null
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = ref({
    configKey: '',
    valueType: 'json',
    value: JSON_DEFAULT,
    remark: ''
})

const rules = computed(() => ({
    configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
    valueType: [{ required: true, message: '请选择值类型', trigger: 'change' }],
    value: [
        { required: true, message: '请输入或选择配置值', trigger: 'blur' },
        {
            validator: (_rule, val, callback) => {
                const t = form.value.valueType
                if (t === 'json') {
                    if (val === undefined || val === null || !String(val).trim()) {
                        callback()
                        return
                    }
                    try {
                        JSON.parse(val)
                        callback()
                    } catch {
                        callback(new Error('配置值不是合法的 JSON'))
                    }
                } else if (t === 'number') {
                    if (val === undefined || val === null || String(val).trim() === '') {
                        callback(new Error('请输入数字'))
                        return
                    }
                    if (Number.isNaN(Number(String(val).trim()))) {
                        callback(new Error('请输入合法数字'))
                    } else {
                        callback()
                    }
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}))

const filteredList = computed(() => {
    const key = (filterKey.value || '').trim().toLowerCase()
    if (!key) return list.value
    return list.value.filter((item) => (item.configKey || '').toLowerCase().includes(key))
})

const valuePlaceholder = computed(() => {
    const t = form.value.valueType
    if (t === 'json') return '如 "hello" 或 {"a":1}，支持 Tab 缩进'
    if (t === 'string') return '普通字符串，如 hello'
    if (t === 'number') return '数字，如 0、1、3.14'
    return '请输入配置值'
})

async function load() {
    loading.value = true
    try {
        const data = await getConfigList()
        list.value = Array.isArray(data) ? data : []
    } catch (err) {
        console.error(err)
        ElMessage.error('加载配置失败：' + (err?.message || '服务器异常'))
        list.value = []
    } finally {
        loading.value = false
    }
}

function onSearchInput() {
    filterKey.value = searchKey.value
}

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

function openDialog(row) {
    editId.value = row ? row.id : null
    if (row) {
        const vt = row.valueType === 'bool' ? 'boolean' : row.valueType || 'json'
        form.value = {
            configKey: row.configKey,
            valueType: vt,
            value: typeof row.value === 'string' ? row.value : JSON.stringify(row.value, null, 2),
            remark: row.remark || ''
        }
    } else {
        form.value = {
            configKey: '',
            valueType: 'json',
            value: JSON_DEFAULT,
            remark: ''
        }
    }
    lastValueType.value = form.value.valueType
    dialogVisible.value = true
}

function resetForm() {
    form.value = { configKey: '', valueType: 'json', value: JSON_DEFAULT, remark: '' }
    lastValueType.value = 'json'
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
                await updateConfig(editId.value, {
                    value: form.value.value,
                    valueType: form.value.valueType,
                    remark: form.value.remark || null
                })
                ElMessage.success('保存成功')
            } else {
                await createConfig({
                    configKey: form.value.configKey.trim(),
                    value: form.value.value,
                    valueType: form.value.valueType,
                    remark: form.value.remark || null
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
        await deleteConfig(row.id)
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
</script>

<style lang="scss" scoped>
.settings-container {
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

.w-full {
    width: 100%;
}

.value-cell {
    word-break: break-all;
}

.text-muted {
    color: #bbb;
}

.mt-4 {
    margin-top: 16px;
}

.config-dialog :deep(.el-dialog__body) {
    padding-top: 24px;
}
.config-form :deep(.el-form-item:first-child) {
    margin-top: 0;
}

.value-editor-wrap {
    width: 100%;
}
.value-editor-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
    padding: 0 2px;
}
.value-editor-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}
.value-editor-wrap .value-textarea {
    display: block;
}
.value-editor-wrap :deep(.el-textarea__inner) {
    font-family: ui-monospace, "Cascadia Code", "Source Code Pro", Menlo, monospace;
    tab-size: 4;
}
</style>

<!-- 弹窗被 teleport 到 body，需单独写未 scoped 的样式才能生效 -->
<style lang="scss">
.config-dialog.el-dialog .el-dialog__body {
    padding-top: 24px;
}
</style>
