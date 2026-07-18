# 高校一体化智慧审计系统

> 基于 Spring Boot + Vue 3 + Element Plus 的前后端分离智慧审计平台

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.7-409EFF?logo=element)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-red)
![Sa-Token](https://img.shields.io/badge/Sa--Token-1.39-blue)
![H2](https://img.shields.io/badge/H2%20Database-2.2-4169E1?logo=h2)
![ECharts](https://img.shields.io/badge/ECharts-5.5-AA344D?logo=echarts)
![Pinia](https://img.shields.io/badge/Pinia-2.1-FFD859?logo=pinia)
![Maven](https://img.shields.io/badge/Maven-3.8-C71A36?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 在线体验

🌐 [http://118.31.104.154](http://118.31.104.154)

---

## 项目简介

面向高校审计业务流程设计的一体化管理平台。系统采用前后端分离架构，后端基于 **Spring Boot 2.7 + MyBatis-Plus + Sa-Token**，前端基于 **Vue 3 + Vite + Element Plus**，内置 **H2 嵌入式数据库**，开箱即用，无需额外安装数据库服务。

### 核心能力

- **多维度审计仪表盘**：ECharts 可视化图表，支持数据下钻与明细查看
- **细粒度权限控制**：基于 Sa-Token 的角色/数据/操作三级权限体系
- **全流程项目管理**：项目创建、进度跟踪、审计记录与归档
- **临时授权机制**：支持临时访问授权与审批流转
- **数据导出**：支持 Excel 格式导出审计报表与明细数据

---

## 功能特性

| 模块       | 功能                               |
|------------|------------------------------------|
| 📊 仪表盘  | 数据概览、图表分析、下钻查询       |
| 🔐 权限管理 | 角色管理、数据权限、用户权限       |
| 📋 项目管理 | 项目创建、进度跟踪、审计记录       |
| 👤 个人中心 | 个人信息、待办任务、审计历史       |
| 🔑 临时授权 | 临时访问授权、审批流程             |
| 📥 数据导出 | Excel 报表导出                     |

---

## 技术栈

### 前端

| 技术              | 用途               |
|-------------------|--------------------|
| Vue 3             | 前端框架           |
| Vite 5            | 构建工具           |
| Element Plus 2.7  | UI 组件库          |
| ECharts 5 + vue-echarts | 数据可视化  |
| Pinia 2.1         | 状态管理           |
| Vue Router 4      | 路由管理           |
| Axios             | HTTP 请求          |
| Dayjs             | 日期处理           |
| XLSX              | Excel 导入导出     |
| vuedraggable      | 拖拽交互           |

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
│   │   │   ├── dashboard/         # 仪表盘（概览、分类、项目、详情）
│   │   │   ├── login/             # 登录注册
│   │   │   ├── permission/        # 权限管理（角色、用户、数据权限）
│   │   │   └── error/             # 403 / 404
│   │   ├── components/            # 通用组件（图表、表格、树形权限）
│   │   ├── layout/                # 布局组件（主布局、仪表盘布局）
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
    │   │   ├── dashboard/         # 仪表盘（Controller、Service、Mapper、VO）
    │   │   └── system/            # 系统管理（用户、角色、菜单、权限）
    │   ├── common/
    │   │   ├── config/            # Sa-Token、MyBatis-Plus、数据初始化
    │   │   └── exception/         # 全局异常处理
    │   └── ShenJiApplication.java
    ├── src/main/resources/
    │   ├── application.yml        # 主配置
    │   ├── application-dev.yml    # 开发环境配置
    │   ├── schema-h2.sql          # H2 数据库建表脚本
    │   └── init.sql               # 初始化数据
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

默认运行在 `http://localhost:5173`，前端会自动代理 API 请求到后端 8088 端口。

### 测试登录

系统内置测试账号：

| 角色   | 账号     | 密码     |
|--------|----------|----------|
| 管理员 | admin    | admin123 |
| 普通用户 | user   | user123  |

---

## 云端部署

以下以 **阿里云 ECS（CentOS / Ubuntu）** 为例，其他云服务器操作类似。

### 1. 后端部署

```bash
# 在本地打包
cd backend
mvn clean package -DskipTests

# 将 target/shenji-server-1.0.0.jar 上传到服务器
# 然后在服务器上运行
nohup java -jar shenji-server-1.0.0.jar > app.log 2>&1 &
```

### 2. 前端部署

```bash
# 在本地构建
cd frontend
npm install
npm run build

# 将 dist/ 目录上传到服务器
```

### 3. 配置 Nginx 反向代理

```nginx
server {
    listen 80;
    server_name 118.31.104.154;

    # 前端静态文件
    root /path/to/dist;
    index index.html;

    # 单页应用路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://127.0.0.1:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # H2 Console（非必需，建议关闭）
    location /h2-console/ {
        proxy_pass http://127.0.0.1:8088;
    }
}
```

```bash
# 重启 Nginx
sudo nginx -s reload
```

### 4. 访问

浏览器打开 `http://你的服务器IP` 即可。

---

## 作者

[@hxxztch](https://github.com/hxxztch)