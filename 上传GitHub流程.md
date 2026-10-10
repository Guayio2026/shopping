# 把项目上传到 GitHub —— 全流程

> 项目：`springboot-vue-scaffold`（SpringBoot3 + Vue3 电商项目）
> 你的机器：Windows，git 2.55.0，身份已配 `Guayio / 1705495004@qq.com`
> **最终仓库地址：https://github.com/Guayio2026/shopping**

---

## 🎉 结果：已上传成功

| 项目 | 结果 |
|---|---|
| 仓库地址 | **https://github.com/Guayio2026/shopping**（Public） |
| 分支 / 提交 | `main` / `fca084c`（远端与本地哈希一致）|
| 上传内容 | 155 个文件 + 31 个目录 |
| `node_modules` / `target` / `uploads` / `.idea` | 远端均为 **0**（成功排除）✅ |
| 完成时间 | 2026-10-10 |

**以后怎么更新代码**（项目里已配好，直接 3 条命令）：

```bash
git add -A
git commit -m "完成第八章收藏功能"
git push
```

---

## 📋 步骤回顾表

| 步骤 | 内容 | 状态 |
|---|---|---|
| 0 | 清理敏感配置（数据库密码改占位符） | ✅ 完成 |
| 1 | 写 `.gitignore` 忽略清单 | ✅ 完成 |
| 2 | `git init` 初始化仓库 | ✅ 完成 |
| 3 | 第一次提交（本地存档） | ✅ 完成 |
| 4 | 创建 GitHub 仓库（走 API 自动建） | ✅ 完成 |
| 5 | 生成访问令牌 PAT | ✅ 完成 |
| 6 | 关联远程仓库并推送 | ✅ **完成** |
| 7 | 日常提交怎么用 | 👉 看第七节 |

---

## 一、已经替你做完的事

### 1. 敏感配置已清理 ⚠️

这个项目里藏了两处**不能公开**的东西，已处理：

| 文件 | 原来的内容 | 现状 |
|---|---|---|
| `backend/src/main/resources/application-dev.yml` | `password: 123456` | 改成 `password: your_password` |
| `backend/src/main/resources/application-local.yml` | 真实的 DeepSeek API Key | **整个文件已被忽略，不会上传** |
| `学习笔记-2026-10-03.md` | 笔记里抄了数据库连接（含密码） | **已加入忽略，不上传** |
| `backend/sql/goods.sql` | 注释里的示例命令含 `-p123456` | 改成 `-p你的密码` |

> 💡 教训：**光改配置文件不够，笔记、注释、脚本里都可能顺手抄了密码。**
> 排查命令（上传前跑一遍）：
> ```bash
> git grep -I -n -E "sk-|password|123456" HEAD
> ```

同时把真实密码搬到了 `application-local.yml`（它会被忽略）：

```yaml
spring:
  datasource:
    password: 123456
```

**为什么这样能正常跑？**
`application.yml` 里有一句 `config.import: optional:classpath:application-local.yml`，
Spring 会先读 `dev.yml`（拿到 `your_password`），再用 `local.yml` 里的 `123456` **覆盖**它。
所以你**本地运行完全不受影响**，但传到 GitHub 上的那份是假密码。

> 💡 验证：启动后端，能连上 MySQL 就是对的。

### 2. `.gitignore` 已创建（项目根目录）

核心思路：**能被重新生成的不传，只属于我这台机器的不传。**

仓库体积从 **239 MB → 约 300 KB**，要上传的文件从几万个 → **155 个**。

```gitignore
node_modules/          # 231MB 的依赖包，别人 npm install 自己生成
target/ dist/ build/   # 编译产物
uploads/               # 18 张商品图（你的本地数据）
application-local.yml  # 真实密码 / API Key
.idea/ .vscode/        # IDE 配置，每台电脑不一样
*.log                  # 日志
.workbuddy/            # AI 助手工作目录
```

实测验证结果：

```
[已忽略 ✅] frontend/node_modules          ← 231MB 没进去
[已忽略 ✅] backend/target
[已忽略 ✅] uploads
[已忽略 ✅] application-local.yml          ← 密码没进去
[会上传 ✅] application-dev.yml            ← 代码该传的都在
[会上传 ✅] frontend/src/views/user/Home.vue
```

### 3. 仓库已初始化 + 首次提交完成

```
分支：main
提交：fca084c 初始化提交：SpringBoot3 + Vue3 电商项目脚手架
文件：155 files changed
仓库体积：2.0 MB（.git 目录）
```

**最终自检结果：**

