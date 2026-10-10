# 用 GitHub 分支做新章节 —— 操作手册

> 适用：每开始一个新章节（如第八章「商品收藏」）
> 仓库：https://github.com/Guayio2026/shopping

---

## 为什么要这样做

| 好处 | 说明 |
|---|---|
| **main 永远干净** | 主线上的代码永远是可运行状态，随时能演示 |
| **写废了能一键丢弃** | 分支上搞砸了，删掉分支即可，不影响 main |
| **学到真实协作流程** | 分支 + Pull Request 是公司里的标准做法，简历能写 |
| **每章一个里程碑** | 合并后 main 上就多一个清晰的提交记录 |

## 整体流程一览

```
main ──●────────────●──────────────●──→   （永远是可用版本）
        \          /                \
         ●──●──●──●                  ●──●──●
         (第八章分支)                 (第九章分支)
           写代码                     写代码
           提交                       提交
           推送 ──→ PR ──→ 合并回 main
```

**一句话：每章开一条分支 → 在上面写 → 写完合并回 main。**

---

## 明天开始前的准备（1 分钟）

**第 0 步：确认 main 是最新的**

双击 **`从GitHub拉取.bat`**，看到 `[OK] Up to date!` 就行。

> 它会自动在你当前所在的分支上拉取最新代码。

---

## 完整流程（8 步）

### 第 1 步：建一条新分支

在 **IDEA 底部的 Terminal** 里执行：

```bash
git checkout main
git checkout -b feature/ch8-collect
```

**这两句什么意思？**

| 命令 | 作用 |
|---|---|
| `git checkout main` | 先切回主分支（确保从最新的 main 开分支） |
| `git checkout -b feature/ch8-collect` | 建一条新分支并切过去（`-b` = create branch） |

**分支命名规范**（照这个来就专业）：

| 前缀 | 用途 | 例子 |
|---|---|---|
| `feature/` | 新功能 | `feature/ch8-collect` |
| `fix/` | 修 bug | `fix/goods-list-paging` |
| `docs/` | 只改文档 | `docs/readme-update` |

执行完，IDEA 右下角会显示 `feature/ch8-collect`（不再是 `main`），说明切换成功。

---

### 第 2 步：写代码

按章节步骤正常写 —— **想怎么改就怎么改，反正改不到 main**。

后端、前端都照常，没有任何区别。

---

### 第 3 步：随时提交

每完成一小块，就双击 **`推送到GitHub.bat`**，输入这次做了什么：

```
Commit message: 第八章：收藏接口后端开发
```

> 脚本会自动识别你当前在 `feature/ch8-collect` 分支上，推到对应分支，不会污染 main。

**建议的提交节奏**（一章大概 3-5 次提交）：

| 时机 | 提交说明示例 |
|---|---|
| 建完表 + 写完实体类 | `第八章：新建 collect 表 + 实体类` |
| 写完后端接口 | `第八章：收藏相关接口开发` |
| 写完前端页面 | `第八章：详情页收藏按钮 + 收藏列表页` |
| 全部测通 | `第八章：收藏功能自测通过` |

---

### 第 4 步：在 GitHub 上开 Pull Request

第一次推送分支后，脚本末尾会给你一个链接：

```
https://github.com/Guayio2026/shopping/compare/main...feature/ch8-collect?expand=1
```

**直接点它**（或去仓库首页，会看到黄色的 `Compare & pull request` 按钮）。

在打开的页面：

1. **base** 选 `main` ← **compare** 选 `feature/ch8-collect`（一般是自动填好的）
2. **标题**填：`第八章 商品收藏`
3. **说明**（可选，建议写）：
   ```
   - 新增 collect 表
   - 后端：收藏 / 取消收藏 / 收藏列表接口
   - 前端：详情页收藏按钮 + 我的收藏页面
   ```
4. 点绿色 **`Create pull request`**

> 💡 **Pull Request（PR）是什么？**
> 就是「嘿，我在分支上做完了一件事，请把它合并到 main」的正式申请。
> 一个人开发时它就是你的「验收单」，多人开发时同事会在上面给你 review。

---

### 第 5 步：合并

PR 页面上：

1. 点 **`Merge pull request`**
2. 再点 **`Confirm merge`**
3. 看到紫色的 `Pull request successfully merged and closed` 就成功了

**合并后 git 会提示可以删掉分支** —— 点 **`Delete branch`**（远端分支删掉，本地分支下一步再删）。

---

### 第 6 步：本地同步 + 清理

回到 IDEA 的 Terminal：

```bash
git checkout main
```

然后**双击 `从GitHub拉取.bat`**，把合并后的 main 拉下来。

最后删掉本地分支：

```bash
git branch -d feature/ch8-collect
```

**这一步做完，第八章的闭环就完整了。**

---

## 一页速查表

| 什么时候 | 做什么 |
|---|---|
| 开新章节 | `git checkout main` → `git checkout -b feature/ch8-xxx` |
| 写代码 | 正常写 |
| 提交 | 双击 **`推送到GitHub.bat`** → 输入说明 |
| 写完了 | GitHub 网页 → **Create pull request** → **Merge** |
| 合并后 | `git checkout main` → 双击 **`从GitHub拉取.bat`** → `git branch -d 分支名` |
| 想看看在哪个分支 | `git branch`（前面带 `*` 的就是当前分支） |

---

## 常见问题

**Q：我忘了建分支，直接在 main 上写了怎么办？**

先提交，然后补救：

```bash
git checkout -b feature/ch8-collect    # 把当前改动带到新分支
git checkout main
git reset --hard origin/main           # main 回退到远端状态
```

（第二条命令会**丢弃 main 上的未推送改动**，所以要先确保改动已经在分支上了）

**Q：分支写着写着发现 main 上有新东西了怎么办？**

把 main 的新内容合到你的分支：

```bash
git checkout main
# 双击 从GitHub拉取.bat
git checkout feature/ch8-collect
git merge main
```

**Q：分支写废了，想全部推翻重来？**

```bash
git checkout main
git branch -D feature/ch8-collect      # 大写 -D 强制删除
```

> 只在**还没推送**或**确定不要了**的时候用。

**Q：PR 页面提示 `Can't automatically merge` 冲突？**

说明 main 和你的分支改了同一个地方。这个情况对一个人开发很少见 ——
真遇到了把提示发我，我带你解决。

**Q：`git checkout` 报错 `Your local changes would be overwritten`？**

说明有改动没提交。先双击 `推送到GitHub.bat` 提交掉，再切换分支。

---

## 记录一下：以后每章的命名

| 章节 | 分支名 | PR 标题 |
|---|---|---|
| 第八章 · 商品收藏 | `feature/ch8-collect` | 第八章 商品收藏 |
| 第九章 · 购物车 | `feature/ch9-cart` | 第九章 购物车 |
| 第十章 · 订单 | `feature/ch10-order` | 第十章 订单管理 |
