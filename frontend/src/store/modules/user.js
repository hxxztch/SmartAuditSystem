import { defineStore } from 'pinia'
import { login, getUserInfo, getUserPermissions } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    // App state
    sidebar: { opened: true }, device: 'desktop', visitedViews: [], cachedViews: [],
    // Dashboard state
    overviewData: null, drillLevel: 0,
    // User state
    token: localStorage.getItem('shenji_token') || '',
    userId: null, username: '', realName: '', avatar: '',
    orgId: '', orgName: '', roles: [], permissions: [],
    roleLabel: '', dataScope: 'self', isTempAuth: false, tempAuthExpireAt: null
  }),
  getters: {
    isLoggedIn: state => !!state.token,
    isSchoolLeader: state => state.roles.includes('school_leader'),
    isAuditDirector: state => state.roles.includes('audit_director'),
    isAuditManager: state => state.roles.includes('audit_manager'),
    isAuditor: state => state.roles.includes('auditor'),
    isAuditee: state => state.roles.includes('auditee_head') || state.roles.includes('auditee_liaison'),
    isIntermediary: state => state.roles.includes('intermediary'),
    hasAdminAccess: state => state.roles.includes('audit_director') || state.roles.includes('school_leader'),
    hasPerm: state => perm => state.permissions.includes(perm),
    hasAnyPerm: state => perms => perms.some(p => state.permissions.includes(p)),
    hasAllPerm: state => perms => perms.every(p => state.permissions.includes(p))
  },
  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      this.token = res.token
      localStorage.setItem('shenji_token', res.token)
      return res
    },
    async fetchUserInfo() {
      const userInfo = await getUserInfo()
      const permRes = await getUserPermissions()
      this.userId = userInfo.userId; this.username = userInfo.username
      this.realName = userInfo.realName; this.avatar = userInfo.avatar
      this.orgId = userInfo.orgId; this.orgName = userInfo.orgName
      this.roles = userInfo.roles || []; this.roleLabel = userInfo.roleLabel || ''
      this.dataScope = userInfo.dataScope || 'self'
      this.permissions = permRes.permissions || []
      this.isTempAuth = userInfo.isTempAuth || false
      this.tempAuthExpireAt = userInfo.tempAuthExpireAt || null

      return userInfo
    },
    async logout() { this.resetState(); localStorage.removeItem('shenji_token') },
    resetState() { this.$reset() },
    setPermissions(perms) { this.permissions = perms },
    // App actions
    toggleSidebar() { this.sidebar.opened = !this.sidebar.opened },
    addVisitedView(view) { if (!this.visitedViews.some(v=>v.path===view.path)) this.visitedViews.push(Object.assign({},view)) },
    delVisitedView(view) { const i=this.visitedViews.findIndex(v=>v.path===view.path); if(i>=0) this.visitedViews.splice(i,1) }
  }
})
