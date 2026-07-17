 <template>
   <div class="chart-line" ref="chartRef" :style="{ height, width }"></div>
 </template>
 
 <script setup>
 import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
 import * as echarts from 'echarts'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   xField: { type: String, default: 'date' },
   yField: { type: String, default: 'value' },
   seriesField: { type: String, default: '' },
   title: { type: String, default: '' },
   height: { type: String, default: '300px' },
   width: { type: String, default: '100%' },
   smooth: { type: Boolean, default: true }
 })
 
 const emit = defineEmits(['chart-click'])
 const chartRef = ref(null)
 let chartInstance = null
 
 function renderChart() {
   if (!chartRef.value || !props.data?.length) return
   chartInstance = echarts.init(chartRef.value)
   const xData = [...new Set(props.data.map(d => d[props.xField]))]
   let series = []
   if (props.seriesField) {
     const groups = [...new Set(props.data.map(d => d[props.seriesField]))]
     series = groups.map(name => ({
       name,
       type: 'line',
       smooth: props.smooth,
       data: xData.map(x => props.data.find(d => d[props.xField] === x && d[props.seriesField] === name)?.[props.yField] || 0)
     }))
   } else {
     series = [{
       type: 'line',
       smooth: props.smooth,
       data: props.data.map(d => d[props.yField]),
       areaStyle: { opacity: 0.15 },
       lineStyle: { width: 2 }
     }]
   }
   chartInstance.setOption({
     title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14 } } : undefined,
     tooltip: { trigger: 'axis' },
     legend: props.seriesField ? { bottom: 0 } : undefined,
     grid: { left: 50, right: 20, top: 30, bottom: 30 },
     xAxis: { type: 'category', data: xData, boundaryGap: false },
     yAxis: { type: 'value', axisLabel: { show: true }, name: '项目数', min: 0, minInterval: 1 },
     series
   })
   chartInstance.on('click', params => emit('chart-click', params))
 }
 
 function resize() { chartInstance?.resize() }
 
 watch(() => props.data, () => nextTick(renderChart), { deep: true })
 onMounted(() => { nextTick(renderChart); window.addEventListener('resize', resize) })
 onBeforeUnmount(() => { window.removeEventListener('resize', resize); chartInstance?.dispose() })
 </script>
 
 <style lang="scss" scoped>
 .chart-line { width: 100%; }
 </style>



