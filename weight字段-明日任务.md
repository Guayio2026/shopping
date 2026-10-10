# 明日任务 · 给商品管理2 加一个「重量 weight」字段

> **日期**：2026-10-07（明天）
> **对象**：`商品管理2` 模块（`goods2` 表 / `Goods2.java` / `Goods2Mapper.xml` / `Goods2.vue`）
> **目标**：让「重量」**弹窗里能填，列表里能看**
> 本文件是**操作面板**，明天照着做，不用翻聊天记录。

---

## 0. 今天（10-06）已经达成什么

| 模块 | 状态 |
|---|---|
| 商品管理2 | ✅ **11/11 全通**，15 项验收全过，与老模块数据完全隔离 |
| 附件 | `商品管理2-操作手册.md`（含踩坑总结） |

**今天真正学到的**：
- `Ctrl+R` 勾了 `W`（全词匹配）会漏掉 `GoodsMapper` / `GoodsService` 这类**拼接词** → 症状是"能跑但跑错表"（静默 bug，比编译报错难查）
- 路由的输入是**网址**不是点击
- 菜单入口（`sys_menu`）和路由（`router/index.js`）是两件事

---

## 1. 明天开机要做的事（3 件）

| 检查 | 怎么做 |
|---|---|
| MySQL | 服务 `MySQL57` 是否在跑（电脑睡眠会杀掉它） |
| 后端 | IDEA 点绿色 ▶，看到 `Started ScaffoldApplication` |
| 前端 | VS Code 里 `npm run dev`，`localhost:5173` 能打开 |

**先自测一下现在是否正常**：打开 `http://localhost:5173/admin/goods2`，能看到 2 条「测试商品A」，分类列显示「食品」，发布者显示「演示用户」。

---

## 2. 为什么这个练习值得做（先读，再动手）

### 真实工作里最高频的改动，就是「给已有功能加一个新字段」

而且这个练习会逼你搞清一个**关键区别**：

| 情况 | 例子 | 要改几层 |
|---|---|---|
| **数据库本来就有这一列** | 上架日期 `date`（10-06 你做的） | **只改前端 1 层** —— 因为管道早就铺好了，只差一个输入框 |
| **数据库没有这一列** | 重量 `weight`（明天要做） | **4 层全改** —— 因为管道本身不存在，得从数据库一层层铺下来 |

**同一个项目，两种完全不同的改法。做完这一遍，你以后看到"加个字段"的需求，第一反应就是先去查数据库有没有这一列。**

---

## 3. 核心原理：4 层管道

```
① 数据库  goods2 表          有没有 weight 这一列？
      ↓
② 实体类  Goods2.java        有没有 weight 字段？
      ↓
③ SQL     Goods2Mapper.xml   查 / 增 / 改的时候带没带上 weight？
      ↓
④ 前端    Goods2.vue         用户能不能填到？列表能不能看到？
```

**一句话判断法**：
> 数据要从数据库一路流到页面，中间任何一层断了，它就过不去。

---

## 4. 要改的 13 处（做完一个划一个）

| 阶段 | # | 改哪里 | 状态 |
|---|---|---|---|
| **A 数据库** | 1 | `goods2` 表加 `weight` 列 | ⬜ |
| **B 实体类** | 2 | `Goods2.java` 加字段 | ⬜ |
| **C XML** | 3 | `resultMap` 加映射 | ⬜ |
| | 4 | `baseColumns` 加 `g.weight` | ⬜ |
| | 5 | `insert` 的列清单加 `weight` | ⬜ |
| | 6 | `insert` 的 VALUES 加 `#{weight}` | ⬜ |
| | 7 | `update` 的 `<set>` 加一个 `<if>` | ⬜ |
| **D 前端** | 8 | `form` 定义加 `weight` | ⬜ |
| | 9 | `resetForm()` 加 `weight` | ⬜ |
| | 10 | `openDialog()` 加 `weight` ⚠️**最容易漏** | ⬜ |
| | 11 | `payload` 加 `weight` ⚠️**注意逗号** | ⬜ |
| | 12 | 弹窗模板加输入框 | ⬜ |
| | 13 | 列表表格加一列 | ⬜ |
| **E 验收** | — | 重启后端 + 逐项测试 | ⬜ |

