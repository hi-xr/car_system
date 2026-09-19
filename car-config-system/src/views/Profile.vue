<template>
  <div class="profile-page">
    <Header />
    <main class="profile-main">
      <div class="container">
        <h1>个人信息</h1>
        <p class="profile-desc">查看并完善您的账户资料。</p>

        <!-- 操作反馈消息 -->
        <div v-if="message" :class="['message', message.type]">
          {{ message.text }}
        </div>

        <div class="profile-layout" v-if="user">
          <!-- 左侧头像与上传 -->
          <section class="profile-left">
            <div class="avatar-card">
              <div class="avatar-wrapper">
                <img
                  v-if="user.avatarUrl"
                  :src="user.avatarUrl"
                  alt="头像"
                  class="avatar-img"
                />
                <div v-else class="avatar-placeholder">
                  {{ avatarInitial }}
                </div>
              </div>
              <button class="btn-upload" @click="triggerUpload" :disabled="uploading">
                {{ uploading ? '上传中...' : '上传头像' }}
              </button>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                class="file-input"
                @change="onFileChange"
              />
            </div>
          </section>

          <!-- 右侧信息表格 -->
          <section class="profile-right">
            <div class="profile-card">
              <div class="profile-row">
                <span class="label">用户名</span>
                <div class="value">
                  <input
                    v-if="editing"
                    v-model="editData.username"
                    type="text"
                    class="edit-input"
                    placeholder="请输入用户名"
                  />
                  <span v-else>{{ user.username }}</span>
                </div>
              </div>
              <div class="profile-row">
                <span class="label">身份</span>
                <span class="value">{{ user.identity || roleText }}</span>
              </div>
              <div class="profile-row" v-if="user.employeeId">
                <span class="label">员工号</span>
                <span class="value">{{ user.employeeId }}</span>
              </div>
              <div class="profile-row">
                <span class="label">电话</span>
                <div class="value">
                  <input 
                    v-if="editing" 
                    v-model="editData.phone" 
                    type="text" 
                    class="edit-input" 
                    placeholder="请输入电话号码"
                  />
                  <span v-else>{{ user.phone || '未设置' }}</span>
                </div>
              </div>
              <div class="profile-row">
                <span class="label">邮箱</span>
                <div class="value">
                  <input 
                    v-if="editing" 
                    v-model="editData.email" 
                    type="email" 
                    class="edit-input" 
                    placeholder="请输入邮箱地址"
                  />
                  <span v-else>{{ user.email || '未设置' }}</span>
                </div>
              </div>
              <div class="profile-row">
                <span class="label">地址</span>
                <div class="value">
                  <textarea 
                    v-if="editing" 
                    v-model="editData.address" 
                    class="edit-textarea" 
                    placeholder="请输入详细地址"
                    rows="2"
                  ></textarea>
                  <span v-else>{{ user.address || '未设置' }}</span>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="profile-actions">
                <button 
                  v-if="!editing" 
                  class="btn-edit" 
                  @click="startEditing"
                >
                  编辑信息
                </button>
                <div v-else class="edit-actions">
                  <button class="btn-save" @click="saveProfile" :disabled="saving">
                    {{ saving ? '保存中...' : '保存' }}
                  </button>
                  <button class="btn-cancel" @click="cancelEditing">
                    取消
                  </button>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div v-else class="profile-loading">正在加载用户信息...</div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { getCurrentUser, uploadAvatar, updateProfile } from '../api/index.js'

const user = ref(null)
const uploading = ref(false)
const saving = ref(false)
const editing = ref(false)
const fileInput = ref(null)
const message = ref(null)

const editData = reactive({
  username: '',
  phone: '',
  email: '',
  address: ''
})

const role = computed(() => localStorage.getItem('role') || 'user')
const roleText = computed(() => (role.value === 'admin' ? '管理员' : '普通用户'))

const avatarInitial = computed(() => {
  if (!user.value || !user.value.username) return '用'
  return user.value.username.charAt(0).toUpperCase()
})

const loadProfile = async () => {
  try {
    const raw = await getCurrentUser()
    const mapped = {
      username: raw.username,
      identity: raw.identity,
      employeeId: raw.employeeId ?? raw.employee_id,
      phone: raw.phone,
      email: raw.email,
      address: raw.address,
      avatarUrl: raw.avatar || raw.avatarUrl || raw.avatar_url
    }
    user.value = mapped

    if (mapped.username) {
      localStorage.setItem('username', mapped.username)
    }
    if (mapped.identity) {
      localStorage.setItem('identity', mapped.identity)
    }
    if (mapped.avatarUrl) {
      localStorage.setItem('avatarUrl', mapped.avatarUrl)
    }
  } catch (e) {
    console.error(e)
  }
}

