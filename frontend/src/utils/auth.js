 /**
  * 五维权限模型工具函数
  * 机构(Org) + 用户(User) + 角色(Role) + 数据(Data) + 功能(Function)
  */
 
 /** 预定义角色枚举 */
 export const RoleType = {
   SCHOOL_LEADER: 'school_leader',             // 校领导
   AUDIT_DIRECTOR: 'audit_director',            // 审计处长
   AUDIT_MANAGER: 'audit_manager',              // 项目组长/主审
   AUDITOR: 'auditor',                          // 普通审计人员
   AUDITEE_HEAD: 'auditee_head',                // 被审计单位负责人
   AUDITEE_LIAISON: 'auditee_liaison',          // 被审计单位联络员
   INTERMEDIARY: 'intermediary'                 // 中介审计机构人员
 }
 
 /** 角色中文映射 */
 export const RoleLabelMap = {
   [RoleType.SCHOOL_LEADER]: '校领导',
   [RoleType.AUDIT_DIRECTOR]: '审计处长',
   [RoleType.AUDIT_MANAGER]: '项目组长/主审',
   [RoleType.AUDITOR]: '普通审计人员',
   [RoleType.AUDITEE_HEAD]: '被审计单位负责人',
   [RoleType.AUDITEE_LIAISON]: '被审计单位联络员',
   [RoleType.INTERMEDIARY]: '中介审计机构人员'
 }
 
 /** 角色层级数字（数值越低权限越大） */
 const RoleLevel = {
   [RoleType.SCHOOL_LEADER]: 1,
   [RoleType.AUDIT_DIRECTOR]: 2,
   [RoleType.AUDIT_MANAGER]: 3,
   [RoleType.AUDITOR]: 4,
   [RoleType.AUDITEE_HEAD]: 5,
   [RoleType.AUDITEE_LIAISON]: 6,
   [RoleType.INTERMEDIARY]: 7
 }
 
 /** 功能权限点 */
 export const PermissionCode = {
   // 驾驶舱权限
   DASHBOARD_VIEW: 'dashboard:view',
   DASHBOARD_OVERVIEW: 'dashboard:overview',
   DASHBOARD_PROJECT: 'dashboard:project',
   DASHBOARD_SOURCE: 'dashboard:source',
   DASHBOARD_EXPORT: 'dashboard:export',
   DASHBOARD_CUSTOMIZE: 'dashboard:customize',
   // 数据权限
   DATA_FINANCIAL: 'data:financial',
   DATA_PROJECT_ALL: 'data:project:all',
   DATA_PROJECT_OWN: 'data:project:own',
   DATA_PROJECT_DEPT: 'data:project:dept',
   // 审计作业权限
   AUDIT_WORKBENCH: 'audit:workbench',
   AUDIT_REVIEW: 'audit:review',
   AUDIT_REPORT: 'audit:report',
   AUDIT_ARCHIVE: 'audit:archive',
   // 整改管理权限
   RECTIFY_VIEW: 'rectify:view',
   RECTIFY_APPROVE: 'rectify:approve',
   RECTIFY_SUBMIT: 'rectify:submit',
   // 权限管理
   PERM_MANAGE: 'perm:manage',
   PERM_ROLE: 'perm:role',
   PERM_USER: 'perm:user',
   PERM_DATA: 'perm:data',
   PERM_TEMP_AUTH: 'perm:temp_auth'
 }
 
 /**
  * 权限层级配置：各角色默认拥有的功能权限
  */
 const RoleDefaultPermissions = {
   [RoleType.SCHOOL_LEADER]: [
     PermissionCode.DASHBOARD_VIEW,
     PermissionCode.DASHBOARD_OVERVIEW,
     PermissionCode.DASHBOARD_PROJECT,
     PermissionCode.RECTIFY_VIEW,
     PermissionCode.AUDIT_REPORT
   ],
   [RoleType.AUDIT_DIRECTOR]: Object.values(PermissionCode),
   [RoleType.AUDIT_MANAGER]: [
     PermissionCode.DASHBOARD_VIEW,
     PermissionCode.DASHBOARD_OVERVIEW,
     PermissionCode.DASHBOARD_PROJECT,
     PermissionCode.DASHBOARD_EXPORT,
     PermissionCode.DASHBOARD_CUSTOMIZE,
     PermissionCode.DATA_PROJECT_ALL,
     PermissionCode.AUDIT_WORKBENCH,
     PermissionCode.AUDIT_REVIEW,
     PermissionCode.AUDIT_REPORT,
     PermissionCode.AUDIT_ARCHIVE,
     PermissionCode.RECTIFY_VIEW,
     PermissionCode.RECTIFY_APPROVE
   ],
   [RoleType.AUDITOR]: [
     PermissionCode.DASHBOARD_VIEW,
     PermissionCode.DASHBOARD_PROJECT,
     PermissionCode.DATA_PROJECT_OWN,
     PermissionCode.AUDIT_WORKBENCH,
     PermissionCode.RECTIFY_VIEW,
     PermissionCode.RECTIFY_SUBMIT
   ],
   [RoleType.AUDITEE_HEAD]: [
     PermissionCode.DASHBOARD_VIEW,
     PermissionCode.DASHBOARD_PROJECT,
     PermissionCode.DATA_PROJECT_DEPT,
     PermissionCode.RECTIFY_VIEW,
     PermissionCode.RECTIFY_SUBMIT
   ],
   [RoleType.AUDITEE_LIAISON]: [
     PermissionCode.DATA_PROJECT_DEPT,
     PermissionCode.RECTIFY_SUBMIT
   ],
   [RoleType.INTERMEDIARY]: [
     PermissionCode.DATA_PROJECT_OWN,
     PermissionCode.AUDIT_WORKBENCH
   ]
 }
 
 /** 各角色默认数据权限范围 */
 export const DataScope = {
   ALL: 'all',              // 全校数据
   DEPT: 'dept',            // 本单位数据
   PROJECT: 'project',      // 仅分配项目
   SELF: 'self'             // 仅本人
 }
 
 const RoleDefaultDataScope = {
   [RoleType.SCHOOL_LEADER]: DataScope.ALL,
   [RoleType.AUDIT_DIRECTOR]: DataScope.ALL,
   [RoleType.AUDIT_MANAGER]: DataScope.PROJECT,
   [RoleType.AUDITOR]: DataScope.PROJECT,
   [RoleType.AUDITEE_HEAD]: DataScope.DEPT,
   [RoleType.AUDITEE_LIAISON]: DataScope.DEPT,
   [RoleType.INTERMEDIARY]: DataScope.PROJECT
 }
 
 /**
  * 检查用户是否拥有指定功能权限
  * @param {string|string[]} permission - 权限码或权限码数组
  * @param {string[]} userPermissions - 用户拥有权限码列表
  * @param {'AND'|'OR'} mode - 多权限检查模式
  * @returns {boolean}
  */
 export function hasPermission(permission, userPermissions, mode = 'OR') {
   if (!userPermissions || !userPermissions.length) return false
   const perms = Array.isArray(permission) ? permission : [permission]
   return mode === 'AND'
     ? perms.every(p => userPermissions.includes(p))
     : perms.some(p => userPermissions.includes(p))
 }
 
 /**
  * 检查角色是否满足最低层级要求
  */
 export function hasRoleLevel(role, minLevel) {
   return RoleLevel[role] <= minLevel
 }
 
 /**
  * 检查数据权限范围
  */
 export function checkDataScope(role, targetDeptId, userId, projectId) {
   const scope = RoleDefaultDataScope[role] || DataScope.SELF
   return { scope, allowed: true }
 }
 
 /**
  * 校验角色默认权限配置
  */
 export function getDefaultPermissions(role) {
   return RoleDefaultPermissions[role] || []
 }
 
 export function getDefaultDataScope(role) {
   return RoleDefaultDataScope[role] || DataScope.SELF
 }
 
 /**
  * 判断是否拥有细粒度按钮级别权限
  * 用于 v-permission 指令
  */
 export function checkButtonPermission(permCode, userPermissions) {
   return hasPermission(permCode, userPermissions)
 }