**⚠️ 有 3 层不用改**（见第 8 节），这 13 处里没有它俩 —— 想清楚为什么，是明天的重点。

---

# 阶段 A · 数据库（第 1 处）

## 步骤 1 · 给 `goods2` 表加列

**Navicat**：右键 `goods2` 表 → 设计表 → 在 `price` 下面加一行 → 保存
**或者** 新建查询执行 SQL：

```sql
ALTER TABLE goods2
  ADD COLUMN weight double(10,2) DEFAULT NULL COMMENT '重量(kg)' AFTER price;

-- 验证
SHOW COLUMNS FROM goods2;
```

**按用户实际表结构填**：

| 字段名 | 类型 | 允许 NULL | 默认值 | 注释 |
|---|---|---|---|---|
| `weight` | `double(10,2)` | 是 | NULL | 重量(kg) |

> 💡 **为什么用 `double(10,2)`**：和 `price` 一样 —— 重量是小数（1.5kg、0.25kg），`(10,2)` 表示总共 10 位、保留 2 位小数。

**做完检查**：`SHOW COLUMNS FROM goods2;` 里能看到 `weight`，位置在 `price` 后面。

> ⚠️ **加列不会影响已有数据** —— 你那 2 条商品的 `weight` 会是 NULL（表示"没填"）。

---

# 阶段 B · 实体类（第 2 处）

## 步骤 2 · `Goods2.java` 加字段

**位置**：`backend/src/main/java/com/scaffold/entity/`

**找到第 51 行**：

```java
    /** 价格（goods 表列：price，double(10,2)）→ 前端 row.price */
    private Double price;
```

**在它下面加一行**。

**自己想两个问题**（先别翻答案）：
1. 类型写什么？`Integer` 还是 `Double`？
2. 要不要写 `@TableField` 之类的注解？

**做完检查**：`Ctrl + F9` 编译，0 红字。

> 💡 提示：去看看 `price` 那一行怎么写的，照着来。

---

# 阶段 C · SQL 映射（第 3~7 处）

## 🚨 全程手动改，禁止 `Ctrl+R`

文件：`backend/src/main/resources/mapper/Goods2Mapper.xml`

---

## 步骤 3 · `resultMap` 加映射（找到第 65 行附近）

```xml
<result property="price" column="price"/>             <!-- 价格 -->
```

**在它下面加一行**，形状是 `<result property="xx" column="xx"/>`。

> 💡 想一想：为什么要有这一行？因为数据库的"列"和 Java 的"字段"要建立对应关系。虽然这里两者同名（都叫 weight），但**不写这一行，MyBatis 就不会把它装进对象**。

---

## 步骤 4 · `baseColumns` 加字段（找到第 84 行附近）

```xml
    <sql id="baseColumns">
        g.id, g.name, g.descr, g.content, g.cover, g.price, g.store,
        g.user_id, g.date, g.type_id, g.state, g.sales,
        t.name AS type_name,
        u.nickname AS user_name
    </sql>
```

**在 `g.price` 后面加 `g.weight`**。

> 💡 这个片段被 `findById` / `findAll` / `findPage` 三处 `<include>` 复用，所以**改一处，三个查询全都带上 weight**。这就是为什么要把字段清单抽出来。

---

## 步骤 5 + 6 · `insert` 加列和值（找到第 158 行附近）

```xml
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO goods2 (name, descr, content, cover, price, store, user_id, date, type_id, state, sales)
        VALUES (#{name}, #{descr}, #{content}, #{cover}, #{price}, #{store},
                #{userId}, #{date}, #{typeId}, #{state}, #{sales})
    </insert>
```

**两行都要改，而且必须成对改**：
- 第 1 行：`price,` 后面加 `weight,`
- 第 2 行：`#{price},` 后面加 `#{weight},`

> ⚠️ **数量必须一一对应**：列清单里多了一个，VALUES 里就必须多一个，否则 SQL 直接报错（`Column count doesn't match value count`）。
> **这是新手最容易犯的错** —— 只改了一行。

---

## 步骤 7 · `update` 加一个 `<if>`（找到第 174 行附近）

```xml
        <set>
            ...
            <if test="price != null">price = #{price},</if>
            <if test="store != null">store = #{store},</if>
            ...
        </set>
```

