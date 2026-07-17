 <template>
   <div class="data-board">
     <div v-for="item in items" :key="item.key" class="data-board-item" @click="$emit('item-click', item)">
       <div class="board-label">{{ item.label }}</div>
       <div class="board-value" :style="{ color: item.color || '#303133' }">
         {{ formatValue(item.value, item.unit) }}
       </div>
       <div v-if="item.trend" class="board-trend" :class="item.trend > 0 ? 'up' : 'down'">
         <el-icon>
           <Top v-if="item.trend > 0" />
           <Bottom v-else />
         </el-icon>
         <span>{{ Math.abs(item.trend) }}%</span>
       </div>
       <div v-if="item.subLabel" class="board-sub">{{ item.subLabel }}: {{ item.subValue }}</div>
     </div>
   </div>
 </template>
 
 <script setup>
 defineProps({
   items: { type: Array, default: () => [] }
 })
 
 defineEmits(['item-click'])
 
 function formatValue(val, unit) {
   if (val === null || val === undefined) return '-'
   if (typeof val === 'number') {
     if (val >= 100000000) return (val / 100000000).toFixed(2) + (unit || '亿')
     if (val >= 10000) return (val / 10000).toFixed(2) + (unit || '万')
     return val.toLocaleString() + (unit || '')
   }
   return val + (unit || '')
 }
 </script>
 
 <style lang="scss" scoped>
 .data-board {
   display: grid;
   grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
   gap: 16px;
 }
 .data-board-item {
   background: #fff;
   border-radius: 8px;
   padding: 20px;
   cursor: pointer;
   transition: all 0.2s;
   border: 1px solid #ebeef5;
   &:hover {
     box-shadow: 0 4px 12px rgba(0,0,0,0.08);
     transform: translateY(-2px);
   }
 }
 .board-label {
   font-size: 13px;
   color: #909399;
   margin-bottom: 8px;
 }
 .board-value {
   font-size: 24px;
   font-weight: 700;
   line-height: 1.2;
 }
 .board-trend {
   display: flex;
   align-items: center;
   gap: 2px;
   font-size: 12px;
   margin-top: 6px;
   &.up { color: #67c23a; }
   &.down { color: #f56c6c; }
 }
 .board-sub {
   font-size: 12px;
   color: #909399;
   margin-top: 4px;
 }
 </style>
