<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>系统日志</span>
          <span class="header-tip">登录、注册与后台操作记录</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名/模块"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="登录" value="LOGIN" />
          <el-option label="注册" value="REGISTER" />
          <el-option label="操作" value="OPERATION" />
          <el-option label="错误" value="ERROR" />
        </el-select>
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" effect="light">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作" min-width="140" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" min-width="170" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteLog, getLogPage } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({ keyword: '', type: '', page: 1, size: 10 })

function typeLabel(type) {
  return { LOGIN: '登录', REGISTER: '注册', OPERATION: '操作', ERROR: '错误' }[type] || type || '-'
}

function typeTag(type) {
  return { LOGIN: 'success', REGISTER: 'warning', OPERATION: 'primary', ERROR: 'danger' }[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.type) delete params.type
    const data = await getLogPage(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该日志记录吗？', '提示', { type: 'warning' })
  await deleteLog(row.id)
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

.header-tip {
  font-size: 13px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
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
