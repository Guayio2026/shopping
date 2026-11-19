<template>
  <div class="mall-home" v-loading="loading">
    <!-- ① 左分类 + 右轮播 -->
    <section v-if="types.length || carousels.length" class="hero">
      <nav v-if="types.length" class="type-panel" aria-label="商品分类">
        <h2>商品分类</h2>
        <button type="button" v-for="t in types" :key="t.id" class="type-item" @click="goGoods(t.id)">
          <span>{{ t.name }}</span>

        </button>
      </nav>

      <div v-if="carousels.length" class="carousel-wrap">
        <el-carousel height="var(--banner-height)" :interval="5000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <div class="feature">
              <div class="feature-copy">
                <h2>{{ item.name || '商城推荐' }}</h2>
                <router-link :to="item.goodsId ? '/user/goods/' + item.goodsId : '/user/goods'">{{ item.goodsId ? '查看商品' : '浏览商品' }} ↗</router-link>
              </div>
              <img class="carousel-img" :src="mediaUrl(item.cover)" :alt="item.name || '商城推荐'" />
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- ①.5 商城公告（第十一章新增）：折叠面板，点标题展开正文 -->
    <section v-if="notices.length" class="block notice-block">
      <header class="block-head">
        <h2>商城公告</h2>
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
        <h2>{{ block.title }}</h2>
        <router-link class="more" to="/user/goods">查看全部 ›</router-link>
      </header>

      <div v-if="block.list.length" class="goods-grid">
        <CatalogProduct v-for="g in block.list" :key="g.id" :goods="g" />
      </div>
      <el-empty v-else description="暂无商品" />
    </section>
  </div>
</template>

<script setup>
import CatalogProduct from '@/components/CatalogProduct.vue'
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
.mall-home { max-width:1280px; margin:auto; padding:8px clamp(16px,3vw,40px) 64px; --banner-height:clamp(270px,32vw,430px); }
.type-panel { display:flex; align-items:center; gap:24px; overflow-x:auto; padding:10px 0 16px; }
.type-panel h2 { font-size:13px; color:#62685b; margin:0; padding-right:18px; border-right:1px solid #cccfc2; white-space:nowrap; }
.type-item { flex:none; border:0; background:none; cursor:pointer; padding:7px 2px; font:inherit; color:#363c33; }
.type-item:hover,.more:hover { text-decoration:underline; }
.feature { display:grid; grid-template-columns: minmax(0,1fr) minmax(0,1.8fr); height:100%; background:#26382f; color:#faf9ef; }
.feature-copy { display:flex; flex-direction:column; align-items:flex-start; justify-content:center; gap:24px; padding:clamp(22px,4vw,64px); }
.feature-copy h2,.block-head h2 { font-family:"Songti SC","STSong","SimSun",serif; margin:0; font-weight:600; }
.feature-copy h2 { font-size:clamp(27px,4vw,60px); line-height:1.22; overflow-wrap:anywhere; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; }
.feature-copy a { color:inherit; text-underline-offset:6px; font-size:14px; }
.carousel-img { width:100%; height:100%; min-height:0; object-fit:cover; }
.block { margin-top:clamp(30px,4vw,58px); }
.block-head { display:flex; justify-content:space-between; align-items:baseline; margin-bottom:18px; }
.block-head h2 { font-size:clamp(24px,2.5vw,36px); }
.more { color:#424b41; font-size:14px; text-decoration:none; }
.goods-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:clamp(12px,2vw,28px); }
.notice-collapse { --el-collapse-header-bg-color:transparent; --el-collapse-content-bg-color:transparent; }
.notice-title-row { display:flex; align-items:center; width:100%; gap:12px; text-align:left; }
.notice-time { margin-left:auto; color:#65695f; }
.notice-body { white-space:pre-wrap; overflow-wrap:anywhere; }
@media(max-width:760px) {
  .goods-grid { grid-template-columns:repeat(2,minmax(0,1fr)); }
  .type-panel { gap:15px; }
  .mall-home { --banner-height:clamp(300px,78vw,420px); }
  .feature { grid-template-columns:1fr; grid-template-rows:minmax(0,1fr) auto; }
  .feature-copy { grid-row:2; flex-direction:row; align-items:center; justify-content:space-between; padding:16px 16px 28px; gap:12px; }
  .feature-copy h2 { font-size:23px; -webkit-line-clamp:2; }
  .feature-copy a { flex:none; }
}
</style>
