<template>
  <div class="project-manage">
    <el-card shadow="never">
      <template #header>
        <div class="page-header"><span>项目管理</span>
          <el-button v-if="!isManager" type="primary" size="small" @click="openAdd"><el-icon><Plus /></el-icon>新增项目</el-button>
        </div>
      </template>
      <el-table :data="projects" border stripe size="small" v-loading="loading">
        <el-table-column prop="code" label="编号" width="140" />
        <el-table-column prop="name" label="名称" min-width="200">
          <template #default="{ row }">{{ nameLabel(row.name) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column label="单位" width="130">
          <template #default="{ row }">{{ orgLabel(row.orgname) }}</template>
        </el-table-column>
        <el-table-column prop="leader" label="组长" width="80">
          <template #default="{ row }">{{ leaderLabel(row.leader) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="金额(万)" width="100">
          <template #default="{ row }">{{ ((row.amount||0)/10000).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="startdate" label="开始" width="100" />
        <el-table-column prop="enddate" label="结束" width="100" />
        <el-table-column label="成员" width="60">
          <template #default="{ row }"><el-tag size="small" round>{{ row.memberCount||0 }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="!isManager" link size="small" style="color:#f56c6c" @click="delProject(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="editVisible" :title="isAdd?'新增项目':'编辑项目'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="small">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="编号"><el-input v-model="form.code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称"><el-input v-model="form.name" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="类型">
              <el-select v-model="form.type" style="width:100%">
                <el-option label="财务收支" value="FINANCE" /><el-option label="经济责任" value="ECONOMY" />
                <el-option label="专项" value="SPECIAL" /><el-option label="工程" value="ENGINEERING" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-select v-model="form.org_name" filterable style="width:100%">
                <el-option v-for="o in orgList" :key="o" :label="orgLabel(o)" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="已立项" value="PLANNED" /><el-option label="实施中" value="ONGOING" />
                <el-option label="征求意见" value="REVIEW" /><el-option label="已完成" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="组长"><el-input v-model="form.leader_name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="金额(万)"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开始日期"><el-input v-model="form.start_date" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="结束日期"><el-input v-model="form.end_date" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">分配人员</el-divider>
        <div class="assign-bar">
          <el-input v-model="memberSearch" placeholder="搜索用户" clearable size="small" style="width:200px" />
          <el-button size="small" @click="selectAll">全选</el-button>
          <el-button size="small" @click="deselectAll">清空</el-button>
          <span style="margin-left:8px;color:#909399">已选 {{ form.userIds.length }} 人</span>
        </div>
        <el-checkbox-group v-model="form.userIds" style="margin-top:8px">
          <el-checkbox v-for="u in filteredMemberUsers" :key="u.userId" :label="u.userId" border size="small" style="margin:3px">
            {{ u.realName||u.username }} <span style="color:#909399;font-size:11px">{{ orgLabel(u.orgName) }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="saveProject" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const projects = ref([]); const loading = ref(false); const saving = ref(false)
const isManager = computed(() => userStore.roles.includes('audit_manager') && !userStore.isAuditDirector && !userStore.isSchoolLeader)
const editVisible = ref(false); const isAdd = ref(true)
const form = ref({code:'',name:'',type:'FINANCE',org_name:'',leader_name:'',status:'PLANNED',amount:0,start_date:'',end_date:'',userIds:[]})
const editPid = ref(null)
const allUsers = ref([]); const memberSearch = ref('')

const orgList = ["CS College","EM College","Research Office","Infrastructure","Medical College","Law School","Physics Dept","Chemistry Dept","Library","Foreign Lang","PE Dept","Logistics"]

const filteredMemberUsers = computed(() => {
  if (!memberSearch.value) return allUsers.value
  const kw = memberSearch.value.toLowerCase()
  return allUsers.value.filter(u => u.username.toLowerCase().includes(kw) || (u.realName||'').toLowerCase().includes(kw))
})

function selectAll() { form.value.userIds = allUsers.value.map(u => u.userId) }
function deselectAll() { form.value.userIds = [] }

async function loadProjects() {
  loading.value = true
  try {
    const res = await request({url:'/dashboard/my-projects',method:'get'})
    projects.value = (res||[]).map(p => ({...p}))
    // Load member counts
    for (const p of projects.value) {
      try {
        const m = await request({url:'/dashboard/project/'+p.pid+'/members',method:'get'})
        p.memberCount = Array.isArray(m) ? m.length : 0
      } catch(e) { p.memberCount = 0 }
    }
  } catch(e) { ElMessage.error('加载失败') }
  loading.value = false
}

async function loadUsers() {
  try {
    const r = await request({url:'/user-mgmt/users',method:'get'});
    allUsers.value = (r||[]).filter(u => {
      if (!isManager.value) return true;
      return !(u.roles||[]).some(r => r==='school_leader'||r==='audit_director');
    });
  } catch(e) {}
}

function openAdd() {
  isAdd.value = true; editPid.value = null
  form.value = {code:'',name:'',type:'FINANCE',org_name:'CS College',leader_name:'',status:'PLANNED',amount:0,start_date:'',end_date:'',userIds:[]}
  editVisible.value = true; memberSearch.value = ''
}

async function openEdit(row) {
  isAdd.value = false; editPid.value = row.pid
  form.value = {
    code:row.code, name:row.name, type:row.type, org_name:row.orgname,
    leader_name:row.leader, status:row.status, amount:(row.amount||0)/10000,
    start_date:row.startdate||'', end_date:row.enddate||'', userIds:[]
  }
  editVisible.value = true; memberSearch.value = ''
  try {
    const m = await request({url:'/dashboard/project/'+row.pid+'/members',method:'get'})
    form.value.userIds = Array.isArray(m) ? [...m] : []
  } catch(e) {}
}

async function saveProject() {
  saving.value = true
  const data = {
    code:form.value.code, name:form.value.name, type:form.value.type,
    org_name:form.value.org_name, leader_name:form.value.leader_name,
    status:form.value.status, amount:form.value.amount*10000,
    start_date:form.value.start_date, end_date:form.value.end_date,
    userIds:form.value.userIds
  }
  try {
    if (isAdd.value) {
      await request({url:'/dashboard/project',method:'post',data})
      ElMessage.success('项目已创建')
    } else {
      await request({url:'/dashboard/project/'+editPid.value,method:'put',data})
      ElMessage.success('项目已更新')
    }
    editVisible.value = false
    await loadProjects()
  } catch(e) { ElMessage.error('保存失败') }
  saving.value = false
}

function delProject(row) {
  ElMessageBox.confirm('删除项目 '+row.name+'？此操作不可恢复。', '确认删除', {type:'warning'}).then(async ()=>{
    try {
      await request({url:'/dashboard/project/'+row.pid,method:'delete'})
      ElMessage.success('已删除')
      await loadProjects()
    } catch(e) { ElMessage.error('删除失败') }
  }).catch(()=>{})
}

function orgLabel(s) { const m={"CS College":"计算机学院","EM College":"经管学院","Research Office":"科研处","Infrastructure":"基建处","Medical College":"医学院","Law School":"法学院","Physics Dept":"物理学院","Chemistry Dept":"化工学院","Library":"图书馆","Foreign Lang":"外语学院","PE Dept":"体育部","Logistics":"后勤集团","Audit":"审计处","Office":"校办","ZhongShen":"中审众环","CS":"计算机学院","EM":"经管学院","PE":"体育部"}; return m[s]||s||"-" }
function statusLabel(s) { if (s==='COMPLETED') return '已完成'; if (s==='ONGOING') return '实施中'; if (s==='REVIEW') return '征求意见'; if (s==='PLANNED') return '已立项'; return s||'-' }
function statusType(s) { if (s==='COMPLETED') return 'success'; if (s==='ONGOING') return 'warning'; if (s==='REVIEW') return 'info'; return '' }
function nameLabel(n) { const m={"2025 Finance Audit":"2025年度财务收支审计","Li Ming Economy Audit":"李明同志经责审计","Research Fund Audit":"科研经费专项审计","Lab Building Audit":"新建实验楼工程审计","Medical College Assets":"医学院资产专项审计","Law School Finance":"法学院财务收支审计","Physics Economy Audit":"物理学院经责审计","Chemical Special Audit":"化工学院专项审计","Library Renovation":"图书馆改造工程审计","Foreign Lang Finance":"外语学院财务收支审计","PE Dept Economy":"体育部经责审计","Logistics Special Audit":"后勤集团专项审计"}; return m[n]||n||'-' }
function leaderLabel(s) { const m={"Zhang Ming":"张明","Wang Fang":"王芳","Zhao Qiang":"赵强"}; return m[s]||s||"-" }
function typeLabel(s) { if (s==='FINANCE') return '财务收支'; if (s==='ECONOMY') return '经济责任'; if (s==='SPECIAL') return '专项'; if (s==='ENGINEERING') return '工程'; return s||'-' }

onMounted(()=>{loadProjects();loadUsers()})
</script>

<style lang="scss" scoped>
.project-manage{display:flex;flex-direction:column;gap:16px}
.page-header{display:flex;justify-content:space-between;align-items:center}
.assign-bar{display:flex;gap:8px;align-items:center}
</style>


