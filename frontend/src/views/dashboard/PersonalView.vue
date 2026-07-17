<template>
  <DashboardLayout>
    <div class="personal-view-page">
      <el-card shadow="never">
        <template #header><span>我的个人视图</span></template>

        <el-empty v-if="views.length === 0" description="暂无个人视图，请在布局自定义页面创建" />

        <div v-else class="view-list">
          <div v-for="view in views" :key="view.name" class="view-card">
            <div class="view-card-header">
              <el-icon :size="24" color="#409eff"><Monitor /></el-icon>
              <span class="view-name">{{ view.name }}</span>
              <el-tag size="small">{{ view.widgetCount }} 个组件</el-tag>
            </div>
            <div class="view-meta">
              <span>创建时间：{{ view.createTime }}</span>
            </div>
            <div class="view-actions">
              <el-button size="small" type="primary" @click="applyView(view)">应用此视图</el-button>
              <el-button size="small" type="danger" plain @click="deleteView(view)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import DashboardLayout from '@/layout/DashboardLayout.vue'

const router = useRouter()
const userStore = useUserStore()
const LAYOUT_KEY = () => 'shenji_dashboard_layout_' + (userStore.username || 'anonymous')
const views = ref([])

function loadViews() {
  views.value = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && key.startsWith(LAYOUT_KEY() + '_view_')) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        const name = key.replace(LAYOUT_KEY() + '_view_', '')
        views.value.push({
          name,
          widgetCount: Array.isArray(data) ? data.length : 0,
          createTime: new Date().toLocaleDateString()
        })
      } catch(e) {}
    }
  }
}

onMounted(loadViews)

function applyView(view) {
  const key = LAYOUT_KEY() + '_view_' + view.name
  const data = localStorage.getItem(key)
  if (data) {
    localStorage.setItem(LAYOUT_KEY(), data)  // copy to active layout
    ElMessage.success('已应用视图「' + view.name + '」，请前往布局自定义页面查看')
    router.push('/dashboard/customize')
  }
}

function deleteView(view) {
  ElMessageBox.confirm('确认删除视图「' + view.name + '」？').then(() => {
    localStorage.removeItem(LAYOUT_KEY() + '_view_' + view.name)
    loadViews()
    ElMessage.success('已删除')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.view-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.view-card { background: #fafafa; border: 1px solid #ebeef5; border-radius: 8px; padding: 20px; transition: all .2s; }
.view-card:hover { border-color: #409eff; box-shadow: 0 2px 8px rgba(64,158,255,.12); }
.view-card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.view-name { font-size: 15px; font-weight: 600; }
.view-meta { font-size: 12px; color: #909399; margin-bottom: 12px; }
.view-actions { display: flex; gap: 8px; }
</style>
