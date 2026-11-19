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
      <h1>我的订单</h1>
      <span v-if="total" class="count">共 {{ total }} 笔</span>
    </header>

    <section class="orders-list" v-loading="loading" aria-label="订单列表">
      <article v-for="row in list" :key="row.id" class="receipt">
        <header class="receipt-head">
          <span>订单号 {{ row.orderNo }}</span>
          <time>{{ row.time }}</time>
          <el-tag size="small" :type="stateTagType(row.state)" effect="plain">{{ row.state }}</el-tag>
        </header>
        <div class="purchase">
          <el-image v-if="row.goods?.cover" class="cover" :src="mediaUrl(row.goods.cover)"
            :preview-src-list="[mediaUrl(row.goods.cover)]" fit="cover" />
          <div v-else class="cover placeholder"><el-icon><Picture /></el-icon></div>
          <div class="item"><strong>{{ row.name || '商品已删除' }}</strong><small>数量 {{ row.nums }}</small></div>
          <div class="amount"><small>订单总额</small><strong>¥{{ money(row.price) }}</strong></div>
        </div>
        <footer class="receipt-foot">
          <details><summary>收货信息</summary><p>{{ row.userName }} · {{ row.userPhone }}<br>{{ row.userAddress }}</p></details>
          <div class="actions">
            <template v-if="row.state === '待付款'">
              <el-button text @click="changeOrder(row, true)">取消订单</el-button>
              <el-button type="primary" @click="handlePay(row)">支付</el-button>
            </template>
            <el-button v-else-if="['已支付','已付款','已取消'].includes(row.state)" text @click="changeOrder(row, false)">移除</el-button>
          </div>
        </footer>
      </article>
      <!-- ③ 分页 -->
      <div v-if="total > query.size" class="pager">
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
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { cancelOrder, deleteOrder, getMyOrders, payOrder } from '@/api/orders'
import { mediaUrl } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

/** 价格：49 → "49.00" */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 订单状态 → 标签颜色（以后加了「已发货 / 已完成」在这里加分支） */
function stateTagType(state) {
  if (state === '已支付' || state === '已付款') return 'success'
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

/** 取消会释放库存；移除仅影响买家的列表。 */
async function changeOrder(row, cancel) {
  const action = cancel ? '取消订单' : '从我的列表移除'
  try {
    await ElMessageBox.confirm(`订单 ${row.orderNo}：确认${action}？`, action)
  } catch {
    return
  }
  try {
    if (cancel) await cancelOrder(row.id)
    else await deleteOrder(row.id)
    ElMessage.success(cancel ? '订单已取消' : '订单已移除')
    if (!cancel && list.value.length === 1 && query.page > 1) query.page--
  } finally {
    await loadOrders()
  }
}

/**
 * 余额支付
 * ⚠️ 只传订单 id，金额由后端从订单里读（orders.price）——
 *    前端就算改了 row.price 也没用，扣多少是后端说了算。
 * ⚠️ 余额不足时后端返回 400「余额不足，请先在个人中心充值」，
 *    request.js 拦截器会自动弹出来，这里不用自己写提示。
 */
async function handlePay(row) {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${row.orderNo}？金额 ¥${money(row.price)}`,
      '确认支付',
      { type: 'info' }
    )
  } catch {
    return // 用户点了取消
  }
  await payOrder(row.id)
  ElMessage.success('支付成功')
  loadOrders() // 重新拉列表，状态变成「已支付」
}

function goGoods() {
  router.push('/user/goods')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders { max-width: 960px; margin: auto; padding: 32px clamp(16px,4vw,48px) 56px; color: #202323; }
.order-head, .receipt-head, .purchase, .receipt-foot, .actions { display: flex; align-items: center; }
.order-head { gap: 16px; margin-bottom: 20px; }
.order-head h1 { font-size: clamp(23px,3vw,32px); margin: 0; }
.count, small, time, summary { color: #646865; }
.orders-list { min-height: 120px; }
.receipt { background: white; border: 1px solid #dddeda; border-radius: 8px; margin-bottom: 14px; padding: 0 clamp(14px,3vw,24px); }
.receipt-head { flex-wrap: wrap; gap: 6px 16px; padding: 13px 0; border-bottom: 1px solid #ecece8; font-size: 13px; font-variant-numeric: tabular-nums; }
.receipt-head .el-tag { margin-left: auto; }
.purchase { gap: 14px; padding: 18px 0; }
.cover { flex: none; width: 64px; height: 64px; border-radius: 4px; }
.placeholder { display: grid; place-items: center; background: #f4f4f1; color: #70746f; }
.item, .amount { display: grid; gap: 5px; min-width: 0; overflow-wrap: anywhere; }
.amount { margin-left: auto; text-align: right; font-variant-numeric: tabular-nums; }
.amount strong { font-size: 20px; }
.receipt-foot { align-items: baseline; justify-content: space-between; gap: 10px; border-top: 1px solid #ecece8; padding: 10px 0; }
details { min-width: 0; font-size: 14px; }
summary { cursor: pointer; padding: 6px 0; }
details p { line-height: 1.7; overflow-wrap: anywhere; }
.actions { flex: none; }
.actions .el-button--primary { --el-button-bg-color: #282c29; --el-button-border-color: #282c29; --el-button-hover-bg-color: #464c48; --el-button-hover-border-color: #464c48; }
.pager { display: flex; justify-content: flex-end; }
@media(max-width: 540px) {
  .my-orders { padding-top: 18px; }
  .receipt-head time { order: 3; width: 100%; }
  .cover { width: 48px; height: 48px; }
  .purchase { gap: 10px; }
  .item { flex: 1; }
  .amount { max-width: 40%; }
}
</style>
