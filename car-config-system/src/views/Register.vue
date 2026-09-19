<template>
  <div class="register-container">
    <!-- 左侧面板（与登录页一致） -->
    <div class="left-panel">
      <div class="logo-box">
        <div class="logo-icon logo-icon-square">
          <svg width="60" height="60" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="12" fill="url(#registerGradient)" />
            <path d="M22 35L25 25H35L38 35H22Z" fill="white" />
            <path d="M20 20H40V23H20V20Z" fill="white" />
            <defs>
              <linearGradient id="registerGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#6366F1" />
                <stop offset="100%" stop-color="#A855F7" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="system-name">汽车个性化选配系统</h1>
      </div>
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon blue-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="4" y1="8" x2="4" y2="16"/><line x1="10" y1="4" x2="10" y2="20"/>
              <line x1="16" y1="12" x2="16" y2="20"/><line x1="20" y1="6" x2="20" y2="16"/>
            </svg>
          </div>
          <div class="feature-text">
            <h3>个性化配置</h3>
            <p>灵活的汽车配置选项</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon purple-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 4v6h-6M1 20v-6h6"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </div>
          <div class="feature-text">
            <h3>实时预览</h3>
            <p>即时查看配置效果</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon green-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </div>
          <div class="feature-text">
            <h3>安全可靠</h3>
            <p>企业级安全保障</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="right-panel">
      <div class="form-container">
        <h2 class="form-title">创建账户</h2>
        <p class="form-subtitle">填写以下信息完成注册</p>

        <div v-if="message.text" :class="['form-message', message.type]">{{ message.text }}</div>

        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-item">
            <input
              v-model="registerForm.username"
              type="text"
              placeholder="请输入用户名"
              class="form-input"
            />
            <span v-if="errors.username" class="form-error">{{ errors.username }}</span>
          </div>

          <div class="form-item">
            <input
              v-model="registerForm.phone"
              type="tel"
              placeholder="请输入电话号码"
              class="form-input"
            />
            <span v-if="errors.phone" class="form-error">{{ errors.phone }}</span>
          </div>

          <div class="form-item">
            <div class="role-label">用户角色</div>
            <div class="role-switch">
              <label class="role-option" :class="{ active: registerForm.role === 'user' }">
                <input v-model="registerForm.role" type="radio" value="user" />
                普通用户
              </label>
              <label class="role-option" :class="{ active: registerForm.role === 'admin' }">
                <input v-model="registerForm.role" type="radio" value="admin" />
                管理员
              </label>
            </div>
          </div>

          <div v-if="registerForm.role === 'admin'" class="form-item">
            <input
              v-model="registerForm.employeeId"
              type="text"
              placeholder="请输入员工号"
              class="form-input"
            />
            <span v-if="errors.employeeId" class="form-error">{{ errors.employeeId }}</span>
          </div>

          <div class="form-item">
            <input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              class="form-input"
            />
            <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
          </div>

          <div class="form-item">
            <input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="form-input"
            />
            <span v-if="errors.confirmPassword" class="form-error">{{ errors.confirmPassword }}</span>
          </div>

          <div class="form-item">
            <input
              v-model="registerForm.address"
              type="text"
              placeholder="请输入您的地址"
              class="form-input"
            />
            <span v-if="errors.address" class="form-error">{{ errors.address }}</span>
          </div>

          <div class="form-item checkbox-item">
            <label class="checkbox-label">
              <input v-model="registerForm.agreement" type="checkbox" class="form-checkbox" />
              我已阅读并同意 <a href="#" class="link-text">服务条款</a> 和 <a href="#" class="link-text">隐私政策</a>
            </label>
            <span v-if="errors.agreement" class="form-error">{{ errors.agreement }}</span>
          </div>

          <div class="form-item submit-item">
            <button type="submit" class="register-btn">注册</button>
          </div>
        </form>

        <p class="login-link">
          已有账户？<a href="#" class="link-text" @click.prevent="goToLogin">立即登录</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, inject } from 'vue'
import { apiRegister } from '../api'

const router = inject('router')
const message = reactive({ text: '', type: 'success' })
const registerForm = reactive({
  username: '',
  phone: '',
  role: 'user',
  employeeId: '',
  password: '',
  confirmPassword: '',
  address: '',
  agreement: false
})
const errors = reactive({
  username: '',
  phone: '',
  employeeId: '',
  password: '',
  confirmPassword: '',
  address: '',
  agreement: ''
})

