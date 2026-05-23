<template>
    <div class="recycle-market-container">
        <el-card shadow="never">
            <el-tabs v-model="activeTab">
                <el-tab-pane label="行情列表" name="price">
                    <div class="toolbar">
                        <el-select v-model="filterLevel1Id" placeholder="一级分类" clearable class="w-48" @change="onFilterChange">
                            <el-option v-for="item in level1List" :key="item.id" :label="item.name" :value="item.id" />
                        </el-select>
                        <el-select v-model="filterLevel2Id" placeholder="二级分类" clearable class="w-48" @change="loadPricePage">
                            <el-option v-for="item in filterLevel2Options" :key="item.id" :label="item.name" :value="item.id" />
                        </el-select>
                        <el-select v-model="filterLevel3Id" placeholder="三级分类" clearable class="w-48" @change="loadPricePage">
                            <el-option v-for="item in filterLevel3Options" :key="item.id" :label="item.name" :value="item.id" />
                        </el-select>
                        <el-date-picker
                            v-model="filterPriceDate"
                            type="date"
                            placeholder="行情日期"
                            value-format="YYYY-MM-DD"
                            class="w-48"
                            @change="loadPricePage"
                        />
                        <el-button v-permissions="['admin:recycle-market:add']" type="primary" @click="openPriceDialog()">新增行情</el-button>
                    </div>
                    <el-table v-loading="priceLoading" :data="priceList" border size="small">
                        <el-table-column label="ID" prop="id" width="70" align="center" />
                        <el-table-column prop="level1Name" label="一级分类" width="120" />
                        <el-table-column prop="level2Name" label="二级分类" min-width="160" />
                        <el-table-column prop="level3Name" label="三级分类" min-width="180" />
                        <el-table-column prop="priceDate" label="行情日期" width="120" />
                        <el-table-column prop="recyclePrice" label="回收价格" width="120" align="right">
                            <template #default="{ row }">¥{{ row.recyclePrice }}</template>
                        </el-table-column>
                        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
                        <el-table-column label="操作" width="140" fixed="right" align="center">
                            <template #default="{ row }">
                                <el-button v-permissions="['admin:recycle-market:update']" size="small" type="primary" link @click="openPriceDialog(row)">编辑</el-button>
                                <el-popconfirm v-permissions="['admin:recycle-market:delete']" title="确认删除该行情记录？" @confirm="doDeletePrice(row)">
                                    <template #reference>
                                        <el-button size="small" type="danger" link>删除</el-button>
                                    </template>
                                </el-popconfirm>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div class="pagination">
                        <el-pagination
                            :current-page="pricePageNum"
                            :page-size="pricePageSize"
                            :total="priceTotal"
                            :page-sizes="[10, 20, 50]"
                            layout="total, sizes, prev, pager, next"
                            @current-change="onPriceCurrentChange"
                            @size-change="onPriceSizeChange"
                        />
                    </div>
                </el-tab-pane>

                <el-tab-pane label="分类管理" name="category">
                    <div class="category-layout">
                        <div class="category-left">
                            <div class="section-header">
                                <span>一级分类</span>
                                <el-button v-permissions="['admin:recycle-market:add']" type="primary" size="small" @click="openLevel1Dialog()">新增</el-button>
                            </div>
                            <el-table ref="level1TableRef" :data="level1List" border size="small" highlight-current-row @current-change="onLevel1Select">
                                <el-table-column prop="name" label="名称" />
                                <el-table-column label="操作" width="100" align="center">
                                    <template #default="{ row }">
                                        <el-button v-permissions="['admin:recycle-market:update']" size="small" type="primary" link @click="openLevel1Dialog(row)">编辑</el-button>
                                        <el-popconfirm v-permissions="['admin:recycle-market:delete']" title="删除将同时删除其下分类及行情，确认？" @confirm="doDeleteLevel1(row)">
                                            <template #reference>
                                                <el-button size="small" type="danger" link>删除</el-button>
                                            </template>
                                        </el-popconfirm>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                        <div class="category-middle">
                            <div class="section-header">
                                <span>二级分类</span>
                                <el-button
                                    v-permissions="['admin:recycle-market:add']"
                                    type="primary"
                                    size="small"
                                    :disabled="!selectedLevel1Id"
                                    @click="openLevel2Dialog()"
                                >
                                    新增
                                </el-button>
                            </div>
                            <el-table ref="level2TableRef" :data="level2List" border size="small" highlight-current-row @current-change="onLevel2Select">
                                <el-table-column prop="name" label="名称" />
                                <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
                                <el-table-column label="操作" width="100" align="center">
                                    <template #default="{ row }">
                                        <el-button v-permissions="['admin:recycle-market:update']" size="small" type="primary" link @click="openLevel2Dialog(row)">编辑</el-button>
                                        <el-popconfirm v-permissions="['admin:recycle-market:delete']" title="删除将同时删除其下三级分类及行情，确认？" @confirm="doDeleteLevel2(row)">
                                            <template #reference>
                                                <el-button size="small" type="danger" link>删除</el-button>
                                            </template>
                                        </el-popconfirm>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                        <div class="category-right">
                            <div class="section-header">
                                <span>三级分类</span>
                                <el-button
                                    v-permissions="['admin:recycle-market:add']"
                                    type="primary"
                                    size="small"
                                    :disabled="!selectedLevel2Id"
                                    @click="openLevel3Dialog()"
                                >
                                    新增
                                </el-button>
                            </div>
                            <el-table :data="level3List" border size="small">
                                <el-table-column prop="name" label="名称" />
                                <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
                                <el-table-column label="操作" width="100" align="center">
                                    <template #default="{ row }">
                                        <el-button v-permissions="['admin:recycle-market:update']" size="small" type="primary" link @click="openLevel3Dialog(row)">编辑</el-button>
                                        <el-popconfirm v-permissions="['admin:recycle-market:delete']" title="确认删除？" @confirm="doDeleteLevel3(row)">
                                            <template #reference>
                                                <el-button size="small" type="danger" link>删除</el-button>
                                            </template>
                                        </el-popconfirm>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-card>

        <!-- 行情编辑对话框 -->
        <el-dialog v-model="priceDialogVisible" :title="editPriceId ? '编辑行情' : '新增行情'" width="460px" destroy-on-close class="recycle-market-dialog" @close="resetPriceForm">
            <el-form ref="priceFormRef" :model="priceForm" :rules="priceRules" label-width="100px">
                <el-form-item v-if="!editPriceId" label="三级分类" prop="level3Id">
                    <el-select
                        v-model="priceForm.level3Id"
                        placeholder="请选择或输入关键字搜索"
                        filterable
                        clearable
                        style="width: 100%"
                    >
                        <el-option
                            v-for="item in priceLevel3Options"
                            :key="item.id"
                            :label="item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item v-if="!editPriceId" label="行情日期" prop="priceDate">
                    <el-date-picker v-model="priceForm.priceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
                <el-form-item label="回收价格" prop="recyclePrice">
                    <el-input-number v-model="priceForm.recyclePrice" :min="0" :precision="2" :step="1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="priceForm.remark" type="textarea" :rows="2" placeholder="可选" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="priceDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="priceSubmitLoading" @click="submitPrice">保存</el-button>
            </template>
        </el-dialog>

        <!-- 一级分类对话框 -->
        <el-dialog v-model="level1DialogVisible" :title="editLevel1Id ? '编辑一级分类' : '新增一级分类'" width="420px" destroy-on-close class="recycle-market-dialog" @close="resetLevel1Form">
            <el-form ref="level1FormRef" :model="level1Form" :rules="level1Rules" label-width="80px">
                <el-form-item label="名称" prop="name">
                    <el-input v-model="level1Form.name" placeholder="如：手机、国产美妆" />
                </el-form-item>
                <el-form-item label="排序" prop="sortOrder">
                    <el-input-number v-model="level1Form.sortOrder" :min="0" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="level1Form.status">
                        <el-radio :label="1">启用</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="level1DialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="level1SubmitLoading" @click="submitLevel1">保存</el-button>
            </template>
        </el-dialog>

        <!-- 二级分类对话框 -->
        <el-dialog v-model="level2DialogVisible" :title="editLevel2Id ? '编辑二级分类' : '新增二级分类'" width="420px" destroy-on-close class="recycle-market-dialog" @close="resetLevel2Form">
            <el-form ref="level2FormRef" :model="level2Form" :rules="level2Rules" label-width="80px">
                <el-form-item label="一级分类" prop="level1Id">
                    <el-select v-model="level2Form.level1Id" placeholder="请选择" style="width: 100%" :disabled="!!editLevel2Id">
                        <el-option v-for="l1 in level1List" :key="l1.id" :label="l1.name" :value="l1.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="level2Form.name" placeholder="如：苹果 iPhone 15 128G" />
                </el-form-item>
                <el-form-item label="排序" prop="sortOrder">
                    <el-input-number v-model="level2Form.sortOrder" :min="0" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="level2Form.status">
                        <el-radio :label="1">启用</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="level2DialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="level2SubmitLoading" @click="submitLevel2">保存</el-button>
            </template>
        </el-dialog>

        <!-- 三级分类对话框 -->
        <el-dialog v-model="level3DialogVisible" :title="editLevel3Id ? '编辑三级分类' : '新增三级分类'" width="420px" destroy-on-close class="recycle-market-dialog" @close="resetLevel3Form">
            <el-form ref="level3FormRef" :model="level3Form" :rules="level3Rules" label-width="80px">
                <el-form-item label="二级分类" prop="level2Id">
                    <el-select v-model="level3Form.level2Id" placeholder="请选择" style="width: 100%" :disabled="!!editLevel3Id">
                        <el-option v-for="l2 in level2ListForSelect" :key="l2.id" :label="l2.name" :value="l2.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="level3Form.name" placeholder="如：iPhone 15 128G" />
                </el-form-item>
                <el-form-item label="排序" prop="sortOrder">
                    <el-input-number v-model="level3Form.sortOrder" :min="0" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="level3Form.status">
                        <el-radio :label="1">启用</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="level3DialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="level3SubmitLoading" @click="submitLevel3">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
    getLevel1List,
    getLevel2List,
    getLevel3List,
    createLevel1,
    updateLevel1,
    deleteLevel1,
    createLevel2,
    updateLevel2,
    deleteLevel2,
    createLevel3,
    updateLevel3,
    deleteLevel3,
    getLatestPriceDate,
    getPricePage,
    createPrice,
    updatePrice,
    deletePrice
} from '@/api/recycle-market'

