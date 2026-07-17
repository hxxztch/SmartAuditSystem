<template>
  <DashboardLayout>
    <div class="my-projects-page">
      <el-card shadow="never">
        <template #header><span>{{ isAdmin ? '所有项目' : '我的项目' }}（{{ projects.length }}条）</span></template>
        <el-table :data="projects" border stripe size="small" v-loading="loading">
          <el-table-column prop="code" label="项目编号" width="160" />
          <el-table-column label="项目名称" min-width="200">
            <template #default="{ row }">{{ nameLabel(row.name) }}</template>
          </el-table-column>
          <el-table-column label="项目类型" width="100">
            <template #default="{ row }">{{ typeLabel(row.type) }}</template>
          </el-table-column>
          <el-table-column label="被审计单位" width="140">
            <template #default="{ row }">{{ orgLabel(row.orgname) }}</template>
          </el-table-column>
          <el-table-column label="审计组长" width="100">
            <template #default="{ row }">{{ leaderLabel(row.leader) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" width="110">
            <template #default="{ row }">{{ row.startdate }}</template>
          </el-table-column>
          <el-table-column label="结束日期" width="110">
            <template #default="{ row }">{{ row.enddate }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" v-if="isAdmin">
            <template #default="{ row }">
              <el-button link size="small" @click="openAssign(row)"><el-icon><User /></el-icon>显示人员</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 分配人员对话框 -->
      <el-dialog v-model="dialogVisible" :title="'分配人员 - ' + (currentProject?.name||'')" width="650px" destroy-on-close>
        <div class="assign-toolbar">
          <el-input v-model="searchText" placeholder="搜索用户名/姓名" clearable size="small" style="width:220px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterRole" placeholder="按角色筛选" clearable size="small" style="width:160px">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
          <el-select v-model="filterOrg" placeholder="按单位筛选" clearable size="small" style="width:160px">
            <el-option v-for="o in orgOptions" :key="o" :label="orgNameCn(o)" :value="o" />
          </el-select>
        </div>
        <el-table ref="tableRef" :data="filteredUsers" max-height="400" size="small" @selection-change="onSelect">
          <el-table-column type="selection" width="45" :selectable="()=>!isReadonly" />
          <el-table-column prop="username" label="用户名" width="130" />
          <el-table-column prop="realName" label="姓名" width="90" />
          <el-table-column label="角色" min-width="140">
            <template #default="{ row }">
              <el-tag v-for="r in (row.roles||[])" :key="r" size="small" style="margin:1px">{{ roleLabel(r) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="130">
            <template #default="{ row }">{{ orgNameCn(row.orgName) }}</template>
          </el-table-column>
        </el-table>
        <template #footer>
          <el-button @click="dialogVisible=false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import DashboardLayout from '@/layout/DashboardLayout.vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const projects = ref([])
const loading = ref(false)
const isAdmin = computed(() => userStore.isAuditDirector || userStore.isSchoolLeader)
const isReadonly = ref(true)

// --- member assignment ---
const dialogVisible = ref(false)
const currentProject = ref(null)
const allUsers = ref([])
const selectedIds = ref([])
const tableRef = ref(null)
const saving = ref(false)
const searchText = ref('')
const filterRole = ref('')
const filterOrg = ref('')

const roleOptions = [
  {label:'校领导',value:'school_leader'},{label:'审计处长',value:'audit_director'},
  {label:'项目组长/主审',value:'audit_manager'},{label:'普通审计人员',value:'auditor'},
  {label:'被审计单位负责人',value:'auditee_head'},{label:'被审计单位联络员',value:'auditee_liaison'},
  {label:'中介审计机构人员',value:'intermediary'}
]

const orgOptions = ["CS College","EM College","Research Office","Infrastructure","Medical College","Law School","Physics Dept","Chemistry Dept","Library","Foreign Lang","PE Dept","Logistics","Audit","Office","ZhongShen","CS","EM"]

function orgNameCn(n) {
  const m = {"CS College":"计算机学院","EM College":"经管学院","Research Office":"科研处","Infrastructure":"基建处","Medical College":"医学院","Law School":"法学院","Physics Dept":"物理学院","Chemistry Dept":"化工学院","Library":"图书馆","Foreign Lang":"外语学院","PE Dept":"体育部","Logistics":"后勤集团","Audit":"审计处","Office":"校办","ZhongShen":"中审众环","CS":"计算机学院","EM":"经管学院","PE":"体育部"}
  return m[n]||n||"-"
}

const roleLabelMap = {school_leader:'校领导',audit_director:'审计处长',audit_manager:'项目组长/主审',auditor:'普通审计人员',auditee_head:'被审计单位负责人',auditee_liaison:'被审计单位联络员',intermediary:'中介审计机构人员'}

function roleLabel(code) {
  return roleLabelMap[code] || code || '-'
}

const filteredUsers = computed(() => {
  let list = allUsers.value
  if (searchText.value) {
    const kw = searchText.value.toLowerCase()
    list = list.filter(u => u.username.toLowerCase().includes(kw) || (u.realName||'').toLowerCase().includes(kw))
  }
  if (filterRole.value) {
    list = list.filter(u => (u.roles||[]).includes(filterRole.value))
  }
  if (filterOrg.value) {
    list = list.filter(u => u.orgName === filterOrg.value)
  }
  return list
})

function onSelect(selection) {
  selectedIds.value = selection.map(u => u.userId)
}

async function openAssign(project) {
  currentProject.value = project
  dialogVisible.value = true
  selectedIds.value = []
  searchText.value = ''
  filterRole.value = ''
  filterOrg.value = ''
  try {
    const [usersRes, membersRes] = await Promise.all([
      request({url:'/user-mgmt/users',method:'get'}),
      request({url:'/dashboard/project/'+project.pid+'/members',method:'get'})
    ])
    allUsers.value = Array.isArray(usersRes) ? usersRes : []
    const memberIds = Array.isArray(membersRes) ? membersRes : []
    selectedIds.value = [...memberIds]
    await nextTick()
    if (tableRef.value) {
      allUsers.value.forEach(u => {
        if (memberIds.includes(u.userId)) tableRef.value.toggleRowSelection(u, true)
      })
    }
  } catch(e) {
    ElMessage.error('加载用户列表失败')
  }
}

async function saveAssign() {
  saving.value = true
  try {
    await request({
      url:'/dashboard/project/'+currentProject.value.pid+'/members',
      method:'post',
      data:{userIds:selectedIds.value}
    })
    ElMessage.success('人员分配已保存')
    dialogVisible.value = false
  } catch(e) {
    ElMessage.error('保存失败')
  }
  saving.value = false
}

// --- labels ---
const nameMap = {
  "2025 Finance Audit":"2025年度财务收支审计","Li Ming Economy Audit":"李明同志经责审计","Research Fund Audit":"科研经费专项审计",
  "Lab Building Audit":"新建实验楼工程审计","Medical College Assets":"医学院资产专项审计","Law School Finance":"法学院财务收支审计",
  "Physics Economy Audit":"物理学院经责审计","Chemical Special Audit":"化工学院专项审计","Library Renovation":"图书馆改造工程审计",
  "Foreign Lang Finance":"外语学院财务收支审计","PE Dept Economy":"体育部经责审计","Logistics Special Audit":"后勤集团专项审计"
}
function nameLabel(n) { return nameMap[n]||n||'-' }
function statusLabel(s) {
  if (s==='COMPLETED') return '已完成'; if (s==='ONGOING') return '实施中'
  if (s==='REVIEW') return '征求意见'; if (s==='PLANNED') return '已立项'
  return s||'-'
}
function statusType(s) {
  if (s==='COMPLETED') return 'success'; if (s==='ONGOING') return 'warning'
  if (s==='REVIEW') return 'info'; if (s==='PLANNED') return ''
  return 'info'
}
function orgLabel(s) { const m={"CS College":"计算机学院","EM College":"经管学院","Research Office":"科研处","Infrastructure":"基建处","Medical College":"医学院","Law School":"法学院","Physics Dept":"物理学院","Chemistry Dept":"化工学院","Library":"图书馆","Foreign Lang":"外语学院","PE Dept":"体育部","Logistics":"后勤集团"}; return m[s]||s||"-" }
function leaderLabel(s) { const m={"Zhang Ming":"张明","Wang Fang":"王芳","Zhao Qiang":"赵强"}; return m[s]||s||"-" }
function typeLabel(s) { if (s==='FINANCE') return '财务收支'; if (s==='ECONOMY') return '经济责任'; if (s==='SPECIAL') return '专项'; if (s==='ENGINEERING') return '工程'; return s||'-' }

onMounted(async () => {
  loading.value = true
  try { const res = await request({url:'/dashboard/my-projects',method:'get'}); projects.value = res||[] } catch(e) {}
  loading.value = false
})
</script>

<style lang="scss" scoped>
.my-projects-page{display:flex;flex-direction:column;gap:16px}
.assign-toolbar{display:flex;gap:12px;margin-bottom:12px;flex-wrap:wrap}
</style>

