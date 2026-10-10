<template>
  <div class="login-page">
    <div class="login-card">
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <h1 class="visual-title">{{ systemStore.displayName }}</h1>
          <p class="visual-desc">创建账号，开启你的使用体验</p>
        </div>
      </section>

      <section class="form-side">
        <div class="form-inner">
          <div class="form-brand">
            <BrandLogo size="lg" class="login-brand-logo" />
            <div class="brand-text">
              <h2>用户注册</h2>
              <p>填写信息创建普通用户账号</p>
            </div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            class="login-form"
            @keyup.enter="handleRegister"
          >
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名（3-32 位）" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input v-model="form.nickname" placeholder="昵称（可选）" prefix-icon="EditPen" size="large" />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="手机号（可选）" prefix-icon="Phone" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码（至少 6 位）"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="确认密码"
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
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form>

          <p class="switch-tip">
            已有账号？
            <router-link to="/login">返回登录</router-link>
          </p>
          <p class="switch-tip">
            <router-link to="/user/home">先逛逛首页</router-link>
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
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3-32 位', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1\d{10}$|^$/, message: '请输入正确的手机号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || undefined,
      phone: form.phone.trim() || undefined
    })
    ElMessage.success('注册成功')
    router.push('/user/home')
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
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(234, 120, 40, 0.22), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(245, 158, 11, 0.16), transparent 55%),
    radial-gradient(ellipse 45% 40% at 70% 12%, rgba(148, 163, 184, 0.22), transparent 60%),
    linear-gradient(155deg, #f3ebe3 0%, #f7f3ee 42%, #f0e6dc 100%);
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
  min-height: 560px;
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
  min-height: 560px;
  overflow: hidden;
}

.visual-bg {
  position: absolute;
  inset: 0;
  background: url('https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80') center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(15, 55, 70, 0.22) 0%,
    rgba(15, 55, 70, 0.64) 100%
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
  padding: 40px 36px;
  background: #fff;
}

.form-inner {
  width: 100%;
}

.form-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
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
  box-shadow: 0 4px 14px rgba(156, 106, 72, 0.22);
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
  margin-bottom: 16px;
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
  box-shadow: 0 0 0 2px rgba(156, 106, 72, 0.28) inset;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: #b0b8c4;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border: none;
}

.login-form :deep(.submit-btn.el-button--primary) {
  --el-button-bg-color: #9c6a48;
  --el-button-border-color: #9c6a48;
  --el-button-hover-bg-color: #b07d5a;
  --el-button-hover-border-color: #b07d5a;
  --el-button-active-bg-color: #82563a;
  --el-button-active-border-color: #82563a;
  background: linear-gradient(135deg, #9c6a48 0%, #b07d5a 100%);
  box-shadow: 0 4px 14px rgba(156, 106, 72, 0.24);
}

.switch-tip {
  margin: 16px 0 0;
  text-align: center;
  font-size: 13px;
  color: #b0b8c4;
}

.switch-tip a {
  color: #9c6a48;
  text-decoration: none;
}

@media (max-width: 820px) {
  .login-card {
    flex-direction: column;
    min-height: auto;
  }

  .visual-side {
    min-height: 200px;
  }

  .visual-title {
    font-size: 28px;
  }

  .form-side {
    width: 100%;
    padding: 32px 24px 36px;
  }
}
</style>
