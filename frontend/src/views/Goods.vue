<!--
  商品管理 页面（前端界面）

  【这个页面在哪】src/views/Goods.vue
  【怎么访问】登录后台后，网址 /admin/goods 或点左侧菜单「商城管理 → 商品管理」
  【谁引用了它】src/router/index.js 里注册的路由；菜单入口在数据库 sys_menu 表

  【数据从哪来】调用 src/api/goods.js 里的方法 → 后端 /api/goods/** 接口 → MySQL 的 goods 表
  【页面结构】上半部分 el-card（搜索框 + 分类筛选 + 表格 + 分页），下半部分 el-dialog（新增/编辑弹窗）

  【和 Types.vue 的区别】
  ① 多了「分类下拉框」筛选（所以 onMounted 里要额外加载分类列表）
  ② 表格列更多（封面图、价格、库存、销量、状态）
  ③ 弹窗表单字段更多
-->
<template>
  <div class="page">
    <!-- 主卡片：标题栏 + 内容 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>商品管理</span>
          <!-- 点击后打开空表单（新增模式） -->
          <el-button type="primary" @click="openDialog()">新增商品</el-button>
        </div>
      </template>

      <!-- 搜索区：名称关键字 + 分类下拉；按回车或点「查询」都会触发 loadData() -->
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索商品名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="query.typeId"
          placeholder="全部分类"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option
            v-for="t in typeList"
            :key="t.id"
            :label="t.name"
            :value="t.id"
          />
        </el-select>
        <el-button type="primary" plain @click="handleSearch">查询</el-button>
      </div>

      <!-- 数据表格：:data="tableData" 就是下面 loadData() 从后端拿到的数组 -->
      <el-table v-loading="loading" :data="tableData" stripe>
        <!-- prop 要和后端返回的字段名一致（Goods.java 里的字段名） -->
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="封面" width="110" align="center">
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
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeName" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.typeName" size="small">{{ row.typeName }}</el-tag>
            <span v-else class="cover-empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="store" label="库存" width="80" align="center" />
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.state === '上架' ? 'success' : 'info'" size="small">
              {{ row.state || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="上架日期" width="160" show-overflow-tooltip />
        <el-table-column prop="userName" label="发布者" width="110" />
        <!-- 操作列：预览=弹窗看富文本详情；编辑=带数据打开弹窗；删除=弹确认框后调删除接口 -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="success" @click="handlePreview(row)">预览</el-button>
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
      :title="form.id ? '编辑商品' : '新增商品'"
      width="900px"
      destroy-on-close
      align-center
    >
      <!-- rules 里配的校验：商品名称和价格必填 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：三只松鼠每日坚果" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="所属分类" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择分类" clearable style="width: 100%">
            <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="store">
          <el-input-number v-model="form.store" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面图" prop="cover">
          <ImageUpload v-model="form.cover" type="cover" upload-text="上传封面" />
          <p class="form-tip">也可以直接填图片地址（http 开头或 /uploads/ 开头）</p>
        </el-form-item>
        <el-form-item label="简述" prop="descr">
          <el-input v-model="form.descr" type="textarea" :rows="2" placeholder="一句话描述商品" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="详情" prop="content">
          <div class="editor-wrap">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
              class="editor-toolbar"
            />
            <Editor
              v-model="form.content"
              :defaultConfig="editorConfig"
              mode="default"
              class="editor-body"
              @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio value="上架">上架</el-radio>
            <el-radio value="下架">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上架日期" prop="date">
          <el-date-picker
              v-model="form.date"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择上架日期"
              style="width: 100%"
          />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览详情弹窗：只读，把 content 里的 HTML 渲染出来 -->
    <!-- ⚠️ v-html 会把字符串当 HTML 解析，后台自己录入的内容是安全的；
         如果以后开放给用户投稿，必须做 XSS 过滤（如 DOMPurify）。 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="60%"
      align-center
    >
      <!-- v-if 保证没内容时不渲染，避免出现空白框 -->
      <div v-if="previewContent" class="preview-content" v-html="previewContent"></div>
      <el-empty v-else description="该商品还没有填写详情" />
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// ========== 依赖 ==========
import { onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 商品接口封装：详见 src/api/goods.js（里面注明了对应的后端地址）
import { createGoods, deleteGoods, getGoodsPage, updateGoods } from '@/api/goods'
// 分类接口：弹窗和筛选都要用分类列表
import { getTypeList } from '@/api/type'
import ImageUpload from '@/components/ImageUpload.vue'
import { mediaUrl } from '@/utils/format'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

// ========== 状态变量（页面上的数据都放这里）==========
const loading = ref(false)        // 表格加载中（控制转圈）
const submitting = ref(false)     // 弹窗提交中（防止重复点确定）
const dialogVisible = ref(false)  // 弹窗是否显示
const tableData = ref([])         // 表格数据（来自后端）
const total = ref(0)              // 总条数（分页用）
const formRef = ref()             // 表单引用（用来触发校验）
const typeList = ref([])          // 分类列表（给下拉框用）

// ========== 预览弹窗（表格里点「预览」用）==========
const previewVisible = ref(false) // 预览弹窗是否显示
const previewTitle = ref('')      // 预览弹窗标题（显示商品名）
const previewContent = ref('')    // 预览的 HTML 内容

// 查询条件：keyword 搜索词，typeId 分类筛选，page 第几页，size 每页几条
const query = reactive({ keyword: '', typeId: null, page: 1, size: 10 })

// 弹窗表单数据：id 是标记（null=新增，有值=编辑）
const form = reactive({
  id: null,
  name: '',
  typeId: null,
  price: 0,
  store: 0,
  cover: '',
  descr: '',
  content: '',
  date:'',
  state: '上架'
})

// 表单校验规则：name 和 price 必填
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入商品详情...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/files/upload',
      fieldName: 'file',
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token') || ''}`
      },
      customInsert(res, insertFn) {
        if (res && res.code === 200 && res.data && res.data.url) {
          insertFn(res.data.url)
        } else {
          ElMessage.error((res && res.message) || '图片上传失败')
        }
      }
    }
  }
}

function handleEditorCreated(editor) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})

// ========== 方法 ==========

/** 清空表单（打开"新增"弹窗前调用） */
function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    typeId: null,
    price: 0,
    store: 0,
    cover: '',
    descr: '',
    content: '',
    date:'',
    state: '上架'
  })
}

