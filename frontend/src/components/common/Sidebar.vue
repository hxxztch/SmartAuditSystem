<template>
  <div class="sidebar-container">
    <div class="sidebar-logo">
      <img src="@/assets/icons/logo.svg" alt="logo" class="sidebar-logo-img" />
      <span v-show="appStore.sidebar.opened || appStore.isMobile" class="sidebar-logo-text">智慧审计平台</span>
    </div>
    <!-- Mobile close button -->
    <div v-if="appStore.isMobile" class="mobile-close">
      <el-button link @click="appStore.closeMobileSidebar" style="color:#bfcbd9;width:100%;justify-content:flex-start;padding:8px 20px">
        <el-icon><Close /></el-icon><span style="margin-left:8px">关闭菜单</span>
      </el-button>
    </div>
    <el-scrollbar class="sidebar-scroll">
      <el-menu :default-active="activeMenu" :collapse="!appStore.isMobile && !appStore.sidebar.opened" :unique-opened="true"
               background-color="#1d1e1f" text-color="#bfcbd9" active-text-color="#409eff" @select="onMenuSelect">
        <el-sub-menu index="sub-dashboard" v-if="hasDashPerm">
          <template #title><el-icon><Monitor /></el-icon><span>数据驾驶舱</span></template>
          <el-menu-item index="/dashboard/overview"><el-icon><DataBoard /></el-icon>全局总览</el-menu-item>
          <el-menu-item index="/my-projects"><el-icon><List /></el-icon>{{ userStore.isAuditDirector||userStore.isSchoolLeader ? '所有项目' : '我的项目' }}</el-menu-item>
          <el-menu-item index="/dashboard/request"><el-icon><Document /></el-icon>申请通道</el-menu-item>
          <el-menu-item index="/dashboard/personal-view" v-if="hasCustomPerm"><el-icon><User /></el-icon>个人视图</el-menu-item>
          <el-menu-item index="/dashboard/customize" v-if="hasCustomPerm"><el-icon><Grid /></el-icon>布局自定义</el-menu-item>
          <el-menu-item index="/dashboard/source" v-if="hasSourcePerm"><el-icon><List /></el-icon>原始明细</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="sub-permission" v-if="hasAnyAdminPerm">
          <template #title><el-icon><Lock /></el-icon><span>权限管理</span></template>
          <el-menu-item index="/permission/roles" v-if="hasRolePerm"><el-icon><UserFilled /></el-icon>角色管理</el-menu-item>
          <el-menu-item index="/permission/users" v-if="userStore.isAuditDirector"><el-icon><Avatar /></el-icon>用户权限</el-menu-item>
          <el-menu-item index="/permission/temp-auth" v-if="hasTempPerm"><el-icon><Timer /></el-icon>申请批复</el-menu-item><el-menu-item index="/permission/temp-grant" v-if="hasTempPerm"><el-icon><Timer /></el-icon>临时授权</el-menu-item>
          <el-menu-item index="/permission/projects" v-if="hasProjectPerm"><el-icon><Edit /></el-icon>项目管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </div>
</template>
<script setup>
import { computed } from 'vue'; import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/modules/app'; import { useUserStore } from '@/store/modules/user'
import { Monitor, DataBoard, List, Document, User, Grid, Lock, UserFilled, Avatar, Timer, Edit, Close } from '@element-plus/icons-vue'
const route = useRoute(); const router = useRouter()
const appStore = useAppStore(); const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const hasDashPerm = computed(() => userStore.permissions.includes('dashboard:view'))
const hasCustomPerm = computed(() => userStore.permissions.includes('dashboard:customize'))
const hasSourcePerm = computed(() => userStore.permissions.includes('dashboard:source'))
const hasRolePerm = computed(() => userStore.permissions.includes('perm:role'))
const hasUserPerm = computed(() => userStore.permissions.includes('perm:user'))
const hasDataPerm = computed(() => userStore.permissions.includes('perm:data'))
const hasTempPerm = computed(() => userStore.permissions.includes('perm:temp_auth'))
const hasProjectPerm = computed(() => userStore.permissions.includes('perm:project'))
const hasAnyAdminPerm = computed(() => hasRolePerm.value||hasUserPerm.value||hasDataPerm.value||hasTempPerm.value||hasProjectPerm.value)
function onMenuSelect(index) {
  if (index.startsWith('/')) { router.push(index); if (appStore.isMobile) appStore.closeMobileSidebar() }
}
</script>
<style lang="scss" scoped>
.sidebar-container{height:100%;display:flex;flex-direction:column}
.sidebar-logo{height:56px;display:flex;align-items:center;padding:0 16px;border-bottom:1px solid rgba(255,255,255,.06)}
.sidebar-logo-img{width:28px;height:28px;flex-shrink:0}
.sidebar-logo-text{color:#fff;font-size:15px;font-weight:600;margin-left:10px;white-space:nowrap}
.sidebar-scroll{flex:1;overflow-y:auto}.el-menu{border-right:none}
.mobile-close{border-bottom:1px solid rgba(255,255,255,.06)}
</style>