// Drill-down utils (merged)
 /**
  * 驾驶舱下钻引擎
  * 数据穿透路径：全局总览 → 类型统计 → 单项目汇总 → 底层凭证/底稿明细
  */
 
 /** 下钻层级枚举 */
 const DrillLevel = {
   OVERVIEW: 0,        // 全局总览
   CATEGORY: 1,        // 分类型统计
   PROJECT: 2,         // 单项目汇总
   SOURCE: 3           // 底层明细
 }
 
 /** 下钻层级元数据 */
 const DrillMeta = {
   [DrillLevel.OVERVIEW]: { label: '全局总览', route: 'DashboardOverview' },
   [DrillLevel.CATEGORY]: { label: '分类统计', route: 'DashboardCategory' },
   [DrillLevel.PROJECT]:  { label: '项目详情', route: 'DashboardProject' },
   [DrillLevel.SOURCE]:   { label: '原始明细', route: 'DashboardSource' }
 }
 
 /**
  * 获取可下钻的下一层级
  */
 function getNextLevel(currentLevel) {
   const next = currentLevel + 1
   return next <= DrillLevel.SOURCE ? next : null
 }
 
 /**
  * 获取可返回的上一层级
  */
 function getPrevLevel(currentLevel) {
   const prev = currentLevel - 1
   return prev >= DrillLevel.OVERVIEW ? prev : null
 }
 
 /**
  * 构建下钻导航路径
  * @param {number} targetLevel - 目标层级
  * @param {object} params - 路径参数 { category, projectId, sourceId }
  * @returns {object} { name, query }
  */
 function buildDrillRoute(targetLevel, params = {}) {
   const meta = DrillMeta[targetLevel]
   if (!meta) return null
   return {
     name: meta.route,
     query: { ...params, level: targetLevel }
   }
 }
 
 /**
  * 生成面包屑导航数据
  */
 function generateBreadcrumbs(currentLevel, labels = {}) {
   const crumbs = []
   for (let lv = DrillLevel.OVERVIEW; lv <= currentLevel; lv++) {
     const meta = DrillMeta[lv]
     const customLabel = labels[lv]
     crumbs.push({
       level: lv,
       label: customLabel || meta.label,
       route: lv < currentLevel ? buildDrillRoute(lv) : null
     })
   }
   return crumbs
 }
 
 /**
  * 模拟数据过滤：基于当前下钻层级和参数过滤数据集
  */
 function filterDataByDrill(data, level, params) {
   if (!data) return null
   switch (level) {
     case DrillLevel.OVERVIEW:
       return data.summary || data
     case DrillLevel.CATEGORY:
       return data.categories?.find(c => c.key === params.category) || data
     case DrillLevel.PROJECT:
       return data.projects?.find(p => p.id === params.projectId) || data
     case DrillLevel.SOURCE:
       return data.sources?.filter(s => s.projectId === params.projectId) || data
     default:
       return data
   }
 }
