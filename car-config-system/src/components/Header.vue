<template>
  <header class="header">
    <div class="header-container">
      <router-link to="/" class="logo">汽车个性化选配系统</router-link>
      <nav class="nav">
        <router-link to="/">全系车型</router-link>
        <router-link to="/configure">在线选装</router-link>

        <div class="nav-dropdown" ref="planDropdown">
          <button class="nav-trigger" @click.stop="togglePlanMenu">
            我的方案
            <span class="nav-caret">▾</span>
          </button>
          <div class="nav-menu" v-show="showPlanMenu">
            <router-link to="/cart#schemes" @click="closePlanMenu">我的方案</router-link>
            <router-link to="/orders" @click="closePlanMenu">{{ isAdmin ? '订单管理' : '我的订单' }}</router-link>
          </div>
        </div>

        <!-- 关于我们：管理员时显示下拉，否则只显示单个入口 -->
        <div
          class="nav-dropdown"
          ref="aboutDropdown"
          v-if="isAdmin"
        >
          <button class="nav-trigger" @click.stop="toggleAboutMenu">
            关于我们
            <span class="nav-caret">▾</span>
          </button>
          <div class="nav-menu" v-show="showAboutMenu">
            <router-link to="/about" @click="closeAboutMenu">关于我们</router-link>
            <router-link to="/rules" @click="closeAboutMenu">配置规则</router-link>
            <router-link to="/vehicle-manage" @click="closeAboutMenu">车型管理</router-link>
          </div>
        </div>
        <router-link v-else to="/about">关于我们</router-link>
      </nav>
      <div class="auth">
        <template v-if="isLoggedIn">
          <div class="user-dropdown" ref="userDropdown">
            <button class="user-trigger" @click.stop="toggleUserMenu">
              <span class="user-avatar">
                <img
                  v-if="avatarUrl"
                  :src="avatarUrl"
                  alt="头像"
                  class="avatar-img"
                />
                <span v-else>{{ userInitial }}</span>
              </span>
              <span class="user-name">{{ username }}</span>
              <span class="nav-caret">▾</span>
            </button>
            <div class="nav-menu user-menu" v-show="showUserMenu">
              <router-link to="/profile" @click="closeUserMenu">个人信息</router-link>
              <button class="menu-item" @click="logout">退出登录</button>
            </div>
          </div>
        </template>
        <template v-else>
          <button class="btn-login" @click="goToLogin">登录</button>
          <button class="btn-register" @click="goToRegister">注册</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { inject, ref, computed, onMounted, onBeforeUnmount } from 'vue'

const router = inject('router')

// 方案下拉菜单
const showPlanMenu = ref(false)
const planDropdown = ref(null)

const togglePlanMenu = () => {
  showPlanMenu.value = !showPlanMenu.value
}

const closePlanMenu = () => {
  showPlanMenu.value = false
}

// 关于我们下拉（仅管理员）
const showAboutMenu = ref(false)
const aboutDropdown = ref(null)
const isAdmin = ref(false)

const toggleAboutMenu = () => {
  showAboutMenu.value = !showAboutMenu.value
}

const closeAboutMenu = () => {
  showAboutMenu.value = false
}

// 用户信息下拉（登录后）
const isLoggedIn = ref(false)
const username = ref('')
const avatarUrl = ref('')
const showUserMenu = ref(false)
const userDropdown = ref(null)

const handleClickOutside = (e) => {
  if (planDropdown.value && !planDropdown.value.contains(e.target)) {
    showPlanMenu.value = false
  }
  if (aboutDropdown.value && !aboutDropdown.value.contains(e.target)) {
    showAboutMenu.value = false
  }
  if (userDropdown.value && !userDropdown.value.contains(e.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  // 简单示例：根据 localStorage 中的角色判断是否为管理员
  const storedRole = localStorage.getItem('role')
  const storedName = localStorage.getItem('username') || ''
  const storedAvatar = localStorage.getItem('avatarUrl') || ''
  isAdmin.value = storedRole === 'admin'
  username.value = storedName
  avatarUrl.value = storedAvatar
  isLoggedIn.value = !!storedName
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const closeUserMenu = () => {
  showUserMenu.value = false
}

const userInitial = computed(() =>
  username.value ? username.value.charAt(0).toUpperCase() : '用'
)

const logout = () => {
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('avatarUrl')
  username.value = ''
  avatarUrl.value = ''
  isLoggedIn.value = false
  isAdmin.value = false
  closeUserMenu()
  router.push('/login')
}
</script>

<style scoped>
.header {
  background-color: #1e293b;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  text-decoration: none;
}

.logo:hover {
  color: #fff;
}

.nav {
  display: flex;
  gap: 32px;
  align-items: center;
}

.nav a,
.nav-trigger {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.3s ease;
  background: transparent;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.nav a:hover,
.nav-trigger:hover {
  color: #fff;
}

.nav-dropdown {
  position: relative;
}

.nav-caret {
  font-size: 10px;
}

.nav-menu {
  position: absolute;
  top: 120%;
  left: 0;
  min-width: 120px;
  padding: 8px 0;
  background: #0f172a;
  border-radius: 6px;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.5);
  display: block;
  z-index: 120;
}

.nav-menu a {
  display: block;
  padding: 6px 14px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.nav-menu a:hover {
  background: rgba(148, 163, 184, 0.18);
  color: #ffffff;
}

.auth {
  display: flex;
  gap: 12px;
}

.user-dropdown {
  position: relative;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.6);
  border-radius: 999px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-trigger:hover {
  border-color: #fff;
  background: rgba(15, 23, 42, 0.7);
}

.user-avatar {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-menu {
  right: 0;
  left: auto;
}

.nav-menu .menu-item {
  width: 100%;
  padding: 6px 14px;
  font-size: 13px;
  color: rgba(248, 250, 252, 0.9);
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
}

.nav-menu .menu-item:hover {
  background: rgba(148, 163, 184, 0.18);
}

.btn-login {
  padding: 8px 20px;
  font-size: 14px;
  color: #fff;
  background-color: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-login:hover {
  border-color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.btn-register {
  padding: 8px 20px;
  font-size: 14px;
  color: #fff;
  background-color: #2563eb;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-register:hover {
  background-color: #1d4ed8;
}

@media (max-width: 768px) {
  .nav {
    display: none;
  }
  
  .header-container {
    height: 56px;
  }
}
</style>
