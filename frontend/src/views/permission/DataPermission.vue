<template>
  <div class="data-permission">
    <el-card shadow="never">
      <template #header><div class="page-header"><span>数据权限配置</span></div></template>
      <el-alert title="数据权限控制说明" type="info" :closable="false" show-icon style="margin-bottom:16px">
        <ul style="margin:4px 0;padding-left:20px;line-height:1.8">
          <li><strong>全校</strong>：查看所有项目数据</li>
          <li><strong>本单位</strong>：仅查看本单位的项目 + 个人分配的项目</li>
          <li><strong>项目</strong>：仅查看自己参与的项目（通过项目成员表）</li>
          <li><strong>留空</strong>：使用角色默认值（审计处长/校领导=全校，被审单位=本单位，其他=项目）</li>
        </ul>
      </el-alert>
      <el-table :data="userList" border stripe size="small" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="角色" width="180">
          <template #default="{ row }"><el-tag v-for="r in (row.roles||[])" :key="r" size="small" style="margin:2px">{{ roleLabelMap[r]||r }}</el-tag></template>
        </el-table-column>
        <el-table-column label="数据范围" width="160">
          <template #default="{ row }">
            <el-select v-model="row.dataScope" size="small" placeholder="留空=角色默认" clearable @change="saveScope(row)" style="width:130px">
              <el-option label="全校" value="all" /><el-option label="本单位" value="dept" /><el-option label="项目" value="project" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="实际生效" width="100">
          <template #default="{ row }"><el-tag :type="scopeType(effectiveScope(row))" size="small">{{ scopeLabel(effectiveScope(row)) }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userList = ref([]); const loading = ref(false)

const roleLabelMap = { school_leader:'校领导',audit_director:'审计处长',audit_manager:'项目组长/主审',auditor:'普通审计人员',auditee_head:'被审计单位负责人',auditee_liaison:'被审计单位联络员',intermediary:'中介审计机构人员' }

function effectiveScope(row) {
  if (row.dataScope) return row.dataScope
  if ((row.roles||[]).some(r => r==='audit_director'||r==='school_leader')) return 'all'
  if ((row.roles||[]).some(r => r==='auditee_head'||r==='auditee_liaison')) return 'dept'
  return 'project'
}
function scopeLabel(s) { const m={all:'全校',dept:'本单位',project:'项目'}; return m[s]||s||'-' }
function scopeType(s) { if (s==='all') return 'danger'; if (s==='dept') return 'warning'; return 'info' }

async function fetchUsers() {
  loading.value = true
  try { userList.value = await request({url:'/user-mgmt/users',method:'get'})||[] } catch(e) {}
  loading.value = false
}

async function saveScope(row) {
  try {
    await request({url:'/user-mgmt/user/'+row.userId+'/data-scope',method:'put',data:{dataScope:row.dataScope||''}})
    ElMessage.success(row.realName+' 数据范围已更新')
  } catch(e) { ElMessage.error('保存失败') }
}

onMounted(fetchUsers)
</script>

<style lang="scss" scoped>.page-header{display:flex;justify-content:space-between;align-items:center}</style>