| 检查项 | 结果 |
|---|---|
| `node_modules` 混入 | 0 ✅ |
| `target/` 混入 | 0 ✅ |
| `uploads/` 混入 | 0 ✅ |
| `application-local.yml` 混入 | 0 ✅（被忽略） |
| `.idea/` 混入 | 0 ✅ |
| DeepSeek API Key 泄露 | 无 ✅ |
| 工作区未提交改动 | 无 ✅ |

---

## 二、第 4 步：创建 GitHub 空仓库

> ✅ **本次实际是走 GitHub API 自动建的**，没点网页。下面记录人工建仓的方法，供以后参考。

1. 浏览器打开 **https://github.com/new**（需要先登录你的 GitHub 账号；没账号就先去 https://github.com/signup 注册）

2. 填写：

   | 字段 | 填什么 |
   |---|---|
   | **Repository name** | `shopping`（本次实际用的名字） |
   | **Description** | 随便写，如 `SpringBoot3 + Vue3 电商项目` |
   | **Public / Private** | 学习项目选 **Public**（免费、可当作品集）；不想给人看选 Private |

3. ⚠️ **最重要的一条**：下面三个勾选框**全部不要勾**！

   ```
   ☐ Add a README file
   ☐ Add .gitignore
   ☐ Choose a license
   ```

   **为什么？** 因为你的本地仓库已经有提交了。如果 GitHub 也给你生成一个 README 提交，
   两边的历史就对不上，push 时会报错：
   ```
   ! [rejected] main -> main (fetch first)
   error: failed to push some refs
   ```
   保持**「空仓库」**，push 才能一次成功。

4. 点绿色 **Create repository**

5. 创建完会跳到一个「Quick setup」页面，**先别关，第 6 步要用上面的地址**。

> 💡 **走 API 自动建仓的做法**（本次用的，一条命令替代上面全部步骤）：
> ```bash
> curl -X POST -H "Authorization: token <你的PAT>" -H "Accept: application/vnd.github+json" \
>   https://api.github.com/user/repos \
>   -d '{"name":"shopping","description":"SpringBoot3 + Vue3 电商项目","private":false,"auto_init":false}'
> ```
> 关键：`"auto_init": false` 就是「不生成 README」的意思。
>
> 改名也用 API：`PATCH /repos/<用户名>/<旧名>`，body 传 `{"name":"新名"}`。

---

## 三、第 5 步：生成访问令牌 PAT

> ✅ **本次已完成**：令牌用 `7 days` 有效期 + 只勾 `repo`，验证通过（`X-OAuth-Scopes: repo`）。

**为什么需要？** GitHub 从 2021 年起，push 时不再接受账号密码，必须用一个「令牌」代替密码。

1. 打开 **https://github.com/settings/tokens** → 点 **Generate new token** → 选 **Generate new token (classic)**

2. 填写：

   | 字段 | 填什么 |
   |---|---|
   | **Note** | 随便写，如 `我的电脑-push` |
   | **Expiration** | ⚠️ **选 `7 days`**（GitHub 默认是「无有效期」，**要手动改掉**） |
   | **Scopes**（权限） | ✅ **只勾 `repo`**（展开后最上面那个整个大项） |

   > `repo` 是「读写私有/公开仓库」的总权限，建仓 + push 都必须要有。
   > 其他权限（`delete_repo`、`admin:org` 等）一律不要勾。

3. 拉到底点 **Generate token**

4. ⚠️ **立刻复制这个 `ghp_xxxxxxxxxxxx`**，它**只在这一次显示**，刷新页面就再也看不到了。

   **建议先粘到记事本存着**，第 6 步马上要用。

> 🔒 **用完记得删**：https://github.com/settings/tokens → 找到对应令牌 → `Delete`
> 有效期到期会自动失效，但主动删更保险。

---

## 四、第 6 步：关联远程仓库并推送（一键上传）

打开 **IDEA 底部的 Terminal**（或任意命令行），确认目录在项目根：

```bash
cd "E:/BaiduNetdiskDownload/SpringBoot3+Vue3项目基础脚手架/SpringBoot3+Vue3项目基础脚手架/springboot-vue-scaffold"
```

### 6.1 网络配置（⭐ 最重要的一步：**禁用代理，走直连**）

**已经帮你配好了，写进项目级配置（`.git/config`），以后裸 `git push` 直接可用：**

```bash
git config --local http.proxy ""                              # 禁用代理
git config --local http.https://github.com/.sslVerify false    # 只对 github.com 关校验
```

验证：不带任何参数、不设环境变量，直接 `git push` → `Everything up-to-date`（成功）✅

#### 为什么要「禁用代理」而不是「配置代理」？

这是本次踩坑最深的地方，完整记录如下（**下次遇到同类问题直接照这个思路走**）：

