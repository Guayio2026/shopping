<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>轮播图管理</span>
          <el-button type="primary" @click="openDialog()">新增轮播</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索标题/副标题"
          clearable
          style="width: 240px"
          @keyup.enter="loadData"
        />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column label="封面" width="140" align="center">
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
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="subtitle" label="副标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="linkUrl" label="跳转链接" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ctaText" label="按钮文案" width="110" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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
          layout="total, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑轮播' : '新增轮播'"
      width="640px"
      destroy-on-close
      align-center
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="轮播主标题" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="一句话介绍" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="封面" prop="cover">
          <ImageUpload v-model="form.cover" type="cover" upload-text="上传封面" />
          <p class="form-tip">建议尺寸 1600×640，横图效果更佳</p>
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="如 https://example.com 或 /about" />
        </el-form-item>
        <el-form-item label="按钮文案" prop="ctaText">
          <el-input v-model="form.ctaText" placeholder="如：了解更多" maxlength="20" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBanner, deleteBanner, getBannerPage, updateBanner } from '@/api/banner'
import ImageUpload from '@/components/ImageUpload.vue'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref()

const query = reactive({ keyword: '', page: 1, size: 10 })
const form = reactive({
  id: null,
  title: '',
  subtitle: '',
  cover: '',
  linkUrl: 'https://example.com',
  ctaText: '了解更多',
  sortOrder: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  cover: [{ required: true, message: '请上传封面', trigger: 'change' }],
  linkUrl: [{ required: true, message: '请输入跳转链接', trigger: 'blur' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    title: '',
    subtitle: '',
    cover: '',
    linkUrl: 'https://example.com',
    ctaText: '了解更多',
    sortOrder: 0,
    status: 1
  })
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    const data = await getBannerPage(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      id: row.id,
      title: row.title || '',
      subtitle: row.subtitle || '',
      cover: row.cover || '',
      linkUrl: row.linkUrl || 'https://example.com',
      ctaText: row.ctaText || '了解更多',
      sortOrder: row.sortOrder ?? 0,
      status: row.status ?? 1
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      subtitle: form.subtitle,
      cover: form.cover,
      linkUrl: form.linkUrl,
      ctaText: form.ctaText,
      sortOrder: form.sortOrder,
      status: form.status
    }
    if (!form.id) {
      await createBanner(payload)
      ElMessage.success('创建成功')
    } else {
      await updateBanner(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除轮播「${row.title}」？`, '提示', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-row,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar {
  justify-content: flex-start;
  margin-bottom: 16px;
}

.cover-thumb {
  width: 108px;
  height: 54px;
  border-radius: 8px;
  overflow: hidden;
}

.cover-empty {
  color: #94a3b8;
  font-size: 13px;
}

.form-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
