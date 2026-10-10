<!--
  轮播图2 页面（前端界面）

  【这个页面在哪】src/views/Carousels.vue
  【怎么访问】登录后台后，点左侧菜单「商城管理 → 轮播图2」（网址 /admin/carousels）
  【谁引用了它】src/router/index.js 里注册的路由；菜单入口在数据库 sys_menu 表

  【和已有的「轮播图管理」不是同一个东西】
    已存在的：views/system/Banners.vue  →  表 home_banner  →  点图片跳「网址」（宣传位）
    本页面：  views/Carousels.vue       →  表 carousel    →  点图片跳「商品」（商品推荐位）
  两者并存，互不干扰。

  【数据从哪来】调用 src/api/carousel.js 里的方法 → 后端 /api/carousel/** 接口 → MySQL 的 carousel 表
  【页面结构】el-card（搜索 + 表格 + 分页）+ el-dialog（新增/编辑弹窗）+ el-drawer（详情抽屉）

  【和教程 Carousel.vue 的写法差异（Vue2 → Vue3 对照）】
    教程（Vue2 + Element UI）              本页面（Vue3 + Element Plus）
    ------------------------------------------------------------------
    <template slot-scope="scope">          <template #default="{ row, $index }">
    :visible.sync="dialogFormVisible"      v-model="dialogVisible"
    this.$request.get(...)                 loadData() 调 api/carousel.js 的函数
    this.$notify.success(...)              ElMessage.success(...)
    size="mini"                            size="small"（或不写，走默认）
    data() { return {...} }                ref() / reactive()
    created() / beforeDestroy()            onMounted() / onBeforeUnmount()
-->
<template>
  <div class="page">
    <!-- 主卡片：标题栏 + 内容 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>轮播图</span>
          <!-- 点击后打开空表单（新增模式） -->
          <el-button type="primary" @click="openDialog()">新增轮播图</el-button>
        </div>
      </template>

      <!-- 搜索区：输入名称关键字后按回车或点「查询」都会触发 loadData() -->
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索轮播图名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" plain @click="handleSearch">查询</el-button>
        <el-button plain @click="handleReset">重置</el-button>
      </div>

      <!-- 数据表格：:data="tableData" 就是下面 loadData() 从后端拿到的数组 -->
      <el-table v-loading="loading" :data="tableData" stripe>
        <!-- 【序号列】不是数据库里的 id，是按「当前页码 + 行号」算出来的连续序号
             第 1 页：1,2,3… ；第 2 页：11,12,13…（假设每页 10 条）
             $index 是 el-table 给的「当前页内的行号」，从 0 开始，所以要 +1 -->
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            <span>{{ (query.page - 1) * query.size + $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 【图片列】mediaUrl() 会把相对路径补成能访问的地址；preview-src-list 让点击能放大预览 -->
        <el-table-column label="图片" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="mediaUrl(row.cover)"
              :preview-src-list="[mediaUrl(row.cover)]"
              fit="cover"
              class="cover-thumb"
              preview-teleported
            />
            <span v-else class="cover-empty">无</span>
          </template>
        </el-table-column>

        <!-- prop 要和后端返回的字段名一致（Carousel.java 里的字段名） -->
        <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />

        <!-- 【商品名称列】prop 用的是后端 JOIN 出来的虚拟字段 goodsName（carousel 表里没有这一列） -->
        <el-table-column label="商品名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.goodsName" size="small" type="warning" effect="plain">{{ row.goodsName }}</el-tag>
            <span v-else class="cover-empty">未关联商品</span>
          </template>
        </el-table-column>

        <!-- 操作列：详情=抽屉看只读详情；编辑=带数据打开弹窗；删除=弹确认框后调删除接口 -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="success" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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
          @size-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗：form.id 有值就是编辑，没值就是新增 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑轮播图' : '新增轮播图'"
      width="520px"
      destroy-on-close
      align-center
    >
      <!-- rules 里配的校验：名称必填 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：首页首屏推荐" maxlength="64" show-word-limit />
        </el-form-item>

        <!-- 封面上传：用本项目自己的 ImageUpload 组件
             （教程用的是 el-upload + :action="$baseUrl + '/file/upload'" + :headers="{ token: user.token }"，
              那是 Vue2 的写法，而且上传地址和鉴权头都跟本项目不一样。ImageUpload 内部已经处理好了。） -->
        <el-form-item label="封面图" prop="cover">
          <ImageUpload v-model="form.cover" type="cover" upload-text="上传封面" />
          <p class="form-tip">也可以直接填图片地址（http 开头或 /uploads/ 开头）</p>
        </el-form-item>

        <!-- 商品下拉框：数据来自 GET /api/goods/list（goods 表里的 8 个真实商品）
             filterable = 可以打字搜索，商品多了好用 -->
        <el-form-item label="推荐商品" prop="goodsId">
          <el-select
            v-model="form.goodsId"
            placeholder="请选择商品"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option v-for="g in goodsList" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：从屏幕右侧滑出一块只读面板（教程里叫「详情」，用的 el-drawer） -->
    <el-drawer v-model="drawerVisible" title="轮播图详情" size="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detail.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="封面">
          <el-image
            v-if="detail.cover"
            :src="mediaUrl(detail.cover)"
            :preview-src-list="[mediaUrl(detail.cover)]"
            fit="cover"
            class="drawer-thumb"
            preview-teleported
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="推荐商品">{{ detail.goodsName || '未关联商品' }}</el-descriptions-item>
        <el-descriptions-item label="商品图">
          <el-image
            v-if="detail.goodsCover"
            :src="mediaUrl(detail.goodsCover)"
            :preview-src-list="[mediaUrl(detail.goodsCover)]"
            fit="cover"
            class="drawer-thumb"
            preview-teleported
          />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="drawer-footer">
        <el-button @click="drawerVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
// ========== 依赖 ==========
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 轮播图接口封装：详见 src/api/carousel.js（里面注明了对应的后端地址）
import { createCarousel, deleteCarousel, getCarouselPage, updateCarousel } from '@/api/carousel'
// 商品接口：弹窗里的「推荐商品」下拉框要用商品列表
import { getGoodsList } from '@/api/goods'
import ImageUpload from '@/components/ImageUpload.vue'
import { mediaUrl } from '@/utils/format'

// ========== 状态变量（页面上的数据都放这里）==========
const loading = ref(false)        // 表格加载中（控制转圈）
const submitting = ref(false)     // 弹窗提交中（防止重复点确定）
const dialogVisible = ref(false)  // 弹窗是否显示
const drawerVisible = ref(false)  // 详情抽屉是否显示
const tableData = ref([])         // 表格数据（来自后端）
const total = ref(0)              // 总条数（分页用）
const formRef = ref()             // 表单引用（用来触发校验）
const goodsList = ref([])         // 商品列表（给下拉框用）
const detail = ref({})            // 详情抽屉里展示的那一条数据

// 查询条件：keyword 搜索词，page 第几页，size 每页几条
const query = reactive({ keyword: '', page: 1, size: 10 })

// 弹窗表单数据：id 是标记（null=新增，有值=编辑）
const form = reactive({
  id: null,
  name: '',
  cover: '',
  goodsId: null
})

// 表单校验规则：name 必填
const rules = {
  name: [{ required: true, message: '请输入轮播图名称', trigger: 'blur' }]
}

// ========== 方法 ==========

/** 清空表单（打开「新增」弹窗前调用） */
function resetForm() {
  Object.assign(form, { id: null, name: '', cover: '', goodsId: null })
}

