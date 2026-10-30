<template>
  <div class="mall-home" v-loading="loading">
    <!-- ① 左分类 + 右轮播 -->
    <section class="hero">
      <aside class="type-panel">
        <div v-for="t in types" :key="t.id" class="type-item" @click="goGoods(t.id)">
          <span>{{ t.name }}</span>
          <span class="arrow">›</span>
        </div>
        <p v-if="!types.length" class="type-empty">暂无分类</p>
      </aside>

      <div class="carousel-wrap">
        <el-carousel v-if="carousels.length" height="400px" :interval="5000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <img class="carousel-img" :src="mediaUrl(item.cover)" alt="" />
          </el-carousel-item>
        </el-carousel>
        <el-empty v-else description="暂无轮播图" />
      </div>
    </section>

    <!-- ①.5 商城公告（第十一章新增）：折叠面板，点标题展开正文 -->
    <section v-if="notices.length" class="block notice-block">
      <header class="block-head">
        <h2><i class="bar" />商城公告</h2>
      </header>

      <el-collapse v-model="activeNotice" class="notice-collapse">
        <el-collapse-item v-for="(n, index) in notices" :key="n.id" :name="index">
          <template #title>
            <div class="notice-title-row">
              <span class="notice-name">{{ n.name }}</span>
              <span class="notice-time">{{ n.time }}</span>
            </div>
          </template>
          <div class="notice-body">{{ n.content || '（无正文）' }}</div>
        </el-collapse-item>
      </el-collapse>
    </section>

    <!-- ②③ 新品上架 / 热销商品：同一份模板，v-for 渲染两次 -->
    <section v-for="block in goodsBlocks" :key="block.title" class="block">
      <header class="block-head">
        <h2><i class="bar" />{{ block.title }}</h2>
        <span class="more" @click="goGoods()">查看更多 ›</span>
      </header>

      <div v-if="block.list.length" class="goods-grid">
        <article v-for="g in block.list" :key="g.id" class="goods-card" @click="goDetail(g.id)">
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
      <el-empty v-else description="暂无商品" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData } from '@/api/homePublic'
import { mediaUrl } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const carousels = ref([])
const types = ref([])
const timeGoods = ref([])
const salesGoods = ref([])
const notices = ref([])          // 公告列表（后端 /public/home 里的 notices）
const activeNotice = ref([0])    // 默认展开第一条公告

/** 价格：49 → "49.00"（后端 price 是 double，直接显示会出现 49.0 这种） */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 两个商品块的配置：标题 + 数据源。这样模板里只写一遍卡片 */
const goodsBlocks = computed(() => [
  { title: '新品上架', list: timeGoods.value },
  { title: '热销商品', list: salesGoods.value }
])

/**
 * 跳到「全部商品页」
 * 点左侧分类 → 传 typeId，跳过去自动带上分类筛选
 * 点「查看更多」→ 不传，看全部商品
 */
function goGoods(typeId) {
  router.push({
    path: '/user/goods',
    query: typeId ? { typeId } : {}
  })
}

/** 进商品详情页（点商品卡片时调用；注意别和上面的 goGoods 搞混：那个是去列表页） */
function goDetail(id) {
  router.push(`/user/goods/${id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    // ⚠️ request.js 已把 data 解包，这里拿到的就是 { carousels, types, timeGoods, salesGoods, notices }
    const data = await getHomeData()
    carousels.value = data?.carousels || []
    types.value = data?.types || []
    timeGoods.value = data?.timeGoods || []
    salesGoods.value = data?.salesGoods || []
    notices.value = data?.notices || []
  } catch {
    /* 公开页：失败保持空数组，页面显示空状态，不弹错 */
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mall-home {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 28px 48px;
}

/* ===== 顶部：左分类 + 右轮播 ===== */
.hero {
  display: flex;
  gap: 12px;
}

.type-panel {
  flex: 0 0 220px;
  background: #2b2f36;
  border-radius: 10px;
  padding: 8px 0;
  min-height: 400px;
}

.type-item {
  height: 40px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.type-item:hover {
  background: var(--accent);
}

.type-item .arrow {
  color: rgba(255, 255, 255, 0.6);
}

.type-empty {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  text-align: center;
  margin-top: 40px;
}

.carousel-wrap {
  flex: 1;
  min-width: 0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--line);
}

.carousel-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  display: block;
}

/* ===== 商品区块 ===== */
.block {
  margin-top: 40px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.block-head h2 {
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

.more {
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.2s ease;
}

.more:hover {
  color: var(--accent);
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

/* 商品名：最多 2 行，超出显示省略号；固定高度保证卡片底部对齐 */
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

/* 描述：最多 1 行 */
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

/* ===== 公告区块（第十一章） ===== */
.notice-collapse {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 0 16px;
}

/* 标题行：公告标题靠左，时间挤到最右 */
.notice-title-row {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--muted);
}

/* pre-wrap：保留正文里的换行（textarea 里敲的回车是 \n，默认会被 HTML 吃掉） */
.notice-body {
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.8;
  color: #4a4f57;
}

@media (max-width: 900px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .type-panel {
    display: none;
  }
}
</style>
