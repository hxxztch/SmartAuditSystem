<template>
  <div class="dashboard-layout">
    <div class="dashboard-toolbar"><span class="toolbar-title">数据驾驶舱</span>
    <el-button size="small" @click="refreshData"><el-icon><Refresh /></el-icon>刷新</el-button></div>
    <div class="dashboard-body"><slot /></div>
  </div>
</template>
<script setup>function refreshData(){location.reload()}</script>
<style lang="scss" scoped>
.dashboard-layout{display:flex;flex-direction:column;height:100%}
.dashboard-toolbar{display:flex;justify-content:space-between;align-items:center;padding:12px 16px;background:#fff;border-radius:6px;margin-bottom:16px;box-shadow:0 1px 3px rgba(0,0,0,.06)}
.toolbar-title{font-size:15px;font-weight:600}
.dashboard-body{flex:1;overflow-y:auto}
</style>