<template>
  <div class="my-collect">
    <!-- ① 顶部：标题 + 件数 -->
    <header class="collect-head">
      <h1><i class="bar" />我的收藏</h1>
      <span v-if="collects.length" class="count">共 {{ collects.length }} 件</span>
    </header>

    <!-- ② 收藏卡片网格 -->
    <div v-loading="loading" class="collect-body">
      <div v-if="collects.length" class="goods-grid">
        <article
          v-for="item in collects"
          :key="item.id"
          class="goods-card"
          @click="goDetail(item.goodsId)"
        >
          <img
            v-if="item.goods?.cover"
            class="goods-cover"
            :src="mediaUrl(item.goods.cover)"
            alt=""
          />
          <div v-else class="goods-cover no-cover">
            <el-icon :size="26"><Picture /></el-icon>
          </div>

          <div class="goods-body">
            <p class="goods-name">{{ item.goods?.name || '商品已下架' }}</p>
            <p class="goods-descr">
              {{ item.goods?.descr || '该商品已被下架或删除' }}
            </p>
            <div class="goods-foot">
              <span class="goods-price">
                {{ item.goods ? '¥' + money(item.goods.price) : '--' }}
              </span>
              <!-- ⚠️ @click.stop：防止点「取消收藏」时把整个卡片的事件一起触发 → 又跳去详情页 -->
              <el-button type="danger" link @click.stop="handleCancel(item)">
                <el-icon :size="14"><Delete /></el-icon>
                <span>取消收藏</span>
              </el-button>
            </div>
          </div>
        </article>
      </div>
      <!-- ⚠️ 用 v-else-if="!loading" 而不是 v-else：否则加载中会先闪一下空状态 -->
      <el-empty v-else-if="!loading" description="您还未收藏哟~" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Picture } from '@element-plus/icons-vue'
import { deleteCollect, getMyCollect } from '@/api/collect'
import { mediaUrl } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const collects = ref([]) // 我的收藏列表（每条里 goods 是嵌套商品对象）

/** 价格：49 → "49.00"（后端 price 是 double，直接显示会出现 49.0 这种） */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/**
 * 加载「我的收藏」
 * 接口：GET /collect/my（要登录，token 由 request.js 自动带上）
 * 后端已经把商品信息用 <association> 嵌套进每条的 goods 字段了 —— 不需要再逐个查商品
 */
async function loadCollect() {
  loading.value = true
  try {
    collects.value = (await getMyCollect()) || []
  } catch {
    collects.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 进商品详情页
 * ⚠️ 用 item.goodsId（collect 表里记的商品 id），不要用 item.goods.id：
 *    商品若被删了，item.goods 就是 null，一取 .id 直接报错。
 */
function goDetail(goodsId) {
  router.push(`/user/goods/${goodsId}`)
}

/** 取消收藏：删掉这条 collect 记录 → 重新拉列表 */
async function handleCancel(item) {
  await deleteCollect(item.id)
  ElMessage.success('已取消收藏')
  loadCollect()
}

onMounted(() => {
  loadCollect()
})
</script>

<style scoped>
.my-collect {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 28px 48px;
}

/* ===== 顶部 ===== */
.collect-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.collect-head h1 {
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

.count {
  font-size: 13px;
  color: var(--muted);
}

/* ===== 卡片网格（和 views/user/GoodsList.vue 保持同一套视觉）===== */
.collect-body {
  margin-top: 20px;
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

/* 商品已被删除 / 没有封面图时的占位块 */
.no-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f5f7;
  color: #c4c8d0;
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

@media (max-width: 1100px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
