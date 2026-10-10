<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 左侧品牌图 -->
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <h1 class="visual-title">{{ systemStore.displayName }}</h1>
          <p class="visual-desc">简洁高效 · 开箱即用 · 快速二次开发</p>
        </div>
      </section>

      <!-- 右侧登录 -->
      <section class="form-side">
        <div class="form-inner">
          <div class="form-brand">
            <BrandLogo size="lg" class="login-brand-logo" />
            <div class="brand-text">
              <h2>{{ systemStore.displayName }}</h2>
              <p>管理后台登录</p>
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
              <el-input
                v-model="form.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
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

          <p class="demo-tip">演示账号 admin / admin123</p>
          <p class="switch-tip">
            普通用户请前往
            <router-link to="/login">用户登录</router-link>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import BrandLogo from '@/components/BrandLogo.vue'

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
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
    if (data?.accountType !== 'ADMIN' && data?.role !== 'ADMIN') {
      userStore.logout()
      ElMessage.error('仅管理员账号可登录后台')
      return
    }
    ElMessage.success('登录成功')
    router.push('/admin/home')
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
  /* 冷灰底 + 品牌珊瑚晕染，让白卡片浮出 */
  background:
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(232, 93, 76, 0.22), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(232, 93, 76, 0.14), transparent 55%),
    radial-gradient(ellipse 45% 40% at 70% 12%, rgba(148, 163, 184, 0.28), transparent 60%),
    linear-gradient(155deg, #e8eef5 0%, #f4f6f9 42%, #ebe4e1 100%);
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

/* ── 左侧视觉 ── */
.visual-side {
  position: relative;
  flex: 1;
  min-height: 500px;
  overflow: hidden;
}

.visual-bg {
  position: absolute;
  inset: 0;
  /* 管理端：控制室/运维氛围 */
  background: url('https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=1200&q=80') center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(15, 23, 42, 0.15) 0%,
    rgba(15, 23, 42, 0.55) 100%
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

/* ── 右侧表单 ── */
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
  background: linear-gradient(135deg, #e85d4c, #f07060);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(232, 93, 76, 0.3);
}

.login-brand-logo {
  box-shadow: 0 4px 14px rgba(232, 93, 76, 0.3);
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
  box-shadow: 0 0 0 2px rgba(232, 93, 76, 0.3) inset;
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
  --el-button-bg-color: #e85d4c;
  --el-button-border-color: #e85d4c;
  --el-button-hover-bg-color: #f07060;
  --el-button-hover-border-color: #f07060;
  --el-button-active-bg-color: #d04a3a;
  --el-button-active-border-color: #d04a3a;
  background: linear-gradient(135deg, #e85d4c 0%, #f07060 100%);
  box-shadow: 0 4px 14px rgba(232, 93, 76, 0.28);
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
  color: #e85d4c;
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
