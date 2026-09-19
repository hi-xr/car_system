<template>
  <div class="save-scheme-page">
    <Header />
    <main class="save-scheme-main">
      <div class="container">
        <section v-if="!payload" class="state-box">
          当前没有可保存的配置，请返回配置页面重新选择。
        </section>

        <template v-else>
          <section class="card overview-card">
            <div class="overview-left">
              <p class="overview-label">当前配置概览</p>
              <h1 class="overview-title">🚗 {{ payload.vehicleName || '未选择车型' }}</h1>
              <p class="overview-meta">
                当前总价：<strong>¥{{ formatPrice(payload.totalPrice) }}</strong>
              </p>
              <p class="overview-meta">
                已选配置：{{ selectedCount }} 项
              </p>
            </div>
            <div class="overview-right" v-if="payload.vehicleImg">
              <img class="vehicle-img" :src="payload.vehicleImg" :alt="payload.vehicleName" />
            </div>
          </section>

          <section class="card form-card">
            <h2 class="section-title">方案信息</h2>

            <label class="field">
              <span class="field-label">方案命名 <span class="required">*</span></span>
              <input
                v-model="schemeName"
                class="field-input"
                type="text"
                placeholder="给您的配置方案起个名字，方便后续查找"
                maxlength="50"
              />
              <span class="field-hint">
                建议：包含车型 + 主要特点，如“530Li 黑外红内 M 套件版”
              </span>
            </label>

            <div class="field">
              <span class="field-label">是否分享</span>
              <div class="radio-row">
                <label class="radio">
                  <input type="radio" :value="true" v-model="isPublic" />
                  <span>是</span>
                </label>
                <label class="radio">
                  <input type="radio" :value="false" v-model="isPublic" />
                  <span>否</span>
                </label>
              </div>
            </div>

            <label class="field">
              <span class="field-label">添加备注（可选）</span>
              <textarea
                v-model="shareDescription"
                class="field-textarea"
                placeholder="记录这个方案的特别之处，如“准备春节前提车...”"
                rows="4"
                maxlength="500"
              ></textarea>
            </label>
          </section>

          <section class="card preview-card">
            <div class="preview-header">
              <h2 class="section-title">📋 配置清单预览</h2>
              <button class="btn-text" @click="toggleAll">
                {{ allExpanded ? '全部收起' : '全部展开' }}
              </button>
            </div>

            <div class="preview-groups">
              <div v-for="g in groups" :key="g.key" class="group">
                <button class="group-toggle" @click="toggleGroup(g.key)">
                  <span class="arrow">{{ expanded[g.key] ? '▼' : '▶' }}</span>
                  <span class="group-title">{{ g.title }}：</span>
                  <span class="group-summary">{{ g.summary }}</span>
                </button>
                <div v-if="expanded[g.key]" class="group-body">
                  <ul class="group-list">
                    <li v-for="name in g.items" :key="name">{{ name }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </section>

          <section class="actions">
            <button class="btn-secondary" :disabled="saving" @click="cancel">取消</button>
            <button class="btn-primary" :disabled="saving || !schemeName.trim()" @click="confirmSave">
              💾 确认保存
            </button>
          </section>
        </template>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { createCustomScheme } from '../api'

const router = inject('router')

const payload = ref(null)
const schemeName = ref('')
const isPublic = ref(false)
const shareDescription = ref('')
const saving = ref(false)

const expanded = ref({
  exterior: true,
  interior: false,
  performance: false,
  tech: false
})

const groups = computed(() => {
  const p = payload.value || {}
  const mk = (key, title, list) => {
    const items = (list || []).map((x) => String(x?.name || '')).filter(Boolean)
    const summary = items.length ? items.slice(0, 6).join('、') + (items.length > 6 ? '...' : '') : '无'
    return { key, title, items, summary }
  }
  return [
    mk('exterior', '外观配置', p.exteriorExtras),
    mk('interior', '内饰配置', p.interiorExtras),
    mk('performance', '性能配置', p.performanceExtras),
    mk('tech', '科技配置', p.techExtras)
  ]
})

const selectedCount = computed(() => {
  const p = payload.value || {}
  const n = (arr) => (Array.isArray(arr) ? arr.length : 0)
  return n(p.exteriorExtras) + n(p.interiorExtras) + n(p.performanceExtras) + n(p.techExtras)
})

const allExpanded = computed(() => {
  const e = expanded.value
  return !!(e.exterior && e.interior && e.performance && e.tech)
})

function formatPrice(v) {
  const num = Number(v) || 0
  return num.toLocaleString()
}

function toggleGroup(key) {
  expanded.value = { ...expanded.value, [key]: !expanded.value[key] }
}

function toggleAll() {
  const next = !allExpanded.value
  expanded.value = {
    exterior: next,
    interior: next,
    performance: next,
    tech: next
  }
}

function cancel() {
  router.push('/configure')
}

async function confirmSave() {
  if (!payload.value) return
  const name = schemeName.value.trim()
  if (!name) return

  saving.value = true
  try {
    const finalPayload = {
      ...(payload.value || {}),
      schemeName: name,
      isPublic: !!isPublic.value,
      // 后端字段：shareDescription（方案介绍/备注）
      shareDescription: shareDescription.value || '',
      shareTitle: name
    }
    const res = await createCustomScheme(finalPayload)
    const schemeId = res?.schemeId ?? res?.scheme_id ?? res?.id
    sessionStorage.removeItem('pendingSchemePayload')
    if (schemeId) {
      router.push(`/scheme-detail?id=${encodeURIComponent(String(schemeId))}`)
      return
    }
    alert('方案已保存')
    router.push('/configure')
  } catch (e) {
    console.error(e)
    alert(e?.message || '保存方案失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  try {
    const raw = sessionStorage.getItem('pendingSchemePayload')
    if (!raw) return
    const p = JSON.parse(raw)
    payload.value = p
    schemeName.value = String(p?.schemeName || '') || String(p?.vehicleName || '') || ''
    isPublic.value = !!p?.isPublic
    shareDescription.value = String(p?.shareDescription || '')
  } catch (e) {
    console.error(e)
    payload.value = null
  }
})
</script>

<style scoped>
.save-scheme-page {
  min-height: 100vh;
  background: #f6f7fb;
  color: #111827;
}

.save-scheme-main {
  min-height: calc(100vh - 64px);
}

.container {
  max-width: 980px;
  margin: 0 auto;
  padding: 20px 20px 64px;
}

.state-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 18px;
  color: #6b7280;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 18px;
  margin-bottom: 14px;
}

