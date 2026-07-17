 <template>
   <slot v-if="hasAccess" />
   <slot v-else name="fallback">
     <el-tooltip :content="tooltip" placement="top">
       <span class="auth-disabled" @click="onDisabledClick">
         <slot name="disabled" />
       </span>
     </el-tooltip>
   </slot>
 </template>
 
 <script setup>
 import { computed } from 'vue'
 import { useUserStore } from '@/store/modules/user'
 import { ElMessage } from 'element-plus'
 
 const props = defineProps({
   /** 所需权限码，支持数组(OR逻辑) */
   permission: { type: [String, Array], default: '' },
   /** 所需角色 */
   role: { type: [String, Array], default: '' },
   /** 无权限提示 */
   tooltip: { type: String, default: '权限不足，请联系审计处长' }
 })
 
 const userStore = useUserStore()
 
 const hasAccess = computed(() => {
   if (props.permission) {
     const perms = Array.isArray(props.permission) ? props.permission : [props.permission]
     const permOk = perms.some(p => userStore.permissions.includes(p))
     if (!permOk) return false
   }
   if (props.role) {
     const roles = Array.isArray(props.role) ? props.role : [props.role]
     return roles.some(r => userStore.roles.includes(r))
   }
   return true
 })
 
 function onDisabledClick() {
   ElMessage.warning(props.tooltip)
 }
 </script>
 
 <style lang="scss" scoped>
 .auth-disabled {
   cursor: not-allowed;
   opacity: 0.5;
   display: inline-block;
 }
 </style>
