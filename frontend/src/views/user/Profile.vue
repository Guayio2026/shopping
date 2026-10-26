<template>
  <div class="profile-page">
    <div class="page-bg" aria-hidden="true">
      <span class="orb orb-a" />
      <span class="orb orb-b" />
      <span class="grain" />
    </div>

    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Account Studio</p>
        <h1>个人中心</h1>
        <p class="hero-text">打理头像与资料，守护登录安全。让每一次展示都干净、可信。</p>
      </div>
      <div class="hero-chip">
        <span class="dot" />
        {{ roleLabel }}
      </div>
    </section>

    <div class="profile-grid" v-loading="loading">
      <aside class="identity">
        <div class="identity-top">
          <div class="avatar-ring">
            <ImageUpload
              v-model="form.avatar"
              type="avatar"
              :avatar-size="112"
              upload-text="更换头像"
              :fallback="avatarFallback"
              :clearable="false"
              :show-success="false"
              @success="onAvatarUploaded"
            />
          </div>
          <h2>{{ form.nickname || form.username || '未设置昵称' }}</h2>
          <p class="identity-meta">@{{ form.username || '-' }}</p>
          <span class="role-pill">{{ roleLabel }}</span>
        </div>

        <ul class="identity-list">
          <li>
            <span>账号</span>
            <strong>{{ form.username || '—' }}</strong>
          </li>
          <li>
            <span>手机</span>
            <strong>{{ form.phone || '未填写' }}</strong>
          </li>
          <li>
            <span>角色</span>
            <strong>{{ roleLabel }}</strong>
          </li>
          <li>
            <span>创建时间</span>
            <strong>{{ createdAtText }}</strong>
          </li>
        </ul>
      </aside>

      <div class="panels">
        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Profile</p>
              <h3>基本资料</h3>
              <p>更新昵称与联系方式，立即同步前台展示</p>
            </div>
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
              <el-form-item label="创建时间">
                <el-input :model-value="createdAtText" disabled />
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

        <!-- 账户余额：充值入口（第十章） -->
        <section class="panel wallet">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Wallet</p>
              <h3>账户余额</h3>
              <p>充值后可到「我的订单」用余额直接支付</p>
            </div>
          </div>
          <div class="wallet-row">
            <div class="wallet-amount">
              <span class="unit">¥</span>
              <strong>{{ money(form.account) }}</strong>
            </div>
            <el-button type="warning" class="recharge-btn" @click="openRecharge">去充值</el-button>
          </div>
        </section>

        <section class="panel security">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Security</p>
              <h3>修改密码</h3>
              <p>建议定期更换密码，降低账号风险</p>
            </div>
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

    <!-- 充值弹窗：金额 + 支付方式（模拟充值，不接真实支付网关） -->
    <el-dialog v-model="rechargeVisible" title="账户充值" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="充值金额">
          <el-input v-model="rechargeForm.amount" placeholder="请输入金额（1 - 10000）" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.type">
            <el-radio-button label="微信支付" />
            <el-radio-button label="支付宝支付" />
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取 消</el-button>
        <el-button type="primary" :loading="recharging" @click="submitRecharge">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, recharge, updateProfile } from '@/api/auth'
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
  createdAt: '',
  account: 0
})

// ===== 充值相关状态 =====
const rechargeVisible = ref(false)
const recharging = ref(false)
const rechargeForm = reactive({ amount: '', type: '微信支付' })

/** 金额格式化：100 → "100.00"（后端 account 是 double，直接显示会出现 100.0） */
function money(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 打开充值弹窗（每次重置输入） */
function openRecharge() {
  rechargeForm.amount = ''
  rechargeForm.type = '微信支付'
  rechargeVisible.value = true
}

/**
 * 提交充值
 * 后端只收「金额 + 支付方式」，余额由后端原子相加（前端绝不自己算余额再加）
 * 返回的是充值后的最新 AccountProfile，直接拿去刷新页面
 */
async function submitRecharge() {
  const amount = Number(rechargeForm.amount)
  if (!Number.isFinite(amount) || amount <= 0) {
    ElMessage.warning('请输入大于 0 的金额')
    return
  }
  recharging.value = true
  try {
    const user = await recharge({ amount, type: rechargeForm.type })
    form.account = user?.account ?? 0
    if (userStore.user) userStore.user.account = form.account
    ElMessage.success(`充值成功，当前余额 ¥${money(form.account)}`)
    rechargeVisible.value = false
  } finally {
    recharging.value = false
  }
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  if (form.role === 'TEACHER') return '教师'
  if (form.role === 'ADMIN') return '管理员'
  return '普通用户'
})

const avatarFallback = computed(() =>
  (form.nickname || form.username || 'U').slice(0, 1).toUpperCase()
)

const createdAtText = computed(() => {
  if (!form.createdAt) return '—'
  return String(form.createdAt).replace('T', ' ').slice(0, 19)
})

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
  form.createdAt = user?.createdAt || ''
  form.account = user?.account ?? 0
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
  fillForm(user)
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
  --ink: #141416;
  --muted: #6f7680;
  --line: rgba(20, 20, 22, 0.08);
  --accent: #e63935;
  --surface: rgba(255, 255, 255, 0.78);
  position: relative;
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px 28px 56px;
  animation: pageIn 0.55s ease;
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-bg {
  position: absolute;
  inset: 0 -8%;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
}

.orb-a {
  width: 340px;
  height: 340px;
  top: -80px;
  right: 4%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.18), transparent 68%);
}