**照着 `price` 那一行的形状，在它下面加一行 weight 的。**

> 💡 **每个 `<if>` 末尾那个逗号不能少** —— `<set>` 标签会自动去掉最后一个多余的逗号，所以每行都写逗号是安全的。
> 💡 这里用 `<if>` 是**动态 SQL**：只有前端传了 weight 才更新它。不传就不动那一列（不会把原来的重量清空）。

**做完检查**：`Ctrl + F9` 编译。

---

# 阶段 D · 前端（第 8~13 处）

文件：`frontend/src/views/Goods2.vue`（共 6 处，**别用 `Ctrl+R`**）

> 💡 **先备份**：把这个文件复制一份到项目根的 `.backup-goods2-vue/` 里，改坏了能还原。
> 或者记住备用方案：**删掉 `Goods2.vue`，从 `Goods.vue` 重新复制，只改 2 处**（第 22 行 `<span>商品管理2</span>`、第 214 行 `from '@/api/goods2'`）。

---

## 步骤 8 · `form` 定义（第 240~251 行）

```js
const form = reactive({
  id: null,
  name: '',
  typeId: null,
  price: 0,
  store: 0,
  cover: '',
  descr: '',
  content: '',
  date: '',
  state: '上架'
})
```

**在 `price: 0,` 后面加一行**。

---

## 步骤 9 · `resetForm()`（第 293~306 行）

```js
function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    ...
    price: 0,
    store: 0,
    ...
  })
}
```

**同样在 `price: 0,` 后面加一行**（和步骤 8 一模一样的内容）。

> 💡 **为什么两处都要加**：`form` 是"初始值"，`resetForm` 是"点新增时重置成的值"。漏了 `resetForm`，你上一次填的重量会残留到下一次新增。

---

## 步骤 10 · `openDialog()`（第 352~369 行）⚠️ 最容易漏的一处

```js
function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      typeId: row.typeId ?? null,
      price: row.price ?? 0,
      store: row.store ?? 0,
      cover: row.cover || '',
      descr: row.descr || '',
      content: row.content || '',
      date: row.date || '',
      state: row.state || '上架'
    })
  }
  dialogVisible.value = true
}
```

**在 `price: row.price ?? 0,` 后面加一行。**

> ⚠️⚠️ **这处是你 10-06 就漏过的同一处！**（当时漏的是 `date`）
> 漏了的症状：点「编辑」打开弹窗，**重量框是空的** —— 但数据库里其实有值。你以为数据丢了，其实只是没回填。

---

## 步骤 11 · `payload`（第 376~386 行）⚠️ 注意逗号

```js
    const payload = {
      name: form.name,
      typeId: form.typeId,
      price: form.price,
      store: form.store,
      cover: form.cover,
      descr: form.descr,
      content: form.content,
      state: form.state,
      date: form.date
    }
```

**在 `price: form.price,` 后面加一行。**

> ⚠️⚠️ **加完立刻检查上一行末尾有没有逗号！** 你 10-06 就是在这里漏了逗号，Vite 报：
> `[vue/compiler-sfc] Unexpected token, expected ","`
> **这是 JS 对象的规矩：每一项后面都要逗号，但最后一项后面可以不要。**

---

## 步骤 12 · 弹窗模板加输入框（找到第 137 行的「价格」form-item）

```html
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
```

**在它下面（「库存」那项之前）加一个重量表单项**，照抄价格的形状，改 3 个地方：`label`、`v-model`、`prop`。

> 💡 **为什么用 `el-input-number`**：数字输入框，自带上下小箭头、限制最小值、限制小数位。价格/库存都用它。
> 💡 `:precision="2"` = 保留 2 位小数；`:step="0.1"` = 点上下箭头每次加减 0.1（价格的 step 是 1，重量更细，用 0.1 更顺手）。

---

## 步骤 13 · 列表加一列（找到第 78 行的「价格」列）

```html
        <el-table-column label="价格" width="110" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
```

**在它下面加一个重量列。** 最简单的写法（和「库存」那列一样）：

```html
<el-table-column prop="weight" label="重量(kg)" width="100" align="center" />
```

