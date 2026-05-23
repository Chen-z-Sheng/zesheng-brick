<template>
  <div ref="chartRef" class="vab-chart" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import echarts from "@/plugins/echarts-core";

const props = defineProps({
  option: {
    type: Object,
    default: () => ({}),
  },
  autoresize: {
    type: Boolean,
    default: true,
  },
});

const chartRef = ref(null);
let chartInstance = null;
let resizeObserver = null;

function renderChart() {
  if (!chartRef.value) {
    return;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  chartInstance.setOption(props.option || {}, true);
}

onMounted(() => {
  renderChart();
  if (props.autoresize && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      chartInstance?.resize();
    });
    resizeObserver.observe(chartRef.value);
  }
});

watch(
  () => props.option,
  () => {
    renderChart();
  },
  { deep: true }
);

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
  chartInstance?.dispose();
  chartInstance = null;
});
</script>

<style scoped>
.vab-chart {
  width: 100%;
  height: 100%;
  min-height: 120px;
}
</style>
