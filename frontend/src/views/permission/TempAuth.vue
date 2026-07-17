<template>
  <div class="temp-auth">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span>{{ isApprovalMode ? '申请批复' : '申请通道' }}</span>
          <el-button v-if="!isApprovalMode" type="primary" size="small" @click="openDialog"><el-icon><Plus /></el-icon>新建申请</el-button>
        </div>
      </template>
      <div style="margin-bottom:12px;display:flex;gap:8px">
        <el-input v-model="search" placeholder="搜索" clearable size="small" style="width:200px"/>
        <el-select v-if="isApprovalMode" v-model="statusFilter" placeholder="状态" clearable size="small" style="width:120px">
          <el-option label="全部" value=""/><el-option label="待审批" value="PENDING"/><el-option label="已同意" value="APPROVED"/><el-option label="已拒绝" value="REJECTED"/>
        </el-select>
      </div>
      <el-table :data="filteredList" border stripe size="small">
        <el-table-column prop="APPLICANT_NAME" label="申请人" width="100" v-if="isApprovalMode"/>
        <el-table-column prop="TYPE" label="类型" width="100"><template #default="{row}"><el-tag size="small">{{ typeLabel(row.TYPE) }}</el-tag></template></el-table-column>
        <el-table-column prop="PROJECT_NAME" label="关联项目" width="150"/>
        <el-table-column label="申请理由" width="100"><template #default="{row}"><el-button link size="small" @click="showDetail(row.DESCRIPTION||'(暂无)')">查看</el-button></template></el-table-column>
        <el-table-column label="金额" width="100"><template #default="{row}">{{ row.AMOUNT ? ((row.AMOUNT/10000).toFixed(2)+'万') : '-' }}</template></el-table-column>
        <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.STATUS==='APPROVED'?'success':row.STATUS==='REJECTED'?'danger':'warning'" size="small">{{ row.STATUS==='PENDING'?'待审批':row.STATUS==='APPROVED'?'已同意':'已拒绝' }}</el-tag></template></el-table-column>
        <el-table-column label="意见" width="100"><template #default="{row}"><el-button link size="small" @click="showDetail(row.COMMENTS||'(暂无)')" >查看</el-button></template></el-table-column>
        <el-table-column label="时间" width="140"><template #default="{row}">{{ fmtTime(row.CREATED_AT) }}</template></el-table-column>
        <el-table-column label="操作" width="80" v-if="!isApprovalMode"><template #default="{row}"><el-button v-if="row.STATUS==='PENDING'" link size="small" style="color:#f56c6c" @click="cancelReq(row.ID)">撤回</el-button></template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right" v-if="isApprovalMode"><template #default="{row}"><el-button v-if="row.STATUS==='PENDING'" link size="small" type="success" @click="approve(row.ID,'approve')">同意</el-button><el-button v-if="row.STATUS==='PENDING'" link size="small" style="color:#f56c6c" @click="approve(row.ID,'reject')">拒绝</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建申请" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="项目访问" value="PROJECT_ACCESS"/><el-option label="经费申请" value="FUNDING"/></el-select></el-form-item>
        <el-form-item label="项目"><el-select v-model="form.projectName" filterable placeholder="选择项目" style="width:100%"><el-option v-for="p in projectList" :key="p.code" :label="p.code+' '+p.name" :value="p.name"/></el-select></el-form-item>
        <el-form-item label="金额" v-if="form.type==='FUNDING'"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" placeholder="万元"/></el-form-item>
        <el-form-item label="审批人"><el-select v-model="form.approverId" filterable placeholder="选择审批人" style="width:100%"><el-option v-for="a in approverList" :key="a.userId" :label="a.realName||a.username" :value="a.userId"/></el-select></el-form-item>
        <el-form-item label="申请理由"><el-input v-model="form.description" type="textarea" rows="3" maxlength="200" show-word-limit/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm" :loading="saving">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="commentVisible" :title="commentAction==='approve'?'同意申请':'拒绝申请'" width="400px">
      <el-form :model="commentForm" label-width="80px" size="small">
        <el-form-item label="意见"><el-input v-model="commentForm.text" type="textarea" rows="2" maxlength="100" show-word-limit placeholder="选填"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="commentVisible=false">取消</el-button><el-button :type="commentAction==='approve'?'success':'danger'" @click="doComment">确认</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="查看详情" width="500px">
      <div style="white-space:pre-wrap;word-break:break-word;max-height:400px;overflow-y:auto">{{ detailText }}</div>
      <template #footer><el-button @click="detailVisible=false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref,computed,onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {useRoute} from 'vue-router'
