<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>菜单管理</span>
          <el-button type="primary" @click="openCreate()">新增菜单</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="treeData"
        class="admin-table menu-tree-table"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="path" label="路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canAddChild(row)"
              link
              type="primary"
              @click.stop="openCreate(row.id)"
            >
              新增子级
            </el-button>
            <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer-meta">共 {{ allMenus.length }} 项</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @closed="onDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="菜单标题" />
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" placeholder="如 /admin/home；目录可用 /admin/system" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名" />
        </el-form-item>
        <el-form-item label="父级菜单" prop="parentId">
          <el-select
            v-model="form.parentId"
            clearable
            filterable
            placeholder="无（顶级）"
            style="width: 100%"
            :teleported="true"
            popper-class="admin-dialog-popper"
          >
            <el-option label="无（顶级）" :value="0" />
            <el-option
              v-for="item in parentSelectOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id"
              :disabled="item.disabled"
            />
          </el-select>
          <p class="field-tip">请选择「系统管理」等目录作为父级；灰色项为页面菜单，不可选</p>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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
import { computed, inject, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMenu, deleteMenu, getAllMenus, updateMenu } from '@/api/menu'

/** 真实页面路由：不应再挂子菜单（侧栏仅支持两级） */
const PAGE_PATHS = new Set([
  '/admin/home',
  '/admin/admins',
  '/admin/users',
  '/admin/menus',
  '/admin/banners',
  '/admin/logs',
  '/admin/settings',
  '/admin/profile'
])

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const allMenus = ref([])
const formRef = ref()
const reloadSideMenus = inject('reloadSideMenus', null)

const form = reactive({
  id: null,
  title: '',
  path: '',
  icon: 'Menu',
  parentId: 0,
  sortOrder: 1,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }]
}

const treeData = computed(() => buildTree(allMenus.value))

function collectBlockedIds(menuId) {
  const blocked = new Set()
  if (!menuId) return blocked
  blocked.add(Number(menuId))
  const walk = (pid) => {
    allMenus.value.forEach((item) => {
      const id = Number(item.id)
      if (Number(item.parentId) === Number(pid) && !blocked.has(id)) {
        blocked.add(id)
        walk(id)
      }
    })
  }
  walk(menuId)
  return blocked
}

/** 可选父级：列出全部菜单（排除自身/子孙）；非目录禁用，避免误选 */
const parentSelectOptions = computed(() => {
  const blocked = collectBlockedIds(form.id)

  return allMenus.value
    .filter((item) => !blocked.has(Number(item.id)))
    .sort((a, b) => {
      const da = getMenuDepth(a)
      const db = getMenuDepth(b)
      if (da !== db) return da - db
      return Number(a.sortOrder || 0) - Number(b.sortOrder || 0) || Number(a.id) - Number(b.id)
    })
    .map((item) => {
      const id = Number(item.id)
      const directory = isDirectoryMenu(item)
      return {
        id,
        label: formatParentLabel(item),
        disabled: !directory
      }
    })
})

function formatParentLabel(item) {
  const depth = getMenuDepth(item)
  const pad = depth > 0 ? `${'　'.repeat(depth)}└ ` : ''
  return `${pad}${item.title}`
}

function getMenuDepth(item) {
  let depth = 0
  let parentId = Number(item.parentId || 0)
  const guard = new Set()
  while (parentId && !guard.has(parentId)) {
    guard.add(parentId)
    depth += 1
    const parent = allMenus.value.find((m) => Number(m.id) === parentId)
    parentId = parent ? Number(parent.parentId || 0) : 0
  }
  return depth
}

function normalizePath(path) {
  if (!path) return ''
  let p = String(path).startsWith('/') ? String(path) : `/${path}`
  if (!p.startsWith('/admin')) p = `/admin${p}`
  return p
}

function hasChildren(row) {
  if (row?.children?.length) return true
  return allMenus.value.some((item) => Number(item.parentId) === Number(row.id))
}