.overview-card {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 16px;
  align-items: center;
}

.overview-label {
  margin: 0 0 6px;
  font-size: 13px;
  color: #6b7280;
}

.overview-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
}

.overview-meta {
  margin: 4px 0;
  color: #374151;
}

.vehicle-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
}

.field {
  display: block;
  margin-bottom: 14px;
}

.field-label {
  display: inline-block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.field-input {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
  font-size: 14px;
}

.field-textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
  font-size: 14px;
  resize: vertical;
}

.field-hint {
  display: block;
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}

.radio-row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-top: 8px;
}

.radio {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #374151;
  font-size: 14px;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.btn-text {
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font-size: 13px;
}

.group {
  border: 1px solid #eef2f7;
  border-radius: 12px;
  margin-top: 10px;
  overflow: hidden;
}

.group-toggle {
  width: 100%;
  text-align: left;
  border: none;
  background: #f9fafb;
  cursor: pointer;
  padding: 12px 12px;
  display: grid;
  grid-template-columns: 26px auto 1fr;
  gap: 8px;
  align-items: center;
}

.arrow {
  color: #6b7280;
}

.group-title {
  font-weight: 700;
  color: #111827;
}

.group-summary {
  color: #6b7280;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-body {
  padding: 12px 14px 14px;
}

.group-list {
  margin: 0;
  padding-left: 18px;
  color: #374151;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.btn-secondary {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  color: #111827;
}

.btn-primary {
  border: 1px solid #2563eb;
  background: #2563eb;
  color: #fff;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 820px) {
  .overview-card {
    grid-template-columns: 1fr;
  }
}
</style>

