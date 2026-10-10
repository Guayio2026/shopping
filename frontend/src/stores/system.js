import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { getSystemConfig, updateSystemConfig } from '@/api/system'
import { mediaUrl } from '@/utils/format'

const DEFAULT_NAME = 'Admin Scaffold'
const DEFAULT_LOGO = '/logo.svg'

export const useSystemStore = defineStore('system', () => {
  const systemName = ref(localStorage.getItem('system-name') || DEFAULT_NAME)
  const systemLogo = ref(localStorage.getItem('system-logo') || '')
  const loaded = ref(false)

  const displayName = computed(() => {
    const name = String(systemName.value || '').trim()
    return name || DEFAULT_NAME
  })

  /** 可用于 <img> 的最终地址；未上传时走默认 logo */
  const logoSrc = computed(() => {
    const custom = String(systemLogo.value || '').trim()
    if (!custom) return DEFAULT_LOGO
    return mediaUrl(custom) || DEFAULT_LOGO
  })

  const hasCustomLogo = computed(() => !!String(systemLogo.value || '').trim())

  function applyDocumentTitle() {
    if (typeof document !== 'undefined') {
      document.title = displayName.value
    }
  }

  function applyFavicon() {
    if (typeof document === 'undefined') return
    let link = document.querySelector("link[rel='icon']")
    if (!link) {
      link = document.createElement('link')
      link.setAttribute('rel', 'icon')
      document.head.appendChild(link)
    }
    const src = logoSrc.value
    link.setAttribute('type', src.endsWith('.svg') ? 'image/svg+xml' : 'image/png')
    link.setAttribute('href', src.includes('?') ? src : `${src}?t=${Date.now()}`)
  }

  function applyName(name) {
    const next = String(name || '').trim() || DEFAULT_NAME
    systemName.value = next
    localStorage.setItem('system-name', next)
    applyDocumentTitle()
  }

  function applyLogo(logo) {
    const next = String(logo || '').trim()
    systemLogo.value = next
    if (next) localStorage.setItem('system-logo', next)
    else localStorage.removeItem('system-logo')
    applyFavicon()
  }

  async function fetchConfig() {
    try {
      const data = await getSystemConfig()
      applyName(data?.systemName)
      applyLogo(data?.systemLogo)
    } catch {
      applyDocumentTitle()
      applyFavicon()
    } finally {
      loaded.value = true
    }
  }

  async function saveConfig({ systemName: name, systemLogo: logo }) {
    const data = await updateSystemConfig({
      systemName: name,
      systemLogo: logo == null ? systemLogo.value : logo
    })
    applyName(data?.systemName || name)
    applyLogo(data?.systemLogo ?? logo ?? '')
    return data
  }

  async function saveSystemName(name) {
    return saveConfig({ systemName: name, systemLogo: systemLogo.value })
  }

  watch(displayName, () => applyDocumentTitle(), { immediate: true })
  watch(logoSrc, () => applyFavicon(), { immediate: true })

  return {
    systemName,
    systemLogo,
    displayName,
    logoSrc,
    hasCustomLogo,
    loaded,
    fetchConfig,
    saveConfig,
    saveSystemName,
    applyName,
    applyLogo
  }
})
