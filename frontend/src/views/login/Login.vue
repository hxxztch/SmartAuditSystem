<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <img src="@/assets/icons/logo.svg" alt="logo" class="login-logo" />
        <h1 class="login-title">高校一体化智慧审计平台</h1>
        <p class="login-subtitle">审计数据驾驶舱 · 分级细粒度权限管控</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" maxlength="32" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" maxlength="32" placeholder="请输入密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="loginForm.captchaCode" placeholder="验证码" size="large" style="flex:1" maxlength="4" />
            <img :src="captchaImage" class="captcha-img" @click="refreshCaptcha" title="点击刷新验证码" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
        <el-divider content-position="center">演示账号快速切换</el-divider>
        <div class="demo-accounts">
          <el-button v-for="acct in demoAccounts" :key="acct.role" size="small" :type="acct.type" @click="quickLogin(acct)">
            {{ acct.label }}
          </el-button>
        </div>
      </el-form>
        <div class="qr-section">
          <img src="/qrcode.png" class="qr-code-img" alt="扫码访问" />
          <p class="qr-tip">手机扫描二维码访问</p>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const loginForm = reactive({ username: 'admindirector', password: '123456', captchaCode: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const demoAccounts = [
  { label: '校领导', username: 'leader', password: '123456', type: 'danger', role: 'school_leader' },
  { label: '审计处长', username: 'admindirector', password: '123456', type: 'danger', role: 'audit_director' },
  { label: '项目组长', username: 'manager', password: '123456', type: 'warning', role: 'audit_manager' },
  { label: '审计人员', username: 'auditor', password: '123456', type: '', role: 'auditor' },
  { label: '被审单位负责人', username: 'depthead', password: '123456', type: 'success', role: 'auditee_head' },
  { label: '联络员', username: 'liaison', password: '123456', type: 'info', role: 'auditee_liaison' },
  { label: '中介人员', username: 'agent', password: '123456', type: 'info', role: 'intermediary' }
]

async function refreshCaptcha() {
  try {
    const res = await request({ url: '/captcha', method: 'get' })
    captchaImage.value = res.captchaImage
    captchaKey.value = res.captchaKey
    loginForm.captchaCode = ''
  } catch(e) {}
}

onMounted(refreshCaptcha)

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ ...loginForm, captchaKey: captchaKey.value })
    await userStore.fetchUserInfo()
    ElMessage.success('欢迎回来，' + userStore.realName)
    router.push(route.query.redirect || '/dashboard/overview')
  } catch (e) {
    refreshCaptcha()
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally { loading.value = false }
}
function quickLogin(acct) {
  loginForm.username = acct.username; loginForm.password = acct.password
  refreshCaptcha()
}
</script>

<style lang="scss" scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); }
.login-container { width: 420px; padding: 40px; background: rgba(255,255,255,0.95); border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
.login-header { text-align: center; margin-bottom: 32px; }
.login-logo { width: 48px; height: 48px; margin-bottom: 12px; }
.login-title { font-size: 20px; color: #303133; margin: 0 0 6px; }
.login-subtitle { font-size: 12px; color: #909399; margin: 0; }
.login-form { max-width: 360px; margin: 0 auto; }
.login-btn { width: 100%; }
.captcha-row { display: flex; gap: 10px; align-items: center; }
.captcha-img { height: 40px; width: 120px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px; flex-shrink: 0; }
.demo-accounts { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.qr-section { text-align: center; margin-top: 16px; }
.qr-code-img { width: 100px; height: 100px; border-radius: 4px; }
.qr-tip { font-size: 11px; color: #909399; margin: 6px 0 0; }
@media (max-width: 768px) { .login-container { width: 90vw; padding: 24px 16px; } .login-title { font-size: 18px; } .login-logo { width: 40px; height: 40px; } .login-form { max-width: 100%; } }
</style>
