<!--
  收藏管理 页面（后台）

  【这个页面在哪】src/views/Collects.vue
  【怎么访问】登录后台后，点左侧菜单「商城管理 → 收藏管理」（网址 /admin/collects）
  【谁引用了它】src/router/index.js 里注册的路由；菜单入口在数据库 sys_menu 表（id=17, parent_id=9）

  【数据从哪来】src/api/collect.js 里的 getCollectPage() → 后端 /api/collect/page → MySQL 的 collect 表
  【页面结构】el-card（表格 + 分页）+ el-drawer（详情抽屉）
  【和别的后台页最大的不同】没有「新增 / 编辑」——
    收藏记录是用户在客户端点出来的，后台不该手工造一条、更不该改（改了就是伪造用户行为）。
    所以只有一个「只读表格 + 删除」。
  结构参考 views/Carousels.vue，弹窗那块整块去掉了。

  【为什么表格能直接显示用户名和商品名】
    collect 表里只有 user_id / goods_id 两个数字。这是后端 findPage 那句 SQL 用
    LEFT JOIN `user` u / LEFT JOIN goods gd 把 u.nickname 和 gd.name 一起查出来，
    别名成 user_name / goods_name 才有的（对应 Collect.java 里的 userName / goodsName）。
    —— 不是循环查库（那样 10 条记录就要多 20 次查询，即 N+1）。
-->
<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>收藏管理</span>
          <span class="header-tip">共 {{ total }} 条收藏记录</span>
        </div>
      </template>

      <!-- 数据表格：:data="tableData" 就是下面 loadData() 从后端拿到的当前页数组 -->
      <el-table v-loading="loading" :data="tableData" stripe>
        <!-- 【序号列】不是数据库里的 id，是按「当前页码 + 行号」算出来的连续序号 -->
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            <span>{{ (query.page - 1) * query.size + $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 【用户列】prop 用的是后端 JOIN 出来的虚拟字段 userName（collect 表里没有这一列） -->
        <el-table-column label="用户" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.userName" size="small" effect="plain">{{ row.userName }}</el-tag>
            <!-- 用户被注销/删除时 LEFT JOIN 出 null，这里兜底显示 -->
            <span v-else class="muted">用户已注销（id={{ row.userId }}）</span>
          </template>
        </el-table-column>

        <!-- 【商品列】同样是 JOIN 出来的 goodsName -->
        <el-table-column label="商品" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.goodsName" size="small" type="warning" effect="plain">{{ row.goodsName }}</el-tag>
            <span v-else class="muted">商品已删除（id={{ row.goodsId }}）</span>
          </template>
        </el-table-column>

        <!-- 【收藏时间】存的是后端格式化好的字符串，直接展示 -->
        <el-table-column prop="time" label="收藏时间" width="180" align="center">
          <template #default="{ row }">
            <span>{{ row.time || '-' }}</span>
          </template>
        </el-table-column>

        <!-- 操作列：详情=抽屉看只读详情；删除=弹确认框后调删除接口 -->
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="success" @click="openDetail(row)">详情</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：total 是后端返回的总条数，翻页后重新调 loadData() -->
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情抽屉：从屏幕右侧滑出一块只读面板（和 Carousels.vue 一致，不做「全屏」按钮） -->
    <el-drawer v-model="drawerVisible" title="收藏详情" size="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="记录 ID">{{ detail.id ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userName || '用户已注销' }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ detail.goodsName || '商品已删除' }}</el-descriptions-item>
        <el-descriptions-item label="收藏时间">{{ detail.time || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="drawer-footer">
        <el-button @click="drawerVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 收藏接口封装：详见 src/api/collect.js（里面注明了对应的后端地址）
import { deleteCollect, getCollectPage } from '@/api/collect'

const loading = ref(false)       // 表格加载中（控制转圈）
const drawerVisible = ref(false) // 详情抽屉是否显示
const tableData = ref([])        // 表格数据（来自后端）
const total = ref(0)             // 总条数（分页用）
const detail = ref({})           // 详情抽屉里展示的那一条数据

// 查询条件：后台收藏接口不支持关键字搜索，所以只有「第几页 / 每页几条」
const query = reactive({ page: 1, size: 10 })

/**
 * 【核心】加载表格数据
 * 流程：调 getCollectPage（即 GET /api/collect/page）→ 把结果给表格和分页
 */
async function loadData() {
  loading.value = true
  try {
    const data = await getCollectPage({ page: query.page, size: query.size })
    tableData.value = data?.list || []   // 当前页数据 → 表格
    total.value = data?.total || 0       // 总条数 → 分页控件
  } finally {
    loading.value = false
  }
}

/** 打开详情抽屉（只读，不调接口 —— 列表接口已经把 userName / goodsName 一起返回了） */
function openDetail(row) {
  detail.value = { ...row }
  drawerVisible.value = true
}

/**
 * 删除一条收藏记录
 * ⚠️ 这里删的是 collect 表的记录 id，不是商品 id。
 *    删完可能把当前页删空了（比如第 2 页只有 1 条），所以顺手把页码往回退一格。
 */
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除这条收藏记录？删除后用户需要重新收藏。', '提示', {
    type: 'warning'
  })
  await deleteCollect(row.id)
  ElMessage.success('已删除')
  if (tableData.value.length === 1 && query.page > 1) query.page -= 1
  loadData()
}

// ========== 生命周期 ==========
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 标题栏：左右两端对齐（左边标题、右边总数提示） */
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-tip {
  font-size: 13px;
  color: #94a3b8;
}

.muted {
  color: #94a3b8;
  font-size: 13px;
}

/* 分页靠右 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 抽屉底部按钮 */
.drawer-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
