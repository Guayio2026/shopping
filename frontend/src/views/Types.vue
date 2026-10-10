<!--
  商品分类管理 页面（前端界面）

  【这个页面在哪】src/views/Types.vue
  【怎么访问】登录后台后，网址 /admin/types 或点左侧菜单「商城管理 → 分类管理」
  【谁引用了它】src/router/index.js 里注册的路由；菜单入口在数据库 sys_menu 表

  【数据从哪来】调用 src/api/type.js 里的方法 → 后端 /api/type/** 接口 → MySQL 的 type 表
  【页面结构】上半部分 el-card（搜索框 + 表格 + 分页），下半部分 el-dialog（新增/编辑弹窗）
-->
<template>
  <div class="page">
    <!-- 主卡片：标题栏 + 内容 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>商品分类管理</span>
          <!-- 点击后打开空表单（新增模式） -->
          <el-button type="primary" @click="openDialog()">新增分类</el-button>
        </div>
      </template>

      <!-- 搜索区：输入关键字后按回车或点「查询」都会触发 loadData() -->
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索分类名称"
          clearable
          style="width: 240px"
          @keyup.enter="loadData"
        />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <!-- 数据表格：:data="tableData" 就是下面 loadData() 从后端拿到的数组 -->
      <el-table v-loading="loading" :data="tableData" stripe>
        <!-- prop 要和后端返回的字段名一致（Type.java 里的字段名） -->
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <!-- 创建时间需要格式化：后端给的是 2026-10-02T23:23:06，去掉中间的 T -->
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <!-- 操作列：编辑=带数据打开弹窗；删除=弹确认框后调删除接口 -->
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
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
      :title="form.id ? '编辑分类' : '新增分类'"
      width="480px"
      destroy-on-close
      align-center
    >
      <!-- rules 里配的校验：分类名称必填 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：数码、服装" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// ========== 依赖 ==========
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 后端接口封装：详见 src/api/type.js（里面注明了对应的后端地址）
import { createType, deleteType, getTypePage, updateType } from '@/api/type'

// ========== 状态变量（页面上的数据都放这里）==========
const loading = ref(false)        // 表格加载中（控制转圈）
const submitting = ref(false)     // 弹窗提交中（防止重复点确定）
const dialogVisible = ref(false)  // 弹窗是否显示
const tableData = ref([])         // 表格数据（来自后端）
const total = ref(0)              // 总条数（分页用）
const formRef = ref()             // 表单引用（用来触发校验）

// 查询条件：keyword 搜索词，page 第几页，size 每页几条
const query = reactive({ keyword: '', page: 1, size: 10 })

// 弹窗表单数据：id 是我自己加的标记（null=新增，有值=编辑）
const form = reactive({
  id: null,
  name: '',
  sortOrder: 0
})

// 表单校验规则：name 必填
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// ========== 方法 ==========

/** 清空表单（打开"新增"弹窗前调用） */
function resetForm() {
  Object.assign(form, { id: null, name: '', sortOrder: 0 })
}

/** 时间格式化：把 2026-10-02T23:23:06 显示成 2026-10-02 23:23:06 */
function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

/**
 * 【本页最核心的函数】加载表格数据 —— 让表格显示出最新的分类列表
 *
 * 一句话：去后端要"第几页 + 关键字"的数据，拿回来塞进表格。
 *
 * ==================== 谁在调用它？（5 个地方） ====================
 *  1. onMounted(loadData)         页面刚打开时 —— 所以一进来就能看到列表
 *  2. 「查询」按钮 / 搜索框回车     用户想筛数据时
 *  3. 分页控件翻页(@current-change) 用户点第 2 页、第 3 页时
 *  4. handleSubmit() 里          新增/编辑成功后 —— 让表格显示最新结果
 *  5. handleDelete() 里          删除成功后 —— 让那一行从表格里消失
 * ================================================================
 * 这就是"一个函数被多处复用"——5 个入口，同一套取数逻辑，不用写 5 遍。
 *
 * 【和 delete 那段的区别】handleDelete 是"改数据"（DELETE），
 * 而 loadData 是"读数据"（GET）。增删改之后，都要再调一次 loadData 把最新数据读回来。
 */
