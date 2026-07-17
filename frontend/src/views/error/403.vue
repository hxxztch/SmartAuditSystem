<template>
  <div class="error-page">
    <el-result icon="warning" title="403" sub-title="权限不足，您没有访问该页面的权限">
      <template #extra>
        <p style="color:#909399;font-size:13px">当前账号权限不足，请切换账号或联系审计处长申请授权</p>
        <el-button type="primary" @click="goLogin">返回登录</el-button>
        <el-button @click="requestAuth">申请权限</el-button>
      </template>
    </el-result>
  </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
const router = useRouter()
const userStore = useUserStore()
function goLogin() { userStore.logout(); router.push('/login') }
function requestAuth() { ElMessage.success('权限申请已发送至审计处长') }
</script>
<style lang="scss" scoped>.error-page{display:flex;justify-content:center;align-items:center;min-height:60vh}</style>
