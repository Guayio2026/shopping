<template>
  <div class="login-page">
    <div class="login-card">
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <h1 class="visual-title">{{ systemStore.displayName }}</h1>
          <p class="visual-desc">用户登录 · 管理你的账号与资料</p>
        </div>
      </section>

      <section class="form-side">
        <div class="form-inner">
          <div class="form-brand">
            <BrandLogo size="lg" class="login-brand-logo" />
            <div class="brand-text">
              <h2>{{ systemStore.displayName }}</h2>
              <p>用户端登录</p>
            </div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>

          <p class="demo-tip">演示账号 user / user123</p>
          <p class="switch-tip">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </p>
          <p class="switch-tip">
            <router-link to="/user/home">先逛逛首页</router-link>
            ·
            <router-link to="/admin/login">后台登录</router-link>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import BrandLogo from '@/components/BrandLogo.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const systemStore = useSystemStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'user',
  password: 'user123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await userStore.login(form)
    if (data?.accountType === 'ADMIN' || data?.role === 'ADMIN') {
      userStore.logout()
      ElMessage.error('管理员请使用后台登录入口')
      return
    }
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.push(redirect && redirect.startsWith('/user') ? redirect : '/user/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  isolation: isolate;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  overflow: hidden;
  background:
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(15, 118, 140, 0.18), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(15, 55, 70, 0.12), transparent 55%),
    radial-gradient(ellipse 45% 40% at 70% 12%, rgba(148, 163, 184, 0.26), transparent 60%),
    linear-gradient(155deg, #e6eef3 0%, #f3f6f8 42%, #e8ecef 100%);
}

.login-page::before {
  content: '';
  position: absolute;
  inset: -20%;
  z-index: -1;
  background-image:
    linear-gradient(rgba(15, 23, 42, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.035) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 65% at 50% 45%, #000 20%, transparent 75%);
  pointer-events: none;
}

.login-card {
  position: relative;
  display: flex;
  width: 900px;
  max-width: 100%;
  min-height: 500px;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow:
    0 4px 6px rgba(15, 23, 42, 0.04),
    0 18px 48px rgba(15, 23, 42, 0.12);
}

.visual-side {
  position: relative;
  flex: 1;
  min-height: 500px;
  overflow: hidden;
}

.visual-bg {
  position: absolute;
  inset: 0;
  /* 用户端：生活/协作场景，与管理端区分 */
  background: url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80') center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(15, 55, 70, 0.2) 0%,
    rgba(15, 55, 70, 0.62) 100%
  );
}

.visual-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.visual-title {
  margin: 0 0 14px;
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
}

.visual-desc {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.88);
  letter-spacing: 1px;
}

.form-side {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #fff;
}

.form-inner {
  width: 100%;
}

.form-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 36px;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1f7a6b, #2a9d8f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(31, 122, 107, 0.3);
}

.login-brand-logo {
  box-shadow: 0 4px 14px rgba(31, 122, 107, 0.3);
}

.brand-icon svg {
  width: 26px;
  height: 26px;
}

.brand-text h2 {
  margin: 0 0 2px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.brand-text p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 12px;
  background: #f5f7fa;
  box-shadow: 0 0 0 1px #e8ecf0 inset;
  transition: all 0.2s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: #fff;
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(31, 122, 107, 0.28) inset;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: #b0b8c4;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border: none;
}

.login-form :deep(.submit-btn.el-button--primary) {
  --el-button-bg-color: #1f7a6b;
  --el-button-border-color: #1f7a6b;
  --el-button-hover-bg-color: #2a9d8f;
  --el-button-hover-border-color: #2a9d8f;
  --el-button-active-bg-color: #155e52;
  --el-button-active-border-color: #155e52;
  background: linear-gradient(135deg, #1f7a6b 0%, #2a9d8f 100%);
  box-shadow: 0 4px 14px rgba(31, 122, 107, 0.28);
}

.demo-tip {
  margin: 28px 0 0;
  text-align: center;
  font-size: 13px;
  color: #b0b8c4;
}

.switch-tip {
  margin: 10px 0 0;
  text-align: center;
  font-size: 13px;
  color: #b0b8c4;
}

.switch-tip a {
  color: #1f7a6b;
  text-decoration: none;
}

@media (max-width: 820px) {
  .login-card {
    flex-direction: column;
    min-height: auto;
  }

  .visual-side {
    min-height: 220px;
  }

  .visual-title {
    font-size: 28px;
  }

  .form-side {
    width: 100%;
    padding: 36px 28px 40px;
  }
}
</style>