import {useUserStore} from '@/store/modules/user'
import request from '@/utils/request'
const route=useRoute()
const userStore=useUserStore()
const isApprovalMode=computed(()=>route.path.includes('/permission/'))
const list=ref([]);const projectList=ref([])
const search=ref('');const statusFilter=ref('')
const dialogVisible=ref(false);const saving=ref(false)
const form=ref({type:'PROJECT_ACCESS',projectName:'',description:'',amount:0,approverId:null})
const approverList=ref([])
const commentVisible=ref(false);const commentAction=ref('approve');const commentTarget=ref(null)
const commentForm=ref({text:''})
const detailVisible=ref(false);const detailText=ref('')
const filteredList=computed(()=>{let l=list.value;if(search.value){const kw=search.value.toLowerCase();l=l.filter(r=>(r.APPLICANT_NAME||'').toLowerCase().includes(kw)||(r.DESCRIPTION||'').toLowerCase().includes(kw)||(r.PROJECT_NAME||'').toLowerCase().includes(kw))}if(isApprovalMode.value&&statusFilter.value)l=l.filter(r=>r.STATUS===statusFilter.value);return l})
function typeLabel(t){const m={PROJECT_ACCESS:'项目访问',FUNDING:'经费申请',DATA_ACCESS:'数据权限'};return m[t]||t}
function fmtTime(t){if(!t)return'-';return (''+t).substring(0,16).replace('T',' ')}
function showDetail(text){detailText.value=text||'';detailVisible.value=true}
async function loadAll(){try{if(isApprovalMode.value)list.value=await request({url:'/auth-request/approval-history',method:'get'})||[];else list.value=await request({url:'/auth-request/my-requests',method:'get'})||[]}catch(e){}}
async function loadProjects(){try{projectList.value=await request({url:'/dashboard/my-projects',method:'get'})||[]}catch(e){}}
async function loadApprovers(){try{const u=await request({url:'/user-mgmt/users',method:'get'});approverList.value=(u||[]).filter(x=>(x.roles||[]).some(r=>['audit_director','school_leader','audit_manager'].includes(r)))}catch(e){}}
function openDialog(){form.value={type:'PROJECT_ACCESS',projectName:'',description:'',amount:0,approverId:null};dialogVisible.value=true}
async function submitForm(){if(!form.value.projectName){ElMessage.warning('请选择关联项目');return};if(!form.value.approverId){ElMessage.warning('请选择审批人');return};saving.value=true;try{await request({url:'/auth-request',method:'post',data:{...form.value,amount:form.value.amount*10000}});dialogVisible.value=false;ElMessage.success('已提交');loadAll()}catch(e){ElMessage.error('提交失败')};saving.value=false}
function approve(id,action){commentTarget.value=id;commentAction.value=action;commentForm.value.text='';commentVisible.value=true}
async function doComment(){try{await request({url:'/auth-request/'+commentTarget.value+'/'+commentAction.value,method:'put',data:{comment:commentForm.value.text}});commentVisible.value=false;ElMessage.success('已处理');loadAll()}catch(e){ElMessage.error('操作失败')}}
async function cancelReq(id){try{await request({url:'/auth-request/'+id,method:'delete'});ElMessage.success('已撤回');loadAll()}catch(e){ElMessage.error('撤回失败')}}
onMounted(()=>{loadAll();loadProjects();loadApprovers()})
</script>
<style lang="scss" scoped>.temp-auth{display:flex;flex-direction:column;gap:16px}.page-header{display:flex;justify-content:space-between;align-items:center}</style>

