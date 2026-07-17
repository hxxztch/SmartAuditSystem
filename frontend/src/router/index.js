import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/Login.vue'), hidden: true },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard/overview',
    children: [
      { path: 'my-projects', name: 'MyProjects', component: () => import('@/views/dashboard/MyProjects.vue'), meta: { title: '我的项目', icon: 'List' } },
      {
        path: 'dashboard',
        name: 'Dashboard',
        redirect: '/dashboard/overview',
        meta: { title: '数据驾驶舱', icon: 'Monitor', roles: ['school_leader','audit_director','audit_manager','auditor','auditee_head','auditee_liaison','intermediary'] },
        children: [
          { path: 'overview', name: 'DashboardOverview', component: () => import('@/views/dashboard/Overview.vue'), meta: { title: '全局总览', icon: 'DataBoard', keepAlive: true, permissions: ['dashboard:view'] } },
          { path: 'personal-view', name: 'DashboardPersonalView', component: () => import('@/views/dashboard/PersonalView.vue'), meta: { title: '个人视图', icon: 'User' } },
          { path: 'customize', name: 'DashboardCustomize', component: () => import('@/views/dashboard/WidgetCustomize.vue'), meta: { title: '布局自定义', icon: 'Grid', permissions: ['dashboard:customize'] } },
          { path: 'source', name: 'DashboardSource', component: () => import('@/views/dashboard/SourceData.vue'), meta: { title: '原始明细', icon: 'List', permissions: ['dashboard:source'] } },{ path: 'request', name: 'DashboardRequest', component: () => import('@/views/permission/TempAuth.vue'), meta: { title: '申请通道', icon: 'Document' } }
        ]
      },
      {
        path: 'permission',
        name: 'Permission',
        meta: { title: '权限管理', icon: 'Lock' },
        children: [
          { path: 'roles', name: 'PermissionRole', component: () => import('@/views/permission/RoleManage.vue'), meta: { title: '角色管理', icon: 'UserFilled', permissions: ['perm:project'] } },
          { path: 'users', name: 'PermissionUser', component: () => import('@/views/permission/UserPermission.vue'), meta: { title: '用户管理', icon: 'Avatar', permissions: ['perm:user'] } },
          { path: 'data', name: 'PermissionData', component: () => import('@/views/permission/DataPermission.vue'), meta: { title: '数据权限', icon: 'DataAnalysis', permissions: ['perm:data'] } },
          { path: 'projects', name: 'ProjectManage', component: () => import('@/views/permission/ProjectManage.vue'), meta: { title: '项目管理', icon: 'Edit', permissions: ['perm:project'] } },
          { path: 'temp-auth', name: 'PermissionTempAuth', component: () => import('@/views/permission/TempAuth.vue'), meta: { title: '申请批复', icon: 'Timer', permissions: ['perm:temp_auth'] } },{ path: 'temp-grant', name: 'TempGrant', component: () => import('@/views/permission/TempGrant.vue'), meta: { title: '临时授权', icon: 'Timer', permissions: ['perm:temp_auth'] } }
        ]
      }
    ]
  },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), hidden: true },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/404.vue'), hidden: true }
]

const router = createRouter({ history: createWebHashHistory(), routes })
router.afterEach((to) => { if (to.meta?.title) document.title = to.meta.title + ' - 智慧审计平台' })
export default router