.orb-b {
  width: 280px;
  height: 280px;
  left: -40px;
  top: 220px;
  background: radial-gradient(circle, rgba(15, 23, 42, 0.08), transparent 70%);
}

.grain {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image:
    radial-gradient(rgba(20, 20, 22, 0.045) 0.6px, transparent 0.6px);
  background-size: 3px 3px;
  mask-image: linear-gradient(180deg, #000 20%, transparent 90%);
}

.hero,
.profile-grid {
  position: relative;
  z-index: 1;
}

.hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 26px;
}

.eyebrow,
.panel-kicker {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent);
  font-weight: 700;
}

.hero h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.1;
  letter-spacing: -0.03em;
  font-weight: 760;
  color: var(--ink);
}

.hero-text {
  margin: 12px 0 0;
  max-width: 36ch;
  color: var(--muted);
  line-height: 1.7;
  font-size: 15px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(230, 57, 53, 0.16);
  background: rgba(255, 255, 255, 0.7);
  color: var(--ink);
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(8px);
}

.hero-chip .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 4px rgba(230, 57, 53, 0.14);
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.identity {
  position: sticky;
  top: 88px;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid var(--line);
  background:
    linear-gradient(165deg, rgba(230, 57, 53, 0.14), rgba(255, 255, 255, 0.55) 38%, #fff 100%);
  box-shadow:
    0 24px 48px rgba(20, 20, 22, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.identity-top {
  padding: 32px 24px 20px;
  text-align: center;
}

.avatar-ring {
  display: inline-flex;
  padding: 5px;
  border-radius: 50%;
  background: linear-gradient(145deg, rgba(230, 57, 53, 0.7), rgba(255, 255, 255, 0.2) 55%, transparent);
  box-shadow: 0 14px 28px rgba(230, 57, 53, 0.18);
}

.identity h2 {
  margin: 18px 0 6px;
  font-size: 22px;
  font-weight: 720;
  color: var(--ink);
}

.identity-meta {
  margin: 0 0 14px;
  color: var(--muted);
  font-size: 13px;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #b82825;
  background: rgba(230, 57, 53, 0.1);
}

.identity-list {
  list-style: none;
  margin: 0;
  padding: 8px 16px 18px;
  display: grid;
  gap: 10px;
}

.identity-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(20, 20, 22, 0.04);
}

.identity-list span {
  color: var(--muted);
  font-size: 12px;
}

.identity-list strong {
  color: var(--ink);
  font-size: 13px;
  font-weight: 650;
}

.panels {
  display: grid;
  gap: 16px;
}

.panel {
  padding: 28px;
  border-radius: 28px;
  border: 1px solid var(--line);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(250, 250, 252, 0.88));
  box-shadow: 0 18px 40px rgba(20, 20, 22, 0.05);
  backdrop-filter: blur(12px);
}

.panel.security {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.9));
}

.panel-head {
  margin-bottom: 20px;
}

.panel-head h3 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 720;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.panel-head p:last-child {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.profile-form {
  max-width: 640px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.form-grid .span-2 {
  grid-column: 1 / -1;
}

.profile-form :deep(.el-form-item__label) {
  color: var(--muted);
  font-weight: 600;
}

.profile-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(20, 20, 22, 0.08) inset;
  background: rgba(255, 255, 255, 0.92);
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(20, 20, 22, 0.14) inset;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--accent) inset,
    0 0 0 4px rgba(230, 57, 53, 0.12);
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f4f5f7;
}

.save-btn {
  --el-button-bg-color: #e63935;
  --el-button-border-color: #e63935;
  --el-button-hover-bg-color: #cf2f2c;
  --el-button-hover-border-color: #cf2f2c;
  --el-button-active-bg-color: #b82825;
  --el-button-active-border-color: #b82825;
  min-width: 140px;
  height: 44px;
  margin-top: 6px;
  border-radius: 999px;
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 14px 28px rgba(230, 57, 53, 0.28);
}

.save-btn.ghost {
  --el-button-bg-color: #fff;
  --el-button-border-color: rgba(20, 20, 22, 0.12);
  --el-button-text-color: var(--ink);
  --el-button-hover-bg-color: rgba(230, 57, 53, 0.06);
  --el-button-hover-border-color: rgba(230, 57, 53, 0.35);
  --el-button-hover-text-color: #cf2f2c;
  --el-button-active-bg-color: rgba(230, 57, 53, 0.1);
  color: var(--ink);
  background: #fff;
  border: 1px solid rgba(20, 20, 22, 0.12);
  box-shadow: none;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .identity {
    position: static;
  }
}

@media (max-width: 720px) {
  .profile-page {
    padding: 18px 16px 40px;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero h1 {
    font-size: 32px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-grid .span-2 {
    grid-column: auto;
  }

  .panel {
    padding: 22px 18px;
    border-radius: 22px;
  }
}

/* ===== 账户余额面板 ===== */
.wallet-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(230, 57, 53, 0.08), rgba(255, 255, 255, 0.6));
  border: 1px solid rgba(230, 57, 53, 0.12);
}

.wallet-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: var(--ink);
}

.wallet-amount .unit {
  font-size: 16px;
  color: var(--accent);
  font-weight: 600;
}

.wallet-amount strong {
  font-size: 30px;
  font-weight: 750;
  letter-spacing: -0.02em;
}

.recharge-btn {
  border-radius: 999px;
  font-weight: 600;
}
</style>
