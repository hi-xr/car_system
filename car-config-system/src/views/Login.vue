<template>
  <div class="login-container">
    <!-- 左侧品牌与特性 -->
    <div class="left-panel">
      <div class="logo-box">
        <div class="logo-icon logo-icon-square">
          <svg width="60" height="60" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="12" fill="url(#loginGradient)" />
            <path d="M22 35L25 25H35L38 35H22Z" fill="white" />
            <path d="M20 20H40V23H20V20Z" fill="white" />
            <defs>
              <linearGradient id="loginGradient" x1="0%" y1="0%" x2="100%" y2="100%">
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

    <!-- 右侧登录表单 -->
    <div class="right-panel">
      <div class="form-container">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">请输入您的账户信息以继续</p>

        <div v-if="message.text" :class="['form-message', message.type]">{{ message.text }}</div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <div class="input-wrap">
              <span class="input-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
              </span>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                class="form-input"
              />
            </div>
            <span v-if="errors.username" class="form-error">{{ errors.username }}</span>
          </div>

          <div class="form-item">
            <label class="form-label">密码</label>
            <div class="input-wrap">
              <span class="input-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
              </span>
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                class="form-input"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword = !showPassword"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              >
                <svg
                  v-if="!showPassword"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg
                  v-else
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
                >
                  <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.79 21.79 0 0 1 5.06-6.94" />
                  <path d="M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 8 11 8a21.82 21.82 0 0 1-3.17 4.42" />
                  <path d="M14.12 14.12a3 3 0 0 1-4.24-4.24" />
                  <path d="M1 1l22 22" />
                </svg>
              </button>
            </div>
            <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
          </div>

          <div class="form-item">
            <label class="form-label">用户角色</label>
            <div class="role-switch">
              <label class="role-option" :class="{ active: loginForm.role === 'user' }">
                <input v-model="loginForm.role" type="radio" value="user" />
                普通用户
              </label>
              <label class="role-option" :class="{ active: loginForm.role === 'admin' }">
                <input v-model="loginForm.role" type="radio" value="admin" />
                管理员
              </label>
            </div>
          </div>

          <div v-if="loginForm.role === 'admin'" class="form-item">
            <label class="form-label">员工号</label>
            <div class="input-wrap">
              <span class="input-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </span>
              <input
                v-model="loginForm.employeeId"
                type="text"
                placeholder="请输入员工号"
                class="form-input"
              />
            </div>
            <span v-if="errors.employeeId" class="form-error">{{ errors.employeeId }}</span>
          </div>

          <div class="form-item options-row">
            <label class="checkbox-label">
              <input v-model="loginForm.remember" type="checkbox" class="form-checkbox" />
              记住我
            </label>
            <a href="#" class="forgot-link">忘记密码?</a>
          </div>

          <div class="form-item submit-item">
            <button type="submit" class="login-btn">登录</button>
          </div>
        </form>

        <p class="register-link">
          还没有账户? <a href="#" class="link-text" @click.prevent="goToRegister">立即注册</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, inject } from 'vue'
import { apiLogin } from '../api'

const router = inject('router')

const message = reactive({ text: '', type: 'success' })
const loginForm = reactive({
  username: '',
  password: '',
  role: 'user',
  employeeId: '',
  remember: false
})
const errors = reactive({ username: '', password: '', employeeId: '' })

// 密码显示 / 隐藏切换
const showPassword = ref(false)

function validate() {
  let valid = true
  errors.username = ''
  errors.password = ''
  errors.employeeId = ''

  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名'
    valid = false
  }
  if (!loginForm.password) {
    errors.password = '请输入密码'
    valid = false
  }
  if (loginForm.role === 'admin' && !loginForm.employeeId.trim()) {
    errors.employeeId = '管理员登录需填写员工号'
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

async function handleLogin() {
  if (!validate()) {
    showMessage('请完善登录信息', 'error')
    return
  }

  try {
    const payload = {
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role,
      remember: loginForm.remember
    }
    if (loginForm.role === 'admin') {
      payload.employeeId = loginForm.employeeId.trim()
    }
    const data = await apiLogin(payload)

    const username = (data && data.username) || loginForm.username
    const role = (data && data.role) || loginForm.role

    localStorage.setItem('username', username)
    localStorage.setItem('role', role)
    if (data && data.token) {
      localStorage.setItem('token', data.token)
    }

    showMessage('登录成功！', 'success')
    setTimeout(() => router.push('/'), 1000)
  } catch (e) {
    console.error(e)
    showMessage(e.message || '登录失败，请稍后重试', 'error')
  }
}

function goToRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  width: 100vw;
  height: 100vh;
}

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

.logo-box, .feature-list { position: relative; z-index: 1; }

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
  background-color: #fff;
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
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 28px;
  text-align: center;
}

.form-message {
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}

.form-message.success { background: #d1fae5; color: #065f46; }
.form-message.error { background: #fee2e2; color: #991b1b; }

.login-form { margin-bottom: 20px; }

.form-item { margin-bottom: 20px; }

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.input-wrap {
  display: flex;
  align-items: center;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.2s;
}

.input-wrap:focus-within { border-color: #6366f1; }

.input-icon {
  padding: 0 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
}

.form-input {
  flex: 1;
  height: 44px;
  padding: 0 12px 0 0;
  border: none;
  border-radius: 0 8px 8px 0;
  font-size: 15px;
  outline: none;
}

.toggle-password {
  border: none;
  background: transparent;
  padding: 0 12px;
  color: #6b7280;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.toggle-password:hover {
  color: #2563eb;
}

.form-input::placeholder { color: #9ca3af; }

.form-error {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #dc2626;
}

.role-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.role-option {
  height: 44px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  background: #fff;
}

.role-option input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.role-option:hover {
  border-color: #6366f1;
}

.role-option.active {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.08);
  color: #1f2937;
}

.options-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}

.form-checkbox { width: 18px; height: 18px; }

.forgot-link {
  font-size: 14px;
  color: #2563eb;
  text-decoration: none;
}

.forgot-link:hover { text-decoration: underline; }

.submit-item { margin-bottom: 24px; }

.login-btn {
  width: 100%;
  height: 46px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:hover { background: #1d4ed8; }

.register-link {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.link-text {
  color: #2563eb;
  text-decoration: none;
}

.link-text:hover { text-decoration: underline; }
</style>
