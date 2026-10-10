import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/** 通用深色菜单基底 */
export const SHELL_BASE = '#1e222c'

export const THEME_CATEGORIES = [
  { key: 'solid', label: '纯色系 · 商务极简', desc: 'OA / ERP / 数据中台' },
  { key: 'gradient', label: '渐变系 · 科技质感', desc: 'SaaS / 物联网 / 开发后台' },
  { key: 'accent', label: '撞色点缀 · 轻奢质感', desc: '商城 / CRM / 会员系统' },
  { key: 'dark', label: '极简灰黑 · 纯黑沉浸', desc: '监控 / 运维 / 硬核工具' }
]

export const THEME_PRESETS = [
  // ── 纯色系 · 商务极简 ──
  {
    key: 'sky-blue',
    label: '深空蓝',
    category: 'solid',
    shell: '#1e222c',
    text: '#a0aec0',
    hover: '#242b38',
    active: '#3b82f6',
    desc: '低饱和蓝，政企、企业管理系统首选'
  },
  {
    key: 'silver-gray',
    label: '雅灰银',
    category: 'solid',
    shell: '#1e222c',
    text: '#94a3b8',
    hover: '#232934',
    active: '#cbd5e1',
    desc: '纯灰度层次，极简后台、监控大屏'
  },
  {
    key: 'ink-green',
    label: '暗青墨绿',
    category: 'solid',
    shell: '#161a23',
    text: '#98a2b3',
    hover: '#222c36',
    active: '#10b981',
    desc: '安全稳重，运维、安防、项目管理'
  },
  // ── 渐变系 · 科技质感 ──
  {
    key: 'aurora',
    label: '极光紫蓝',
    category: 'gradient',
    shell: '#161a23',
    text: '#9ca3af',
    hover: '#212733',
    active: '#8b5cf6',
    gradientFrom: '#8b5cf6',
    gradientTo: '#06b6d4',
    desc: '赛博科技风，选中条渐变高亮'
  },
  {
    key: 'deep-sea',
    label: '深海青蓝',
    category: 'gradient',
    shell: '#1e222c',
    text: '#a1acbf',
    hover: '#202835',
    active: '#0ea5e9',
    desc: '冷色调，大数据、可视化后台'
  },
  {
    key: 'neon-cyan',
    label: '霓虹浅青',
    category: 'gradient',
    shell: '#161a23',
    text: '#8d99ae',
    hover: '#242d3a',
    active: '#22d3ee',
    desc: '通透轻盈，年轻向 SaaS、小程序后台'
  },
  // ── 撞色点缀 · 轻奢质感 ──
  {
    key: 'champagne',
    label: '香槟金',
    category: 'accent',
    shell: '#161a23',
    text: '#a8b2c4',
    hover: '#252c38',
    active: '#d4af37',
    desc: '金色点缀，电商、商家、会员系统'
  },
  {
    key: 'wine-red',
    label: '豆沙酒红',
    category: 'accent',
    shell: '#1e222c',
    text: '#9b9faa',
    hover: '#222934',
    active: '#e17055',
    desc: '低饱和红，内容、店铺、营销后台'
  },
  {
    key: 'warm-brown',
    label: '暖棕咖色',
    category: 'accent',
    shell: '#161a23',
    text: '#949fb0',
    hover: '#232a36',
    active: '#c9a87c',
    desc: '暖调温润，文创、餐饮、零售后台'
  },
  // ── 极简灰黑 · 纯黑沉浸 ──
  {
    key: 'pure-white',
    label: '纯白高亮',
    category: 'dark',
    shell: '#0f1117',
    text: '#8892a0',
    hover: '#1a1d24',
    active: '#ffffff',
    primary: '#3b82f6',
    desc: '经典高对比，监控、运维、游戏后台'
  },
  {
    key: 'ice-blue',
    label: '冰蓝点缀',
    category: 'dark',
    shell: '#0f1117',
    text: '#86909c',
    hover: '#181b22',
    active: '#67e8f9',
    desc: '纯黑底最佳搭档，沉浸式工具后台'
  }
]

export function getThemeGroups() {
  return THEME_CATEGORIES.map((cat) => ({
    ...cat,
    themes: THEME_PRESETS.filter((item) => item.category === cat.key)
  }))
}

function hexToRgb(hex) {
  const normalized = hex.replace('#', '')
  return {
    r: parseInt(normalized.slice(0, 2), 16),
    g: parseInt(normalized.slice(2, 4), 16),
    b: parseInt(normalized.slice(4, 6), 16)
  }
}

function mixColor(hex, percent) {
  const { r, g, b } = hexToRgb(hex)
  const ratio = percent / 100
  const mix = (channel) => Math.round(channel + (255 - channel) * ratio)
  return `rgb(${mix(r)}, ${mix(g)}, ${mix(b)})`
}

function shadeColor(hex, percent) {
  const { r, g, b } = hexToRgb(hex)
  const ratio = percent / 100
  const mix = (channel) => Math.round(channel * (1 - ratio))
  return `rgb(${mix(r)}, ${mix(g)}, ${mix(b)})`
}

export function applyTheme(preset) {
  const config =
    typeof preset === 'object'
      ? preset
      : THEME_PRESETS.find((item) => item.key === preset) || THEME_PRESETS[0]

  const primary = config.primary || config.active
  const activeBar = config.gradientFrom
    ? `linear-gradient(180deg, ${config.gradientFrom}, ${config.gradientTo})`
    : config.active

  const root = document.documentElement
  root.style.setProperty('--app-shell-bg', config.shell)
  root.style.setProperty('--app-shell-hover', config.hover)
  root.style.setProperty('--app-shell-border', 'rgba(255, 255, 255, 0.06)')
  root.style.setProperty('--app-menu-text', config.text)
  root.style.setProperty('--app-menu-hover-bg', config.hover)
  root.style.setProperty('--app-menu-active', config.active)
  root.style.setProperty('--app-menu-active-bar', activeBar)
  root.style.setProperty('--app-primary', primary)
  root.style.setProperty('--app-primary-light', mixColor(primary, 85))
  root.style.setProperty('--app-primary-soft', mixColor(primary, 92))
  root.style.setProperty('--app-page-bg', '#f5f7fa')

  // Element Plus 按钮 hover/active 依赖 light/dark 色阶，需随主题一并刷新
  root.style.setProperty('--el-color-primary', primary)
  root.style.setProperty('--el-color-primary-light-3', mixColor(primary, 30))
  root.style.setProperty('--el-color-primary-light-5', mixColor(primary, 50))
  root.style.setProperty('--el-color-primary-light-7', mixColor(primary, 70))
  root.style.setProperty('--el-color-primary-light-8', mixColor(primary, 80))
  root.style.setProperty('--el-color-primary-light-9', mixColor(primary, 90))
  root.style.setProperty('--el-color-primary-dark-2', shadeColor(primary, 20))
  root.dataset.themeCategory = config.category
}

export const useThemeStore = defineStore('theme', () => {
  const currentKey = ref(localStorage.getItem('theme-key') || 'sky-blue')

  function setTheme(key) {
    const preset = THEME_PRESETS.find((item) => item.key === key) || THEME_PRESETS[0]
    currentKey.value = preset.key
    localStorage.setItem('theme-key', preset.key)
    applyTheme(preset)
  }

  watch(
    currentKey,
    (key) => {
      const preset = THEME_PRESETS.find((item) => item.key === key) || THEME_PRESETS[0]
      applyTheme(preset)
    },
    { immediate: true }
  )

  return {
    currentKey,
    setTheme
  }
})
