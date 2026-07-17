<template>
  <div class="widget-container">
    <div class="widget-grid">
      <div v-for="w in widgetList" :key="w.id" class="widget-item"
           :style="{ gridColumn: 'span ' + (w.cols||1), gridRow: 'span ' + (w.rows||1) }">
        <div class="widget-header">
          <span class="widget-title">{{ w.title }}</span>
          <div class="widget-header-actions" v-if="editable">
            <el-icon class="widget-action" @click.stop="removeWidget(w.id)"><Close /></el-icon>
          </div>
        </div>
        <div class="widget-body">
          <slot :name="w.type" :data="w.data" :widget="w">
            <div class="widget-placeholder">{{ w.title }}</div>
          </slot>
        </div>
      </div>
    </div>
    <el-button v-if="editable" class="add-widget-btn" type="dashed" @click="openPicker">
      <el-icon><Plus /></el-icon>添加组件
    </el-button>

    <el-dialog v-model="showWidgetPicker" title="添加驾驶舱组件" width="500px">
      <p class="picker-hint">已存在的组件类型不可重复添加</p>
      <el-checkbox-group v-model="selectedWidgets">
        <el-checkbox v-for="w in availableWidgets" :key="w.type" :label="w.type"
                     :disabled="existingTypes.includes(w.type)" border style="margin:8px 8px 8px 0">
          {{ w.label }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showWidgetPicker = false">取消</el-button>
        <el-button type="primary" :disabled="selectedWidgets.length === 0" @click="addSelectedWidgets">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  widgets: { type: Array, default: () => [] },
  editable: { type: Boolean, default: false }
})
const emit = defineEmits(['update:widgets'])

const widgetList = ref(JSON.parse(JSON.stringify(props.widgets)))
const showWidgetPicker = ref(false)
const selectedWidgets = ref([])
let selfUpdating = false

const availableWidgets = [
  { type: 'data-board',  label: '数据看板' },
  { type: 'chart-pie',   label: '饼图（项目分布）' },
  { type: 'chart-bar',   label: '柱状图（进度对比）' },
  { type: 'chart-line',  label: '折线图（月度趋势）' },
  { type: 'chart-heatmap', label: '热力图' },
  { type: 'chart-gantt', label: '甘特图' },
  { type: 'detail-table', label: '明细表格' }
]

const existingTypes = computed(() => widgetList.value.map(w => w.type))

// Sync from parent: when parent changes widgets (load from localStorage / reset), update local list
watch(() => props.widgets, (val) => {
  if (selfUpdating) return
  widgetList.value = JSON.parse(JSON.stringify(val))
}, { deep: true })

// Sync to parent: when local list changes, emit to parent
watch(widgetList, (val) => {
  selfUpdating = true
  emit('update:widgets', JSON.parse(JSON.stringify(val)))
  setTimeout(() => { selfUpdating = false }, 50)
}, { deep: true })

function openPicker() {
  selectedWidgets.value = []
  showWidgetPicker.value = true
}

function removeWidget(id) {
  widgetList.value = widgetList.value.filter(w => w.id !== id)
}

function addSelectedWidgets() {
  if (selectedWidgets.value.length === 0) return
  const now = Date.now()
  const newWidgets = selectedWidgets.value.map((type, i) => {
    const meta = availableWidgets.find(w => w.type === type)
    const cm = {'data-board':3,'detail-table':2,'chart-heatmap':2}; return { id: 'w_' + now + '_' + i, type, title: meta?.label || type, cols: cm[type]||1, rows: 1, data: null }
  })
  widgetList.value = [...widgetList.value, ...newWidgets]
  selectedWidgets.value = []
  showWidgetPicker.value = false
}
</script>

<style lang="scss" scoped>
.widget-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; min-height: 100px; }
.widget-item { background: #fff; border-radius: 8px; border: 1px solid #ebeef5; overflow: hidden; }
.widget-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-bottom: 1px solid #f2f2f2; }
.widget-title { font-size: 13px; font-weight: 600; color: #303133; }
.widget-action { font-size: 14px; color: #909399; cursor: pointer; }
.widget-action:hover { color: #f56c6c; }
.widget-body { padding: 12px; min-height: 200px; }
.widget-placeholder { display: flex; align-items: center; justify-content: center; height: 100%; color: #909399; font-size: 14px; }
.add-widget-btn { width: 100%; margin-top: 16px; border-style: dashed; }
.picker-hint { font-size: 12px; color: #909399; margin-bottom: 12px; }
</style>