const triggerUpload = () => {
  if (fileInput.value) fileInput.value.click()
}

const onFileChange = async (e) => {
  const [file] = e.target.files
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    const url = res.avatarUrl || res.avatar_url || res.url
    if (url) {
      if (!user.value) user.value = {}
      user.value.avatarUrl = url
      localStorage.setItem('avatarUrl', url)
      showMessage('头像上传成功', 'success')
    }
  } catch (err) {
    console.error(err)
    showMessage(err?.message || '上传头像失败，请稍后重试', 'error')
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

const startEditing = () => {
  editing.value = true
  editData.username = user.value.username || ''
  editData.phone = user.value.phone || ''
  editData.email = user.value.email || ''
  editData.address = user.value.address || ''
}

const cancelEditing = () => {
  editing.value = false
  editData.username = ''
  editData.phone = ''
  editData.email = ''
  editData.address = ''
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateProfile(editData)
    
    // 更新本地数据
    if (editData.username) {
      user.value.username = editData.username
      localStorage.setItem('username', editData.username)
    }
    if (editData.phone) user.value.phone = editData.phone
    if (editData.email) user.value.email = editData.email
    if (editData.address) user.value.address = editData.address
    
    editing.value = false
    showMessage('个人信息更新成功', 'success')
  } catch (err) {
    console.error(err)
    showMessage('更新失败：' + err.message, 'error')
  } finally {
    saving.value = false
  }
}

const showMessage = (text, type = 'info') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = null
  }, 3000)
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.profile-main {
  flex: 1;
  padding: 60px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.profile-desc {
  color: #64748b;
  font-size: 15px;
  margin-bottom: 24px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
  align-items: flex-start;
}

.profile-left {
  display: flex;
  flex-direction: column;
}

.avatar-card {
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 20px 24px;
  text-align: center;
}

.avatar-wrapper {
  width: 96px;
  height: 96px;
  border-radius: 999px;
  margin: 0 auto 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 32px;
  font-weight: 600;
  color: #ffffff;
}

.btn-upload {
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-upload:hover:not(:disabled) {
  border-color: #2563eb;
  color: #2563eb;
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.profile-right {
  max-width: 540px;
}

.profile-card {
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.profile-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e5e7eb;
}

.profile-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 14px;
  color: #6b7280;
}

.value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.profile-loading {
  margin-top: 20px;
  font-size: 14px;
  color: #64748b;
}

/* 消息样式 */
.message {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
}

.message.success {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.message.error {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.message.info {
  background-color: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

/* 编辑输入框样式 */
.edit-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.edit-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.edit-textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  min-height: 60px;
  transition: border-color 0.2s;
}

.edit-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 操作按钮样式 */
.profile-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
}

.btn-edit {
  padding: 10px 20px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.btn-save {
  padding: 10px 20px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-save:hover:not(:disabled) {
  background-color: #059669;
}

.btn-save:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 20px;
  background-color: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background-color: #4b5563;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-right {
    max-width: 100%;
  }
  
  .edit-actions {
    flex-direction: column;
  }
}

.message.success {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.message.error {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.message.info {
  background-color: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

/* 编辑输入框样式 */
.edit-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.edit-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.edit-textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  min-height: 60px;
  transition: border-color 0.2s;
}

.edit-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 操作按钮样式 */
.profile-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
}

.btn-edit {
  padding: 10px 20px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.btn-save {
  padding: 10px 20px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-save:hover:not(:disabled) {
  background-color: #059669;
}

.btn-save:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 20px;
  background-color: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background-color: #4b5563;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-right {
    max-width: 100%;
  }
  
  .edit-actions {
    flex-direction: column;
  }
}

.message.success {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.message.error {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.message.info {
  background-color: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

/* 编辑输入框样式 */
.edit-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.edit-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.edit-textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  min-height: 60px;
  transition: border-color 0.2s;
}

.edit-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 操作按钮样式 */
.profile-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
}

.btn-edit {
  padding: 10px 20px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.btn-save {
  padding: 10px 20px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-save:hover:not(:disabled) {
  background-color: #059669;
}

.btn-save:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 20px;
  background-color: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background-color: #4b5563;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-right {
    max-width: 100%;
  }
  
  .edit-actions {
    flex-direction: column;
  }
}
</style>