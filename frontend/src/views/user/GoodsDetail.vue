<template>
  <div class="detail-page" v-loading="loading">
    <!-- ① 面包屑：给游客一条退路 -->
    <nav class="crumb">
      <button type="button" class="back" @click="goBack">
        <el-icon :size="14"><ArrowLeft /></el-icon>
        <span>返回商品列表</span>
      </button>
      <span class="crumb-path">全部商品 / 商品详情</span>
    </nav>

    <!-- ② 商品不存在 / 已下架 -->
    <el-empty v-if="!loading && notFound" description="商品不存在或已下架">
      <el-button type="primary" @click="goBack">去逛逛别的</el-button>
    </el-empty>

    <template v-else-if="goods">
      <!-- ③ 主区：左图右信息 -->
      <section class="main">
        <div class="gallery">
          <el-image
              class="gallery-img"
              :src="mediaUrl(goods.cover)"
              :preview-src-list="[mediaUrl(goods.cover)]"
              :preview-teleported="true"
              fit="cover"
          />
        </div>

        <div class="info">
          <h1 class="info-name">{{ goods.name }}</h1>
          <p class="info-descr">{{ goods.descr || '暂无简介' }}</p>

          <div class="price-row">
            <span class="price-label">价格</span>
            <span class="price">¥{{ money(goods.price) }}</span>
          </div>

          <ul class="meta">
            <li><span>库存</span><b>{{ goods.store }}</b></li>
            <li><span>累计热销</span><b>{{ goods.sales }}</b></li>
            <li><span>所属分类</span><b>{{ goods.typeName || '未分类' }}</b></li>
            <li><span>上架时间</span><b>{{ goods.date || '—' }}</b></li>
          </ul>

          <div class="buy-row">
            <el-input-number
                v-model="num"
                :min="1"
                :max="maxNum"
                :disabled="!goods.store"
            />
            <el-button
                type="primary"
                class="buy-btn"
                :disabled="!goods.store"
                @click="handleBuy"
            >
              {{ goods.store ? '立即购买' : '已售罄' }}
            </el-button>
            <el-button type="warning" plain @click="handleFav">
              <el-icon :size="14"><Star /></el-icon>
              <span>收藏</span>
            </el-button>
          </div>
        </div>
      </section>

      <!-- ④ 详细介绍 / 购买须知 -->
      <section class="tabs-wrap">
        <el-card shadow="never">
          <el-tabs v-model="activeName" type="card">
            <el-tab-pane label="详细介绍" name="goods">
              <div v-if="hasContent" class="detail-content" v-html="goods.content"></div>
              <el-empty v-else description="该商品还没有填写详细介绍" :image-size="90" />
            </el-tab-pane>

            <el-tab-pane label="购买须知" name="notice">
              <div class="notice">
                <h3>购买说明</h3>
                <p>1、正品保证</p>
                <p>2、7 天无理由退货</p>
                <p>3、全国包邮</p>
                <p>4、售后无忧</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star } from '@element-plus/icons-vue'
import { getPublicGoodsById } from '@/api/goodsPublic'
import { mediaUrl } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const notFound = ref(false)   // 商品不存在 / 已下架
const goods = ref(null)       // null = 还没加载出来
const num = ref(1)            // 购买数量
const activeName = ref('goods')

/** 数量上限 = 库存。store 为 0 时兜底成 1，否则 max=0 < min=1 会报错 */
const maxNum = computed(() => Math.max(Number(goods.value?.store) || 0, 1))

/** content 可能是 null / '' / 纯空格，都算「没填」 */
const hasContent = computed(() => {
  const c = goods.value?.content
  return typeof c === 'string' && c.trim() !== ''
})

/** 价格格式化：89 → "89.00" */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

async function loadGoods() {
  loading.value = true
  notFound.value = false
  try {
    goods.value = await getPublicGoodsById(route.params.id)
  } catch {
    // 后端对「不存在」和「已下架」都返回 404，前端统一按「不存在」处理
    goods.value = null
    notFound.value = true
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/user/goods')
}

function handleBuy() {
  ElMessage.info('下单功能在第九章做，敬请期待')
}

function handleFav() {
  ElMessage.info('收藏功能在第八章做，敬请期待')
}

onMounted(loadGoods)
</script>

<style scoped>
.detail-page {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  max-width: 1280px;
  margin: 0 auto;
  padding: 18px 28px 48px;
}

/* ===== 面包屑 ===== */
.crumb {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: #4b5563;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.crumb-path {
  font-size: 12px;
  color: var(--muted);
}

/* ===== 主区 ===== */
.main {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 32px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 24px;
}

.gallery-img {
  width: 100%;
  height: 400px;
  border-radius: 10px;
  overflow: hidden;
  cursor: zoom-in;
}

.info-name {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.4;
}

.info-descr {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.7;
}

.price-row {
  margin-top: 18px;
  padding: 14px 18px;
  border-radius: 10px;
  background: #fff6f5;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-label {
  font-size: 12px;
  color: var(--muted);
}

.price {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
}

.meta {
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 24px;
}

.meta li {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
}

.meta li span {
  color: var(--muted);
  min-width: 60px;
}

.meta li b {
  color: var(--ink);
  font-weight: 600;
}

.buy-row {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.buy-btn {
  width: 160px;
}

/* ===== 标签页 ===== */
.tabs-wrap {
  margin-top: 20px;
}

.detail-content {
  padding: 8px 4px 20px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;
}

/* ⚠️ scoped 样式默认作用不到 v-html 生成的内容，必须用 :deep() 穿透 */
.detail-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

.detail-content :deep(p) {
  margin: 10px 0;
}

.detail-content :deep(ul),
.detail-content :deep(ol) {
  padding-left: 22px;
}

.notice {
  padding: 10px 6px 20px;
}

.notice h3 {
  margin: 6px 0 14px;
  font-size: 15px;
  color: var(--ink);
}

.notice p {
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

/* ===== 窄屏堆叠 ===== */
@media (max-width: 900px) {
  .main {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .gallery-img {
    height: 300px;
  }
}
</style>
