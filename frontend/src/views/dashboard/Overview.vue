<template>
  <DashboardLayout>
    <div class="overview-page">
      <div class="welcome-banner">
        <h2>{{ userStore.realName }}，欢迎回来</h2>
        <p>当前角色：{{ roleLabel }} | 所属单位：{{ orgLabel }}</p>
      </div>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6" v-for="card in cards" :key="card.key">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            <div class="stat-sub">{{ card.sub }}</div>
          </el-card>
        </el-col>
      </el-row>
      <div class="perm-info">
        <h3>功能权限列表（{{ userStore.permissions.length }}项）</h3>
        <el-tag v-for="p in userStore.permissions" :key="p" size="small" style="margin:4px 4px 4px 0">{{ p }}</el-tag>
      </div>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import DashboardLayout from '@/layout/DashboardLayout.vue'

const userStore = useUserStore()
const overviewData = ref(null)

onMounted(async () => {
  try { const res = await request({ url: '/dashboard/overview', method: 'get' }); overviewData.value = res } catch(e) {}
})

const roleLabels = { school_leader:'校领导',audit_director:'审计处长',audit_manager:'项目组长/主审',auditor:'普通审计人员',auditee_head:'被审计单位负责人',auditee_liaison:'被审计单位联络员',intermediary:'中介审计机构人员' }
const roleLabel = computed(() => roleLabels[userStore.roles[0]] || userStore.roles[0] || '未知')

const orgLabelMap = { CS:'计算机学院','CS College':'计算机学院',EM:'经管学院','EM College':'经管学院',Audit:'审计处',Office:'校办',ZhongShen:'中审众环',ScienceEngDept:'理工处' }
const orgLabel = computed(() => orgLabelMap[userStore.orgName] || userStore.orgName || '-')

const cards = computed(() => {
  const d = overviewData.value
  if (!d) return []
  return [
    { key:'projects',label:'审计项目总数',value:d.totalProjects||0,color:'#409eff',sub:'进行中：'+(d.ongoingProjects||0) },
    { key:'ongoing',label:'进行中项目',value:d.ongoingProjects||0,color:'#e6a23c',sub:'已完成：'+(d.completedProjects||0) },
    { key:'completed',label:'已完成项目',value:d.completedProjects||0,color:'#67c23a' },
    { key:'issues',label:'发现问题数',value:d.totalIssues||0,color:'#f56c6c',sub:'已整改：'+(d.rectifiedIssues||0) }
  ]
})
</script>

<style lang="scss" scoped>
.overview-page{display:flex;flex-direction:column;gap:16px}
.welcome-banner{background:linear-gradient(135deg,#409eff,#67c23a);color:#fff;padding:24px;border-radius:8px}
.welcome-banner h2{margin:0 0 8px;font-size:20px}.welcome-banner p{margin:0;font-size:13px;opacity:.85}
.stat-card{text-align:center;padding:8px}.stat-label{font-size:12px;color:#909399;margin-bottom:8px}
.stat-value{font-size:28px;font-weight:700}.stat-sub{font-size:12px;color:#909399;margin-top:4px;min-height:18px}
.perm-info{background:#fff;padding:16px;border-radius:8px}.perm-info h3{font-size:14px;margin:0 0 8px;color:#303133}
</style>
