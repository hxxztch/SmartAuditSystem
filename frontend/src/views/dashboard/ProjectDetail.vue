 <template>
   <DashboardLayout>
     <div class="project-page" v-loading="loading">
       <!-- 项目基础信息 -->
       <el-card shadow="never" class="project-info">
         <template #header>
           <div class="project-header">
             <span class="project-title">{{ project.code }} - {{ project.name }}</span>
             <div class="project-header-actions">
               <el-tag v-if="project.status" :type="statusTypeMap[project.status]" effect="dark">
                 {{ project.status }}
               </el-tag>
               <AuthWrapper :permission="'dashboard:export'">
                 <el-button size="small" @click="exportData">
                   <el-icon><Download /></el-icon> 导出
                 </el-button>
               </AuthWrapper>
               <el-button size="small" type="primary" @click="drillToSource">
                 穿透查看明细 <el-icon><DArrowRight /></el-icon>
               </el-button>
             </div>
           </div>
         </template>
         <el-descriptions :column="4" border size="small">
           <el-descriptions-item label="项目类型">{{ project.type }}</el-descriptions-item>
           <el-descriptions-item label="被审计单位">{{ project.orgName }}</el-descriptions-item>
           <el-descriptions-item label="审计组长">{{ project.leader }}</el-descriptions-item>
           <el-descriptions-item label="覆盖期间">{{ project.period }}</el-descriptions-item>
           <el-descriptions-item label="开始日期">{{ project.startDate }}</el-descriptions-item>
           <el-descriptions-item label="结束日期">{{ project.endDate }}</el-descriptions-item>
           <el-descriptions-item label="涉及金额">{{ project.amount | formatMoney }}</el-descriptions-item>
           <el-descriptions-item label="发现问题数">{{ project.problemCount }}</el-descriptions-item>
         </el-descriptions>
       </el-card>
 
       <!-- 项目统计图表 -->
       <el-row :gutter="16" class="chart-row">
         <el-col :xs="24" :md="8">
           <el-card shadow="never" class="chart-card">
             <template #header>项目资金构成</template>
             <ChartPie :data="fundData" name-field="name" value-field="value" height="260px" donut />
           </el-card>
         </el-col>
         <el-col :xs="24" :md="16">
           <el-card shadow="never" class="chart-card">
             <template #header>各月审计工作量</template>
             <ChartLine :data="workloadData" x-field="date" y-field="value" height="260px" />
           </el-card>
         </el-col>
       </el-row>
 
       <!-- 问题清单 -->
       <el-card shadow="never" class="list-card">
         <template #header>发现问题清单（{{ problems.length }}条）</template>
         <DetailTable
           :columns="problemColumns"
           :data="problems"
           show-actions
           @view="onViewProblem"
           @drill="onDrillProblem"
         />
       </el-card>
 
       <!-- 底稿清单 -->
       <el-card shadow="never" class="list-card">
         <template #header>审计底稿</template>
         <DetailTable
           :columns="paperColumns"
           :data="papers"
           show-actions
           @view="onViewPaper"
         />
       </el-card>
     </div>
   </DashboardLayout>
 </template>
 
 <script setup>
 import { ref, onMounted } from 'vue'
 import { useRoute, useRouter } from 'vue-router'
 import DashboardLayout from '@/layout/DashboardLayout.vue'
 
 const route = useRoute()
 const router = useRouter()
 const loading = ref(false)
 
 const statusTypeMap = { '已完成': 'success', '实施中': 'warning', '征求意见': '', '已立项': 'info', '已归档': '' }
 
 const project = ref({
   code: 'SJ-2026-001',
   name: '2025年度财务收支审计',
   type: '财务收支审计',
   orgName: '计算机学院',
   leader: '张明',
   period: '2025.01 - 2025.12',
   startDate: '2026-01-15',
   endDate: '2026-03-30',
   amount: 25800000,
   problemCount: 23,
   status: '已完成'
 })
 
 const problemColumns = [
   { prop: 'id', label: '编号', width: 100 },
   { prop: 'category', label: '问题类别', width: 100 },
   { prop: 'description', label: '问题描述', minWidth: 300 },
   { prop: 'amount', label: '涉及金额（万元）', width: 140 },
   { prop: 'level', label: '风险等级', width: 90 },
   { prop: 'status', label: '整改状态', width: 90 }
 ]
 
 const problems = ref([
   { id: 'WT-001', category: '采购', description: '采购项目未按规定进行公开招标', amount: 320, level: '高', status: '整改中' },
   { id: 'WT-002', category: '财务', description: '专项经费未专款专用，挤占挪用', amount: 156, level: '高', status: '已完成' },
   { id: 'WT-003', category: '资产', description: '固定资产未及时入账，账实不符', amount: 88, level: '中', status: '已完成' }
 ])
 
 const paperColumns = [
   { prop: 'code', label: '底稿编号', width: 140 },
   { prop: 'title', label: '底稿名称', minWidth: 250 },
   { prop: 'author', label: '编制人', width: 100 },
   { prop: 'date', label: '编制日期', width: 110 },
   { prop: 'reviewStatus', label: '复核状态', width: 100 }
 ]
 
 const papers = ref([
   { code: 'DG-2026-001', title: '采购业务审计工作底稿', author: '李四', date: '2026-02-15', reviewStatus: '已复核' },
   { code: 'DG-2026-002', title: '预算执行审计工作底稿', author: '王五', date: '2026-02-20', reviewStatus: '待复核' }
 ])
 
 const fundData = [
   { name: '财政拨款', value: 12800 },
   { name: '科研经费', value: 6500 },
   { name: '自筹资金', value: 3800 },
   { name: '专项经费', value: 2700 }
 ]
 
 const workloadData = [
   { date: '1月', value: 3 }, { date: '2月', value: 8 }, { date: '3月', value: 12 },
   { date: '4月', value: 6 }, { date: '5月', value: 4 }, { date: '6月', value: 2 }
 ]
 
 function drillToSource() {
   router.push({ name: 'DashboardSource', query: { projectId: project.value.code } })
 }
 
 function onViewProblem(row) { /* 查看问题详情 */ }
 function onDrillProblem(row) { router.push({ name: 'DashboardSource', query: { problemId: row.id, projectId: project.value.code } }) }
 function onViewPaper(row) { /* 查看底稿内容 */ }
 function exportData() { ElMessage.success('数据导出中...') }
 
 onMounted(() => { loading.value = false })
 </script>
 
 <style lang="scss" scoped>
 .project-page { display: flex; flex-direction: column; gap: 16px; }
 .project-header { display: flex; justify-content: space-between; align-items: center; }
 .project-title { font-size: 15px; font-weight: 600; }
 .project-header-actions { display: flex; gap: 8px; align-items: center; }
 .chart-card { margin-bottom: 16px; }
 .list-card { :deep(.el-card__header) { font-weight: 600; } }
 </style>
