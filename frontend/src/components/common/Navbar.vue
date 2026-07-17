<template>
  <div class="navbar">
    <div class="navbar-left">
      <!-- Hamburger for mobile -->
      <el-icon v-if="appStore.isMobile" class="hamburger-btn" @click="appStore.toggleMobileSidebar" :size="22">
        <Expand v-if="appStore.mobileSidebar" /><Menu v-else />
      </el-icon>
      <!-- Sidebar toggle for desktop -->
      <el-icon v-if="!appStore.isMobile" class="collapse-btn" @click="appStore.toggleSidebar" :size="20">
        <Fold v-if="appStore.sidebar.opened" /><Expand v-else />
      </el-icon>
      <span class="navbar-title">高校一体化智慧审计平台</span>
    </div>
    <div class="navbar-right">
      <RoleTag v-if="!appStore.isMobile" :roles="userStore.roles" />
      <span class="user-info">{{ appStore.isMobile ? '' : userStore.realName }}</span>
      <el-button class="logout-btn" @click="doLogout">退出</el-button>
    </div>
  </div>
</template>
<script setup>
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import RoleTag from '@/components/permission/RoleTag.vue'
import { Fold, Expand, Menu } from '@element-plus/icons-vue'
const appStore = useAppStore()
const userStore = useUserStore()
function doLogout() { userStore.logout(); location.href = '/login' }
</script>
<style lang="scss" scoped>
.navbar{width:100%;display:flex;align-items:center;justify-content:space-between;padding:0 20px;height:100%}
.navbar-left{display:flex;align-items:center}.collapse-btn{cursor:pointer;margin-right:16px}.collapse-btn:hover,.hamburger-btn:hover{color:#409eff}
.hamburger-btn{cursor:pointer;margin-right:12px;font-size:22px}
.navbar-title{font-size:14px;font-weight:600;color:#303133;white-space:nowrap}
.navbar-right{display:flex;align-items:center;gap:12px}
.user-info{font-size:13px;color:#606266}
.logout-btn{font-size:13px;color:#f56c6c;padding:4px 8px}
@media (max-width: 768px) {
  .navbar{padding:0 12px}
  .navbar-title{font-size:13px}
  .navbar-right{gap:6px}
  .logout-btn{padding:4px 6px;font-size:12px}
}
</style>