> 💡 想去掉 NULL 显示成空白的问题？`prop` 简写就够了（空值会显示成空白）。想显示 `-` 或加单位，就得写 `<template #default>`，参考「分类」列。

---

# 阶段 E · 验收（重启后端之后）

## 必做：重启后端

```
IDEA 控制台左侧 → 红色 ■ 停止 → 绿色 ▶ 启动
```

**为什么要重启**：① 新增了字段（Java 类变了）② **XML 改了必须重启才生效**（MyBatis 只在启动时加载 XML）
**前端不用重启**，Vite 热更新会自动加载。

## 逐项测试

| # | 测试 | 预期结果 | ✅ |
|---|---|---|---|
| 1 | 打开 `/admin/goods2` | 列表多了一列「重量」，2 条商品显示空白（因为没填过） | ⬜ |
| 2 | 点「新增商品」 | 重量框是**空的**（或 0） | ⬜ |
| 3 | 填「测试重量商品」+ 重量填 **1.5** → 确定 | 提示"创建成功" | ⬜ |
| 4 | 看列表 | 新那行「重量」列显示 **1.5** | ⬜ |
| 5 | **点新那行的「编辑」** | 重量框显示 **1.5**（不是空的）← 验证步骤 10 | ⬜ |
| 6 | 改成 **2.25** → 确定 | 列表更新成 2.25 | ⬜ |
| 7 | 编辑**老商品**（没填过重量的那 2 条） | 重量框空白，改个别的字段保存 | ⬜ |
| 8 | 再打开老商品编辑 | 重量还是空白（没被乱填） | ⬜ |
| 9 | Navicat 看 `goods2` 表 | `weight` 列有值 | ⬜ |
| 10 | Navicat 看 `goods` 表 | **纹丝不动**（8 条，没有 weight 列） | ⬜ |

## 出问题时的排查方向

| 症状 | 先查哪 |
|---|---|
| 列表没有「重量」列 | 步骤 13 加了吗？前端保存了吗（Vite 有没有报错） |
| 弹窗没有重量输入框 | 步骤 12 |
| 编辑时重量框是空的，但数据库有值 | **步骤 10（`openDialog`）漏了** |
| 保存后数据库 `weight` 还是 NULL | 步骤 11（`payload`）漏了，或步骤 5/6 没配对 |
| 报 `Column count doesn't match value count` | **步骤 5 和 6 只改了一行**（列数 ≠ 值数） |
| 报 `Unknown column 'weight'` | 步骤 1 没做，或**后端没重启** |
| 列表报 500 | 步骤 3/4/7 某处漏了；看 IDEA 控制台的具体报错 |
| Vite 报 `Unexpected token, expected ","` | 步骤 11 漏了逗号 |
| 改重量顺带把别的字段清空了 | 步骤 7 写的不是 `<if>`，或者没写逗号 |

---

# 8. 有 3 层**不用**改（这是明天的重点，先想明白再看）

| 层 | 为什么不用改 |
|---|---|
| `Goods2Controller.java` | 它的参数是 `@RequestBody Goods2 goods` —— **整个对象都接住**，前端传什么字段它自动认，不用逐个声明 |
| `Goods2Service.java` | `normalize()` 只管补默认值（状态/销量/库存/价格/用户/日期），weight **没填就该是空**，所以不用动。<br>`update()` 里"继承原值"那两行只管 `date` 和 `userId`，也不涉及 weight |
| `api/goods2.js` | 它只管 **URL**（`/goods2/page` 等），不关心请求体里有什么字段 |

**一句话理解**：
> **后端是一条"通吃的管道"。表里有列、实体里有字段，管道就自动认了 —— 前端多传一个 `weight`，它就顺顺当当流到数据库里。**

**这就是为什么"加新字段"要改的前端最多（6 处）、后端反而只要 6 处** —— 因为后端那 3 层是"通用"的，写一次管所有字段。

---

# 9. 答案对照（⚠️ 先自己写完，卡住了再看）

> **建议**：先把 13 处都自己写完、编译通过、页面测一遍。实在卡住某处，再看下面那处；全做完了再整体对一遍。

<details>
<summary>点开看完整答案（13 处）</summary>

### ① 数据库

