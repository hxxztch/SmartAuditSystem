import router from './index'
import { useUserStore } from '@/store/modules/user'
import { ElNotification } from 'element-plus'
const whiteList = ['/login', '/403', '/404']
let adminVerified = false
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const hasToken = userStore.isLoggedIn
  if (to.path.startsWith('/permission') && !adminVerified) {
    const pwd = prompt('请输入管理员密码以访问权限管理：')
    if (pwd !== '123456') { ElNotification.warning({ title: '验证失败', message: '密码错误' }); next({ path: '/dashboard/overview' }); return }
    adminVerified = true
  }
if (hasToken) {
    if (to.path === '/login') { next({ path: '/' }) }
    else {
      if (!userStore.userId) {
        try { await userStore.fetchUserInfo() }
        catch (e) { userStore.resetState(); localStorage.removeItem('shenji_token'); next('/login?redirect=' + to.path); return }
      }
      if (to.meta?.permissions && to.meta.permissions.length > 0) {
        const hasPerm = to.meta.permissions.some(p => userStore.permissions.includes(p))
        if (!hasPerm) { ElNotification.warning({ title: '权限不足', message: '您没有访问该页面的权限，如需开通请联系审计处长' }); next({ path: '/403', query: { from: to.path } }); return }
      }
      if (to.meta?.roles && to.meta.roles.length > 0) {
        const hasRole = to.meta.roles.some(r => userStore.roles.includes(r))
        if (!hasRole) { ElNotification.warning({ title: '角色权限不足', message: '您的角色无权访问该页面' }); next({ path: '/403', query: { from: to.path } }); return }
      }
      next()
    }
  } else {
    if (whiteList.includes(to.path)) { next() } else { next('/login?redirect=' + to.path) }
  }
})
export default router

