<template>
  <div class="user-permission">
    <el-card shadow="never">
      <template #header><div class="page-header"><span>用户权限分配</span>
        <el-button type="primary" size="small" @click="openCreate"><el-icon><Plus /></el-icon>新增用户</el-button></div></template>
      <div style="margin-bottom:12px"><el-input v-model="searchKw" placeholder="搜索用户名/姓名" clearable size="small" style="width:240px"><template #prefix><el-icon><Search /></el-icon></template></el-input></div>
      <el-table :data="filteredUsers" border stripe size="small" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="所属单位" width="240">
          <template #default="{ row }"><span>{{ orgNameCn(row.orgName) }}</span><el-button link size="small" style="margin-left:8px" @click="editOrg(row)">修改</el-button></template>
        </el-table-column>
        <el-table-column label="当前角色" width="250">
          <template #default="{ row }"><el-tag v-for="r in (row.roles||[])" :key="r" size="small" style="margin:2px">{{ roleLabelMap[r]||r }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="editUser(row)">编辑</el-button>
            <el-button link size="small" @click="assignRole(row)">分配角色</el-button>
            <el-button link size="small" @click="openPwd(row)">重置密码</el-button>
            <el-button link size="small" style="color:#f56c6c" @click="delUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="roleDialogVisible" :title="'分配角色 - '+(targetUser?.realName||'')" width="500px">
      <el-checkbox-group v-model="selectedRoles" style="display:flex;flex-wrap:wrap;gap:8px">
        <el-checkbox v-for="r in allRoles" :key="r.code" :label="r.code" border>{{ cnRoleName(r.name||r.code) }}</el-checkbox>
      </el-checkbox-group>
      <template #footer><el-button @click="roleDialogVisible=false">取消</el-button><el-button type="primary" @click="saveRoleAssign" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="orgVisible" title="修改单位" width="400px">
      <el-select v-model="newOrg" filterable placeholder="选择单位" style="width:100%">
        <el-option v-for="o in orgList" :key="o" :label="orgNameCn(o)" :value="o" />
      </el-select>
      <template #footer><el-button @click="orgVisible=false">取消</el-button><el-button type="primary" @click="saveOrg">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑用户" width="420px" destroy-on-close>
      <el-form :model="editForm" label-width="70px" size="small">
        <el-form-item label="用户名"><el-input v-model="editForm.username" maxlength="32" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="editForm.realName" maxlength="32" /></el-form-item><el-form-item label="密码"><el-input v-model="editForm.password" type="password" maxlength="32" placeholder="输入新密码" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" @click="doEdit" :loading="editSaving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="createVisible" title="新增用户" width="420px" destroy-on-close>
      <el-form :model="createForm" label-width="70px" size="small">
        <el-form-item label="用户名"><el-input v-model="createForm.username" maxlength="32" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="createForm.password" type="password" maxlength="32" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="createForm.realName" maxlength="32" /></el-form-item>
        <el-form-item label="单位"><el-select v-model="createForm.orgName" filterable style="width:100%"><el-option v-for="o in orgList" :key="o" :label="orgNameCn(o)" :value="o" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible=false">取消</el-button><el-button type="primary" @click="doCreate" :loading="creating">创建</el-button></template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="重置密码" width="350px">
      <el-form :model="pwdForm" label-width="70px" size="small">
        <el-form-item label="用户"><span>{{ pwdTarget?.realName||pwdTarget?.username }}</span></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.password" type="password" maxlength="32" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pwdVisible=false">取消</el-button><el-button type="primary" @click="doResetPwd" :loading="pwdSaving">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'

const userStore = useUserStore()
const userList = ref([]); const loading = ref(false); const saving = ref(false); const searchKw = ref('')
const roleDialogVisible = ref(false); const targetUser = ref(null); const selectedRoles = ref([])
const allRoles = ref([]); const orgVisible = ref(false); const newOrg = ref(""); const orgTarget = ref(null)
const editVisible = ref(false); const editTarget = ref(null); const editSaving = ref(false); const editForm = ref({username:"",realName:"",orgName:"",password:""})
const createVisible = ref(false); const creating = ref(false); const createForm = ref({username:'',password:'',realName:'',orgName:'Audit'})
const pwdVisible = ref(false); const pwdTarget = ref(null); const pwdSaving = ref(false); const pwdForm = ref({password:''})

const filteredUsers = computed(() => {
  if(!searchKw.value) return userList.value
  const kw = searchKw.value.toLowerCase()
  return userList.value.filter(u => u.username.toLowerCase().includes(kw) || (u.realName||'').toLowerCase().includes(kw))
})

