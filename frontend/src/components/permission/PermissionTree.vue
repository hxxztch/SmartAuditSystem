 <template>
   <div class="permission-tree">
     <el-tree
       ref="treeRef"
       :data="treeData"
       :props="treeProps"
       :default-checked-keys="checkedKeys"
       :show-checkbox="checkable"
       :node-key="nodeKey"
       :default-expand-all="defaultExpandAll"
       :highlight-current="true"
       :filter-node-method="filterNode"
       @check="onCheck"
     >
       <template #default="{ node, data }">
         <span class="perm-tree-node">
           <span class="perm-node-label">{{ data.label }}</span>
           <el-tag v-if="data.code" size="small" type="info" effect="plain">{{ data.code }}</el-tag>
           <el-tag v-if="data.type" size="small" :type="tagTypeMap[data.type]">{{ typeLabelMap[data.type] }}</el-tag>
         </span>
       </template>
     </el-tree>
   </div>
 </template>
 
 <script setup>
 import { ref, computed, watch } from 'vue'
 
 const props = defineProps({
   data: { type: Array, default: () => [] },
   checkedKeys: { type: Array, default: () => [] },
   checkable: { type: Boolean, default: true },
   nodeKey: { type: String, default: 'code' },
   defaultExpandAll: { type: Boolean, default: false },
   filterText: { type: String, default: '' }
 })
 
 const emit = defineEmits(['check', 'node-click'])
 
 const treeRef = ref(null)
 
 const tagTypeMap = { menu: '', button: 'success', data: 'warning', api: 'info' }
 const typeLabelMap = { menu: '菜单', button: '按钮', data: '数据', api: '接口' }
 
 const treeProps = {
   children: 'children',
   label: 'label',
   disabled: 'disabled'
 }
 
 const treeData = computed(() => props.data)
 
 function onCheck(data, checkedState) {
   emit('check', {
     checked: checkedState.checkedKeys,
     halfChecked: checkedState.halfCheckedKeys,
     node: data
   })
 }
 
 function filterNode(value, data) {
   if (!value) return true
   return data.label?.includes(value) || data.code?.includes(value)
 }
 
 watch(() => props.filterText, val => {
   treeRef.value?.filter(val)
 })
 
 function setCheckedKeys(keys) {
   treeRef.value?.setCheckedKeys(keys)
 }
 
 function getCheckedKeys() {
   return treeRef.value?.getCheckedKeys() || []
 }
 
 defineExpose({ setCheckedKeys, getCheckedKeys })
 </script>
 
 <style lang="scss" scoped>
 .permission-tree {
   max-height: 400px;
   overflow-y: auto;
 }
 .perm-tree-node {
   display: flex;
   align-items: center;
   gap: 8px;
   .perm-node-label { font-size: 13px; }
 }
 </style>
