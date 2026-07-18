# 高校一体化智慧审计系统

> 基于 Spring Boot + Vue 3 + Element Plus 的前后端分离智慧审计平台

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.7-409EFF?logo=element)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-red)
![Sa-Token](https://img.shields.io/badge/Sa--Token-1.39-blue)
![H2](https://img.shields.io/badge/H2%20Database-2.2-4169E1)
![ECharts](https://img.shields.io/badge/ECharts-5.5-AA344D)
![Pinia](https://img.shields.io/badge/Pinia-2.1-FFD859)
![Maven](https://img.shields.io/badge/Maven-3.8-C71A36?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 在线体验

点击前往 → [高校一体化智慧审计平台](http://118.31.104.154)

---

## 项目简介

面向高校审计业务流程设计的一体化管理平台。系统采用前后端分离架构，后端基于 **Spring Boot 2.7 + MyBatis-Plus + Sa-Token**，前端基于 **Vue 3 + Vite + Element Plus**，内置 **H2 嵌入式数据库**，开箱即用，无需额外安装数据库服务。

### 核心功能

- **数据驾驶舱**：ECharts 可视化图表，支持按审计类型/状态分类统计、数据下钻与明细查看
- **多角色权限体系**：7 种预置角色，覆盖校领导、审计处、被审计单位到中介机构
- **细粒度权限控制**：基于 Sa-Token 的角色/数据/操作三级权限体系
- **临时授权**：支持临时访问权限授予与撤销
- **看板自定义**：拖拽式仪表盘布局，用户可自由配置展示内容

---

## 内置测试账号

系统预置了 7 个角色账号，覆盖完整审计业务链：

| 角色                     | 账号            | 密码     | 说明                     |
|--------------------------|-----------------|----------|--------------------------|
| 校领导                   | `leader`        | 123456   | 只读驾驶舱总览           |
| 审计处长                 | `admindirector` | 123456   | 全部菜单与数据权限       |
| 项目组长 / 主审          | `manager`       | 123456   | 驾驶舱 + 权限管理部分    |
| 普通审计人员             | `auditor`       | 123456   | 基础驾驶舱与自己项目数据 |
| 被审计单位负责人         | `depthead`      | 123456   | 仅本单位数据             |
| 被审计单位联络员         | `liaison`       | 123456   | 仅本单位数据             |
| 中介审计机构人员         | `agent`         | 123456   | 仅项目数据               |

---

## 功能模块

### 数据驾驶舱

| 页面          | 功能                                   |
|---------------|----------------------------------------|
| 全局总览      | 审计项目数量、金额、问题数等核心指标   |
| 分类统计      | 按审计类型/状态分类的图表统计          |
| 原始明细      | 审计项目原始数据明细表                 |
| 项目详情      | 单个审计项目的详细信息                 |
| 个人看板      | 个性化数据展示                        |
| 布局自定义    | 拖拽式看板布局调整                    |

### 权限管理

| 页面          | 功能                                   |
|---------------|----------------------------------------|
| 角色管理      | 角色增删改查与权限配置                 |
| 用户权限      | 用户管理与角色分配                     |
| 数据权限      | 按角色/组织的数据访问权限控制          |
| 临时授权      | 临时权限申请与授权管理                 |
| 项目管理（权限）| 项目级数据权限管理                    |

### 其他

| 页面    | 功能                                   |
|---------|----------------------------------------|
| 登录页  | 账号密码登录                           |
| 403     | 无权限提示页                           |
| 404     | 页面不存在提示页                       |

---

## 技术栈

### 前端

| 技术                | 用途               |
|---------------------|--------------------|
| Vue 3               | 前端框架           |
| Vite 5              | 构建工具           |
| Element Plus 2.7    | UI 组件库          |
| ECharts 5 + vue-echarts | 数据可视化    |
| Pinia 2.1           | 状态管理           |
| Vue Router 4        | 路由管理           |
| Axios               | HTTP 请求          |
| Dayjs               | 日期处理           |
| vuedraggable        | 拖拽交互           |

### 后端

| 技术                    | 用途                   |
|-------------------------|------------------------|
| Spring Boot 2.7.18      | 后端框架               |
| MyBatis-Plus 3.5        | ORM 持久层             |
| Sa-Token 1.39           | 权限认证与鉴权         |
| H2 Database 2.2         | 嵌入式数据库           |
| Lombok                  | 代码简化               |
| Spring AOP              | 面向切面编程           |
| Spring Validation       | 参数校验               |

---

## 项目结构

```
SmartAuditSystem/
│
├── frontend/                      # 前端 - Vue 3 + Vite
│   ├── src/
│   │   ├── views/                 # 页面视图
│   │   │   ├── dashboard/         # 数据驾驶舱
│   │   │   ├── login/             # 登录
│   │   │   ├── permission/        # 权限管理
│   │   │   └── error/             # 403 / 404
│   │   ├── components/            # 通用组件
│   │   ├── layout/                # 布局组件
│   │   ├── store/                 # Pinia 状态管理
│   │   ├── router/                # 路由与权限守卫
│   │   ├── api/                   # Axios 接口封装
│   │   └── assets/                # 样式与图标
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
└── backend/                       # 后端 - Spring Boot + Maven
    ├── src/main/java/com/shenji/
    │   ├── modules/
    │   │   ├── dashboard/         # 仪表盘模块
    │   │   └── system/            # 系统管理模块
    │   ├── common/
    │   │   ├── config/            # Sa-Token、MyBatis-Plus、数据初始化
    │   │   └── exception/         # 全局异常处理
    │   └── ShenJiApplication.java
    ├── src/main/resources/
    │   ├── application.yml        # 主配置
    │   ├── application-dev.yml    # 开发环境配置
    │   ├── schema-h2.sql          # H2 数据库建表脚本
    │   └── init.sql               # MySQL 初始化脚本
    └── pom.xml                    # Maven 依赖
```

---

## 本地启动

### 环境要求

- JDK 17+
- Node.js 18+
- Maven 3.8+

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认运行在 `http://localhost:8088`，H2 数据库控制台访问 `http://localhost:8088/h2-console`

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认运行在 `http://localhost:5173`，前端自动代理 API 请求到后端 8088 端口。

---

## 云端部署

以阿里云 ECS 为例。

### 1. 后端部署

```bash
# 本地打包
cd backend
mvn clean package -DskipTests

# 将 target/shenji-server-1.0.0.jar 上传到服务器
# 服务器上运行
nohup java -jar shenji-server-1.0.0.jar > app.log 2>&1 &
```

### 2. 前端部署

```bash
# 本地构建
cd frontend
npm install
npm run build

# 将 dist/ 目录上传到服务器
```

### 3. 配置 Nginx

```nginx
server {
    listen 80;
    server_name 118.31.104.154;

    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo nginx -s reload
```

### 4. 访问

浏览器打开 `http://你的服务器IP` 即可。

---

## 作者

[@hxxztch](https://github.com/hxxztch)