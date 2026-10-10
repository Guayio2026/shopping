<template>
  <div class="mall-list">
    <!-- ① 顶部：标题 + 搜索框 -->
    <header class="list-head">
      <h1><i class="bar" />热卖商品</h1>
      <div class="search-box">
        <input
          v-model="keyword"
          type="text"
          placeholder="请输入搜索商品名称"
          @keyup.enter="handleSearch"
        />
        <button type="button" class="search-btn" @click="handleSearch">
          <el-icon :size="16"><Search /></el-icon>
        </button>
      </div>
    </header>

    <!-- ② 分类按钮组：「全部」是 0，其余是分类 id -->
    <div class="type-group">
      <button
        type="button"
        class="type-btn"
        :class="{ active: selectedTypeId === 0 }"
        @click="pickType(0)"
      >
        全部
      </button>
      <button
        v-for="t in types"
        :key="t.id"
        type="button"
        class="type-btn"
        :class="{ active: selectedTypeId === t.id }"
        @click="pickType(t.id)"
      >
        {{ t.name }}
      </button>
    </div>

    <!-- ③ 商品网格 -->
    <div v-loading="loading" class="list-body">
      <div v-if="goods.length" class="goods-grid">
        <article v-for="g in goods" :key="g.id" class="goods-card" @click="goDetail(g.id)">
          <img class="goods-cover" :src="mediaUrl(g.cover)" alt="" />
          <div class="goods-body">
            <p class="goods-name">{{ g.name }}</p>
            <p class="goods-descr">{{ g.descr }}</p>
            <div class="goods-foot">
              <span class="goods-price">¥{{ money(g.price) }}</span>
              <span class="goods-sales">累计热销：{{ g.sales }}</span>
            </div>
          </div>
        </article>
      </div>
      <!-- ⚠️ 用 v-else-if="!loading" 而不是 v-else：否则加载中会先闪一下「没有商品哟~」 -->
      <el-empty v-else-if="!loading" description="没有商品哟~" />
    </div>

    <!-- ④ 分页：total 为 0 时不显示（空状态已经说明了） -->
    <div v-if="total > 0" class="pager">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :page-sizes="[8, 16, 32]"
        :total="total"
        layout="total, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getPublicGoods, getPublicTypes } from '@/api/goodsPublic'
import { mediaUrl } from '@/utils/format'

const route = useRoute()
const router = useRouter()

// ========== 状态 ==========
const loading = ref(false)
const types = ref([])     // 分类列表（顶部那排按钮）
const goods = ref([])     // 当前页的商品
const total = ref(0)      // 总条数（分页控件要显示「共 N 条」）
const page = ref(1)       // 第几页（从 1 开始，和后端一致）
const size = ref(8)       // 每页几条
const keyword = ref('')   // 搜索词

// 从地址栏 ?typeId=10 初始化筛选状态（首页点分类跳过来时会带上这个参数）
// ⚠️ route.query 里取出来的是「字符串」，必须 Number() 转换 ——
//    否则下面 selectedTypeId === t.id 是「数字 vs 字符串」，永远不相等，分类按钮不高亮。
//    0 表示「全部」。
const selectedTypeId = ref(Number(route.query.typeId) || 0)

// ========== 工具 ==========
/** 价格：49 → "49.00"（后端 price 是 double，直接显示会出现 49.0 这种） */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

// ========== 数据加载 ==========
/** 加载分类列表 */
async function loadTypes() {
  try {
    types.value = (await getPublicTypes()) || []
  } catch {
    /* 公开页：失败就只显示「全部」按钮，不弹错 */
    types.value = []
  }
}

/**
 * 加载商品列表
 * 页面加载 / 点搜索 / 切分类 / 翻页 —— 全部走这一个函数
 */
async function loadGoods() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    // ⚠️ 只有选了具体分类才传 typeId。「全部」时传 0 会去查 type_id = 0 → 0 条商品
    if (selectedTypeId.value) params.typeId = selectedTypeId.value

    // request.js 已解包，拿到的直接就是 { total, list }
    const data = await getPublicGoods(params)
    goods.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    goods.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ========== 交互 ==========
/**
 * 点搜索 / 按回车
 * ⚠️ 必须把 page 重置为 1：假设你在第 3 页，然后搜了个只有 2 条结果的关键词，
 *    第 3 页根本没数据 —— 表格会空白，用户以为坏了。
 */
function handleSearch() {
  page.value = 1
  loadGoods()
}

/**
 * 点分类按钮
 * 三件事：更新选中态 → 同步到地址栏（刷新/分享不丢筛选）→ 回第 1 页重新查
 */
function pickType(typeId) {
  selectedTypeId.value = typeId
  page.value = 1
  // 用 replace 而不是 push：连点 5 个分类不该在浏览器历史里留 5 条记录
  router.replace({ path: '/user/goods', query: typeId ? { typeId } : {} })
  loadGoods()
}

/** 翻页 */
function handlePageChange(p) {
  page.value = p
  loadGoods()
}
/** 进商品详情页 */
function goDetail(id) {
  router.push(`/user/goods/${id}`)
}

onMounted(() => {
  loadTypes()
  loadGoods()
})
</script>

<style scoped>
.mall-list {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 28px 48px;
}

/* ===== 顶部：标题 + 搜索框 ===== */
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.list-head h1 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--ink);
}

.bar {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: var(--accent);
  display: inline-block;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-box input {
  width: 240px;
  height: 38px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  color: var(--ink);
  outline: none;
  transition: border-color 0.2s ease;
}

.search-box input:focus {
  border-color: var(--accent);
}

.search-btn {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--accent);
  border-radius: 8px;
  background: var(--accent);
  color: #fff;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.search-btn:hover {
  background: #cf2f2c;
  border-color: #cf2f2c;
}

/* ===== 分类按钮组 ===== */
.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0 20px;
}

.type-btn {
  height: 34px;
  padding: 0 18px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: #4b5563;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.type-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

/* ===== 商品网格（和首页 user/Home.vue 保持同一套视觉）===== */
.list-body {
  min-height: 320px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.goods-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.goods-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 26, 26, 0.08);
}

.goods-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.goods-body {
  padding: 12px;
}

/* 商品名：最多 2 行，超出省略；固定高度保证卡片底部对齐 */
.goods-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  line-height: 1.45;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-descr {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-foot {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goods-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
}

.goods-sales {
  font-size: 11px;
  color: var(--muted);
}

/* ===== 分页 ===== */
.pager {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .search-box input {
    width: 150px;
  }
}
</style>