/** 点「查询」时：回到第 1 页再查（避免停在第 3 页却搜出 2 条结果 → 表格空白） */
function handleSearch() {
  query.page = 1
  loadData()
}

/** 点「重置」：清空关键字 + 回第 1 页 + 重查（教程里也是这三个动作） */
function handleReset() {
  query.keyword = ''
  query.page = 1
  loadData()
}

/**
 * 【核心】加载表格数据
 * 流程：组装查询条件 → 调 getCarouselPage（即 GET /api/carousel/page）→ 把结果给表格
 */
async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    const data = await getCarouselPage(params)   // 后端返回 { total, list }
    tableData.value = data.list || []            // 当前页数据 → 表格
    total.value = data.total || 0                // 总条数 → 分页控件
  } finally {
    loading.value = false
  }
}

/** 加载商品列表（给「推荐商品」下拉框用） */
async function loadGoods() {
  try {
    goodsList.value = (await getGoodsList()) || []
  } catch {
    goodsList.value = []
  }
}

/** 打开弹窗：不传参数=新增（空表单）；传 row=编辑（把该行数据填进表单） */
function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      cover: row.cover || '',
      goodsId: row.goodsId ?? null
    })
  }
  dialogVisible.value = true
}

/** 打开详情抽屉（只读，不调接口 —— 列表接口已经把 goodsName / goodsCover 一起返回了） */
function openDetail(row) {
  detail.value = { ...row }
  drawerVisible.value = true
}

/** 提交表单：先校验 → id 为空走新增接口，否则走更新接口 → 成功后刷新列表 */
async function handleSubmit() {
  await formRef.value.validate()   // 校验不通过会抛异常，下面的代码不执行
  submitting.value = true
  try {
    const payload = {
      name: form.name,
      cover: form.cover,
      goodsId: form.goodsId
    }
    if (!form.id) {
      await createCarousel(payload)          // POST /api/carousel
      ElMessage.success('创建成功')
    } else {
      await updateCarousel(form.id, payload) // PUT /api/carousel/{id}
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()                               // 重新拉列表，看到最新数据
  } finally {
    submitting.value = false
  }
}

/** 删除：弹确认框 → 调删除接口 → 刷新列表 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除轮播图「${row.name || '未命名'}」？`, '提示', { type: 'warning' })
  await deleteCarousel(row.id)               // DELETE /api/carousel/{id}
  ElMessage.success('已删除')
  loadData()
}

// ========== 生命周期 ==========
// 页面一打开就加载数据（商品列表 + 轮播图列表）
onMounted(() => {
  loadGoods()
  loadData()
})
</script>

<style scoped>
/* 标题栏：左右两端对齐（左边标题、右边按钮） */
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

/* 搜索区 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

/* 表格里的缩略图 */
.cover-thumb {
  width: 72px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
}

.cover-empty {
  color: #94a3b8;
  font-size: 13px;
}

.form-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

/* 分页靠右 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 详情抽屉里的图（比表格里大一点） */
.drawer-thumb {
  width: 160px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
}

/* 抽屉底部按钮 */
.drawer-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
