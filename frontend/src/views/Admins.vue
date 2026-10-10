<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>管理员管理</span>
          <el-button type="primary" @click="openDialog()">新增管理员</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名/昵称"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" class="admin-table" table-layout="auto">
        <el-table-column label="头像" width="72" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="avatarSrc(row.avatar)">
              {{ (row.nickname || row.username || 'A').slice(0, 1) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="110" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
        <el-table-column label="角色" width="100" align="center">
          <template #default>
            <el-tag type="danger" effect="light" round>管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="128" align="center">
          <template #default="{ row }">
            <span :class="{ 'cell-empty': !row.phone }">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="88" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="168" align="center" />
        <el-table-column label="操作" width="120" align="center">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑管理员' : '新增管理员'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <ImageUpload
            v-model="form.avatar"
            type="avatar"
            :avatar-size="72"
            upload-text="上传头像"
            :fallback="form.nickname || form.username || 'A'"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
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
import { createAdmin, deleteAdmin, getAdminPage, updateAdmin } from '@/api/admin'
import ImageUpload from '@/components/ImageUpload.vue'
import { mediaUrl } from '@/utils/format'

function avatarSrc(url) {
  return mediaUrl(url)
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref()

const query = reactive({ keyword: '', page: 1, size: 10 })
const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  phone: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{
    validator: (_rule, value, callback) => {
      if (!form.id && !value) {
        callback(new Error('请输入密码'))
        return
      }
      callback()
    },
    trigger: 'blur'
  }]
}

function resetForm() {
  form.id = null
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.avatar = ''
  form.phone = ''
  form.status = 1
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminPage({ ...query })
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
      username: row.username,
      nickname: row.nickname,
      avatar: row.avatar || '',
      phone: row.phone,
      status: row.status
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.id && !form.password) {
    ElMessage.warning('请输入密码')
    return
  }
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      nickname: form.nickname,
      avatar: form.avatar || '',
      phone: form.phone,
      status: form.status
    }
    if (!form.id) {
      payload.password = form.password
      await createAdmin(payload)
      ElMessage.success('创建成功')
    } else {
      await updateAdmin(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除管理员「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteAdmin(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.panel {
  border: none;
}

.header-row,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar {
  margin-bottom: 16px;
  justify-content: flex-start;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