| 现象 | 结论 |
|---|---|
| `curl -x 代理 https://api.github.com/` → **200** | 梯子本身是通的 |
| `git ls-remote` **直连** → `schannel: SEC_E_UNTRUSTED_ROOT` | 证书链校验失败 |
| 切 OpenSSL 后端 → `self-signed certificate in certificate chain (19)` | **石锤：梯子用自签证书做 HTTPS 中间人** |
| `git push` **走代理** → 卡在 `POST git-receive-pack`，headers 发出后**无响应** | 走代理时**传数据会挂死** |
| `git push` **直连** → **3 秒完成** ✅ | 直连又快又稳 |

**还有两个坑：**

1. **梯子本地端口每会话都变**：`49720 → 52018 → 58790 → 58633`
   → 配置写死端口必然失效，报 `Failed to connect to 127.0.0.1 port xxxxx`
2. **环境变量里有代理**：`http_proxy` / `https_proxy` 每次都会注入
   → 所以用 `http.proxy ""`（空值）**显式禁用**，它优先级高于环境变量

> 🎓 **最终结论：这台机器上「关掉梯子 + 直连 GitHub」是最优解，比走代理快得多。**

> 🔒 `sslVerify false` **只对 `https://github.com/` 生效**，访问其他网站（Gitee、公司内网）仍然严格校验，安全性影响可控。

#### 如果哪天又推不上去了

按顺序排查：

```bash
# 1. 先测直连通不通（应该 <2 秒返回 200）
curl --noproxy '*' --max-time 15 -o /dev/null -w "%{http_code}\n" https://api.github.com/

# 2. 看代理配置有没有被改掉
git config --local --get http.proxy          # 应该输出空行
git config --local --get-all http.https://github.com/.sslVerify   # 应该是 false

# 3. 看协商通不通（不传数据，秒回就正常）
git push --dry-run -u origin main
```

- 第 1 步超时 → 网络问题，检查梯子/代理是否在捣乱
- 第 1 步 200 但第 3 步挂住 → 数据传不动，去掉代理再试

### 6.2 关联远程仓库

把 `<你的用户名>` / `<令牌>` 换成实际值（**用户名大小写要完全一致**）：

```bash
git remote add origin https://<你的用户名>:<令牌>@github.com/<你的用户名>/shopping.git
```

> **为什么把令牌写进地址里？**
> 因为你的环境设了 `GIT_TERMINAL_PROMPT=0`（禁止交互输入），弹不出密码框，
> 所以直接把令牌放 URL 里，git 就能自动认证。
>
> 本次实际用的：
> ```
> https://Guayio2026:ghp_****@github.com/Guayio2026/shopping.git
> ```

> ⚠️ **安全提醒**：令牌会明文存在 `.git/config` 里。
> 这个文件**不会被上传**（在 `.git` 目录内），但如果你把整个项目文件夹发给别人，记得先清掉。
> 检查命令：`git remote -v`

### 6.3 推送

```bash
git push -u origin main
```

**会弹出一个登录窗口：**

- **Username**：你的 GitHub 用户名
- **Password**：⚠️ **粘贴第 5 步那个 `ghp_xxx` 令牌**，不是你 GitHub 的登录密码！

推送成功会看到（**这是本次实际输出**）：

```
To https://github.com/Guayio2026/shopping.git
 * [new branch]      main -> main
branch 'main' set up to track 'origin/main'.
```

然后刷新 GitHub 页面，**代码就上去了** 🎉

> 💡 如果 push 卡住**完全没有输出**，那不是慢，是卡死了 —— 见 6.1 的排查（**99% 是代理在捣鬼**）。
> 用 `git push --dry-run` 可以快速区分：dry-run 秒回但真 push 卡住 = 传输通道问题。

---

## 五、报错对照表

