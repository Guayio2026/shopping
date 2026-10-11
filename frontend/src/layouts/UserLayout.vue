<template>
  <div class="user-shell">
    <header class="user-nav" :class="{ scrolled: scrolled }">
      <div class="nav-inner">
        <router-link to="/user/home" class="brand">
          <BrandLogo size="md" class="nav-brand-logo" />
          <span class="brand-text">
            <span class="brand-name">{{ systemStore.displayName }}</span>
            <span class="brand-tagline">简洁高效 · 开箱即用</span>
          </span>
        </router-link>

        <nav class="nav-links">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
          >
            <el-icon :size="16"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
        </nav>

        <div class="nav-actions">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="click" @command="onCommand">
              <button type="button" class="user-chip">
                <el-avatar :size="34" :src="avatarSrc">{{ avatarFallback }}</el-avatar>
                <span class="user-meta">
                  <span class="user-name">{{ displayName }}</span>
                  <span class="user-role">已登录</span>
                </span>
                <el-icon class="chev" :size="14"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">首页</el-dropdown-item>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link class="btn-login" to="/login">登录</router-link>
            <router-link class="btn-primary" to="/register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="user-main">
      <router-view />
    </main>

    <footer class="user-footer">
      <p>{{ systemStore.displayName }} · 用户中心</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Goods, HomeFilled, Star, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import { mediaUrl } from '@/utils/format'
import BrandLogo from '@/components/BrandLogo.vue'

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()
const scrolled = ref(false)

const navItems = [
  { path: '/user/home', title: '首页', icon: HomeFilled },
  { path: '/user/goods', title: '全部商品', icon: Goods },
  // 第八章新增：我的收藏（要登录 —— 游客点它会被路由守卫送去 /login）
  { path: '/user/collect', title: '我的收藏', icon: Star },
  { path: '/user/profile', title: '个人中心', icon: User }
]

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const avatarSrc = computed(() => mediaUrl(userStore.user?.avatar))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const isLoggedIn = computed(() => !!userStore.token)

function onScroll() {
  scrolled.value = window.scrollY > 8
}

function onCommand(cmd) {
  if (cmd === 'home') router.push('/user/home')
  if (cmd === 'profile') router.push('/user/profile')
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/user/home')
  }
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  if (userStore.token && !userStore.user) {
    try {
      await userStore.fetchUser()
    } catch {
      userStore.logout()
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.user-shell {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e63935;
  --accent-soft: rgba(230, 57, 53, 0.08);
  --bg: #f7f8fa;
  min-height: 100vh;
  color: var(--ink);
  background: var(--bg);
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
}

.user-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid transparent;
  backdrop-filter: blur(14px);
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.user-nav.scrolled {
  border-bottom-color: var(--line);
  box-shadow: 0 8px 24px rgba(26, 26, 26, 0.04);
}

.nav-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--ink);
  flex-shrink: 0;
  transition: opacity 0.2s ease;
}

.brand:hover {
  opacity: 0.92;
}

.brand-logo {
  width: 42px;
  height: 42px;
  display: block;
  filter: drop-shadow(0 6px 14px rgba(230, 57, 53, 0.28));
}

.nav-brand-logo {
  filter: drop-shadow(0 6px 14px rgba(230, 57, 53, 0.28));
}

.brand-logo svg {
  width: 100%;
  height: 100%;
  display: block;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.01em;
}

.brand-tagline {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.2;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 1;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 999px;
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.nav-link:hover {
  color: var(--ink);
  background: rgba(26, 26, 26, 0.04);
}

.nav-link.router-link-active {
  color: var(--accent);
  background: var(--accent-soft);
  font-weight: 600;
}

.nav-link :deep(.el-icon) {
  transition: transform 0.2s ease;
}

.nav-link:hover :deep(.el-icon) {
  transform: translateY(-1px);
}

.nav-actions {
  margin-left: auto;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.btn-login,
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  padding: 0 18px;
  border-radius: 999px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.btn-login {
  color: var(--ink);
  background: #fff;
  border: 1px solid var(--line);
}

.btn-login:hover {
  border-color: #dddfe5;
  transform: translateY(-1px);
}

.btn-primary {
  color: #fff;
  background: var(--accent);
  border: 1px solid var(--accent);
}

.btn-primary:hover {
  background: #cf2f2c;
  border-color: #cf2f2c;
  transform: translateY(-1px);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 4px 12px 4px 4px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.user-chip:hover {
  border-color: #dddfe5;
  box-shadow: 0 6px 18px rgba(26, 26, 26, 0.06);
  transform: translateY(-1px);
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1px;
  min-width: 0;
  text-align: left;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  max-width: 108px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: var(--muted);
}

.chev {
  color: #9ca3af;
  margin-right: 2px;
}

.user-main {
  min-height: calc(100vh - 148px);
}

.user-footer {
  padding: 28px;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
  border-top: 1px solid var(--line);
  background: #fff;
}

.user-footer p {
  margin: 0;
}

@media (max-width: 900px) {
  .nav-inner {
    padding: 12px 16px;
    gap: 14px;
  }

  .brand-tagline {
    display: none;
  }

  .nav-links {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .user-meta {
    display: none;
  }

  .chev {
    display: none;
  }

  .user-chip {
    padding: 3px;
  }
}

@media (max-width: 640px) {
  .nav-link span {
    display: none;
  }

  .nav-link {
    padding: 10px 12px;
  }
}
</style>
