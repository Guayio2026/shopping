<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="aside-brand">
        <BrandLogo size="sm" class="aside-brand-logo" />
        <div class="brand-copy">
          <span class="logo">{{ systemStore.displayName }}</span>
          <span class="logo-sub">管理后台</span>
        </div>
      </div>
      <el-menu
        :key="menuTreeVersion"
        ref="sideMenuRef"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        router
        class="side-menu"
        @select="handleMenuSelect"
      >
        <template v-for="item in menuTree" :key="item.key || item.path">
          <el-sub-menu v-if="item.children?.length" :index="item.key || item.path">
            <template #title>
              <el-icon><component :is="resolveMenuIcon(item.icon)" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon><component :is="resolveMenuIcon(child.icon)" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="resolveMenuIcon(item.icon)" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="content-wrap">
      <el-header class="top-bar" height="56px">
        <div class="top-toolbar">
          <ThemeSwitcher />
          <span class="toolbar-divider" />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="toolbar-item user-item">
              <el-avatar :size="28" :src="headerAvatarUrl" class="header-avatar">
                {{ avatarText }}
              </el-avatar>
              <span class="username">{{ displayName }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <PageTabs />
      <el-main class="main">
        <router-view v-slot="{ Component, route: currentRoute }">
          <component
            :is="Component"
            :key="`${currentRoute.fullPath}-${viewKey}`"
          />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useTabsStore } from '@/stores/tabs'
import { useSystemStore } from '@/stores/system'
import { getAllMenus } from '@/api/menu'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import PageTabs from '@/components/PageTabs.vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { resolveImageUrl } from '@/utils/format'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tabsStore = useTabsStore()
const systemStore = useSystemStore()

function resolveMenuIcon(name) {
  if (!name) return ElementPlusIconsVue.Menu
  return ElementPlusIconsVue[name] || ElementPlusIconsVue.Menu
}

/** 侧栏兜底（接口失败时使用）
 *  注意：正常情况菜单来自数据库 sys_menu 表（见下方 buildMenuTreeFromDb），
 *  这里只是「接口挂了」时的备用菜单。新增菜单记得两边都加。 */
const STATIC_MENU_TREE = [
  { key: 'home', path: '/admin/home', title: '首页', icon: 'HomeFilled' },
  {
    key: 'system',
    path: '/admin/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/admin/admins', title: '管理员管理', icon: 'Avatar' },
      { path: '/admin/users', title: '用户管理', icon: 'User' },
      { path: '/admin/menus', title: '菜单管理', icon: 'Grid' },
      { path: '/admin/banners', title: '轮播图管理', icon: 'Picture' },
      { path: '/admin/logs', title: '系统日志', icon: 'Tickets' },
      { path: '/admin/settings', title: '系统设置', icon: 'Tools' }
    ]
  },
  // 商城管理分组（新增）：下面挂的是商城业务模块，目前只有分类管理
  {
    key: 'mall',
    path: '/admin/mall',
    title: '商城管理',
    icon: 'Goods',
    children: [
      { path: '/admin/types', title: '分类管理', icon: 'Grid' }
    ]
  }
]

const DIRECTORY_PATHS = new Set(['/admin/system'])

function cloneMenuTree(tree) {
  return tree.map((item) => ({
    ...item,
    children: item.children ? item.children.map((c) => ({ ...c })) : undefined
  }))
}

function normalizeAdminPath(path) {
  if (!path) return ''
  let p = path.startsWith('/') ? path : `/${path}`
  if (!p.startsWith('/admin')) p = `/admin${p}`
  return p
}

/** 侧栏与菜单管理共用：完全按数据库 parentId 建树 */
function buildMenuTreeFromDb(list) {
  const enabled = list.filter((item) => item.status !== 0 && item.path)
  const map = new Map()
  enabled.forEach((item) => {
    const id = Number(item.id)
    map.set(id, {
      key: `menu-${id}`,
      id,
      path: normalizeAdminPath(item.path),
      title: item.title,
      icon: item.icon || 'Menu',
      parentId: Number(item.parentId || 0),
      sortOrder: Number(item.sortOrder || 0),
      children: []
    })
  })

  const roots = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })

  const sortNodes = (nodes) => {
    nodes.sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    nodes.forEach((n) => {
      if (n.children?.length) sortNodes(n.children)
      else delete n.children
    })
  }
  sortNodes(roots)
  return roots
}

function collectOpenKeys(tree) {
  const keys = []
  const walk = (nodes) => {
    nodes.forEach((n) => {
      if (n.children?.length) {
        keys.push(n.key)
        walk(n.children)
      }
    })
  }
  walk(tree)
  return keys
}

const menuTree = ref(cloneMenuTree(STATIC_MENU_TREE))
const menuTreeVersion = ref(0)
const defaultOpeneds = ref(['system'])
const viewKey = ref(0)
const sideMenuRef = ref()
let sideMenuScrollTop = 0

function getSideMenuEl() {
  const inst = sideMenuRef.value
  return inst?.$el || inst || null
}

function rememberSideMenuScroll() {
  const el = getSideMenuEl()
  if (el) sideMenuScrollTop = el.scrollTop
}

function restoreSideMenuScroll() {
  const el = getSideMenuEl()
  if (!el) return
  el.scrollTop = sideMenuScrollTop
}

const activeMenu = computed(() => route.path || '/admin/home')
const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const headerAvatarUrl = computed(() => resolveImageUrl(userStore.user?.avatar))