/** 目录菜单：可挂子级；页面菜单不可再挂子级 */
function isDirectoryMenu(row) {
  const path = normalizePath(row.path)
  if (!PAGE_PATHS.has(path)) return true
  return hasChildren(row)
}

function canAddChild(row) {
  return isDirectoryMenu(row)
}

function buildTree(list) {
  const map = new Map()
  const roots = []
  list.forEach((item) => {
    map.set(Number(item.id), { ...item, id: Number(item.id), parentId: Number(item.parentId || 0), children: [] })
  })
  map.forEach((node) => {
    const parentId = Number(node.parentId || 0)
    if (parentId && map.has(parentId)) {
      map.get(parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })
  const sortNodes = (nodes) => {
    nodes.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0) || a.id - b.id)
    nodes.forEach((n) => {
      if (n.children?.length) sortNodes(n.children)
      else delete n.children
    })
  }
  sortNodes(roots)
  return roots
}

function nextSortOrder(parentId) {
  const pid = Number(parentId || 0)
  const siblings = allMenus.value.filter((item) => Number(item.parentId || 0) === pid)
  if (!siblings.length) return 1
  return Math.max(...siblings.map((item) => Number(item.sortOrder || 0))) + 1
}

function resetForm() {
  form.id = null
  form.title = ''
  form.path = ''
  form.icon = 'Menu'
  form.parentId = 0
  form.sortOrder = 1
  form.status = 1
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAllMenus()
    const list = Array.isArray(data) ? data : data?.list || []
    allMenus.value = list.map((item) => ({
      ...item,
      id: Number(item.id),
      parentId: Number(item.parentId || 0)
    }))
  } catch {
    allMenus.value = []
  } finally {
    loading.value = false
  }
}

async function openCreate(parentId = null) {
  resetForm()
  const pid = parentId ? Number(parentId) : 0
  if (pid) {
    const parent = allMenus.value.find((item) => Number(item.id) === pid)
    if (!parent || !canAddChild(parent)) {
      ElMessage.warning('该菜单是页面菜单，不能再添加子级')
      return
    }
  }
  form.parentId = pid
  form.sortOrder = nextSortOrder(pid)
  dialogTitle.value = pid ? '新增子菜单' : '新增菜单'
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row) {
  resetForm()
  const fresh = allMenus.value.find((item) => Number(item.id) === Number(row.id)) || row
  form.id = Number(fresh.id)
  form.title = fresh.title
  form.path = fresh.path
  form.icon = fresh.icon
  form.parentId = Number(fresh.parentId || 0)
  form.sortOrder = Number(fresh.sortOrder || 0)
  form.status = fresh.status
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

function onDialogClosed() {
  resetForm()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  await formRef.value.validate()
  const parentId = Number(form.parentId || 0)
  if (parentId) {
    const parent = allMenus.value.find((item) => Number(item.id) === parentId)
    if (parent && PAGE_PATHS.has(normalizePath(parent.path)) && !hasChildren(parent)) {
      ElMessage.warning('不能选择页面菜单作为父级，请选择「系统管理」等目录')
      return
    }
  }
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      path: form.path,
      icon: form.icon,
      parentId,
      sortOrder: form.sortOrder,
      status: form.status
    }
    if (form.id) {
      await updateMenu(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createMenu(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadData()
    reloadSideMenus?.()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  if (hasChildren(row)) {
    ElMessage.warning('请先删除子菜单')
    return
  }
  await ElMessageBox.confirm(`确定删除菜单「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await loadData()
  reloadSideMenus?.()
}

onMounted(loadData)
</script>

<style scoped>
.panel {
  border: none;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-meta {
  margin-top: 14px;
  font-size: 13px;
  color: #94a3b8;
  text-align: right;
}

.field-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.menu-tree-table :deep(.el-table__expand-icon) {
  margin-right: 6px;
}

.menu-tree-table :deep(.el-table__cell) {
  overflow: visible;
}

.menu-tree-table :deep(.cell) {
  white-space: nowrap;
}
</style>