/** 价格显示：把 49 显示成 49.00 */
function formatPrice(value) {
  const n = Number(value)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/**
 * 点「查询」时：回到第 1 页再查
 * 【为什么要回第 1 页】假设你在第 3 页，然后搜了个只有 2 条结果的关键词，
 * 第 3 页根本没数据 —— 表格会空白，用户以为坏了。所以筛选条件一变就回第 1 页。
 */
function handleSearch() {
  query.page = 1
  loadData()
}

/**
 * 【核心】加载表格数据
 * 流程：组装查询条件 → 调 getGoodsPage（即 GET /api/goods/page）→ 把结果给表格
 */
async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.typeId) params.typeId = query.typeId
    const data = await getGoodsPage(params)   // 后端返回 { total, list }
    tableData.value = data.list || []         // 当前页数据 → 表格
    total.value = data.total || 0             // 总条数 → 分页控件
  } finally {
    loading.value = false
  }
}

/** 加载分类列表（给筛选下拉框和弹窗里的分类选择用） */
async function loadTypes() {
  try {
    typeList.value = (await getTypeList()) || []
  } catch {
    typeList.value = []
  }
}

/** 打开弹窗：不传参数=新增（空表单）；传 row=编辑（把该行数据填进表单） */
function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      typeId: row.typeId ?? null,
      price: row.price ?? 0,
      store: row.store ?? 0,
      cover: row.cover || '',
      descr: row.descr || '',
      content: row.content || '',
      date: row.date || '',
      state: row.state || '上架'
    })
  }
  dialogVisible.value = true
}

/** 提交表单：先校验 → id 为空走新增接口，否则走更新接口 → 成功后刷新列表 */
async function handleSubmit() {
  await formRef.value.validate()   // 校验不通过会抛异常，下面的代码不执行
  submitting.value = true
  try {
    const payload = {
      name: form.name,
      typeId: form.typeId,
      price: form.price,
      store: form.store,
      cover: form.cover,
      descr: form.descr,
      content: form.content,
      state: form.state,
      date: form.date
    }
    if (!form.id) {
      await createGoods(payload)          // POST /api/goods
      ElMessage.success('创建成功')
    } else {
      await updateGoods(form.id, payload) // PUT /api/goods/{id}
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()                            // 重新拉列表，看到最新数据
  } finally {
    submitting.value = false
  }
}

/** 删除：弹确认框 → 调删除接口 → 刷新列表 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await deleteGoods(row.id)               // DELETE /api/goods/{id}
  ElMessage.success('已删除')
  loadData()
}

/**
 * 预览详情：把这一行的 content（HTML 字符串）渲染到弹窗里
 *
 * 【为什么不用再调接口】列表接口 /api/goods/page 已经把 content 一起返回了
 * （GoodsMapper.xml 的 baseColumns 里有 g.content），所以直接用 row 里的数据就行，
 * 不用为了看详情再发一次请求。
 */
function handlePreview(row) {
  previewTitle.value = `预览详情 · ${row.name}`
  previewContent.value = row.content || ''
  previewVisible.value = true
}

// ========== 生命周期 ==========
// 页面一打开就加载数据（分类列表 + 商品列表）
onMounted(() => {
  loadTypes()
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

/* 封面缩略图 */
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

.price {
  color: #e24b4a;
  font-weight: 500;
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

/* ========== 富文本编辑器（wangEditor v5）========== */
/* 外框：给编辑器一圈边框，看起来像个输入框 */
.editor-wrap {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

/* 工具栏：和编辑区之间加条分隔线 */
.editor-toolbar {
  border-bottom: 1px solid #dcdfe6;
}

/* 编辑区：固定高度 + 内容多了可以滚动 */
.editor-body {
  height: 260px;
  overflow-y: auto;
}

/* ========== 预览弹窗的内容区 ========== */
.preview-content {
  max-height: 60vh;       /* 内容太长时弹窗内部滚动，不撑破屏幕 */
  overflow-y: auto;
  line-height: 1.7;
  color: #303133;
  word-break: break-word;
}

/* 详情里的图片限制最大宽度（否则上传的大图会把弹窗撑变形） */
.preview-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

/* 详情里的段落、列表留点间距，读起来舒服 */
.preview-content :deep(p) {
  margin: 8px 0;
}
.preview-content :deep(ul),
.preview-content :deep(ol) {
  padding-left: 22px;
}
</style>
