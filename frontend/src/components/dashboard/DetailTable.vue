 <template>
   <div class="detail-table">
     <el-table
       :data="tableData"
       border
       stripe
       size="small"
       :max-height="maxHeight"
       @row-click="onRowClick"
       @sort-change="onSortChange"
     >
       <el-table-column type="index" label="#" width="50" v-if="showIndex" />
       <el-table-column
         v-for="col in columns"
         :key="col.prop"
         :prop="col.prop"
         :label="col.label"
         :width="col.width"
         :min-width="col.minWidth"
         :sortable="col.sortable"
         :formatter="col.formatter"
         :fixed="col.fixed"
       >
         <template #default="{ row }" v-if="col.slot">
           <slot :name="col.slot" :row="row" :prop="col.prop" />
         </template>
       </el-table-column>
       <el-table-column label="操作" width="150" fixed="right" v-if="showActions">
         <template #default="{ row }">
           <el-button link size="small" @click.stop="$emit('view', row)">查看</el-button>
           <el-button link size="small" @click.stop="$emit('edit', row)">编辑</el-button>
           <el-button link size="small" @click.stop="$emit('drill', row)">下钻</el-button>
         </template>
       </el-table-column>
     </el-table>
     <div class="table-footer" v-if="showPagination">
       <el-pagination
         v-model:current-page="currentPage"
         v-model:page-size="pageSize"
         :page-sizes="[10, 20, 50, 100]"
         :total="total"
         layout="total, sizes, prev, pager, next, jumper"
         small
         @change="onPageChange"
       />
     </div>
   </div>
 </template>
 
 <script setup>
 import { ref, computed } from 'vue'
 
 const props = defineProps({
   columns: { type: Array, default: () => [] },
   data: { type: Array, default: () => [] },
   maxHeight: { type: [Number, String], default: 400 },
   showIndex: { type: Boolean, default: true },
   showActions: { type: Boolean, default: false },
   showPagination: { type: Boolean, default: true },
   pageSizeProp: { type: Number, default: 20 },
   total: { type: Number, default: 0 }
 })
 
 const emit = defineEmits(['view', 'edit', 'drill', 'row-click', 'page-change', 'sort-change'])
 
 const currentPage = ref(1)
 const pageSize = ref(props.pageSizeProp)
 
 const tableData = computed(() => props.data)
 
 function onRowClick(row) { emit('row-click', row) }
 function onPageChange(page, size) { emit('page-change', page, size) }
 function onSortChange(sort) { emit('sort-change', sort) }
 </script>
 
 <style lang="scss" scoped>
 .detail-table {
   background: #fff;
   border-radius: 6px;
   padding: 12px;
 }
 .table-footer {
   margin-top: 12px;
   display: flex;
   justify-content: flex-end;
 }
 </style>
