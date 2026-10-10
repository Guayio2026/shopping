import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { messageConfig } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import { useSystemStore } from './stores/system'
import './styles/main.css'
import './styles/dialog.css'
import './styles/admin.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局消息：右上角，顶栏下方头图上沿附近（圈出位置）
Object.assign(messageConfig, {
  placement: 'top-right',
  offset: 128,
  duration: 2600
})

const pinia = createPinia()
app.use(pinia)
useThemeStore()
useSystemStore().fetchConfig()
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