const activeTab = ref('price')
const level1List = ref([])
const level2List = ref([])
const level3List = ref([])
const level1TableRef = ref(null)
const level2TableRef = ref(null)
const isRestoringCategorySelection = ref(false)
const selectedLevel1Id = ref(null)
const selectedLevel2Id = ref(null)
const filterLevel1Id = ref(null)
const filterLevel2Id = ref(null)
const filterLevel3Id = ref(null)
const filterPriceDate = ref(null)
const priceList = ref([])
const priceLoading = ref(false)
const pricePageNum = ref(1)
const pricePageSize = ref(20)
const priceTotal = ref(0)

const level2Map = computed(() => {
    const map = {}
    level1List.value.forEach(l1 => {
        map[l1.id] = level2ListForSelect.value.filter(l2 => l2.level1Id === l1.id)
    })
    return map
})

const level3Map = computed(() => {
    const map = {}
    level2ListForSelect.value.forEach(l2 => {
        map[l2.id] = level3ListForSelect.value.filter(l3 => l3.level2Id === l2.id)
    })
    return map
})

// 筛选器二级分类选项：未选一级时显示全部二级，选了则显示该一级下的二级
const filterLevel2Options = computed(() => {
    const lid = filterLevel1Id.value
    if (lid != null && lid !== '') {
        return level2List.value
    }
    return level2ListForSelect.value
})