async function loadData() {
  // ① 打开 loading 状态 → 表格出现转圈遮罩（v-loading="loading" 绑的就是它）
  loading.value = true
  try {
    // ② 组装请求参数。相当于问后端："我要第 1 页、每页 10 条、关键字是空的"
    //    query 是上面定义的 reactive 对象：{ keyword:'', page:1, size:10 }
    const params = { page: query.page, size: query.size }
    // 有关键字才带上 keyword（空字符串就不传，后端就当"不筛选"）
    if (query.keyword) params.keyword = query.keyword
    // ③ 发请求给后端，等它返回
    //    getTypePage 来自 api/type.js，里面是 request.get('/type/page', { params })
    //    → 加上 /api 前缀和 token → 最终 GET http://localhost:8080/api/type/page?page=1&size=10
    //    await 就是"停在这，等数据回来再往下"（后端这会儿正在查数据库）
    const data = await getTypePage(params)   // 后端返回 { total: 4, list: [...] }
    // ④ 把拿到的数据塞给页面变量。这一步一改，表格立刻自己重画（Vue 的响应式）
    tableData.value = data.list || []        // list 是当前页的数据 → 表格每一行
    total.value = data.total || 0            // total 是总条数 → 分页控件显示"共 4 条"
  } finally {
    // ⑤ 不管成功还是失败，都关掉 loading 转圈
    //    用 finally 是为了"请求报错时转圈也不能一直转着"——体验细节
    loading.value = false
  }
}

/** 打开弹窗：不传参数=新增（空表单）；传 row=编辑（把该行数据填进表单） */
function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      sortOrder: row.sortOrder ?? 0
    })
  }
  dialogVisible.value = true
}

/** 提交表单：先校验 → id 为空走新增接口，否则走更新接口 → 成功后刷新列表 */
async function handleSubmit() {
  await formRef.value.validate()   // 校验不通过会抛异常，下面的代码不执行
  submitting.value = true
  try {
    const payload = { name: form.name, sortOrder: form.sortOrder }
    if (!form.id) {
      await createType(payload)          // POST /api/type
      ElMessage.success('创建成功')
    } else {
      await updateType(form.id, payload) // PUT /api/type/{id}
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()                            // 重新拉列表，看到最新数据
  } finally {
    submitting.value = false
  }
}

/**
 * 【删除】表格里点「删除」时执行
 *
 * 参数 row：表格这一行的数据对象，长这样（就是后端返回的那一条）
 *     { id: 11, name: '服装', sortOrder: 2, createdAt: '2026-10-02T23:23:06' }
 *   它从哪来？el-table-column 的插槽 <template #default="{ row }"> 传进来的。
 *
 * 关键字 async / await：
 *   async —— 声明"我这个函数里有需要等待的慢操作"（发请求、等用户点确认）
 *   await —— "在这儿停下，等它做完再往下走"
 *   规则：await 只能写在 async 函数里面，两个是一对。
 *   没有 await 的话，JS 不会等，会直接往下跑，导致"先刷新列表、后删除完"，顺序全乱。
 */
async function handleDelete(row) {
  // ① 弹确认框，等用户点「确定」
  //    ElMessageBox.confirm(...) 返回一个"未来才有结果"的东西，await 就是等它的结果。
  //    模板字符串 `${row.name}` 会把变量拼进文字里 → 显示成：确认删除分类「服装」？
  //    返回值我们没接收（前面没有 const ok = ）——因为用户点「取消」时它会抛异常，
  //    后面的代码自动不执行，效果正好就是"取消 = 什么都不做"。
  //    ⚠️ 这一步纯前端，不发任何网络请求。
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })

  // ② 真正发请求给后端，等它返回结果
  //    deleteType 是 src/api/type.js 里的函数，里面执行的是：
  //        request.delete('/type/' + row.id)
  //    row.id 是 11 → 拼成 "/type/11" → request.js 再加 /api 前缀和 token
  //    → 最终请求：DELETE http://localhost:8080/api/type/11
  //    后端 TypeController 的 delete() 接住 → TypeService.delete() → SQL 执行删除
  //    等它返回 {"code":200,...} 才继续（如果返回 401/500，request.js 会弹红字提示并中断）
  await deleteType(row.id)                // DELETE /api/type/{id}

  // ③ 走到这里说明删除成功了，右上角弹一个绿色提示
  //    纯界面效果，不碰网络。
  ElMessage.success('已删除')

  // ④ 重新查一遍列表，让表格里那行消失
  //    loadData() 内部也是一次请求：GET /api/type/page
  //    为什么要重新查而不是手动从 tableData 里删掉那一项？
  //      重新查最省事也最准确（顺便同步了总条数 total、别人可能改过的数据）。
  loadData()
}

// ========== 生命周期 ==========
// 页面一打开就加载数据（所以进来就能看到分类列表）
onMounted(loadData)
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
}

/* 分页靠右 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
