import { defineStore } from 'pinia'
import { ref } from 'vue'

const HOME_TAB = { path: '/admin/home', title: '首页', name: 'AdminHome', closable: false }

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref([{ ...HOME_TAB }])

  function normalizePath(path) {
    return path === '' || path === '/admin' ? '/admin/home' : path
  }

  function openTab(route) {
    const path = normalizePath(route.path)
    const exists = tabs.value.find((item) => item.path === path)
    if (exists) {
      return
    }
    tabs.value.push({
      path,
      title: route.meta?.title || '未命名',
      name: route.name,
      closable: path !== '/admin/home'
    })
  }

  function closeTab(path, router) {
    const normalized = normalizePath(path)
    if (normalized === '/admin/home') {
      return
    }

    const index = tabs.value.findIndex((item) => item.path === normalized)
    if (index === -1) {
      return
    }

    const isActive = router.currentRoute.value.path === normalized
    tabs.value.splice(index, 1)

    if (!isActive) {
      return
    }

    const nextTab = tabs.value[index] || tabs.value[index - 1] || tabs.value[0]
    router.push(nextTab.path)
  }

  function resetTabs() {
    tabs.value = [{ ...HOME_TAB }]
  }

  return {
    tabs,
    openTab,
    closeTab,
    resetTabs
  }
})
