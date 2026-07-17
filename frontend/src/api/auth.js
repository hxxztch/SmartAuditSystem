 import request from '@/utils/request'
 
 /** 登录 */
 export function login(data) {
   return request({
     url: '/auth/login',
     method: 'post',
     data
   })
 }
 
 /** 获取当前用户信息 */
 export function getUserInfo() {
   return request({
     url: '/auth/userinfo',
     method: 'get'
   })
 }
 
 /** 获取用户菜单权限 */
 export function getUserMenus() {
   return request({
     url: '/auth/menus',
     method: 'get'
   })
 }
 
 /** 获取用户功能权限列表 */
 export function getUserPermissions() {
   return request({
     url: '/auth/permissions',
     method: 'get'
   })
 }
 
 /** 角色列表 */
 export function getRoleList(params) {
   return request({
     url: '/auth/roles',
     method: 'get',
     params
   })
 }
 
 /** 创建/更新角色 */
 export function saveRole(data) {
   return request({
     url: '/auth/role',
     method: data.id ? 'put' : 'post',
     data
   })
 }
 
 /** 删除角色 */
 export function deleteRole(id) {
   return request({
     url: `/auth/role/${id}`,
     method: 'delete'
   })
 }
 
 /** 获取用户列表 */
 export function getUserList(params) {
   return request({
     url: '/auth/users',
     method: 'get',
     params
   })
 }
 
 /** 分配用户角色 */
 export function assignUserRole(userId, roleIds) {
   return request({
     url: `/auth/user/${userId}/roles`,
     method: 'put',
     data: { roleIds }
   })
 }
 
 /** 配置数据权限 */
 export function saveDataPermission(data) {
   return request({
     url: '/auth/data-permission',
     method: 'post',
     data
   })
 }
 
 /** 获取数据权限配置 */
 export function getDataPermission(params) {
   return request({
     url: '/auth/data-permission',
     method: 'get',
     params
   })
 }
 
 /** 创建临时授权 */
 export function createTempAuth(data) {
   return request({
     url: '/auth/temp-auth',
     method: 'post',
     data
   })
 }
 
 /** 临时授权列表 */
 export function getTempAuthList(params) {
   return request({
     url: '/auth/temp-auth',
     method: 'get',
     params
   })
 }
 
 /** 撤销临时授权 */
 export function revokeTempAuth(id) {
   return request({
     url: `/auth/temp-auth/${id}/revoke`,
     method: 'post'
   })
 }

// Dashboard APIs (merged)
/** 获取驾驶舱概览数据 */
 export function getDashboardOverview(params) {
   return request({
     url: '/dashboard/overview',
     method: 'get',
     params
   })
 }
 
 /** 获取分类统计数据 */
 export function getCategoryStats(params) {
   return request({
     url: '/dashboard/category-stats',
     method: 'get',
     params
   })
 }
 
 /** 获取项目详情数据 */
 export function getProjectDetail(projectId, params) {
   return request({
     url: `/dashboard/project/${projectId}`,
     method: 'get',
     params
   })
 }
 
 /** 获取底层明细数据 */
 export function getSourceData(params) {
   return request({
     url: '/dashboard/source-data',
     method: 'get',
     params
   })
 }
 
 /** 获取驾驶舱看板配置 */
 export function getDashboardLayout() {
   return request({
     url: '/dashboard/layout',
     method: 'get'
   })
 }
 
 /** 保存驾驶舱看板配置 */
 export function saveDashboardLayout(data) {
   return request({
     url: '/dashboard/layout',
     method: 'put',
     data
   })
 }
 
 /** 获取个人视图模板列表 */
 export function getPersonalViews() {
   return request({
     url: '/dashboard/views',
     method: 'get'
   })
 }
 
 /** 保存个人视图 */
 export function savePersonalView(data) {
   return request({
     url: '/dashboard/view',
     method: data.id ? 'put' : 'post',
     data
   })
 }
 
 /** 删除个人视图 */
 export function deletePersonalView(id) {
   return request({
     url: `/dashboard/view/${id}`,
     method: 'delete'
   })
 }
 
 /** 获取预警数据 */
 export function getWarnings(params) {
   return request({
     url: '/dashboard/warnings',
     method: 'get',
     params
   })
 }
