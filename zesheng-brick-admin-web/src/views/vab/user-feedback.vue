<template>
    <div class="user-feedback-page">
        <div class="toolbar">
            <el-select v-model="query.feedbackType" clearable placeholder="反馈类型" class="w-180" @change="onSearch">
                <el-option label="功能异常" value="功能异常" />
                <el-option label="产品建议" value="产品建议" />
                <el-option label="体验问题" value="体验问题" />
                <el-option label="其他" value="其他" />
            </el-select>
        </div>

        <el-table v-loading="loading" :data="list" border size="small">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="displayName" label="反馈用户" width="120" show-overflow-tooltip />
            <el-table-column prop="feedbackType" label="反馈类型" width="120" />
            <el-table-column prop="content" label="反馈内容" min-width="280" show-overflow-tooltip />
            <el-table-column prop="replyContent" label="管理员回复" min-width="240" show-overflow-tooltip>
                <template #default="{ row }">
                    <span v-if="row.replyContent">{{ row.replyContent }}</span>
                    <span v-else class="text-muted">暂未回复</span>
                </template>
            </el-table-column>
            <el-table-column label="图片数量" width="100" align="center">
                <template #default="{ row }">
                    {{ Array.isArray(row.imageUrls) ? row.imageUrls.length : 0 }}
                </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="170" align="center">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default="{ row }">
                    <el-button v-permissions="['admin:user-feedback:reply']" type="primary" link @click="openReplyDialog(row)">
                        {{ row.replyContent ? '修改回复' : '回复' }}
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <div v-if="pagination.total > 0" class="pager">
            <el-pagination
                :current-page="pagination.page"
                :page-size="pagination.pageSize"
                :total="pagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="onPageChange"
                @size-change="onPageSizeChange"
            />
        </div>

        <el-dialog v-model="replyDialogVisible" title="回复用户反馈" width="560px" destroy-on-close>
            <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="90px">
                <el-form-item label="反馈类型">
                    <span>{{ currentRow?.feedbackType || '-' }}</span>
                </el-form-item>
                <el-form-item label="反馈内容">
                    <span>{{ currentRow?.content || '-' }}</span>
                </el-form-item>
                <el-form-item label="回复内容" prop="replyContent">
                    <el-input v-model="replyForm.replyContent" type="textarea" :rows="5" maxlength="1000" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="replyDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="replySubmitting" @click="submitReply">保存回复</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserFeedbackPage, replyUserFeedback } from '@/api/user-feedback'

const loading = ref(false)
const list = ref([])
const query = ref({
    feedbackType: ''
})
const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0
})
const replyDialogVisible = ref(false)
const replySubmitting = ref(false)
const replyFormRef = ref(null)
const currentRow = ref(null)
const replyForm = ref({
    replyContent: ''
})
const replyRules = {
    replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

function formatTime(v) {
    if (!v) return ''
    const d = new Date(v)
    if (Number.isNaN(d.getTime())) return v
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const mi = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${mi}`
}

async function load() {
    loading.value = true
    try {
        const res = await getUserFeedbackPage({
            pageNum: pagination.value.page,
            pageSize: pagination.value.pageSize,
            orderBy: 'createdAt',
            order: 'DESC',
            feedbackType: query.value.feedbackType || undefined
        })
        list.value = Array.isArray(res?.records) ? res.records : []
        pagination.value.total = Number(res?.pageMeta?.total) || 0
    } catch (error) {
        ElMessage.error(error?.message || '加载反馈列表失败')
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

function onPageChange(page) {
    pagination.value.page = page
    load()
}

function onPageSizeChange(pageSize) {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    load()
}

function openReplyDialog(row) {
    currentRow.value = row
    replyForm.value.replyContent = row.replyContent || ''
    replyDialogVisible.value = true
}

async function submitReply() {
    if (!currentRow.value || !replyFormRef.value) {
        return
    }
    await replyFormRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        replySubmitting.value = true
        try {
            await replyUserFeedback(currentRow.value.id, {
                replyContent: replyForm.value.replyContent.trim()
            })
            ElMessage.success('回复成功')
            replyDialogVisible.value = false
            load()
        } catch (error) {
            ElMessage.error(error?.message || '回复失败')
        } finally {
            replySubmitting.value = false
        }
    })
}

onMounted(() => {
    load()
})
</script>

<style scoped>
.user-feedback-page {
    padding: 20px;
    background-color: #fff;
    min-height: calc(100vh - 120px);
}

.toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
}

.w-180 {
    width: 180px;
}

.pager {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}

.text-muted {
    color: #909399;
}
</style>
