 <template>
   <div class="chart-heatmap" ref="chartRef" :style="{ height, width }"></div>
 </template>
 
 <script setup>
 import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
 import * as echarts from 'echarts'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   xData: { type: Array, default: () => [] },
   yData: { type: Array, default: () => [] },
   title: { type: String, default: '' },
   height: { type: String, default: '300px' },
   width: { type: String, default: '100%' }
 })
 
 const emit = defineEmits(['chart-click'])
 const chartRef = ref(null)
 let chartInstance = null
 
 function renderChart() {
   if (!chartRef.value || !props.data?.length) return
   chartInstance = echarts.init(chartRef.value)
   chartInstance.setOption({
     title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14 } } : undefined,
     tooltip: { position: 'top', formatter: p => `${p.value[0]}, ${p.value[1]}: ${p.value[2]}` },
     grid: { left: '8%', right: '4%', bottom: '10%', containLabel: true },
     xAxis: { type: 'category', data: props.xData, splitArea: { show: true } },
     yAxis: { type: 'category', data: props.yData, splitArea: { show: true } },
     visualMap: { min: 0, max: Math.max(...props.data.map(d => d[2]), 100), calculable: true, inRange: { color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695'] } },
     series: [{
       type: 'heatmap',
       data: props.data,
       label: { show: props.data.length < 50 },
       emphasis: { itemStyle: { shadowBlur: 10 } }
     }]
   })
   chartInstance.on('click', params => emit('chart-click', params))
 }
 
 function resize() { chartInstance?.resize() }
 
 watch(() => props.data, () => nextTick(renderChart), { deep: true })
 onMounted(() => { nextTick(renderChart); window.addEventListener('resize', resize) })
 onBeforeUnmount(() => { window.removeEventListener('resize', resize); chartInstance?.dispose() })
 </script>
 
 <style lang="scss" scoped>
 .chart-heatmap { width: 100%; }
 </style>
