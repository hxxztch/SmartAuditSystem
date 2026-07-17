<template>
  <el-container class="main-layout" :class="{ 'is-mobile': appStore.isMobile }">
    <!-- Desktop sidebar -->
    <el-aside v-if="!appStore.isMobile" :width="appStore.sidebar.opened?'220px':'64px'" class="main-sidebar">
      <Sidebar />
    </el-aside>
    <!-- Mobile sidebar overlay -->
    <div v-if="appStore.isMobile && appStore.mobileSidebar" class="mobile-overlay" @click="appStore.closeMobileSidebar">
      <div class="mobile-sidebar" @click.stop>
        <Sidebar />
      </div>
    </div>
    <el-container>
      <el-header class="main-header"><Navbar /></el-header>
      <el-main class="main-content" :class="{ 'mobile-content': appStore.isMobile }">
        <router-view v-slot="{ Component }">
          <keep-alive :include="appStore.cachedViews">
            <component :is="Component" :key="$route.fullPath" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/store/modules/app'
import Sidebar from '@/components/common/Sidebar.vue'
import Navbar from '@/components/common/Navbar.vue'
const appStore = useAppStore()

function onResize() { appStore.setDevice(window.innerWidth) }
onMounted(() => { onResize(); window.addEventListener('resize', onResize) })
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>
<style lang="scss" scoped>
.main-layout{height:100vh;overflow:hidden}
.main-layout.is-mobile .main-content { padding: 12px; }
.main-sidebar{background-color:#1d1e1f;transition:width .28s;overflow:hidden}
.main-header{height:56px;padding:0;background:#fff;border-bottom:1px solid #e4e7ed;display:flex;align-items:center;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.main-content{background-color:#f0f2f5;padding:20px;height:calc(100vh - 56px);overflow-y:auto}

// Mobile overlay
.mobile-overlay{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.45);z-index:2000}
.mobile-sidebar{position:absolute;top:0;left:0;bottom:0;width:240px;background:#1d1e1f;box-shadow:2px 0 8px rgba(0,0,0,.15);animation:slideIn .25s ease}
@keyframes slideIn{from{transform:translateX(-100%)}to{transform:translateX(0)}}

.mobile-content { padding: 12px !important; }
</style>
