<!--
  订单管理 页面（后台）

  【这个页面在哪】src/views/Orders.vue
  【怎么访问】登录后台后，点左侧菜单「商城管理 → 订单管理」（网址 /admin/orders）
  【谁引用了它】src/router/index.js 里注册的路由；菜单入口在数据库 sys_menu 表
  【数据从哪来】src/api/orders.js 的 getOrderPage() → 后端 /api/orders/page
              → 要管理员权限（后端 AuthContext.requireAdmin，普通用户调直接 403）

  【数据为什么能直接显示用户昵称和商品封面】
    orders 表里只有 goods_id / user_id 两个数字。是后端 findPage 那句 SQL 用
    LEFT JOIN goods / LEFT JOIN `user` 把 g_cover / u_nickname 一起查出来，
    别名后由 OrdersMapper.xml 的 PageMap 装进 orders.goods / orders.user 才有这些字段
    —— 不是循环查库（那样 10 条订单就要多 20 次查询，即 N+1）。

  【和前台 views/user/MyOrders.vue 的区别】
    本页：全部订单、有搜索、显示「是谁下的」、详情抽屉
    前台：只显示自己的、无搜索、无用户列
-->
<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
          <span class="header-tip">共 {{ total }} 笔订单</span>
        </div>
      </template>

      <!-- 搜索区：商品名 / 订单号，都为空 = 不筛（后端 <where> 处理） -->
      <div class="toolbar">
        <el-input v-model="query.name" style="width: 200px" placeholder="商品名称" clearable />
        <el-input v-model="query.orderNo" style="width: 200px" placeholder="订单号" clearable />
        <el-button type="success" plain @click="handleSearch">查询</el-button>
        <el-button type="info" plain @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <!-- 序号列：不是数据库 id，是按「当前页码 + 行号」算的连续序号 -->
        <el-table-column label="序号" width="70" align="center">
          <template #default="{ $index }">
            <span>{{ (query.page - 1) * query.size + $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 商品列：封面 + 名称快照（订单里的 name 是下单时存的副本） -->
        <el-table-column label="商品" min-width="200" show-overflow-tooltip>
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
              <span>{{ row.name || '商品已删除' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderNo" label="订单号" width="170" show-overflow-tooltip />
        <el-table-column label="总价" width="90" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ money(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nums" label="数量" width="60" align="center" />
        <el-table-column prop="userName" label="收货人" width="90" show-overflow-tooltip />
        <el-table-column prop="userPhone" label="联系方式" width="120" show-overflow-tooltip />
        <el-table-column prop="userAddress" label="地址" min-width="140" show-overflow-tooltip />
        <el-table-column prop="time" label="下单时间" width="165" show-overflow-tooltip />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="stateTagType(row.state)">
              {{ row.state || '待付款' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 用户列：后端 JOIN user 表取出来的昵称（订单表里没有这一列） -->
        <el-table-column label="下单用户" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.user?.nickname" size="small" effect="plain">{{ row.user.nickname }}</el-tag>
            <span v-else class="muted">用户已注销（id={{ row.userId }}）</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="130" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="success" @click="openDetail(row)">详情</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          @current-change="loadOrders"
          @size-change="loadOrders"
        />
      </div>
    </el-card>

    <!-- 详情抽屉：只读展示（订单是交易凭证，后台不提供编辑） -->
    <el-drawer v-model="detailVisible" title="订单详情" :with-header="false" size="480px">
      <div class="drawer-body" v-loading="loading">
        <h3 class="drawer-title">订单详情</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ form.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ form.name }}</el-descriptions-item>
          <el-descriptions-item label="总价">¥{{ money(form.price) }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ form.nums }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ form.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ form.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ form.userAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ form.time }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ form.state || '待付款' }}</el-descriptions-item>
          <el-descriptions-item label="下单用户">
            {{ form.user?.nickname || '用户已注销（id=' + form.userId + '）' }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="drawer-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { deleteOrder, getOrderPage } from '@/api/orders'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, name: '', orderNo: '' })

const detailVisible = ref(false)
const form = ref({})

/** 价格：49 → "49.00" */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 订单状态 → 标签颜色 */
function stateTagType(state) {
  if (state === '已付款') return 'success'
  if (state === '已取消') return 'info'
  return 'warning'
}

/**
 * 拉订单分页
 * 后端返回 { total, list } —— 注意是 list，不是 MyBatis-Plus 的 records
 * 后端要求管理员：普通用户账号调这个接口会 403（request.js 会弹红字）
 */
async function loadOrders() {
  loading.value = true
  try {
    const data = await getOrderPage({
      page: query.page,
      size: query.size,
      name: query.name,
      orderNo: query.orderNo
    })
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 点「查询」：回到第 1 页再拉（不然可能停在第 5 页，新结果只有 1 页，页面空白） */
function handleSearch() {
  query.page = 1
  loadOrders()
}

function handleReset() {
  query.name = ''
  query.orderNo = ''
  query.page = 1
  loadOrders()
}

/** 详情：表格行里已经有全部字段，直接深拷贝一份用，不用再请求 */
function openDetail(row) {
  form.value = JSON.parse(JSON.stringify(row))
  detailVisible.value = true
}

/** 删除：后台入口，谁的单都能删（后端按 accountType 判断，不信前端） */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除订单 ${row.orderNo} 吗？`, '确认删除', { type: 'warning' })
  } catch {
    return
  }
  await deleteOrder(row.id)
  ElMessage.success('删除成功')
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page {
  padding: 16px;
}

.panel {
  border-radius: 12px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-tip {
  font-size: 12px;
  color: #8a8f98;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.goods-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-cover {
  width: 44px;
  height: 44px;
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

.price {
  font-weight: 600;
  color: #e63935;
}

.muted {
  font-size: 12px;
  color: #8a8f98;
}

.table-ops {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.drawer-body {
  padding: 20px;
}

.drawer-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
}

.drawer-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