```sql
ALTER TABLE goods2
  ADD COLUMN weight double(10,2) DEFAULT NULL COMMENT '重量(kg)' AFTER price;
```

### ② `Goods2.java`（第 51 行 `private Double price;` 下面）

```java
    /** 重量 kg（goods2 表列：weight，double(10,2)）→ 前端 row.weight */
    private Double weight;
```

> **不用写任何注解。** 原生 MyBatis 不需要 `@TableField` —— 那是 MyBatis-Plus 的写法，抄了会编译报错。

### ③ `Goods2Mapper.xml` · resultMap

```xml
        <result property="price" column="price"/>             <!-- 价格 -->
        <result property="weight" column="weight"/>           <!-- 重量 kg -->
```

### ④ `Goods2Mapper.xml` · baseColumns

```xml
        g.id, g.name, g.descr, g.content, g.cover, g.price, g.weight, g.store,
        g.user_id, g.date, g.type_id, g.state, g.sales,
```

### ⑤⑥ `Goods2Mapper.xml` · insert

```xml
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO goods2 (name, descr, content, cover, price, weight, store, user_id, date, type_id, state, sales)
        VALUES (#{name}, #{descr}, #{content}, #{cover}, #{price}, #{weight}, #{store},
                #{userId}, #{date}, #{typeId}, #{state}, #{sales})
    </insert>
```

### ⑦ `Goods2Mapper.xml` · update

```xml
            <if test="price != null">price = #{price},</if>
            <if test="weight != null">weight = #{weight},</if>
```

### ⑧ `Goods2.vue` · form 定义

```js
  price: 0,
  weight: null,
  store: 0,
```

### ⑨ `Goods2.vue` · resetForm

```js
    price: 0,
    weight: null,
    store: 0,
```

### ⑩ `Goods2.vue` · openDialog

```js
      price: row.price ?? 0,
      weight: row.weight ?? null,
      store: row.store ?? 0,
```

### ⑪ `Goods2.vue` · payload

```js
      price: form.price,
      weight: form.weight,
      store: form.store,
```

> ⚠️ 注意 `price: form.price,` 末尾那个逗号，必须有。

### ⑫ `Goods2.vue` · 弹窗模板（价格 form-item 下面）

```html
        <el-form-item label="重量(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
```

### ⑬ `Goods2.vue` · 表格列（价格列下面）

```html
        <el-table-column prop="weight" label="重量(kg)" width="100" align="center" />
```

</details>

---

# 10. 改坏了怎么办

| 文件 | 恢复办法 |
|---|---|
| `Goods2.vue` | 删掉 → 从 `Goods.vue` 复制 → 改 **2 处**（第 22 行标题「商品管理2」、第 214 行 `from '@/api/goods2'`） |
| `Goods2Mapper.xml` | 删掉 → 从 `GoodsMapper.xml` 复制 → 改 **6 处**（namespace 1 + resultMap type 1 + 表名 4） |
| `Goods2.java` / `Goods2Mapper.java` / `Goods2Service.java` / `Goods2Controller.java` | 删掉 → 从对应的非 2 文件复制 → `Ctrl+R` 把 `Goods` → `Goods2`（**`Cc` 和 `W` 都不要勾！**），然后核对 4 个位置（import / private final / `@RequestMapping` / XML namespace） |
| 数据库加错列 | `ALTER TABLE goods2 DROP COLUMN weight;` 然后重来 |

**或者直接喊我** —— 我能秒查出来改错在哪。

---

# 11. 做完之后还可以再练（有余力再看）

| 挑战 | 练什么 |
|---|---|
| 给 weight 加**搜索范围筛选**（`minWeight` / `maxWeight`） | 动态 SQL 的 `<if>` + `BETWEEN`，要从 Controller 一路改到页面 |
| 加「**按状态筛选**」（上架/下架下拉框） | 完整走一遍"加一个筛选条件"（6 个文件） |
| 把 `Goods2.vue` 的富文本删掉，**闭卷重写** | 练"给已有页面换组件" |
| 加**多图**（新建 `goods2_image` 子表） | 一对多关系 + `@Transactional` 事务 ← 难度最高 |

---

**创建时间**：2026-10-06
**前置阅读**：`商品管理2-操作手册.md`（踩坑总结在那一份里）
