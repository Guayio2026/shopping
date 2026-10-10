<template>
  <div class="page home" v-loading="loading">
    <section class="welcome-card">
      <div class="welcome-glow" />
      <div class="welcome-body">
        <p class="welcome-kicker">管理后台</p>
        <h2>你好，{{ displayName }}</h2>
        <p class="welcome-desc">
          欢迎使用 {{ systemStore.displayName }}。这是一个精简的管理脚手架，包含用户、菜单、轮播图、日志与系统设置等基础能力。
        </p>
        <div class="welcome-links">
          <router-link to="/admin/admins" class="link-chip">管理员管理</router-link>
          <router-link to="/admin/users" class="link-chip">用户管理</router-link>
          <router-link to="/admin/menus" class="link-chip">菜单管理</router-link>
          <router-link to="/admin/banners" class="link-chip">轮播图管理</router-link>
          <router-link to="/admin/logs" class="link-chip">系统日志</router-link>
          <router-link to="/admin/settings" class="link-chip">系统设置</router-link>
        </div>
      </div>
    </section>

    <section class="stat-grid">
      <article v-for="item in statCards" :key="item.key" class="stat-card" :class="item.key">
        <div class="stat-icon">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-meta">
          <p class="stat-label">{{ item.label }}</p>
          <p class="stat-value">{{ item.value }}</p>
        </div>
      </article>
    </section>

    <section class="chart-grid">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="header-row">
            <span>近 7 日用户新增</span>
          </div>
        </template>
        <div ref="userChartRef" class="chart-box" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="header-row">
            <span>近 7 日日志量</span>
          </div>
        </template>
        <div ref="logChartRef" class="chart-box" />
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { Avatar, Tickets, User } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import { useThemeStore } from '@/stores/theme'

const userStore = useUserStore()
const systemStore = useSystemStore()
const themeStore = useThemeStore()
const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '管理员')

const loading = ref(false)
const stats = ref({
  adminCount: 0,
  userCount: 0,
  logCount: 0,
  userTrend: [],
  logTrend: []
})

const userChartRef = ref()
const logChartRef = ref()
let userChart
let logChart

const statCards = computed(() => [
  { key: 'admin', label: '管理员数', value: stats.value.adminCount, icon: Avatar },
  { key: 'user', label: '用户数', value: stats.value.userCount, icon: User },
  { key: 'log', label: '日志数', value: stats.value.logCount, icon: Tickets }
])

function primaryColor() {
  return getComputedStyle(document.documentElement).getPropertyValue('--app-primary').trim() || '#3b82f6'
}

function softColor() {
  return getComputedStyle(document.documentElement).getPropertyValue('--app-primary-soft').trim() || '#eff6ff'
}

function buildTrendOption(title, points, isBar) {
  const labels = (points || []).map((p) => p.date)
  const values = (points || []).map((p) => Number(p.value || 0))
  const color = primaryColor()
  return {
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 28, bottom: 28 },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      isBar
        ? {
            name: title,
            type: 'bar',
            barWidth: 22,
            itemStyle: {
              borderRadius: [8, 8, 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color },
                { offset: 1, color: softColor() }
              ])
            },
            data: values
          }
        : {
            name: title,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: { width: 3 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color },
                { offset: 1, color: 'rgba(255,255,255,0)' }
              ])
            },
            data: values
          }
    ]
  }
}

function renderCharts() {
  if (!userChartRef.value || !logChartRef.value) return
  if (!userChart) userChart = echarts.init(userChartRef.value)
  if (!logChart) logChart = echarts.init(logChartRef.value)
  userChart.setOption(buildTrendOption('用户新增', stats.value.userTrend, true), true)
  logChart.setOption(buildTrendOption('日志量', stats.value.logTrend, false), true)
}

function handleResize() {
  userChart?.resize()
  logChart?.resize()
}

async function loadStats() {
  loading.value = true
  try {
    const data = await getDashboardStats()
    stats.value = {
      adminCount: data?.adminCount || 0,
      userCount: data?.userCount || 0,
      logCount: data?.logCount || 0,
      userTrend: data?.userTrend || [],
      logTrend: data?.logTrend || []
    }
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

watch(
  () => themeStore.currentKey,
  async () => {
    await nextTick()
    renderCharts()
  }
)

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userChart?.dispose()
  logChart?.dispose()
  userChart = null
  logChart = null
})
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.welcome-card {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.welcome-glow {
  position: absolute;
  width: 320px;
  height: 220px;
  right: -40px;
  top: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--app-primary, #3b82f6) 18%, transparent), transparent 70%);
  pointer-events: none;
}

.welcome-body {
  position: relative;
  padding: 32px 28px;
}

.welcome-kicker {
  margin: 0 0 10px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: #64748b;
  text-transform: uppercase;
}

.welcome-body h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 760;
  color: #0f172a;
}

.welcome-desc {
  margin: 12px 0 0;
  max-width: 640px;
  font-size: 14px;
  line-height: 1.7;
  color: #64748b;
}

.welcome-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.link-chip {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--app-primary, #3b82f6) 16%, transparent);
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 6%, transparent);
  color: var(--app-primary, #1d4ed8);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s ease, transform 0.2s ease;
}

.link-chip:hover {
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 12%, transparent);
  transform: translateY(-1px);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--app-primary, #3b82f6);
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 12%, #fff);
}

.stat-card.admin .stat-icon {
  color: #0f766e;
  background: rgba(15, 118, 110, 0.1);
}

.stat-card.user .stat-icon {
  color: var(--app-primary, #3b82f6);
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 12%, #fff);
}

.stat-card.log .stat-icon {
  color: #b45309;
  background: rgba(180, 83, 9, 0.1);
}

.stat-label {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin: 6px 0 0;
  font-size: 28px;
  font-weight: 760;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-card {
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 18px;
}

.chart-box {
  height: 300px;
  width: 100%;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@media (max-width: 960px) {
  .stat-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
