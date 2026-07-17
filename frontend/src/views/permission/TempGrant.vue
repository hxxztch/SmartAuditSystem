<template>
  <div class="temp-grant">
    <el-card shadow="never">
      <template #header><div class="page-header"><span>临时授权</span><el-button type="primary" size="small" @click="openAdd"><el-icon><Plus /></el-icon>新增授权</el-button></div></template>
      <div style="margin-bottom:12px"><el-input v-model="search" placeholder="搜索用户/项目" clearable size="small" style="width:220px" /></div>
      <el-table :data="filteredList" border stripe size="small">
        <el-table-column prop="REAL_NAME" label="用户" width="120" />
        <el-table-column label="类型" width="100"><template #default="{row}"><el-tag size="small">{{ row.TYPE==='PROJECT_ACCESS'?'项目访问':'数据权限' }}</el-tag></template></el-table-column>
        <el-table-column prop="TARGET_NAME" label="授权内容" min-width="200" />
        <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.STATUS==='ACTIVE'?'success':'info'" size="small">{{ row.STATUS==='ACTIVE'?'生效中':'已撤销' }}</el-tag></template></el-table-column>
        <el-table-column prop="GRANTED_AT" label="授权时间" width="160">
          <template #default="{row}">{{ (row.GRANTED_AT||'').toString().substring(0,16).replace('T',' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{row}"><el-button v-if="row.STATUS==='ACTIVE'" link size="small" style="color:#f56c6c" @click="revoke(row.ID)">撤销</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" title="新增临时授权" width="500px" destroy-on-close>
      <el-form :model="f" label-width="80px" size="small">
        <el-form-item label="用户"><el-select v-model="f.userId" filterable placeholder="选择用户" style="width:100%"><el-option v-for="u in users" :key="u.userId" :label="(u.realName||u.username)+' ('+(u.orgName||'')+')'" :value="u.userId" /></el-select></el-form-item>
        <el-form-item label="类型"><el-select v-model="f.type" style="width:100%"><el-option label="项目访问" value="PROJECT_ACCESS" /><el-option label="数据权限" value="DATA_PERM" /></el-select></el-form-item>
        <el-form-item :label="f.type==='PROJECT_ACCESS'?'项目':'权限'" v-if="f.type">
          <el-select v-if="f.type==='PROJECT_ACCESS'" v-model="f.targetId" filterable placeholder="选择项目" style="width:100%"><el-option v-for="p in projects" :key="p.pid" :label="p.code+' '+p.name" :value="p.pid" /></el-select>
          <el-select v-else v-model="f.targetId" filterable placeholder="选择权限" style="width:100%"><el-option label="全量项目" value="data:project:all" /><el-option label="原始明细" value="dashboard:source" /><el-option label="布局自定义" value="dashboard:customize" /><el-option label="项目管理" value="perm:project" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="doGrant" :loading="saving">授权</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref,computed,onMounted} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import request from '@/utils/request'
const list=ref([]);const users=ref([]);const projects=ref([])
const search=ref('');const dlg=ref(false);const saving=ref(false)
const f=ref({userId:null,type:'PROJECT_ACCESS',targetId:null,targetName:''})
const filteredList=computed(()=>{if(!search.value)return list.value;const kw=search.value.toLowerCase();return list.value.filter(r=>(r.REAL_NAME||'').toLowerCase().includes(kw)||(r.TARGET_NAME||'').toLowerCase().includes(kw))})
async function loadAll(){try{list.value=await request({url:'/temp-grant/list',method:'get'})||[]}catch(e){}}
async function loadUsers(){try{users.value=await request({url:'/user-mgmt/users',method:'get'})||[]}catch(e){}}
async function loadProjects(){try{projects.value=await request({url:'/dashboard/my-projects',method:'get'})||[]}catch(e){}}
function openAdd(){f.value={userId:null,type:'PROJECT_ACCESS',targetId:null,targetName:''};dlg.value=true}
async function doGrant(){
  if(!f.value.userId||!f.value.targetId){ElMessage.warning('请完整填写');return}
  saving.value=true
  try{
    let targetName=''
    if(f.value.type==='PROJECT_ACCESS'){const p=projects.value.find(x=>x.pid==f.value.targetId);targetName=p?p.name:''}
    else targetName=f.value.targetId
    await request({url:'/temp-grant',method:'post',data:{...f.value,targetName}})
    dlg.value=false;ElMessage.success('已授权');loadAll()
  }catch(e){ElMessage.error('授权失败')}
  saving.value=false
}
async function revoke(id){ElMessageBox.confirm('确认撤销此授权？').then(async()=>{try{await request({url:'/temp-grant/'+id,method:'delete'});ElMessage.success('已撤销');loadAll()}catch(e){ElMessage.error('撤销失败')}}).catch(()=>{})}
onMounted(()=>{loadAll();loadUsers();loadProjects()})
</script>
<style lang="scss" scoped>.temp-grant{display:flex;flex-direction:column;gap:16px}.page-header{display:flex;justify-content:space-between;align-items:center}</style>


