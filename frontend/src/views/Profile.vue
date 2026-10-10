<template>
  <div class="profile-page">
    <section class="intro">
      <p class="eyebrow">Account</p>
      <h1>个人中心</h1>
      <p class="intro-text">维护头像、昵称与登录安全，保持账号信息清晰可靠。</p>
    </section>

    <div class="profile-grid" v-loading="loading">
      <aside class="identity">
        <div class="identity-glow" />
        <div class="identity-ring">
          <ImageUpload
            v-model="form.avatar"
            type="avatar"
            :avatar-size="108"
            upload-text="更换头像"
            :fallback="avatarFallback"
            :clearable="false"
            :show-success="false"
            @success="onAvatarUploaded"
          />
        </div>
        <h2>{{ form.nickname || form.username || '未设置昵称' }}</h2>
        <p class="identity-meta">{{ roleLabel }} · @{{ form.username || '-' }}</p>
        <ul class="identity-list">
          <li><span>账号</span>{{ form.username || '-' }}</li>
          <li><span>手机</span>{{ form.phone || '未填写' }}</li>
          <li><span>角色</span>{{ roleLabel }}</li>
        </ul>
      </aside>

      <div class="panels">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h3>基本资料</h3>
              <p>更新后台对外展示信息</p>
            </div>
            <span class="panel-badge">Profile</span>
          </div>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="profile-form"
          >
            <div class="form-grid">
              <el-form-item label="用户名">
                <el-input :model-value="form.username" disabled />
              </el-form-item>
              <el-form-item label="角色">
                <el-input :model-value="roleLabel" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="32" show-word-limit />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </div>
            <el-button class="save-btn" type="primary" :loading="submitting" @click="handleSubmit">
              保存修改
            </el-button>
          </el-form>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <h3>修改密码</h3>
              <p>定期更新密码，降低账号风险</p>
            </div>
            <span class="panel-badge soft">Security</span>
          </div>
          <el-form
            ref="pwdRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-position="top"
            class="profile-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <div class="form-grid">
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
              </el-form-item>
            </div>
            <el-button class="save-btn ghost" type="default" :loading="pwdSubmitting" @click="handlePasswordSubmit">
              更新密码
            </el-button>
          </el-form>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import ImageUpload from '@/components/ImageUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const pwdSubmitting = ref(false)
const formRef = ref()
const pwdRef = ref()

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  role: '',
  accountType: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  if (form.accountType === 'ADMIN' || form.role === 'ADMIN') return '管理员'
  if (form.role === 'TEACHER') return '教师'
  return '普通用户'
})

const avatarFallback = computed(() =>
  (form.nickname || form.username || 'U').slice(0, 1).toUpperCase()
)

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$|^$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function fillForm(user) {
  form.username = user?.username || ''
  form.nickname = user?.nickname || ''
  form.phone = user?.phone || ''
  form.avatar = user?.avatar || ''
  form.role = user?.role || ''
  form.accountType = user?.accountType || ''
}

async function loadProfile() {
  loading.value = true
  try {
    const user = await userStore.fetchUser()
    fillForm(user)
  } finally {
    loading.value = false
  }
}

async function persistProfile(payload, successMsg) {
  const user = await updateProfile(payload)
  userStore.user = {
    ...user,
    accountType: user.accountType || (user.role === 'ADMIN' ? 'ADMIN' : 'USER')
  }
  fillForm(userStore.user)
  ElMessage.success(successMsg)
}

async function onAvatarUploaded(url) {
  form.avatar = url
  await persistProfile(
    { nickname: form.nickname, phone: form.phone, avatar: form.avatar },
    '头像已更新'
  )
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await persistProfile(
      { nickname: form.nickname, phone: form.phone, avatar: form.avatar },
      '个人信息已更新'
    )
  } finally {
    submitting.value = false
  }
}

async function handlePasswordSubmit() {
  await pwdRef.value.validate()
  pwdSubmitting.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码已更新')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  --ink: #0f172a;
  --muted: #64748b;
  --line: rgba(15, 23, 42, 0.08);
  --accent: var(--app-primary, #3b82f6);
  max-width: 1120px;
  animation: pageIn 0.45s ease;
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.intro {
  margin-bottom: 22px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent);
  font-weight: 700;
}

.intro h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 760;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.intro-text {
  margin: 0;
  max-width: 46ch;
  color: var(--muted);
  line-height: 1.7;
  font-size: 14px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.identity {
  position: relative;
  overflow: hidden;
  padding: 28px 22px;
  border-radius: 22px;
  background:
    linear-gradient(165deg, color-mix(in srgb, var(--accent) 14%, #ffffff) 0%, #ffffff 48%),
    #ffffff;
  border: 1px solid var(--line);
  box-shadow:
    0 18px 40px rgba(15, 23, 42, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
  text-align: center;
}

.identity-glow {
  position: absolute;
  width: 200px;
  height: 200px;
  top: -70px;
  right: -50px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--accent) 22%, transparent), transparent 70%);
  pointer-events: none;
}

.identity-ring {
  position: relative;
  display: inline-flex;
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(145deg, color-mix(in srgb, var(--accent) 55%, #fff), transparent 70%);
}

.identity h2 {
  position: relative;
  margin: 16px 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: var(--ink);
}

.identity-meta {
  position: relative;
  margin: 0 0 20px;
  color: var(--muted);
  font-size: 13px;
}

.identity-list {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 0;
  text-align: left;
  display: grid;
  gap: 10px;
}

.identity-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(15, 23, 42, 0.04);
  font-size: 13px;
  color: var(--ink);
}

.identity-list span {
  color: var(--muted);
}

.panels {
  display: grid;
  gap: 16px;
}

.panel {
  padding: 26px 28px;
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92));
  border: 1px solid var(--line);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.05);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.panel-head h3 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
}

.panel-head p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}

.panel-badge {
  flex-shrink: 0;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, #ffffff);
  border: 1px solid color-mix(in srgb, var(--accent) 18%, transparent);
}

.panel-badge.soft {
  color: #64748b;
  background: #f8fafc;
  border-color: #e2e8f0;
}

.profile-form {
  max-width: 640px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.profile-form :deep(.el-form-item__label) {
  color: var(--muted);
  font-weight: 600;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08) inset;
  background: rgba(255, 255, 255, 0.92);
  transition: box-shadow 0.2s ease;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.14) inset;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--accent) inset,
    0 0 0 3px color-mix(in srgb, var(--accent) 16%, transparent);
}

.save-btn {
  min-width: 132px;
  height: 40px;
  margin-top: 4px;
  border-radius: 12px;
  font-weight: 650;
  box-shadow: 0 10px 20px color-mix(in srgb, var(--accent) 22%, transparent);
}

.save-btn.ghost {
  --el-button-bg-color: #ffffff;
  --el-button-border-color: #e2e8f0;
  --el-button-text-color: var(--ink);
  --el-button-hover-bg-color: #f8fafc;
  --el-button-hover-border-color: color-mix(in srgb, var(--accent) 35%, #e2e8f0);
  --el-button-hover-text-color: var(--accent);
  --el-button-active-bg-color: #f1f5f9;
  color: var(--ink);
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: none;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .panel {
    padding: 22px 18px;
  }

  .intro h1 {
    font-size: 24px;
  }
}
</style>
