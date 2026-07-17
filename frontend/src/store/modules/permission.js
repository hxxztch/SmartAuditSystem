import { defineStore } from 'pinia'
import { getRoleList } from '@/api/auth'
export const usePermissionStore = defineStore('permission', {
  state: () => ({ roleList: [] }),
  actions: { async fetchRoleList(p) { const r=await getRoleList(p); this.roleList=r.rows||r.data||[]; return this.roleList } }
})
