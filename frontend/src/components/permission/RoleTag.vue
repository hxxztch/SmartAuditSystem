 <template>
   <el-tooltip :content="tooltip" placement="bottom" v-if="label">
     <el-tag :type="tagType" size="small" effect="plain" class="role-tag">
       {{ label }}
     </el-tag>
   </el-tooltip>
 </template>
 
 <script setup>
 import { computed } from 'vue'
 import { RoleType, RoleLabelMap } from '@/utils/auth'
 
 const props = defineProps({
   roles: { type: Array, default: () => [] },
   showAll: { type: Boolean, default: false }
 })
 
 const roleConfig = {
   [RoleType.SCHOOL_LEADER]: { type: 'danger', label: '校领导' },
   [RoleType.AUDIT_DIRECTOR]: { type: 'danger', label: '审计处长' },
   [RoleType.AUDIT_MANAGER]: { type: 'warning', label: '项目组长' },
   [RoleType.AUDITOR]: { type: 'success', label: '审计人员' },
   [RoleType.AUDITEE_HEAD]: { type: 'info', label: '被审单位' },
   [RoleType.AUDITEE_LIAISON]: { type: 'info', label: '联络员' },
   [RoleType.INTERMEDIARY]: { type: '', label: '中介人员' }
 }
 
 const primaryRole = computed(() => {
   if (!props.roles?.length) return null
   const priority = [
     RoleType.SCHOOL_LEADER, RoleType.AUDIT_DIRECTOR, RoleType.AUDIT_MANAGER,
     RoleType.AUDITOR, RoleType.AUDITEE_HEAD, RoleType.AUDITEE_LIAISON, RoleType.INTERMEDIARY
   ]
   for (const r of priority) {
     if (props.roles.includes(r)) return roleConfig[r] || null
   }
   return null
 })
 
 const label = computed(() => props.roles?.length > 0 ? RoleLabelMap[props.roles[0]] || props.roles[0] : '')
 const tagType = computed(() => primaryRole.value?.type || '')
 const tooltip = computed(() => props.roles.map(r => RoleLabelMap[r] || r).join(', '))
 </script>
 
 <style lang="scss" scoped>
 .role-tag { cursor: default; }
 </style>
