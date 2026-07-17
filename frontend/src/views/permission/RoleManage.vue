<template>
  <div class="role-manage">
    <el-card shadow="never">
      <template #header><div class="page-header"><span>角色管理</span>
        <el-button type="primary" size="small" @click="openDialog()"><el-icon><Plus /></el-icon>新增角色</el-button></div></template>
      <el-table :data="roleList" border stripe size="small" v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="角色编码" width="180" />
        <el-table-column label="角色名称" width="140"><template #default="{ row }">{{ cnName(row.name) }}</template></el-table-column>
        <el-table-column label="角色层级"><template #default="{ row }"><el-tag size="small" :type="row.level==='high'?'danger':row.level==='mid'?'warning':'info'">{{ row.level==='high'?'高级':row.level==='mid'?'中级':'基础' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }"><el-button link size="small" @click="openDialog(row)">编辑</el-button><el-button link size="small" style="color:#f56c6c" @click="deleteRole(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editRole?'编辑角色':'新增角色'" width="550px" destroy-on-close @closed="editRole=null">
      <el-form :model="roleForm" label-width="80px" size="small">
        <el-form-item label="角色编码"><el-input v-model="roleForm.code" placeholder="如: auditor" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="roleForm.name" placeholder="如: 审计人员" /></el-form-item>
        <el-form-item label="角色层级"><el-radio-group v-model="roleForm.level"><el-radio label="high">高级</el-radio><el-radio label="mid">中级</el-radio><el-radio label="basic">基础</el-radio></el-radio-group></el-form-item>
        <el-form-item label="权限分配"><el-tree ref="permTreeRef" :data="permTreeData" :props="{children:'children',label:'label'}" show-checkbox node-key="code" default-expand-all /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveRole" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'

const userStore = useUserStore()
const dialogVisible = ref(false); const editRole = ref(null); const permTreeRef = ref(null); const saving = ref(false); const loading = ref(false)
const roleForm = reactive({ code:'',name:'',level:'mid' })
const roleList = ref([])

const nameMap = { SchoolLeader:'校领导',AuditDirector:'审计处长',AuditManager:'项目组长/主审',Auditor:'普通审计人员',AuditeeHead:'被审计单位负责人',AuditeeLiaison:'被审计单位联络员',Intermediary:'中介审计机构人员' }
function cnName(n) { return nameMap[n] || n || '-' }
const permTreeData = ref([
  { code:'dashboard',label:'数据驾驶舱',children:[{code:'dashboard:view',label:'查看驾驶舱'},{code:'dashboard:source',label:'原始明细'},{code:'dashboard:customize',label:'布局自定义'}]},
  { code:'perm',label:'权限管理',children:[{code:'perm:role',label:'角色管理'},{code:'perm:user',label:'用户权限分配'},{code:'perm:data',label:'数据权限'},{code:'perm:temp_auth',label:'临时授权'},{code:'perm:project',label:'项目管理'}]},
  { code:'data',label:'数据权限',children:[{code:'data:financial',label:'财务明细'},{code:'data:project:all',label:'全量项目'},{code:'data:project:own',label:'本人项目'},{code:'data:project:dept',label:'本单位项目'}]}
])

async function loadRoles() {
  loading.value = true
  try { const res = await request({url:'/user-mgmt/roles',method:'get'}); roleList.value = Array.isArray(res) ? res : [] }
  catch(e) { ElMessage.error('加载角色失败') }
  loading.value = false
}

async function openDialog(role) {
  editRole.value = role || null
  if (role) { roleForm.code=role.code; roleForm.name=role.name; roleForm.level=role.level }
  else { roleForm.code=''; roleForm.name=''; roleForm.level='mid' }
  dialogVisible.value = true
  await nextTick()
  if (permTreeRef.value) permTreeRef.value.setCheckedKeys(role?.perms||[])
}

async function saveRole() {
  saving.value = true
  const keys = permTreeRef.value?.getCheckedKeys(true)||[]
  const data = { code:roleForm.code, name:roleForm.name, level:roleForm.level, perms:keys }
  if (editRole.value) data.id = editRole.value.id
  try {
    await request({url:'/user-mgmt/role',method:'post',data})
    if (editRole.value) { editRole.value.perms=keys; editRole.value.name=roleForm.name; editRole.value.level=roleForm.level; if (userStore.roles.includes(editRole.value.code)) userStore.permissions=keys }
    ElMessage.success('已保存')
    dialogVisible.value = false
    window.dispatchEvent(new Event('roles-updated'))
    await loadRoles()
  } catch(e) { ElMessage.error('保存失败：'+(e.message||'未知错误')) }
  saving.value = false
}

function deleteRole(row) {
  ElMessageBox.confirm('删除角色'+row.name+'？').then(async ()=>{
    try { await request({url:'/user-mgmt/role/'+row.id,method:'delete'}); window.dispatchEvent(new Event('roles-updated')); await loadRoles(); ElMessage.success('已删除') }
    catch(e) { ElMessage.error('删除失败') }
  }).catch(()=>{})
}

onMounted(loadRoles)
</script>

<style lang="scss" scoped>.page-header{display:flex;justify-content:space-between;align-items:center}</style>