function validate() {
  let valid = true
  errors.username = ''
  errors.phone = ''
  errors.employeeId = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.address = ''
  errors.agreement = ''

  if (!registerForm.username.trim()) {
    errors.username = '请输入用户名'
    valid = false
  } else if (registerForm.username.length < 3 || registerForm.username.length > 20) {
    errors.username = '用户名长度在 3 到 20 个字符'
    valid = false
  }

  if (!registerForm.phone.trim()) {
    errors.phone = '请输入电话号码'
    valid = false
  } else if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    errors.phone = '请输入正确的手机号码'
    valid = false
  }

  if (registerForm.role !== 'admin') {
    registerForm.employeeId = ''
  } else {
    if (!registerForm.employeeId.trim()) {
      errors.employeeId = '管理员注册需要填写员工号'
      valid = false
    } else if (!/^[A-Za-z0-9]{4,12}$/.test(registerForm.employeeId.trim())) {
      errors.employeeId = '员工号格式：4-12 位字母或数字'
      valid = false
    }
  }

  if (!registerForm.password) {
    errors.password = '请输入密码'
    valid = false
  } else if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    errors.password = '密码长度在 6 到 20 个字符'
    valid = false
  }

  if (!registerForm.confirmPassword) {
    errors.confirmPassword = '请再次输入密码'
    valid = false
  } else if (registerForm.confirmPassword !== registerForm.password) {
    errors.confirmPassword = '两次输入的密码不一致'
    valid = false
  }

  if (!registerForm.address.trim()) {
    errors.address = '请输入地址'
    valid = false
  }

  if (!registerForm.agreement) {
    errors.agreement = '请同意服务条款和隐私政策'
    valid = false
  }

  return valid
}

function showMessage(text, type = 'success') {
  message.text = text
  message.type = type
  setTimeout(() => {
    message.text = ''
  }, 3000)
}

async function handleRegister() {
  if (!validate()) {
    showMessage('请完善表单信息', 'error')
    return
  }

  try {
    await apiRegister({
      username: registerForm.username,
      phone: registerForm.phone,
      role: registerForm.role,
      employeeId: registerForm.employeeId || undefined,
      password: registerForm.password,
      address: registerForm.address
    })
    showMessage('注册成功！', 'success')
    setTimeout(() => router.push('/login'), 1000)
  } catch (e) {
    console.error(e)
    showMessage(e.message || '注册失败，请稍后重试', 'error')
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  width: 100vw;
  height: 100vh;
}

/* 左侧面板与登录页一致 */
.left-panel {
  flex: 1;
  background: linear-gradient(180deg, #0f0f1a 0%, #1a1a2e 50%, #16213e 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.left-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 30% 40%, rgba(99, 102, 241, 0.25) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(168, 85, 247, 0.2) 0%, transparent 50%);
  z-index: 0;
}

.logo-box, .feature-list {
  position: relative;
  z-index: 1;
}

.logo-icon-square { margin-bottom: 20px; }

.logo-icon-square rect { border-radius: 12px; }

.system-name {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 60px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.blue-icon { background-color: #3B82F6; }
.purple-icon { background-color: #9333EA; }
.green-icon { background-color: #10B981; }

.feature-text h3 { font-size: 18px; margin: 0 0 4px 0; }
.feature-text p { font-size: 14px; color: #CBD5E1; margin: 0; }

.right-panel {
  flex: 1;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 8px;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 30px;
  text-align: center;
}

.form-message {
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}

.form-message.success {
  background: #d1fae5;
  color: #065f46;
}

.form-message.error {
  background: #fee2e2;
  color: #991b1b;
}

.register-form { margin-bottom: 20px; }

.form-item {
  margin-bottom: 20px;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 15px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #6366F1;
}

.form-error {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #dc2626;
}

.role-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.role-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.role-option {
  height: 44px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  background: #fff;
  position: relative;
}

.role-option input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.role-option:hover {
  border-color: #6366F1;
}

.role-option.active {
  border-color: #6366F1;
  background: rgba(99, 102, 241, 0.08);
  color: #1f2937;
}

.checkbox-item { margin-bottom: 20px; }

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}

.form-checkbox {
  width: 18px;
  height: 18px;
}

.submit-item { margin-bottom: 10px; }

.register-btn {
  width: 100%;
  height: 44px;
  background-color: #3B82F6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.register-btn:hover {
  background-color: #2563EB;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #6B7280;
}

.link-text {
  color: #3B82F6;
  text-decoration: none;
}

.link-text:hover {
  color: #2563EB;
  text-decoration: underline;
}
</style>
