 # 高校一体化智慧审计平台 — 后端设计文档
 
 ## 1. 技术选型
 
 | 层级 | 技术 | 版本 |
 |------|------|------|
 | 基础框架 | Spring Boot + RuoYi-Vue-Plus | 3.x (JDK 17+) |
 | ORM | MyBatis-Plus | 3.5.x |
 | 数据库 | MySQL 8.0 + Redis 7.x | — |
 | 权限框架 | Sa-Token（RBAC + 数据权限） | 1.38+ |
 | 流程引擎 | Flowable | 7.0.x |
 | 接口文档 | Knife4j (Swagger) | 4.x |
 | 对象存储 | MinIO / 本地文件系统 | — |
 | 缓存 | Caffeine (L1) + Redis (L2) | — |
 | 日志 | Logback + SkyWalking 探针 | — |
 | 构建工具 | Maven 3.9+ | — |
 
 ## 2. 分层架构
 
 ```
 ┌──────────────────────────────────────────────┐
 │              前端 (Vue3 + Element Plus)        │
 ├──────────────────────────────────────────────┤
 │              Nginx 反向代理 / 负载均衡           │
 ├──────────────────────────────────────────────┤
 │  ┌─────────┐  ┌─────────┐  ┌──────────────┐ │
 │  │ 控制层   │  │  WebSocket│  │ 全局异常处理  │ │
 │  │ Rest API│  │  实时推送  │  │ + 参数校验    │ │
 │  └────┬────┘  └────┬────┘  └──────────────┘ │
 │  ┌────┴─────────────┴──────────────────────┐ │
 │  │            业务服务层 (Service)           │ │
 │  │   审计计划 / 作业 / 整改 / 驾驶舱 / 权限    │ │
 │  └────────────────────┬───────────────────┘ │
 │  ┌────────────────────┴───────────────────┐ │
 │  │            数据访问层 (Mapper)            │ │
 │  │         MyBatis-Plus BaseMapper          │ │
 │  └────────────────────┬───────────────────┘ │
 │  ┌────────────────────┴───────────────────┐ │
 │  │          MySQL 8.0 + Redis 7.x          │ │
 │  └────────────────────────────────────────┘ │
 └──────────────────────────────────────────────┘
 ```
 
 ## 3. 包结构
 
 ```
 com.shenji
 ├── ShenJiApplication.java
 ├── common/
 │   ├── config/         # 安全、CORS、MyBatis-Plus、线程池配置
 │   ├── exception/      # 全局异常定义
 │   ├── interceptor/    # 登录拦截器 / 限流
 │   ├── annotation/     # 自定义注解（@RateLimit, @Log, @DataScope）
 │   ├── aspect/         # AOP 切面（操作日志、数据权限过滤）
 │   └── utils/          # 通用工具（JWT、Excel、树形结构）
 ├── modules/
 │   ├── system/         # 系统管理
 │   │   ├── controller/ # SysUserController, SysRoleController ...
 │   │   ├── service/
 │   │   ├── mapper/
 │   │   └── domain/     # SysUser, SysRole, SysMenu, SysDept ...
 │   ├── audit/          # 审计核心业务
 │   │   ├── plan/       # 审计计划管理
 │   │   ├── workbench/  # 审计作业管理
 │   │   ├── rectify/    # 审计整改管理
 │   │   └── base/       # 审计依据库
 │   ├── dashboard/      # 数据驾驶舱
 │   │   ├── controller/ # DashboardController
 │   │   ├── service/
 │   │   ├── mapper/
 │   │   └── vo/         # 视图对象（聚合数据）
 │   ├── permission/     # 分级权限管理
 │   │   ├── controller/ # DataPermissionController, TempAuthController
 │   │   ├── service/
 │   │   └── handler/    # 数据权限 SQL 拦截器
 │   ├── workflow/       # 审批流程引擎
 │   └── ai/             # AI 智能体
 └── infrastructure/     # 基础设施
     ├── cache/          # Redis / Caffeine 缓存
     ├── mq/             # 消息队列（RocketMQ / RabbitMQ）
     └── oss/            # 对象存储
 ```
 
 ## 4. 数据库设计
 
 ### 4.1 整体 ER 概要
 
 ```
   sys_dept ───< sys_user >─── sys_user_role ───< sys_role >─── sys_role_menu >── sys_menu
                                     │
                                     │ (org_id)
                                     ▼
   ┌─────────────────────────────────────────────────────────────────────────┐
   │                           审计业务表                                      │
   │                                                                          │
   │  audit_plan ──< audit_project ──< audit_task ──< audit_evidence          │
   │                                          │                               │
   │                                          ▼                               │
   │                                    audit_issue ──< rectify_task          │
   │                                          │                               │
   │                                          ▼                               │
   │                                    rectify_record                        │
   │                                                                          │
   │  audit_object_lib  (被审计单位库 — 独立维护)                                │
   │  audit_regulation  (政策法规库)                                           │
   │  audit_risk_base   (问题风险库)                                           │
   │  audit_case_lib    (审计案例库)                                           │
   └─────────────────────────────────────────────────────────────────────────┘
 ```
 
 ### 4.2 权限管理核心表
 
 #### sys_user（用户表）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | user_id | bigint PK | 用户ID |
 | username | varchar(64) | 登录名 |
 | password | varchar(128) | BCrypt 密文 |
 | real_name | varchar(64) | 真实姓名 |
 | avatar | varchar(256) | 头像URL |
 | org_id | bigint FK | 所属机构 |
 | status | tinyint | 0-禁用 1-启用 |
 | is_temp_auth | tinyint | 0-否 1-是（临时授权用户） |
 | temp_auth_expire | datetime | 临时授权过期时间 |
 | create_time | datetime | |
 
 #### sys_role（角色表）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | role_id | bigint PK | |
 | role_code | varchar(64) UNIQUE | school_leader / audit_director / auditor ... |
 | role_name | varchar(64) | 校领导 / 审计处长 / 项目组长 ... |
 | role_level | tinyint | 1-校领导 2-审计处长 3-组长 4-审计员 5-被审单位 6-联络员 7-中介 |
 | data_scope | varchar(16) | all / dept / project / self |
 | status | tinyint | |
 
 #### sys_menu（菜单权限表）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | menu_id | bigint PK | |
 | parent_id | bigint | 上级菜单 |
 | menu_name | varchar(64) | |
 | menu_type | char(1) | M-目录 C-菜单 F-按钮 |
 | perms | varchar(128) | 权限标识 `dashboard:view` |
 | path | varchar(256) | 路由地址 |
 | icon | varchar(64) | 图标 |
 | order_num | int | 排序 |
 
 #### sys_data_permission（数据权限规则表）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | id | bigint PK | |
 | role_id | bigint FK | 适用角色 |
 | scope | varchar(16) | all/dept/project/self |
 | table_name | varchar(64) | 受控表名（audit_project / audit_voucher ...） |
 | filter_sql | varchar(512) | 动态 SQL 过滤片段 |
 | org_ids | varchar(1024) | 可见机构ID列表（JSON数组） |
 
 #### sys_temp_auth（临时授权表）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | id | bigint PK | |
 | user_id | bigint FK | 被授权用户 |
 | auth_type | varchar(16) | data-数据 / function-功能 |
 | resources | varchar(2048) | 授权资源范围 (JSON) |
 | start_time | datetime | |
 | expire_time | datetime | 到期后自动回收 |
 | reason | varchar(512) | 授权原因 |
 | grantor_id | bigint FK | 授权人 |
 | status | tinyint | 0-已撤销 1-生效中 2-已过期 |
 
 #### 五维权限模型关系
 
 ```
   机构(org) ─┬─ 用户(user)
              │
   role ──────┴─────── 功能(menu.perms)
              │
          数据(data_scope + data_permission)
 ```
 
 ### 4.3 驾驶舱相关表
 
 #### dashboard_layout（看板布局）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | id | bigint PK | |
 | user_id | bigint FK | 所属用户 |
 | layout_json | text | 布局JSON: `[{id,type,cols,rows,title},...]` |
 | is_default | tinyint | 是否默认模板 |
 | update_time | datetime | |
 
 #### dashboard_view（个人视图）
 
 | 字段 | 类型 | 说明 |
 |------|------|------|
 | id | bigint PK | |
 | user_id | bigint FK | |
 | view_name | varchar(128) | |
 | layout_json | text | 同 dashboard_layout |
 | create_time | datetime | |
 
 ### 4.4 审计核心业务表（概要）
 
 | 表名 | 核心字段 | 对应模块 |
 |------|----------|----------|
 | audit_plan | plan_code, plan_name, plan_year, plan_type, status, approval_id | 审计计划 |
 | audit_project | project_code, project_name, plan_id, org_id, audit_type, leader_id, start_date, end_date, status | 审计项目 |
 | audit_evidence | evidence_code, project_id, issue_desc, category, amount, risk_level, regulation_ref | 取证/底稿 |
 | audit_issue | issue_code, project_id, evidence_id, category, desc, amount, regulation_id, rect_status | 问题台账 |
 | rectify_task | task_code, issue_id, org_id, deadline, status, rect_measure | 整改任务 |
 | rectify_record | rect_id, task_id, material, proof_files, status | 整改记录 |
 | audit_object | obj_id, obj_name, obj_type, org_info, audit_history | 被审计对象库 |
 | audit_regulation | reg_id, reg_title, reg_type, reg_level, status, content | 政策法规库 |
 | audit_risk | risk_id, risk_name, category, reg_ref, typical_form | 问题风险库 |
 | audit_case | case_id, case_title, case_type, project_ref, summary | 审计案例库 |
 
 ## 5. API 设计
 
 ### 5.1 通用响应体
 
 ```json
 {
   "code": 200,
   "msg": "success",
   "data": {},
   "timestamp": 1751683200000
 }
 ```
 
 ### 5.2 认证接口
 
 | 方法 | 路径 | 说明 |
 |------|------|------|
 | POST | `/api/auth/login` | 登录，返回 Sa-Token |
 | GET | `/api/auth/userinfo` | 获取当前用户信息 |
 | GET | `/api/auth/permissions` | 获取用户功能权限码列表 |
 | GET | `/api/auth/menus` | 获取用户可访问菜单树 |
 | POST | `/api/auth/logout` | 登出 |
 
 **登录请求/响应示例**：
 
 ```
 POST /api/auth/login
 { "username": "auditor", "password": "123456" }
 
 Response:
 {
   "code": 200,
   "data": {
     "token": "sa-token-value-xxxxx"
   }
 }
 ```
 
 ### 5.3 驾驶舱接口
 
 | 方法 | 路径 | 说明 |
 |------|------|------|
 | GET | `/api/dashboard/overview` | 概览数据（6个数据看板指标） |
 | GET | `/api/dashboard/category-stats` | 按项目类型/状态分类统计 |
 | GET | `/api/dashboard/project/{id}` | 项目详情（含图表数据） |
 | GET | `/api/dashboard/source-data` | 底层明细（凭证/合同/支付） |
 | GET | `/api/dashboard/layout` | 获取看板布局 |
 | PUT | `/api/dashboard/layout` | 保存看板布局 |
 | GET | `/api/dashboard/views` | 个人视图列表 |
 | POST/PUT | `/api/dashboard/view` | 新增/编辑个人视图 |
 | DELETE | `/api/dashboard/view/{id}` | 删除个人视图 |
 | GET | `/api/dashboard/warnings` | 预警信息 |
 
 **概览接口响应示例**：
 
 ```json
 {
   "code": 200,
   "data": {
     "totalProjects": 186,
     "totalProjectsThisYear": 42,
     "ongoingProjects": 28,
     "completedProjects": 142,
     "totalIssues": 856,
     "rectifiedIssues": 723,
     "rectifyRate": 84.5,
     "totalAmountInvolved": 125600000,
     "rectifiedAmount": 98600000,
     "projectTypeDist": [
       { "name": "经济责任审计", "value": 45 },
       { "name": "财务收支审计", "value": 68 },
       { "name": "专项审计", "value": 32 },
       { "name": "工程审计", "value": 28 },
       { "name": "其他", "value": 13 }
     ],
     "monthlyTrend": [
       { "date": "2026-01", "value": 8 },
       { "date": "2026-02", "value": 6 }
     ],
     "warnings": [
       { "id": 1, "level": "high", "message": "项目XX超期30天未启动", "detail": "..." }
     ]
   }
 }
 ```
 
 ### 5.4 权限管理接口
 
 | 方法 | 路径 | 说明 |
 |------|------|------|
 | GET | `/api/auth/roles` | 角色列表 |
 | POST | `/api/auth/role` | 新增角色 |
 | PUT | `/api/auth/role` | 编辑角色 |
 | DELETE | `/api/auth/role/{id}` | 删除角色 |
 | GET | `/api/auth/users` | 用户列表（分页） |
 | PUT | `/api/auth/user/{userId}/roles` | 分配用户角色 |
 | POST | `/api/auth/data-permission` | 保存数据权限规则 |
 | GET | `/api/auth/data-permission` | 数据权限规则列表 |
 | POST | `/api/auth/temp-auth` | 创建临时授权 |
 | GET | `/api/auth/temp-auth` | 临时授权列表 |
 | POST | `/api/auth/temp-auth/{id}/revoke` | 撤销临时授权 |
 
 ## 6. 核心模块设计
 
 ### 6.1 分级细粒度权限（模块八）
 
 #### 五维权限模型
 
 ```
           ┌──────────┐
           │   机构    │  用户所属部门/单位 (sys_dept → sys_user.org_id)
           └────┬─────┘
                │
     ┌──────────┼──────────┐
     │          │          │
 ┌───┴───┐ ┌───┴───┐ ┌───┴───┐
 │ 角色  │ │ 用户  │ │ 数据  │
 │ role  │ │ user  │ │ scope │
 └───┬───┘ └───┬───┘ └───┬───┘
     │         │         │
     └────┬────┘         │
          │              │
     ┌────┴────┐    ┌────┴────────────────┐
     │ 功能权限 │    │   数据权限 SQL 拦截   │
     │ (菜单/  │    │   MyBatis 拦截器     │
     │  按钮)  │    │  自动注入 WHERE 条件  │
     └─────────┘    └─────────────────────┘
 ```
 
 #### 功能权限实现：Sa-Token + 注解
 
 ```java
 // 菜单级权限
 @SaCheckPermission("dashboard:view")
 @GetMapping("/overview")
 public R<DashboardVO> overview() { ... }
 
 // 角色级权限
 @SaCheckRole("audit_director")
 @PostMapping("/role")
 public R<Void> createRole() { ... }
 ```
 
 #### 数据权限实现：AOP + MyBatis 拦截器
 
 ```java
 // 1. 自定义注解标记需要数据权限过滤的方法
 @DataScope(deptAlias = "d", userAlias = "u")
 public List<AuditProject> list(AuditProjectQuery query) { ... }
 
 // 2. AOP 切面根据当前用户角色计算 where 条件
 @Aspect
 public class DataScopeAspect {
     @Before("@annotation(dataScope)")
     public void doBefore(DataScope dataScope) {
         SysUser user = SecurityUtils.getLoginUser();
         String scopeSql = buildScopeSql(user);  // 如: "AND d.org_id = 101"
         // 将 scopeSql 写入 BaseContext / ThreadLocal
         DataScopeContextHolder.set(scopeSql);
     }
 }
 
 // 3. MyBatis 拦截器注入 SQL
 @Intercepts(@Signature(type = StatementHandler.class, ...))
 public class DataScopeInterceptor implements Interceptor {
     public Object intercept(Invocation invocation) {
         String sql = boundSql.getSql();
         String scopeSql = DataScopeContextHolder.get();
         if (scopeSql != null) {
             boundSql.setSql(sql + " " + scopeSql);
         }
         return invocation.proceed();
     }
 }
 ```
 
 **数据范围 SQL 生成规则**：
 
 | 角色 | scope | 生成 SQL |
 |------|-------|----------|
 | 校领导 | `all` | 不附加条件（全量汇总，屏蔽财务明细） |
 | 审计处长 | `all` | 不附加条件 |
 | 项目组长 | `project` | `AND p.leader_id = #{userId}` |
 | 审计人员 | `project` | `AND EXISTS (SELECT 1 FROM audit_task t WHERE t.project_id = p.id AND t.assignee_id = #{userId})` |
 | 被审单位负责人 | `dept` | `AND p.org_id = #{userOrgId}` |
 | 联络员 | `dept` | `AND p.org_id = #{userOrgId}` |
 | 中介人员 | `project` | `AND p.id IN (中介分配的项目ID列表) AND p.is_intermediary = 1` |
 
 #### 临时授权过期自动回收
 
 ```java
 @Component
 public class TempAuthExpireJob {
     // XXL-Job 定时任务，每分钟执行
     @XxlJob("tempAuthExpireHandler")
     public void execute() {
         // UPDATE sys_temp_auth SET status = 2
         // WHERE status = 1 AND expire_time < NOW()
         // 同时调用 Sa-Token 踢人下线
         tempAuthService.expireAuths();
     }
 }
 ```
 
 ### 6.2 数据驾驶舱（模块五）
 
 #### 下钻数据流
 
 ```
   ① 全局总览 (OVERVIEW)
   │  数据源: 各表聚合 COUNT / SUM
   │  性能: Redis 缓存，5min 刷新
   ▼
   ② 分类统计 (CATEGORY)
   │  数据源: GROUP BY project_type / status
   │  参数: ?type=economy&year=2026
   ▼
   ③ 项目详情 (PROJECT)
   │  数据源: JOIN audit_project + audit_evidence + rectify_task
   │  参数: /project/SJ-2026-001
   ▼
   ④ 原始明细 (SOURCE)
      数据源: audit_voucher / audit_contract / audit_payment
      参数: ?projectId=SJ-2026-001&type=voucher&page=1
      权限: 仅审计处长 + 项目组长可穿透查看
      隔离: 被审计单位/中介人员 500ms 后返回 "权限不足"
 ```
 
 #### 概览数据 Service
 
 ```java
 @Service
 public class DashboardServiceImpl implements DashboardService {
 
     @Cacheable(value = "dashboard:overview", key = "#user.orgId + '_' + #user.roles[0]")
     public DashboardVO getOverview(SysUser user) {
         DashboardVO vo = new DashboardVO();
 
         // 数据看板指标（按数据权限过滤）
         vo.setTotalProjects(auditProjectMapper.countByScope(user));
         vo.setOngoingProjects(auditProjectMapper.countByStatus(user, "ONGOING"));
         vo.setCompletedProjects(auditProjectMapper.countByStatus(user, "COMPLETED"));
 
         // 问题统计
         vo.setTotalIssues(auditIssueMapper.countByScope(user));
         vo.setRectifiedIssues(auditIssueMapper.countByRectStatus(user, "DONE"));
         vo.setRectifyRate(calculateRate(vo));
 
         // 涉及金额
         vo.setTotalAmountInvolved(auditIssueMapper.sumAmountByScope(user));
 
         // 图表数据
         vo.setProjectTypeDist(auditProjectMapper.groupByType(user));
         vo.setMonthlyTrend(auditProjectMapper.monthlyTrend(user));
 
         return vo;
     }
 }
 ```
 
 #### 大数据量性能优化
 
 - **概览数据**：定时任务（XXL-Job）预聚合到 `dashboard_snapshot` 快照表，接口直接查快照
 - **分类统计**：MySQL GROUP BY + 覆盖索引，结果集 < 100 行
 - **项目详情**：单次 JOIN 3 表以内，避免 N+1 查询
 - **原始明细**：强制分页，`pageSize ≤ 50`，复杂查询走 ElasticSearch（如果后期数据量大）
 
 ## 7. 多角色登录的 Mock 数据示例
 
 后端启动后应预置以下测试账号，对应前端的 7 个演示按钮：
 
 ```
 ┌────────────────────┬──────────────┬──────────────────────────────┐
 │      用户名         │     密码     │         角色 + 权限           │
 ├────────────────────┼──────────────┼──────────────────────────────┤
 │ leader             │ 123456      │ school_leader, 全校汇总       │
 │ admindirector      │ 123456      │ audit_director, 全量全功能    │
 │ manager            │ 123456      │ audit_manager, 项目组范围     │
 │ auditor            │ 123456      │ auditor, 自己的项目          │
 │ depthead           │ 123456      │ auditee_head, 本院数据       │
 │ liaison            │ 123456      │ auditee_liaison, 本院数据    │
 │ agent              │ 123456      │ intermediary, 项目临时权限    │
 └────────────────────┴──────────────┴──────────────────────────────┘
 ```
 
 ## 8. 关键 Maven 依赖
 
 ```xml
 <dependencies>
     <!-- Spring Boot Starter Web -->
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
 
     <!-- Sa-Token 权限认证 -->
     <dependency>
         <groupId>cn.dev33</groupId>
         <artifactId>sa-token-spring-boot3-starter</artifactId>
         <version>1.38.0</version>
     </dependency>
 
     <!-- MyBatis-Plus -->
     <dependency>
         <groupId>com.baomidou</groupId>
         <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
         <version>3.5.7</version>
     </dependency>
 
     <!-- MySQL -->
     <dependency>
         <groupId>com.mysql</groupId>
         <artifactId>mysql-connector-j</artifactId>
     </dependency>
 
     <!-- Flowable 流程引擎 -->
     <dependency>
         <groupId>org.flowable</groupId>
         <artifactId>flowable-spring-boot-starter</artifactId>
         <version>7.0.1</version>
     </dependency>
 
     <!-- XXL-Job 定时任务 -->
     <dependency>
         <groupId>com.xuxueli</groupId>
         <artifactId>xxl-job-core</artifactId>
         <version>2.4.1</version>
     </dependency>
 
     <!-- Knife4j 接口文档 -->
     <dependency>
         <groupId>com.github.xiaoymin</groupId>
         <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
         <version>4.5.0</version>
     </dependency>
 </dependencies>
 ```
 
 ## 9. 部署配置
 
 ```yaml
 # application.yml
 spring:
   datasource:
     url: jdbc:mysql://localhost:3306/shenji_audit?useUnicode=true&serverTimezone=Asia/Shanghai
     username: root
     password: ${DB_PASSWORD}
   redis:
     host: localhost
     port: 6379
   data:
     redis:
       timeout: 5000ms
 
 sa-token:
   token-name: shenji-token
   timeout: 86400           # 1天
   is-concurrent: false     # 不允许并发登录
   is-share: false
   is-log: true
 
 # 自定义配置
 shenji:
   file-upload-path: /data/shenji/files
   temp-auth:
     check-interval: 60     # 临时授权检查间隔（秒）
   dashboard:
     cache-ttl: 300         # 驾驶舱缓存刷新间隔（秒）
 ```
 
 ## 10. 前后端联调对照表
 
 | 前端调用 | 后端接口 | 请求方式 |
 |----------|----------|----------|
 | `api/auth.js` → `login()` | `/api/auth/login` | POST |
 | `api/auth.js` → `getUserInfo()` | `/api/auth/userinfo` | GET |
 | `api/auth.js` → `getUserPermissions()` | `/api/auth/permissions` | GET |
 | `api/dashboard.js` → `getDashboardOverview()` | `/api/dashboard/overview` | GET |
 | `api/dashboard.js` → `getCategoryStats()` | `/api/dashboard/category-stats` | GET |
 | `api/dashboard.js` → `getProjectDetail()` | `/api/dashboard/project/{id}` | GET |
 | `api/dashboard.js` → `getSourceData()` | `/api/dashboard/source-data` | GET |
 | `api/dashboard.js` → `getDashboardLayout()` | `/api/dashboard/layout` | GET |
 | `api/dashboard.js` → `saveDashboardLayout()` | `/api/dashboard/layout` | PUT |
 
 ---
 
 *文档版本: 1.0 | 日期: 2026-07-05*
