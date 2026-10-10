import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 【为什么需要 rewrite】
      // 后端配了 context-path: /api，所以静态图片的真实地址是：
      //   http://localhost:8080/api/uploads/xxx.png
      // 但富文本编辑器（wangEditor）上传后拿到的是 /uploads/xxx.png（不带 /api），
      // 这个地址直接写进 <img src>，浏览器去请求 5173/uploads/xxx.png 就会 404。
      // 加上 rewrite 后：5173/uploads/xxx.png → 8080/api/uploads/xxx.png ✅
      // 这样编辑器里插图、v-html 预览渲染的图片都能正常显示。
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => `/api${path}`
      }
    }
  }
})
