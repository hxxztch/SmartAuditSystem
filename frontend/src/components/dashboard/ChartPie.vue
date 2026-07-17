 <template>
   <div class="chart-pie" ref="chartRef" :style="{ height, width }"></div>
 </template>
 
 <script setup>
 import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
 import * as echarts from 'echarts'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   nameField: { type: String, default: 'name' },
   valueField: { type: String, default: 'value' },
   title: { type: String, default: '' },
   height: { type: String, default: '300px' },
   width: { type: String, default: '100%' },
   roseType: { type: Boolean, default: false },
   donut: { type: Boolean, default: false }
 })
 
 const emit = defineEmits(['chart-click'])
 const chartRef = ref(null)
 let chartInstance = null
 
 function renderChart() {
   if (!chartRef.value || !props.data?.length) return
   chartInstance = echarts.init(chartRef.value)
   chartInstance.setOption({
     title: props.title ? { text: props.title, left: 'center', textStyle: { fontSize: 14 } } : undefined,
     tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
     legend: { bottom: 0, type: 'scroll' },
     series: [{
       type: 'pie',
       radius: props.donut ? ['35%', '55%'] : '55%',
       roseType: props.roseType ? 'area' : undefined,
       data: props.data.map(d => ({ name: d[props.nameField], value: d[props.valueField] })),
       emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' } },
       label: { formatter: '{b}\n{d}%' }
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
 .chart-pie { width: 100%; }
 </style>