const filterLevel3Options = computed(() => {
    const l2id = filterLevel2Id.value
    if (l2id != null && l2id !== '') {
        return level3ListForSelect.value.filter(item => item.level2Id === l2id)
    }
    return level3ListForSelect.value
})

/** 新增行情：扁平三级分类选项（含完整路径，便于 filterable 搜索） */
const priceLevel3Options = computed(() => {
    const options = []
    level1List.value.forEach((l1) => {
        (level2Map.value[l1.id] || []).forEach((l2) => {
            (level3Map.value[l2.id] || []).forEach((l3) => {
                options.push({
                    id: l3.id,
                    label: `${l1.name} / ${l2.name} / ${l3.name}`,
                })
            })
        })
    })
    return options
})

const level2ListForSelect = ref([])
const level3ListForSelect = ref([])

// 行情弹窗
const priceDialogVisible = ref(false)
const editPriceId = ref(null)
const priceSubmitLoading = ref(false)
const priceFormRef = ref(null)
const priceForm = ref({
    level3Id: null,
    priceDate: null,
    recyclePrice: null,
    remark: ''
})
const priceRules = {
    level3Id: [{ required: true, message: '请选择三级分类', trigger: 'change' }],
    priceDate: [{ required: true, message: '请选择行情日期', trigger: 'change' }],
    recyclePrice: [{ required: true, message: '请输入回收价格', trigger: 'blur' }]
}