async function loadMenus() {
  try {
    const data = await getAllMenus()
    const list = Array.isArray(data) ? data : data?.list || []
    if (list.length) {
      const tree = buildMenuTreeFromDb(list)
      menuTree.value = tree
      defaultOpeneds.value = collectOpenKeys(tree)
      menuTreeVersion.value += 1
    } else {
      menuTree.value = cloneMenuTree(STATIC_MENU_TREE)
      defaultOpeneds.value = ['system']
    }
  } catch {
    menuTree.value = cloneMenuTree(STATIC_MENU_TREE)
    defaultOpeneds.value = ['system']
  }
}

watch(
  () => route.fullPath,
  () => {
    if (route.meta?.public) {
      return
    }
    tabsStore.openTab(route)
    // 路由切换后恢复侧栏滚动位置，避免点菜单滚回顶部
    nextTick(() => {
      restoreSideMenuScroll()
      requestAnimationFrame(restoreSideMenuScroll)
    })
  },
  { immediate: true }
)

function normalizeMenuPath(path) {
  if (!path) return '/admin/home'
  const p = path.startsWith('/') ? path : `/${path}`
  return p.startsWith('/admin') ? p : `/admin${p}`
}

function handleMenuSelect(index) {
  rememberSideMenuScroll()
  const path = normalizeMenuPath(index)
  // 目录节点不跳转
  if (DIRECTORY_PATHS.has(path) || String(index).startsWith('menu-')) {
    return
  }
  if (path === route.path) {
    viewKey.value += 1
  }
}

function refreshView() {
  viewKey.value += 1
}

provide('refreshView', refreshView)
provide('reloadSideMenus', loadMenus)

function handleCommand(command) {
  if (command === 'logout') {
    tabsStore.resetTabs()
    userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/admin/login')
    return
  }
  if (command === 'profile') {
    router.push('/admin/profile')
  }
}

onMounted(() => {
  document.documentElement.classList.add('is-admin')
  document.documentElement.classList.remove('is-portal')
  loadMenus()
  nextTick(() => {
    const el = getSideMenuEl()
    if (!el) return
    el.addEventListener(
      'scroll',
      () => {
        sideMenuScrollTop = el.scrollTop
      },
      { passive: true }
    )
  })
})

onUnmounted(() => {
  document.documentElement.classList.remove('is-admin')
})
</script>

<style scoped>
.layout {
  height: 100vh;
  overflow: hidden;
  background: var(--app-page-bg, #f5f7fa);
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8ecf0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.logo {
  font-size: 14px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: 0.4px;
  white-space: nowrap;
  line-height: 1.2;
}

.content-wrap {
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
}

.top-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-toolbar :deep(.toolbar-item) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
  outline: none;
}

.top-toolbar :deep(.toolbar-item:hover) {
  background: #f1f5f9;
  color: var(--app-primary);
}

.toolbar-divider {
  width: 1px;
  height: 18px;
  margin: 0 2px;
  background: #e2e8f0;
}

.user-item {
  padding-right: 10px;
}

.header-avatar {
  flex-shrink: 0;
  background: #f1f5f9;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #334155;
}

.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
  transition: color 0.2s ease;
}

.user-item:hover .arrow-icon {
  color: #64748b;
}

.aside {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--app-shell-bg);
  border-right: 1px solid var(--app-shell-border);
}

.aside-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--app-shell-border);
  flex-shrink: 0;
}

.brand-mark {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  color: #fff;
  background:
    radial-gradient(circle at 30% 24%, rgba(255, 255, 255, 0.4), transparent 42%),
    linear-gradient(145deg, #ff5a5a 0%, #f01414 48%, #c01010 100%);
  box-shadow:
    0 6px 14px rgba(240, 20, 20, 0.32),
    inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

.aside-brand-logo {
  width: 34px !important;
  height: 34px !important;
  border-radius: 10px;
  box-shadow: 0 6px 14px rgba(240, 20, 20, 0.28);
  flex-shrink: 0;
}

.brand-mark::after {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.16);
  pointer-events: none;
}

.brand-glyph {
  width: 18px;
  height: 18px;
  display: block;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  min-width: 0;
}

.brand-copy .logo,
.brand-copy .logo-sub {
  display: block;
  margin: 0;
  padding: 0;
  letter-spacing: 0.05em;
  text-align: left;
  white-space: nowrap;
}

.brand-copy .logo-sub {
  font-size: 10px;
  font-weight: 400;
  color: rgba(248, 250, 252, 0.52);
}

.side-menu {
  flex: 1;
  min-height: 0;
  border-right: none;
  background: transparent;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
}

.side-menu:hover {
  scrollbar-color: rgba(148, 163, 184, 0.35) transparent;
}

.side-menu::-webkit-scrollbar {
  width: 4px;
}

.side-menu::-webkit-scrollbar-track {
  background: transparent;
}

.side-menu::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 999px;
}

.side-menu:hover::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.35);
}

.side-menu:hover::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.55);
}

.side-menu :deep(.el-menu) {
  background-color: transparent !important;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  position: relative;
  color: var(--app-menu-text) !important;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.side-menu :deep(.el-menu-item .el-icon),
.side-menu :deep(.el-sub-menu__title .el-icon) {
  color: var(--app-menu-text) !important;
  transition: color 0.2s ease;
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--app-menu-hover-bg) !important;
  color: var(--app-menu-text) !important;
}

.side-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title .el-icon) {
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background-color: var(--app-menu-hover-bg) !important;
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-menu-active-bar);
}

.side-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 52px !important;
}

.main {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px;
  background: var(--app-page-bg, #f5f7fa);
  overscroll-behavior: contain;
}
</style>