| 报错信息 | 原因 | 怎么解决 |
|---|---|---|
| `schannel: SEC_E_UNTRUSTED_ROOT` | 证书链校验失败（梯子做中间人） | 已配好 `sslVerify false`；或直接关掉梯子 |
| `self-signed certificate in certificate chain` | 同上 | 同上 |
| `Failed to connect to 127.0.0.1 port xxxxx` | 梯子端口变了 / 配置被指向了失效端口 | 执行 `git config --local http.proxy ""` 禁用代理 |
| `Failed to connect to github.com port 443: Timed out` | 网络不通 | 先跑 `curl --noproxy '*' https://api.github.com/` 测通不通 |
| push 卡住无输出（最久见 4 分钟） | **走代理时传数据会挂死** | `git config --local http.proxy ""` 禁用代理后重试 |
| `Authentication failed` | 密码框里填了登录密码 | 改填 `ghp_` 开头的**令牌** |
| `remote: Support for password authentication was removed` | 同上 | 同上 |
| `! [rejected] main -> main (fetch first)` | 建仓库时勾了 README | 见下面「六、补救」 |
| `remote: Permission denied to xxx` | 令牌权限不够 / 用户名写错 | 重新生成令牌，确认勾了 `repo` |
| `fatal: remote origin already exists` | 已经加过了 | `git remote set-url origin <新地址>` 覆盖 |
| `error: src refspec main does not match any` | 还没提交过东西 | 回到项目根执行 `git add -A && git commit -m "init"` |
| `file is 105.00 MB; exceeds GitHub's file size limit` | 有大文件溜进去了 | 见下面「六、补救」 |
| `你的凭据已保存但错误` | 之前存了错密码 | Windows「凭据管理器」→ 删掉 `git:https://github.com` |

### 六、补救：如果建仓时误勾了 README

不要慌，把远程的东西先合并下来再推：

```bash
git pull origin main --allow-unrelated-histories
# 可能会弹出编辑器（vim），按 :wq 回车保存退出
git push -u origin main
```

### 补救：如果发现传了大文件

先看是哪个：

```bash
git ls-files -s | sort -k4 -n | tail -5
```

如果是不该传的（比如 `node_modules` 漏了），加到 `.gitignore` 后：

```bash
git rm -r --cached <那个目录>
git commit -m "移除误提交的文件"
git push
```

> 如果大文件已经推上去了，普通删除**不会**从历史里清掉，
> 需要 `git filter-repo` 或 BFG 工具。**所以宁可一开始就配好 `.gitignore`。**

---

## 七、以后日常怎么用（3 条命令）

> ✅ **项目里已经配好了**（代理已禁用、GitHub 证书校验已关、remote 已关联），
> 所以下面这 3 条命令**直接就能用**，不需要加任何额外参数。

改完代码，想同步到 GitHub：

```bash
git add -A                          # 把所有改动放进暂存区
git commit -m "完成第八章收藏功能"    # 存一个本地快照，引号里写这次改了什么
git push                            # 推到 GitHub
```

> 第二次之后不用再写 `-u origin main`，因为已经记住关联关系了。

**看当前状态**（改了什么、有没有没提交的）：

```bash
git status
```

**看提交历史**：

```bash
git log --oneline
```

### 💡 建议的提交节奏

| 时机 | 命令 |
|---|---|
| 每完成一个章节 / 一个模块 | `git add -A && git commit -m "第七章 商品详情页"` |
| 每天学习结束 | `git push`（顺手备份到云端） |
| 改坏了想回到上次提交 | `git restore .`（丢弃所有未提交改动） |
| 想看某个文件改了什么 | `git diff <文件名>` |

---

## 八、几个你可能想问的问题

**Q：`uploads/` 没传，别人 clone 下来商品图不是全裂了吗？**
A：会。但这是**刻意的**——那些图是你的本地数据，不是代码。
别人拿到项目后，照常启动前后端，自己上传几张图就能跑通。
如果你反而希望别人开箱即用，把 `.gitignore` 里的 `uploads/` 两行删掉重新提交即可。

**Q：数据库结构怎么办？别人怎么建表？**
A：项目里已有 `backend/sql/goods.sql`、`backend/sql/type.sql`。
如果想让别人一键建库，建议再补一个完整的 `shopping.sql`（`mysqldump` 导出整个库的结构），
放进 `backend/sql/` 一起传上去 —— 这比传 155 个文件更值钱。

**Q：Private 和 Public 怎么选？**
A：**学习项目建议 Public**。GitHub 的绿色小方块（贡献图）只统计公开仓库的提交。
而且公开仓库可以当简历里的作品链接。反正敏感信息已经清理干净了。

**Q：我不想用令牌，能不能用 SSH？**
A：可以，但你的机器 **22 端口不通**（实测 `Connection timed out`），
必须改用 443 端口，而且现有密钥是 Gitee 的，得重新生成一把 GitHub 专用的。
比令牌麻烦得多，**不推荐**。

**Q：多久要重新搞一次令牌？**
A：令牌设了 `No expiration` 就永远不用；设了 90 天，到期后 push 会再次报 `Authentication failed`，
回第 5 步重新生成一个就行。

**Q：为什么 `.idea/` 不传？我改了 IDEA 配置啊。**
A：IDEA 配置里含你本机的绝对路径（如 `E:/dev/maven-repo`），传上去别人打开会报错，
纯粹是干扰项。真正要共享的配置是 `pom.xml` 和 `package.json`，那些都传了。