const roleLabelMap = { school_leader:'校领导',audit_director:'审计处长',audit_manager:'项目组长/主审',auditor:'普通审计人员',auditee_head:'被审计单位负责人',auditee_liaison:'被审计单位联络员',intermediary:'中介审计机构人员' }
const roleNameMap2 = { SchoolLeader:'校领导',AuditDirector:'审计处长',AuditManager:'项目组长/主审',Auditor:'普通审计人员',AuditeeHead:'被审计单位负责人',AuditeeLiaison:'被审计单位联络员',Intermediary:'中介审计机构人员' }
const orgNameMap = { "CS College":"计算机学院","EM College":"经管学院","Research Office":"科研处","Infrastructure":"基建处","Medical College":"医学院","Law School":"法学院","Physics Dept":"物理学院","Chemistry Dept":"化工学院","Library":"图书馆","Foreign Lang":"外语学院","PE Dept":"体育部","Logistics":"后勤集团","Audit":"审计处","Office":"校办","ZhongShen":"中审众环","ScienceEngDept":"理工处" }
const orgList = ref(["Audit","Office","ZhongShen","CS College","EM College","Research Office","Infrastructure","Medical College","Law School","Physics Dept","Chemistry Dept","Library","Foreign Lang","PE Dept","Logistics"])

function cnRoleName(n) { return roleNameMap2[n] || roleLabelMap[n] || n || '-' }
function orgNameCn(n) { return orgNameMap[n] || n || "-" }

async function refreshRoles() {
  try { const res = await request({url:'/user-mgmt/roles',method:'get'}); allRoles.value = Array.isArray(res)&&res.length>0 ? res.map(r=>({code:r.code,name:r.name})) : [{code:'school_leader',name:'校领导'},{code:'audit_director',name:'审计处长'},{code:'audit_manager',name:'项目组长/主审'},{code:'auditor',name:'普通审计人员'},{code:'auditee_head',name:'被审计单位负责人'},{code:'auditee_liaison',name:'被审计单位联络员'},{code:'intermediary',name:'中介审计机构人员'}] }
  catch(e) {}
}
window.addEventListener('roles-updated', refreshRoles)

async function fetchUsers() { loading.value=true; try{const r=await request({url:'/user-mgmt/users',method:'get'});userList.value=Array.isArray(r)?r:[]}catch(e){ElMessage.error('获取用户列表失败')};loading.value=false }
onMounted(()=>{refreshRoles();fetchUsers()})

function assignRole(user) { targetUser.value=user; selectedRoles.value=[...(user.roles||[])]; refreshRoles(); roleDialogVisible.value=true }
async function saveRoleAssign() { if(!targetUser.value)return; saving.value=true; try{await request({url:'/user-mgmt/user/'+targetUser.value.userId+'/roles',method:'put',data:{roleCodes:selectedRoles.value}});targetUser.value.roles=[...selectedRoles.value];if(targetUser.value.username===userStore.username)userStore.roles=[...selectedRoles.value];roleDialogVisible.value=false;ElMessage.success('角色已更新')}catch(e){ElMessage.error('保存失败')};saving.value=false }

function editOrg(user) { orgTarget.value=user; newOrg.value=user.orgName||""; orgVisible.value=true }
async function saveOrg() { if(!orgTarget.value)return; try{await request({url:'/user-mgmt/user/'+orgTarget.value.userId+'/org',method:'put',data:{orgName:newOrg.value}});orgTarget.value.orgName=newOrg.value;orgVisible.value=false;ElMessage.success('单位已更新')}catch(e){ElMessage.error('更新失败')} }

function editUser(u) { editTarget.value=u; editForm.value={username:u.username,realName:u.realName||'',orgName:u.orgName||'',password:u.password||''}; editVisible.value=true }
async function doEdit() { if(!editTarget.value)return; editSaving.value=true; try{await request({url:'/user-mgmt/user/'+editTarget.value.userId+'/profile',method:'put',data:{username:editForm.value.username,realName:editForm.value.realName,orgName:editForm.value.orgName,password:editForm.value.password||null}});editTarget.value.realName=editForm.value.realName;editTarget.value.orgName=editForm.value.orgName;editTarget.value.username=editForm.value.username;editVisible.value=false;ElMessage.success('已更新')}catch(e){ElMessage.error('更新失败')};editSaving.value=false }

function openCreate() { createForm.value = {username:'',password:'',realName:'',orgName:'Audit'}; createVisible.value = true }
async function doCreate() { creating.value=true; try{await request({url:'/user-mgmt/user',method:'post',data:createForm.value});createVisible.value=false;ElMessage.success('用户已创建');await fetchUsers()}catch(e){ElMessage.error('创建失败，用户名可能重复')};creating.value=false }

function openPwd(user) { pwdTarget.value=user; pwdForm.value.password=''; pwdVisible.value=true }
async function doResetPwd() { if(!pwdTarget.value)return; pwdSaving.value=true; try{await request({url:'/user-mgmt/user/'+pwdTarget.value.userId+'/password',method:'put',data:{password:pwdForm.value.password}});pwdVisible.value=false;ElMessage.success('密码已重置')}catch(e){ElMessage.error('重置失败')};pwdSaving.value=false }

function delUser(row) {
  ElMessageBox.confirm('删除用户 '+row.realName+'？此操作不可恢复。','确认删除',{type:'warning'}).then(async ()=>{
    try { await request({url:'/user-mgmt/user/'+row.userId,method:'delete'}); ElMessage.success('已删除'); await fetchUsers() }
    catch(e) { ElMessage.error('删除失败') }
  }).catch(()=>{})
}
</script>

<style lang="scss" scoped>.page-header{display:flex;justify-content:space-between;align-items:center}</style>











