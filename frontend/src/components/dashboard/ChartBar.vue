 <template>
   <div class="chart-bar" ref="chartRef" :style="{ height, width }"></div>
 </template>
 
 <script setup>
 import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
 import * as echarts from 'echarts'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   xField: { type: String, default: 'name' },
   yField: { type: String, default: 'value' },
   title: { type: String, default: '' },
   height: { type: String, default: '300px' },
   width: { type: String, default: '100%' },
   color: { type: Array, default: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'] }
 })
 
 const emit = defineEmits(['chart-click'])
 
 const chartRef = ref(null)
 let chartInstance = null
 
 function renderChart() {
   if (!chartRef.value || !props.data?.length) return
   chartInstance = echarts.init(chartRef.value)
   const xData = props.data.map(d => d[props.xField])
   const yData = props.data.map(d => d[props.yField])
   chartInstance.setOption({
     title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14 } } : undefined,
     tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
     grid: { left: 50, right: 20, top: 30, bottom: 30 },
     xAxis: { type: 'category', data: xData, axisLabel: { rotate: xData.length > 6 ? 30 : 0 } },
     yAxis: { type: 'value', axisLabel: { show: true }, name: '项目数', min: 0, minInterval: 1 },
     series: [{
       type: 'bar',
       data: yData,
       itemStyle: {
         borderRadius: [4, 4, 0, 0],
         color: params => props.color[params.dataIndex % props.color.length]
       },
       barMaxWidth: 50
     }]
   })
   chartInstance.on('click', params => emit('chart-click', { name: params.name, value: params.value, dataIndex: params.dataIndex }))
 }
 
 function resize() { chartInstance?.resize() }
 
 watch(() => props.data, () => nextTick(renderChart), { deep: true })
 
 onMounted(() => { nextTick(renderChart); window.addEventListener('resize', resize) })
 onBeforeUnmount(() => { window.removeEventListener('resize', resize); chartInstance?.dispose() })
 </script>
 
 <style lang="scss" scoped>
 .chart-bar { width: 100%; }
 </style>



