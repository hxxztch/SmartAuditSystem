<template>
  <DashboardLayout>
    <div class="customize-page">
      <div class="customize-toolbar">
        <el-button type="primary" @click="saveLayout" :loading="saving"><el-icon><Check /></el-icon>保存布局</el-button>
        <el-button @click="resetDefault">恢复默认</el-button>
        <el-button @click="saveAsView">另存为个人视图</el-button>
        <el-switch v-model="isEditing" active-text="编辑模式" inactive-text="预览模式" style="margin-left:16px" />
      </div>
      <WidgetContainer v-model:widgets="widgets" :editable="isEditing">
        <template #data-board><DataBoard :items="mockBoardData" /></template>
        <template #chart-pie><ChartPie :data="mockPieData" height="260px" /></template>
        <template #chart-bar><ChartBar :data="mockBarData" height="260px" /></template>
        <template #chart-line><ChartLine :key="mockLineData.length" :data="mockLineData" height="260px" /></template>
        <template #chart-heatmap><ChartHeatmap :data="mockHeatData" :x-data="mockHeatX" :y-data="mockHeatY" height="260px" /></template>
        <template #chart-gantt><div style="padding:8px;overflow-y:auto"><div v-for="p in mockGanttData" :key="p.name" style="display:flex;justify-content:space-between;padding:5px 0;border-bottom:1px solid #ebeef5;font-size:12px"><span style="font-weight:600">{{ p.name }}</span><span style="color:#909399">{{ p.start }} ~ {{ p.end }}</span><el-tag size="small">{{ p.status }}</el-tag></div></div></template>
        <template #detail-table><DetailTable v-if="tableReady" :columns="mockTableColumns" :data="mockTableData" show-pagination :total="mockTableData.length" /></template>
      </WidgetContainer>
    </div>
    <el-dialog v-model="viewDialogVisible" title="保存为个人视图" width="400px">
      <el-form :model="viewForm" label-width="80px"><el-form-item label="视图名称"><el-input v-model="viewForm.name" /></el-form-item></el-form>
      <template #footer><el-button @click="viewDialogVisible=false">取消</el-button><el-button type="primary" @click="confirmSaveView">保存</el-button></template>
    </el-dialog>
  </DashboardLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import DashboardLayout from '@/layout/DashboardLayout.vue'
import WidgetContainer from '@/components/dashboard/WidgetContainer.vue'
import ChartBar from '@/components/dashboard/ChartBar.vue'
import ChartLine from '@/components/dashboard/ChartLine.vue'
import ChartPie from '@/components/dashboard/ChartPie.vue'
import ChartHeatmap from '@/components/dashboard/ChartHeatmap.vue'
import ChartGantt from '@/components/dashboard/ChartGantt.vue'
import DataBoard from '@/components/dashboard/DataBoard.vue'
import DetailTable from '@/components/dashboard/DetailTable.vue'

const userStore = useUserStore()
const LAYOUT_KEY = () => 'shenji_dashboard_v2_' + (userStore.userId || userStore.username || 'fallback')
const DEFAULT_WIDGETS = [
  { id:'w1',type:'data-board',title:'数据看板',cols:3,rows:1 },
  { id:'w2',type:'chart-pie',title:'饼图（项目分布）',cols:1,rows:1 },
  { id:'w3',type:'chart-bar',title:'柱状图（进度对比）',cols:1,rows:1 },
  { id:'w4',type:'chart-line',title:'折线图（月度趋势）',cols:1,rows:1 },
  { id:'w5',type:'detail-table',title:'明细表格',cols:2,rows:1 },
  { id:'w6',type:'chart-heatmap',title:'热力图',cols:2,rows:1 },
  { id:'w7',type:'chart-gantt',title:'甘特图',cols:1,rows:1 }
]
const isEditing = ref(true); const tableReady = ref(false); const saving = ref(false); const viewDialogVisible = ref(false)
const viewForm = reactive({ name: '' })
const widgets = ref(JSON.parse(JSON.stringify(DEFAULT_WIDGETS)))

const mockBoardData = ref([{key:'total',label:'项目总数',value:0},{key:'ongoing',label:'实施中',value:0},{key:'completed',label:'已完成',value:0},{key:'amount',label:'涉及金额(万)',value:0}])
const mockBarData = ref([{name:'已立项',value:0},{name:'实施中',value:0},{name:'已完成',value:0},{name:'征求意见',value:0}])
const mockPieData = ref([{name:'财务收支',value:0},{name:'经济责任',value:0},{name:'专项',value:0},{name:'工程',value:0}])
const mockLineData = ref([{date:'1月',value:0},{date:'2月',value:0},{date:'3月',value:0},{date:'4月',value:0},{date:'5月',value:0},{date:'6月',value:0}])
const mockHeatData = ref([[0,0,3],[1,0,3],[0,1,4],[1,1,2]])
const mockHeatX = ref(['财务收支','经济责任'])
const mockHeatY = ref(['计算机学院','经管学院'])
const mockGanttData = ref([{name:'SJ-2026-001',start:'2026-01-15',end:'2026-03-30',status:'已完成'},{name:'SJ-2026-002',start:'2026-02-01',end:'2026-04-30',status:'实施中'}])
const mockTableColumns = [{prop:'code',label:'项目编号'},{prop:'name',label:'项目名称'},{prop:'type',label:'项目类型'},{prop:'orgName',label:'被审计单位'},{prop:'status',label:'状态'}]
const mockTableData = ref([])
const _old = [{code:'SJ-2026-001',name:'2025年度财务收支审计',type:'财务收支',orgName:'计算机学院',status:'已完成'},{code:'SJ-2026-002',name:'李明同志经责审计',type:'经济责任',orgName:'经管学院',status:'实施中'},{code:'SJ-2026-003',name:'科研经费专项审计',type:'专项',orgName:'科研处',status:'实施中'},{code:'SJ-2026-004',name:'新建实验楼工程审计',type:'工程',orgName:'基建处',status:'征求意见'},{code:'SJ-2026-005',name:'医学院资产专项审计',type:'专项',orgName:'医学院',status:'已立项'},{code:'SJ-2026-006',name:'法学院财务收支审计',type:'财务收支',orgName:'法学院',status:'已完成'},{code:'SJ-2026-007',name:'物理学院经责审计',type:'经济责任',orgName:'物理学院',status:'已完成'},{code:'SJ-2026-008',name:'化工学院专项审计',type:'专项',orgName:'化工学院',status:'实施中'},{code:'SJ-2026-009',name:'图书馆改造工程审计',type:'工程',orgName:'图书馆',status:'已立项'},{code:'SJ-2026-010',name:'外语学院财务收支审计',type:'财务收支',orgName:'外语学院',status:'已完成'},{code:'SJ-2026-011',name:'体育部经责审计',type:'经济责任',orgName:'体育部',status:'实施中'},{code:'SJ-2026-012',name:'后勤集团专项审计',type:'专项',orgName:'后勤集团',status:'征求意见'}]

