<template>
  <el-card shadow="hover">
    <template #header>
      <vab-icon icon="send-plane-2-line" />
      <!-- 计划 -->
      <el-tag class="card-header-tag" type="success">里程碑与进度（示例数据）</el-tag>
    </template>
    <el-table :data="tableData" height="283px" row-key="title">
      <el-table-column align="center" label="拖拽" width="50px">
        <template #default="{}">
          <vab-icon :icon="['fas', 'arrows-alt']" style="cursor: pointer" />
        </template>
      </el-table-column>
      <el-table-column width="20px" />
      <el-table-column label="目标" prop="title" width="230px" />
      <el-table-column label="进度" width="220px">
        <template #default="{ row }">
          <el-progress :color="row.color" :percentage="row.percentage" />
        </template>
      </el-table-column>
      <el-table-column width="50px" />
      <el-table-column label="完成时间" prop="endTIme" />
    </el-table>
  </el-card>
</template>
<script>
export default {
  data() {
    return {
      tableData: [
        {
          title: "核心业务流程联调",
          endTIme: "2026-06-30",
          percentage: 45,
          color: "#95de64",
        },
        {
          title: "权限与菜单配置完善",
          endTIme: "2026-05-31",
          percentage: 70,
          color: "#69c0ff",
        },
        {
          title: "报表与导出功能",
          endTIme: "2026-07-15",
          percentage: 30,
          color: "#409EFF",
        },
        {
          title: "移动端适配验收",
          endTIme: "2026-08-01",
          percentage: 15,
          color: "#ffc069",
        },
        {
          title: "性能与体验优化",
          endTIme: "2026-09-30",
          percentage: 40,
          color: "#5cdbd3",
        },
        {
          title: "上线前安全与审计",
          endTIme: "2026-10-15",
          percentage: 10,
          color: "#b37feb",
        },
      ],
    };
  },
};
</script>
