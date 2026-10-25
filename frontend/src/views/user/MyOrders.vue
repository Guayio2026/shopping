<!--
  我的订单 页面（前台）

  【这个页面在哪】src/views/user/MyOrders.vue
  【怎么访问】登录后点顶部导航「我的订单」（网址 /user/orders），
              或下单成功后自动跳过来
  【谁引用了它】src/router/index.js 里注册的路由（meta.requiresAuth + requiresUser，
              所以未登录访问会被路由守卫送去 /login，登录后回来）
  【数据从哪来】src/api/orders.js 的 getMyOrders() → 后端 /api/orders/my
              → 只查当前登录用户的订单（userId 从 token 取，前端传不了别人的）

  【和后台 views/Orders.vue 的区别】
    本页：只显示自己的订单，没有搜索框，没有用户昵称列
    后台：显示全部订单，有搜索，要显示「是谁下的」
-->
<template>
  <div class="my-orders">
    <!-- ① 顶部：标题 + 件数 -->
    <header class="order-head">
      <h1><i class="bar" />我的订单</h1>
      <span v-if="total" class="count">共 {{ total }} 笔</span>
    </header>

    <el-card shadow="never" class="panel">
      <!-- ② 订单表格 -->
      <el-table v-loading="loading" :data="list" stripe>
        <!-- 商品列：封面 + 名称（名称点进去可以跳到商品详情） -->
        <el-table-column label="商品" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="goods-cell">
              <el-image
                v-if="row.goods?.cover"
                class="goods-cover"
                :src="mediaUrl(row.goods.cover)"
                :preview-src-list="[mediaUrl(row.goods.cover)]"
                fit="cover"
              />
              <div v-else class="goods-cover no-cover">
                <el-icon :size="18"><Picture /></el-icon>
              </div>
              <span class="goods-name">{{ row.name || '商品已删除' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderNo" label="订单号" width="170" show-overflow-tooltip />

        <!-- 总价：后端重算后存进订单的（单价×数量），直接显示 -->
        <el-table-column label="总价" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ money(row.price) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="nums" label="数量" width="70" align="center" />
        <el-table-column prop="userName" label="收货人" width="100" show-overflow-tooltip />
        <el-table-column prop="userPhone" label="电话" width="130" show-overflow-tooltip />
        <el-table-column prop="userAddress" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="time" label="下单时间" width="170" show-overflow-tooltip />

        <!-- 状态：目前只有「待付款」（下单时数据库给的默认值） -->
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="stateTagType(row.state)" effect="plain">
              {{ row.state || '待付款' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ③ 分页 -->
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, prev, pager, next"
          @current-change="loadOrders"
          @size-change="loadOrders"
        />
      </div>

      <!-- ④ 空状态 -->
      <el-empty v-if="!loading && !list.length" description="还没有订单，去挑一件吧">
        <el-button type="primary" @click="goGoods">去逛逛</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { deleteOrder, getMyOrders } from '@/api/orders'
import { mediaUrl } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

/** 价格：49 → "49.00"（后端 price 是 double，直接显示会出现 49.0 这种） */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 订单状态 → 标签颜色（以后加了「已付款 / 已发货」在这里加分支） */
function stateTagType(state) {
  if (state === '已付款') return 'success'
  if (state === '已取消') return 'info'
  return 'warning'
}

/**
 * 拉「我的订单」
 * 后端返回 { total, list } —— 注意是 list，不是 MyBatis-Plus 的 records
 */
async function loadOrders() {
  loading.value = true
  try {
    const data = await getMyOrders({ page: query.page, size: query.size })
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 删除自己的订单
 * ⚠️ 不用再问「是不是我的」—— 后端 delete 里会校验：
 *    普通账号删别人的单，直接 403「只能删除自己的订单」
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除订单 ${row.orderNo} 吗？`, '确认删除', { type: 'warning' })
  } catch {
    return // 用户点了取消
  }
  await deleteOrder(row.id)
  ElMessage.success('已删除')
  loadOrders()
}

function goGoods() {
  router.push('/user/goods')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 28px 48px;
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.order-head h1 {
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

.panel {
  margin-top: 20px;
}

/* 商品列：小封面图 + 名称横着排 */
.goods-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  flex-shrink: 0;
}

.no-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f5f7;
  color: #c4c8d0;
}

.goods-name {
  font-size: 13px;
  color: var(--ink);
}

.price {
  font-weight: 600;
  color: var(--accent);
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
