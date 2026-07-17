 <template>
   <DashboardLayout>
     <div class="category-page">
       <!-- 筛选栏 -->
       <el-card shadow="never" class="filter-card">
         <el-form :inline="true" size="small">
           <el-form-item label="项目类型">
             <el-select v-model="filters.type" placeholder="全部类型" clearable style="width: 160px;">
               <el-option label="经济责任审计" value="economy" />
               <el-option label="财务收支审计" value="finance" />
               <el-option label="专项审计" value="special" />
               <el-option label="工程审计" value="engineering" />
             </el-select>
           </el-form-item>
           <el-form-item label="实施状态">
             <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px;">
               <el-option label="已立项" value="planned" />
               <el-option label="实施中" value="ongoing" />
               <el-option label="征求意见" value="reviewing" />
               <el-option label="已完成" value="completed" />
             </el-select>
           </el-form-item>
           <el-form-item label="年度">
             <el-date-picker v-model="filters.year" type="year" placeholder="选择年度" value-format="YYYY" style="width: 140px;" />
           </el-form-item>
           <el-form-item>
             <el-button type="primary" @click="search">
               <el-icon><Search /></el-icon> 查询
             </el-button>
             <el-button @click="resetFilters">重置</el-button>
           </el-form-item>
         </el-form>
       </el-card>
 
       <!-- 分类统计图表 -->
       <el-row :gutter="16" class="chart-row">
         <el-col :xs="24" :md="12">
           <el-card shadow="never" class="chart-card">
             <template #header>各类型项目数量对比</template>
             <ChartBar
               :data="categoryChartData"
               x-field="name"
               y-field="value"
               height="300px"
               @chart-click="goToProject"
             />
           </el-card>
         </el-col>
         <el-col :xs="24" :md="12">
           <el-card shadow="never" class="chart-card">
             <template #header>各类型涉及金额</template>
             <ChartPie
               :data="amountChartData"
               name-field="name"
               value-field="value"
               height="300px"
               rose-type
               @chart-click="goToProject"
             />
           </el-card>
         </el-col>
       </el-row>
 
       <!-- 项目清单 -->
       <el-card shadow="never" class="list-card">
         <template #header>
           <span>项目清单（{{ totalProjects }}条）</span>
         </template>
         <DetailTable
           :columns="projectColumns"
           :data="projectList"
           @view="viewProject"
           @drill="drillToProject"
         />
       </el-card>
     </div>
   </DashboardLayout>
 </template>
 
 <script setup>
 import { ref, reactive, computed } from 'vue'
 import { useRouter } from 'vue-router'
 import DashboardLayout from '@/layout/DashboardLayout.vue'
 
 const router = useRouter()
 
 const filters = reactive({
   type: '',
   status: '',
   year: '2026'
 })
 
 const totalProjects = ref(42)
 
 const projectColumns = [
   { prop: 'code', label: '项目编号', width: 180 },
   { prop: 'name', label: '项目名称', minWidth: 200 },
   { prop: 'type', label: '项目类型', width: 120 },
   { prop: 'orgName', label: '被审计单位', width: 150 },
   { prop: 'leader', label: '审计组长', width: 100 },
   { prop: 'startDate', label: '开始日期', width: 110 },
   { prop: 'endDate', label: '结束日期', width: 110 },
   { prop: 'status', label: '状态', width: 100 }
 ]
 
 const projectList = ref([
   { code: 'SJ-2026-001', name: '2025年度财务收支审计', type: '财务收支审计', orgName: '计算机学院', leader: '张明', startDate: '2026-01-15', endDate: '2026-03-30', status: '已完成' },
   { code: 'SJ-2026-002', name: '李明同志经责审计', type: '经济责任审计', orgName: '经管学院', leader: '王芳', startDate: '2026-02-01', endDate: '2026-04-30', status: '实施中' },
   { code: 'SJ-2026-003', name: '科研经费专项审计', type: '专项审计', orgName: '科研处', leader: '赵强', startDate: '2026-03-01', endDate: '2026-05-15', status: '实施中' },
   { code: 'SJ-2026-004', name: '新建实验楼工程审计', type: '工程审计', orgName: '基建处', leader: '陈刚', startDate: '2026-02-20', endDate: '2026-06-30', status: '征求意见' },
   { code: 'SJ-2026-005', name: '医学院资产专项审计', type: '专项审计', orgName: '医学院', leader: '张明', startDate: '2026-04-01', endDate: '2026-06-30', status: '已立项' }
 ])
 
 const categoryChartData = computed(() => [
   { name: '经济责任审计', value: 45 },
   { name: '财务收支审计', value: 68 },
   { name: '专项审计', value: 32 },
   { name: '工程审计', value: 28 },
   { name: '其他', value: 13 }
 ])
 
 const amountChartData = computed(() => [
   { name: '经济责任审计', value: 3560 },
   { name: '财务收支审计', value: 12580 },
   { name: '专项审计', value: 6800 },
   { name: '工程审计', value: 15200 },
   { name: '其他', value: 960 }
 ])
 
 function search() { /* 触发查询 */ }
 function resetFilters() { filters.type = ''; filters.status = ''; filters.year = '2026' }
 
 function goToProject(params) {
   router.push({ name: 'DashboardCategory', query: { category: params.name } })
 }
 
 function viewProject(row) {
   router.push({ name: 'DashboardProject', params: { id: row.code } })
 }
 
 function drillToProject(row) {
   router.push({ name: 'DashboardProject', params: { id: row.code } })
 }
 </script>
 
 <style lang="scss" scoped>
 .category-page { display: flex; flex-direction: column; gap: 16px; }
 .filter-card { :deep(.el-card__body) { padding-bottom: 0; } }
 .chart-card { margin-bottom: 16px; }
 .list-card { :deep(.el-card__header) { font-weight: 600; } }
 </style>