// 一级分类弹窗
const level1DialogVisible = ref(false)
const editLevel1Id = ref(null)
const level1SubmitLoading = ref(false)
const level1FormRef = ref(null)
const level1Form = ref({ name: '', sortOrder: 0, status: 1 })
const level1Rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

// 二级分类弹窗
const level2DialogVisible = ref(false)
const editLevel2Id = ref(null)
const level2SubmitLoading = ref(false)
const level2FormRef = ref(null)
const level2Form = ref({ level1Id: null, name: '', sortOrder: 0, status: 1 })
const level2Rules = {
    level1Id: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const level3DialogVisible = ref(false)
const editLevel3Id = ref(null)
const level3SubmitLoading = ref(false)
const level3FormRef = ref(null)
const level3Form = ref({ level2Id: null, name: '', sortOrder: 0, status: 1 })
const level3Rules = {
    level2Id: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

async function loadLevel1() {
    try {
        const data = await getLevel1List()
        level1List.value = data || []
        const all = []
        const allLevel3 = []
        for (const l1 of level1List.value) {
            const sub = await getLevel2List(l1.id)
            ;(sub || []).forEach(s => all.push({ ...s, level1Id: s.level1Id ?? l1.id }))
            for (const l2 of (sub || [])) {
                const level3 = await getLevel3List(l2.id)
                ;(level3 || []).forEach(l3 => allLevel3.push({ ...l3, level2Id: l3.level2Id ?? l2.id }))
            }
        }
        level2ListForSelect.value = all
        level3ListForSelect.value = allLevel3
    } catch (err) {
        ElMessage.error('加载分类失败：' + (err?.message || err))
    }
}

async function loadLevel2(level1Id) {
    if (level1Id == null || level1Id === '') {
        level2List.value = []
        return
    }
    try {
        const data = await getLevel2List(Number(level1Id) || level1Id)
        level2List.value = data || []
    } catch (err) {
        level2List.value = []
    }
}

async function loadLevel3(level2Id) {
    if (level2Id == null || level2Id === '') {
        level3List.value = []
        return
    }
    try {
        const data = await getLevel3List(Number(level2Id) || level2Id)
        level3List.value = data || []
    } catch {
        level3List.value = []
    }
}

async function reloadCategorySelection(preferredLevel1Id = null, preferredLevel2Id = null) {
    isRestoringCategorySelection.value = true
    try {
        await loadLevel1()
        const level1Id = preferredLevel1Id ?? selectedLevel1Id.value
        const matchedLevel1 = level1List.value.find(item => String(item.id) === String(level1Id))
        selectedLevel1Id.value = matchedLevel1 ? matchedLevel1.id : (level1List.value[0]?.id ?? null)
        await loadLevel2(selectedLevel1Id.value)

        const level2Id = preferredLevel2Id ?? selectedLevel2Id.value
        const matchedLevel2 = level2List.value.find(item => String(item.id) === String(level2Id))
        selectedLevel2Id.value = matchedLevel2 ? matchedLevel2.id : (level2List.value[0]?.id ?? null)
        await loadLevel3(selectedLevel2Id.value)

        await nextTick()
        if (level1TableRef.value) {
            level1TableRef.value.setCurrentRow(matchedLevel1 || level1List.value[0] || null)
        }
        if (level2TableRef.value) {
            level2TableRef.value.setCurrentRow(matchedLevel2 || level2List.value[0] || null)
        }
    } finally {
        isRestoringCategorySelection.value = false
    }
}

function onLevel1Select(row) {
    if (!row) return
    if (isRestoringCategorySelection.value) return
    selectedLevel1Id.value = row?.id ?? null
    selectedLevel2Id.value = null
    level3List.value = []
    loadLevel2(selectedLevel1Id.value)
}

function onFilterChange() {
    filterLevel2Id.value = null
    filterLevel3Id.value = null
    const lid = filterLevel1Id.value
    if (lid != null && lid !== '') {
        loadLevel2(Number(lid) || lid)
    } else {
        level2List.value = []
    }
    loadPricePage()
}

async function loadPricePage() {
    priceLoading.value = true
    try {
        const params = {
            pageNum: pricePageNum.value,
            pageSize: pricePageSize.value,
            level1Id: filterLevel1Id.value || undefined,
            level2Id: filterLevel2Id.value || undefined,
            level3Id: filterLevel3Id.value || undefined,
            priceDate: filterPriceDate.value || undefined
        }
        const res = await getPricePage(params)
        const records = res?.records || []
        const pageMeta = res?.pageMeta || {}
        priceList.value = records
        priceTotal.value = pageMeta.total ?? 0
    } catch (err) {
        ElMessage.error('加载行情失败：' + (err?.message || err))
        priceList.value = []
    } finally {
        priceLoading.value = false
    }
}

function onPriceCurrentChange(page) {
    pricePageNum.value = page
    loadPricePage()
}

function onPriceSizeChange(size) {
    pricePageSize.value = size
    pricePageNum.value = 1
    loadPricePage()
}

function openPriceDialog(row) {
    editPriceId.value = row?.id ?? null
    if (row) {
        priceForm.value = {
            level3Id: row.level3Id,
            priceDate: row.priceDate,
            recyclePrice: Number(row.recyclePrice),
            remark: row.remark || ''
        }
    } else {
        const filteredLevel3Id = filterLevel3Id.value
        priceForm.value = {
            level3Id: filteredLevel3Id != null && filteredLevel3Id !== '' ? filteredLevel3Id : null,
            priceDate: filterPriceDate.value || getYesterdayStr(),
            recyclePrice: null,
            remark: ''
        }
    }
    priceDialogVisible.value = true
}

function resetPriceForm() {
    priceForm.value = { level3Id: null, priceDate: null, recyclePrice: null, remark: '' }
    editPriceId.value = null
}

async function submitPrice() {
    if (!priceFormRef.value || priceSubmitLoading.value) return

    const isValid = await priceFormRef.value.validate().catch(() => false)
    if (!isValid) {
        return
    }

    const level3Id = Number(priceForm.value.level3Id)
    const recyclePrice = Number(priceForm.value.recyclePrice)
    const priceDate = priceForm.value.priceDate
    const remark = priceForm.value.remark || ''

    if (!editPriceId.value && (!Number.isFinite(level3Id) || !priceDate || !Number.isFinite(recyclePrice))) {
        ElMessage.error('请完整填写三级分类、行情日期和回收价格')
        return
    }

    if (editPriceId.value && !Number.isFinite(recyclePrice)) {
        ElMessage.error('请填写有效的回收价格')
        return
    }

    priceSubmitLoading.value = true
    try {
        if (editPriceId.value) {
            await updatePrice(editPriceId.value, {
                recyclePrice,
                remark
            })
            ElMessage.success('保存成功')
        } else {
            await createPrice({
                level3Id,
                priceDate,
                recyclePrice,
                remark
            })
            ElMessage.success('新增成功')
        }
        priceDialogVisible.value = false
        loadPricePage()
    } catch (err) {
        ElMessage.error(err?.message || '操作失败')
    } finally {
        priceSubmitLoading.value = false
    }
}

async function doDeletePrice(row) {
    try {
        await deletePrice(row.id)
        ElMessage.success('删除成功')
        loadPricePage()
    } catch (err) {
        ElMessage.error('删除失败：' + (err?.message || err))
    }
}

function openLevel1Dialog(row) {
    editLevel1Id.value = row?.id ?? null
    if (row) {
        level1Form.value = { name: row.name, sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 }
    } else {
        level1Form.value = { name: '', sortOrder: level1List.value.length, status: 1 }
    }
    level1DialogVisible.value = true
}

function resetLevel1Form() {
    level1Form.value = { name: '', sortOrder: 0, status: 1 }
    editLevel1Id.value = null
}

async function submitLevel1() {
    if (!level1FormRef.value) return
    await level1FormRef.value.validate(async valid => {
        if (!valid) return
        level1SubmitLoading.value = true
        try {
            if (editLevel1Id.value) {
                await updateLevel1(editLevel1Id.value, level1Form.value)
                ElMessage.success('保存成功')
            } else {
                await createLevel1(level1Form.value)
                ElMessage.success('新增成功')
            }
            level1DialogVisible.value = false
            await reloadCategorySelection(selectedLevel1Id.value, selectedLevel2Id.value)
        } catch (err) {
            ElMessage.error(err?.message || '操作失败')
        } finally {
            level1SubmitLoading.value = false
        }
    })
}

async function doDeleteLevel1(row) {
    try {
        await deleteLevel1(row.id)
        ElMessage.success('删除成功')
        if (selectedLevel1Id.value === row.id) selectedLevel1Id.value = null
        await reloadCategorySelection(selectedLevel1Id.value, selectedLevel2Id.value)
    } catch (err) {
        ElMessage.error('删除失败：' + (err?.message || err))
    }
}

function openLevel2Dialog(row) {
    editLevel2Id.value = row?.id ?? null
    if (row) {
        level2Form.value = {
            level1Id: row.level1Id ?? selectedLevel1Id.value,
            name: row.name,
            sortOrder: row.sortOrder ?? 0,
            status: row.status ?? 1
        }
    } else {
        level2Form.value = {
            level1Id: selectedLevel1Id.value,
            name: '',
            sortOrder: level2List.value.length,
            status: 1
        }
    }
    level2DialogVisible.value = true
}

function onLevel2Select(row) {
    if (!row) return
    if (isRestoringCategorySelection.value) return
    selectedLevel2Id.value = row?.id ?? null
    loadLevel3(selectedLevel2Id.value)
}

function resetLevel2Form() {
    level2Form.value = { level1Id: null, name: '', sortOrder: 0, status: 1 }
    editLevel2Id.value = null
}

async function submitLevel2() {
    if (!level2FormRef.value) return
    await level2FormRef.value.validate(async valid => {
        if (!valid) return
        level2SubmitLoading.value = true
        try {
            if (editLevel2Id.value) {
                await updateLevel2(editLevel2Id.value, {
                    name: level2Form.value.name,
                    sortOrder: level2Form.value.sortOrder,
                    status: level2Form.value.status
                })
                ElMessage.success('保存成功')
            } else {
                await createLevel2(level2Form.value)
                ElMessage.success('新增成功')
            }
            level2DialogVisible.value = false
            await reloadCategorySelection(selectedLevel1Id.value, selectedLevel2Id.value)
        } catch (err) {
            ElMessage.error(err?.message || '操作失败')
        } finally {
            level2SubmitLoading.value = false
        }
    })
}

async function doDeleteLevel2(row) {
    try {
        await deleteLevel2(row.id)
        ElMessage.success('删除成功')
        if (selectedLevel2Id.value === row.id) {
            selectedLevel2Id.value = null
            level3List.value = []
        }
        await reloadCategorySelection(selectedLevel1Id.value, selectedLevel2Id.value)
    } catch (err) {
        ElMessage.error('删除失败：' + (err?.message || err))
    }
}

function openLevel3Dialog(row) {
    editLevel3Id.value = row?.id ?? null
    if (row) {
        level3Form.value = {
            level2Id: row.level2Id ?? selectedLevel2Id.value,
            name: row.name,
            sortOrder: row.sortOrder ?? 0,
            status: row.status ?? 1
        }
    } else {
        level3Form.value = {
            level2Id: selectedLevel2Id.value,
            name: '',
            sortOrder: level3List.value.length,
            status: 1
        }
    }
    level3DialogVisible.value = true
}

function resetLevel3Form() {
    level3Form.value = { level2Id: null, name: '', sortOrder: 0, status: 1 }
    editLevel3Id.value = null
}

async function submitLevel3() {
    if (!level3FormRef.value) return
    await level3FormRef.value.validate(async valid => {
        if (!valid) return
        level3SubmitLoading.value = true
        try {
            if (editLevel3Id.value) {
                await updateLevel3(editLevel3Id.value, {
                    name: level3Form.value.name,
                    sortOrder: level3Form.value.sortOrder,
                    status: level3Form.value.status
                })
            } else {
                await createLevel3(level3Form.value)
            }
            ElMessage.success('保存成功')
            level3DialogVisible.value = false
            await reloadCategorySelection(selectedLevel1Id.value, selectedLevel2Id.value)
        } catch (err) {
            ElMessage.error(err?.message || '操作失败')
        } finally {
            level3SubmitLoading.value = false
        }
    })
}

async function doDeleteLevel3(row) {
    try {
        await deleteLevel3(row.id)
        ElMessage.success('删除成功')
        loadLevel3(selectedLevel2Id.value)
    } catch (err) {
        ElMessage.error('删除失败：' + (err?.message || err))
    }
}

function getYesterdayStr() {
    const d = new Date()
    d.setDate(d.getDate() - 1)
    return d.toISOString().slice(0, 10)
}

onMounted(async () => {
    await reloadCategorySelection()
    try {
        const latestDate = await getLatestPriceDate()
        filterPriceDate.value = latestDate || getYesterdayStr()
    } catch {
        filterPriceDate.value = getYesterdayStr()
    }
    loadPricePage()
})

watch(activeTab, tab => {
    if (tab === 'category' && level1List.value.length > 0 && !selectedLevel1Id.value) {
        selectedLevel1Id.value = level1List.value[0]?.id
        loadLevel2(selectedLevel1Id.value)
    }
})

watch(filterLevel2Id, () => {
    filterLevel3Id.value = null
})
</script>

<style lang="scss" scoped>
.recycle-market-container {
    padding: 20px;
}

.toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
}

.w-48 {
    width: 180px;
}

.pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}

.category-layout {
    display: flex;
    gap: 24px;
}

.category-left,
.category-middle,
.category-right {
    flex: 1;
    min-width: 0;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-weight: 500;
}
</style>

<style lang="scss">
.recycle-market-dialog.el-dialog .el-dialog__body {
    padding-top: 24px;
}
</style>