onMounted(async () => { tableReady.value = false
  const saved = localStorage.getItem(LAYOUT_KEY())
  if (saved) { try { widgets.value = JSON.parse(saved) } catch(e) {} }
  try {
    const res = await request({url:'/dashboard/overview',method:'get'})
    const d = res || {}
    mockBoardData.value = [
      {key:'total',label:'项目总数',value:d.totalProjects||0},
      {key:'ongoing',label:'实施中',value:d.ongoingProjects||0},
      {key:'completed',label:'已完成',value:d.completedProjects||0},
      {key:'amount',label:'涉及金额(万)',value:((d.totalAmountInvolved||0)/10000).toFixed(0)}
    ]
    if (d.projectStatusDist) {
      const names = {COMPLETED:'已完成',ONGOING:'实施中',REVIEW:'征求意见',PLANNED:'已立项'}
      mockBarData.value = d.projectStatusDist.map(s => ({name:names[s.name]||s.name,value:s.value}))
    }
    if (d.projectTypeDist) {
      const tns = {FINANCE:'财务收支',ECONOMY:'经济责任',SPECIAL:'专项',ENGINEERING:'工程'}
      mockPieData.value = d.projectTypeDist.map(t => ({name:tns[t.name]||t.name,value:t.value}))
    }
  } catch(e) {}
  try {
    const src = await request({url:'/dashboard/source-data',method:'get'})
    if (src && src.length) {
      mockTableData.value = src.map(s => ({code:s.code,name:s.name,type:s.type,orgName:s.orgname,status:s.status}))
      const orgs = [...new Set(src.map(s => s.orgname))]
      const types = [...new Set(src.map(s => s.type))]
      const orgCn = {'CS College':'计算机学院','EM College':'经管学院','Research Office':'科研处','Infrastructure':'基建处','Medical College':'医学院','Law School':'法学院','Physics Dept':'物理学院','Chemistry Dept':'化工学院','Library':'图书馆','Foreign Lang':'外语学院','PE Dept':'体育部','Logistics':'后勤集团'}
      const typeCn = {FINANCE:'财务收支',ECONOMY:'经济责任',SPECIAL:'专项',ENGINEERING:'工程'}
      mockHeatX.value = types.map(t => typeCn[t]||t)
      mockHeatY.value = orgs.map(o => orgCn[o]||o)
      const heat = []
      orgs.forEach((o, yi) => types.forEach((t, xi) => { const cnt = src.filter(s => s.orgname===o && s.type===t).length; if (cnt) heat.push([xi, yi, cnt]) }))
      mockHeatData.value = heat.length ? heat : [[0,0,0]]
      const months = {}
      src.forEach(s => { const m = (s.startdate || '').substring(0, 7); if (m) months[m] = (months[m] || 0) + 1 })
      mockLineData.value = Object.entries(months).sort().map(([k, v]) => ({date: k, value: v}))
      const statusNames = {COMPLETED:'已完成',ONGOING:'实施中',REVIEW:'征求意见',PLANNED:'已立项'}
      mockGanttData.value = src.map(s => ({name: s.code, start: s.startdate, end: s.enddate || s.startdate, status: statusNames[s.status] || s.status}))
      
    }
  } catch(e) {}
  tableReady.value = true
})

function saveLayout() {
  saving.value = true
  localStorage.setItem(LAYOUT_KEY(), JSON.stringify(widgets.value))
  setTimeout(() => { saving.value = false; ElMessage.success('布局已保存') }, 300)
}
function resetDefault() {
  ElMessageBox.confirm('将恢复默认布局，确认？').then(() => {
    widgets.value = JSON.parse(JSON.stringify(DEFAULT_WIDGETS))
    localStorage.removeItem(LAYOUT_KEY())
    ElMessage.success('已恢复默认布局')
  }).catch(() => {})
}
function saveAsView() { viewDialogVisible.value = true }
function confirmSaveView() {
  viewDialogVisible.value = false
  localStorage.setItem(LAYOUT_KEY() + '_view_' + viewForm.name, JSON.stringify(widgets.value))
  ElMessage.success('已保存为个人视图：' + viewForm.name)
}
</script>

<style lang="scss" scoped>
.customize-page{display:flex;flex-direction:column;gap:12px}
.customize-toolbar{display:flex;gap:8px;align-items:center;flex-wrap:wrap;background:#fff;padding:12px 16px;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
</style>











