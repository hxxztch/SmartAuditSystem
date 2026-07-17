 /**
  * 细粒度按钮级权限指令 v-permission
  * 用法: v-permission="'dashboard:export'"
  *       v-permission="['dashboard:view', 'dashboard:export']"
  *       v-permission:and="['perm:role', 'perm:user']"  (AND逻辑)
  */
 import { useUserStore } from '@/store/modules/user'
 
 function checkPermission(el, binding) {
   const userStore = useUserStore()
   const { value, modifiers } = binding
 
   if (!value) return
 
   const permissions = Array.isArray(value) ? value : [value]
   const isAnd = modifiers.and || modifiers.all
 
   const has = isAnd
     ? permissions.every(p => userStore.permissions.includes(p))
     : permissions.some(p => userStore.permissions.includes(p))
 
   if (!has) {
     el.parentNode?.removeChild(el)
   }
 }
 
 export default {
   mounted(el, binding) {
     checkPermission(el, binding)
   },
   updated(el, binding) {
     checkPermission(el, binding)
   }
 }

// Role directive (merged)
 /**
  * 角色级别指令 v-role
  * 用法: v-role="'audit_director'"
  *       v-role="['audit_director', 'audit_manager']"
  */
 
 
 function checkRole(el, binding) {
   const userStore = useUserStore()
   const { value } = binding
 
   if (!value) return
 
   const roles = Array.isArray(value) ? value : [value]
   const has = roles.some(r => userStore.roles.includes(r))
 
   if (!has) {
     el.parentNode?.removeChild(el)
   }
 }
 
 const roleDirective = {
   mounted(el, binding) {
     checkRole(el, binding)
   },
   updated(el, binding) {
     checkRole(el, binding)
   }
 }
