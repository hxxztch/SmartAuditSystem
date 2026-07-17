 <template>
   <div class="chart-gantt" ref="chartRef" :style="{ height, width }"></div>
 </template>
 
 <script setup>
 import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
 import * as echarts from 'echarts'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   title: { type: String, default: '' },
   height: { type: String, default: '400px' },
   width: { type: String, default: '100%' }
 })
 
 const emit = defineEmits(['chart-click'])
 const chartRef = ref(null)
 let chartInstance = null
 
 function renderChart() {
   if (!chartRef.value || !props.data?.length) return
   chartInstance = echarts.init(chartRef.value)
   const tasks = props.data.map(d => ({
     name: d.name,
     start: new Date(d.start).getTime(),
     end: new Date(d.end).getTime(),
     status: d.status || '进行中'
   }))
   const statusColors = { '已完成': '#67c23a', '进行中': '#409eff', '未开始': '#909399', '已逾期': '#f56c6c' }
   chartInstance.setOption({
     title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14 } } : undefined,
     tooltip: { formatter: p => `${p.name}<br/>${new Date(p.data[0]).toLocaleDateString()} - ${new Date(p.data[1]).toLocaleDateString()}` },
     grid: { left: '20%', right: '4%', bottom: '3%' },
     xAxis: { type: 'time', position: 'top' },
     yAxis: {
       type: 'category',
       data: tasks.map(t => t.name),
       axisLabel: { width: 100, overflow: 'truncate' }
     },
     series: [{
       type: 'bar',
       data: tasks.map(t => ({
         value: [t.start, t.end],
         name: t.name,
         itemStyle: { color: statusColors[t.status] || '#409eff', borderRadius: 4 }
       })),
       barWidth: 20
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
 .chart-gantt { width: 100%; }
 </style>



