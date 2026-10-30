<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>公告管理</span>
          <el-button type="primary" @click="openDialog()">新增公告</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="搜索公告标题"
            clearable
            style="width: 240px"
            @keyup.enter="loadData"
        />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="公告内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="time" label="发布时间" width="180" align="center" />
        <el-table-column prop="userId" label="发布人ID" width="100" align="center" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增 / 编辑弹窗：form.id 有值 = 编辑，没值 = 新增 -->
    <el-dialog
        v-model="dialogVisible"
        :title="form.id ? '编辑公告' : '新增公告'"
        width="560px"
        destroy-on-close
        align-center
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="公告标题" prop="name">
          <el-input
              v-model="form.name"
              placeholder="如：双十一活动通知"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
              v-model="form.content"
              type="textarea"
              :rows="6"
              placeholder="写清时间、范围、注意事项"
              maxlength="2000"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：公告正文可能很长，右侧滑出比弹窗好读 -->
    <el-drawer v-model="detailVisible" title="公告详情" size="480px" destroy-on-close>
      <div class="detail">
        <div class="detail-item">
          <div class="detail-label">公告标题</div>
          <div class="detail-value">{{ detail.name }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">发布时间</div>
          <div class="detail-value">{{ detail.time || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">发布人ID</div>
          <div class="detail-value">{{ detail.userId ?? '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">公告内容</div>
          <div class="detail-value detail-content">{{ detail.content || '（无正文）' }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createNotice, deleteNotice, getNoticePage, updateNotice } from '@/api/notice'

// ========== 列表相关 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

// ========== 新增/编辑弹窗 ==========
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', content: '' })
const rules = {
  name: [{ required: true, message: '请输入公告标题', trigger: 'blur' }]
}

// ========== 详情抽屉 ==========
const detailVisible = ref(false)
const detail = reactive({ name: '', content: '', time: '', userId: null })

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    const data = await getNoticePage(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { id: null, name: '', content: '' })
}

/** 不传 row = 新增；传 row = 编辑 */
function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, { id: row.id, name: row.name || '', content: row.content || '' })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { name: form.name, content: form.content }
    if (!form.id) {
      await createNotice(payload)
      ElMessage.success('发布成功')
    } else {
      await updateNotice(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function openDetail(row) {
  Object.assign(detail, {
    name: row.name || '',
    content: row.content || '',
    time: row.time || '',
    userId: row.userId
  })
  detailVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除公告「${row.name}」？`, '提示', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-item {
  margin-bottom: 18px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  word-break: break-word;
}

.detail-content {
  white-space: pre-wrap;
}
</style>
