 <template>
   <div class="data-drill-down">
     <!-- 层级选择器 -->
     <div class="drill-level-selector">
       <el-steps :active="currentLevel" align-center finish-status="success" process-status="process" size="small">
         <el-step title="全局总览" />
         <el-step title="分类统计" />
         <el-step title="项目详情" />
         <el-step title="原始明细" />
       </el-steps>
     </div>
 
     <!-- 下钻路径参数 -->
     <div class="drill-params" v-if="drillParams && Object.keys(drillParams).length">
       <el-tag
         v-for="(val, key) in drillParams"
         :key="key"
         closable
         size="small"
         @close="clearDrillParam(key)"
       >
         {{ key }}: {{ val }}
       </el-tag>
     </div>
 
     <!-- 导航按钮 -->
     <div class="drill-actions">
       <el-button size="small" @click="goUp" :disabled="currentLevel <= 0">
         <el-icon><DArrowLeft /></el-icon> 上钻
       </el-button>
       <el-button size="small" type="primary" @click="goDown" :disabled="currentLevel >= 3">
         下钻 <el-icon><DArrowRight /></el-icon>
       </el-button>
       <el-button size="small" text @click="resetDrill" v-if="currentLevel > 0">
         <el-icon><RefreshLeft /></el-icon> 重置
       </el-button>
     </div>
 
     <!-- 插槽：当前层级内容 -->
     <div class="drill-content">
       <slot />
     </div>
   </div>
 </template>
 
 <script setup>
 import { computed } from 'vue'
 import { useRouter } from 'vue-router'
 import { useDashboardStore } from '@/store/modules/dashboard'
 import { DrillMeta } from '@/utils/drill-down'
 
 const router = useRouter()
 const dashboardStore = useDashboardStore()
 
 const currentLevel = computed(() => dashboardStore.drillLevel)
 const drillParams = computed(() => dashboardStore.drillParams)
 
 function goUp() {
   if (currentLevel.value > 0) {
     const next = currentLevel.value - 1
     dashboardStore.drillUp(next)
     const meta = DrillMeta[next]
     if (meta) router.push({ name: meta.route })
   }
 }
 
 function goDown() {
   if (currentLevel.value < 3) {
     const next = currentLevel.value + 1
     const meta = DrillMeta[next]
     if (meta) router.push({ name: meta.route })
   }
 }
 
 function resetDrill() {
   dashboardStore.resetDrillState()
   router.push({ name: 'DashboardOverview' })
 }
 
 function clearDrillParam(key) {
   const params = { ...drillParams.value }
   delete params[key]
   dashboardStore.drillDown(currentLevel.value, params)
 }
 </script>
 
 <style lang="scss" scoped>
 .data-drill-down {
   background: #fff;
   border-radius: 8px;
   padding: 16px;
   margin-bottom: 16px;
 }
 .drill-level-selector { margin-bottom: 12px; }
 .drill-params {
   display: flex;
   gap: 6px;
   flex-wrap: wrap;
   margin-bottom: 12px;
 }
 .drill-actions {
   display: flex;
   gap: 8px;
   margin-bottom: 16px;
 }
 </style>
