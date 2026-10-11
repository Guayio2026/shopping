# SpringBoot3 + Vue3 电商项目

一个**前后端分离**的电商系统，包含后台管理系统和前台商城，正在跟教程一步步手写完成。

> 🚧 **项目进行中**：目前完成到「商品收藏」，后续持续更新。

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.3.7 | 主框架 |
| Java | 17 | 开发语言 |
| MyBatis | 3.0.4 | 持久层（原生 XML 配置，非 MyBatis-Plus） |
| MySQL | 5.7 | 数据库 |
| JJWT | 0.12.6 | JWT 登录认证 |
| SpringDoc | 2.6.0 | 接口文档（Swagger UI） |

### 前端

| 技术 | 版本 | 说明 |
|---|---|---|
| Vue | 3.5 | 框架 |
| Vite | 4.5 | 构建工具 |
| Element Plus | 2.9 | UI 组件库 |
| Pinia | 2.3 | 状态管理 |
| Vue Router | 4.5 | 路由 |
| Axios | 1.7 | HTTP 请求 |
| ECharts | 6.1 | 数据可视化 |
| wangEditor | 5.1 | 富文本编辑器 |

---

## 功能清单

### ✅ 后台管理端（`/admin`）

| 模块 | 功能 |
|---|---|
| 登录鉴权 | JWT 拦截器，未登录自动跳转 |
| 首页看板 | 数据统计 + 图表 |
| 商品管理 | 增删改查 + 多图上传 + 规格管理 + 富文本详情 |
| 分类管理 | 增删改查 |
| 轮播图管理 | 增删改查 + 关联商品 |
| 收藏管理 | 只读列表（用户 / 商品 / 收藏时间）+ 分页 + 详情 / 删除 |
| 用户管理 | 增删改查 |
| 菜单管理 | 动态菜单（数据库驱动，`sys_menu` 表） |
| 操作日志 | 记录与查询 |
| 个人中心 / 系统设置 | — |

### ✅ 前台商城（`/user`）

| 页面 | 功能 |
|---|---|
| 首页 | 轮播图 + 左侧分类导航 + 新品推荐 + 热销推荐 |
| 全部商品 | 关键词搜索 + 分类筛选 + 分页 |
| 商品详情 | 商品图文详情 + 购买须知 + 数量选择 |
| 我的收藏 | 收藏的商品网格 + 取消收藏 + 空状态（需登录） |

### 🚧 计划中

- 购物车
- 下单与订单管理

---

## 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.8+
- **Node.js** 16+
- **MySQL** 5.7+

### 第 1 步：创建数据库

```sql
CREATE DATABASE shopping
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> ⚠️ 注意：仓库里目前只带了部分建表脚本（`backend/sql/goods.sql`、`backend/sql/type.sql`），
> 完整的库结构脚本还在整理中。首次运行建议参考 `backend/src/main/resources/schema.sql`。

### 第 2 步：配置数据库密码

数据库密码**不写在 `application-dev.yml` 里**（那个文件是要提交到仓库的）。请在同目录新建一个
`application-local.yml`（该文件已被 `.gitignore` 排除，不会上传），照 `application-local.yml.example` 抄：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
```

> ⚠️ **千万别**在 `application-dev.yml` 里加 `password:` 字段。
> 那里的优先级**高于** `application.yml` 中 `spring.config.import` 导入的 `application-local.yml`，
> 会把你的真实密码压掉，启动时直接报 `Access denied for user 'root'@'localhost'`，而且进程会立刻退出。

### 第 3 步：启动后端

```bash
cd backend
mvn spring-boot:run
```

- 服务地址：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/api/swagger-ui.html
- 健康检查：http://localhost:8080/api/health

### 第 4 步：启动前端

```bash
cd frontend
npm install
npm run dev
```

- 访问地址：http://localhost:5173

> 前端已在 `vite.config.js` 里配好代理：`/api` 和 `/uploads` 都会转发到后端 8080 端口。

### 第 5 步：登录

| 端 | 地址 | 账号 | 密码 |
|---|---|---|---|
| **后台管理** | http://localhost:5173/admin | `admin` | `admin123` |
| **前台商城** | http://localhost:5173/user | 免登录 | — |

---

## 目录结构

```
springboot-vue-scaffold/
├── backend/                          # 后端服务
│   ├── sql/                          # 建表脚本
│   └── src/main/
│       ├── java/com/scaffold/
│       │   ├── controller/           # 接口层（含 admin/ 子包）
│       │   ├── service/              # 业务层
│       │   ├── mapper/               # 持久层接口
│       │   ├── entity/               # 实体类
│       │   ├── dto/                  # 数据传输对象
│       │   ├── common/               # 统一返回、异常
│       │   ├── config/               # 配置类
│       │   └── security/             # JWT 拦截器
│       └── resources/
│           ├── mapper/               # MyBatis XML（SQL 写在这里）
│           └── application*.yml      # 配置文件
│
└── frontend/                         # 前端工程
    └── src/
        ├── api/                      # 接口封装
        ├── views/                    # 页面
        │   ├── user/                 # 前台页面
        │   └── ...                   # 后台页面
        ├── router/                   # 路由配置
        ├── store/                    # Pinia 状态
        └── utils/                    # 工具函数
```

---

## 常见问题

**Q：商品图片显示不出来？**

本项目**没有上传 `uploads/` 目录**（那是本地运行产生的数据，不属于代码）。
你需要自己后台上传商品图，或手动把图片放进项目根目录的 `uploads/` 文件夹。

**Q：数据库连不上 / 报 Access denied？**

检查 `backend/src/main/resources/application-local.yml` 是否存在、里面的密码是不是你自己的。

⚠️ 注意：**不要在 `application-dev.yml` 里加密码** —— 那里的优先级会压掉 `application-local.yml`，
反而导致启动失败（详见「快速开始 · 第 2 步」）。

**Q：上传的图片存在哪儿？**

项目根目录的 `uploads/`（不是 `backend/uploads/`）。
因为 `application.yml` 里配的是相对路径 `app.upload.path: uploads`，
而 IDEA/Maven 的工作目录是项目根目录。

---

## 开发笔记

这个项目是跟着教程手写的练习项目，每一步的详细操作手册都在项目根目录：

| 文档 | 内容 |
|---|---|
| `第五章-前台首页-步骤.md` | 前台首页（轮播图 + 分类 + 新品热销） |
| `第六章-全部商品页-步骤.md` | 商品列表（搜索 + 筛选 + 分页） |
| `第七章-商品详情页-步骤.md` | 商品详情（图文 + 购买须知） |
| `第八章-收藏模块-步骤.md` | 商品收藏（前台我的收藏 + 后台收藏管理） |
| `上传GitHub流程.md` | 把项目推到 GitHub 的完整流程 |
| `GitHub分支工作流.md` | 新章节怎么用分支 + Pull Request 开发 |

---

## License

仅用于学习交流。
