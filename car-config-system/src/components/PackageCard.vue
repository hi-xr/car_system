<template>
  <div class="package-card" :class="{ 'package-card--pinned': !!pinned }">
    <span v-if="pinned" class="hot-badge" aria-label="置顶热门方案">HOT</span>
    <div class="card-img-wrapper clickable" role="button" tabindex="0" @click="goToDetail">
      <img :src="imgSrc" :alt="title" class="card-img">
    </div>
    <div class="card-content">
      <h3 class="clickable" role="button" tabindex="0" @click="goToDetail">{{ title }}</h3>
      <p class="author">由用户 {{ author }} 分享</p>
      <div class="card-footer">
        <div class="stats">
          <span class="stat-item">
            <svg class="icon-heart" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            {{ likes }}
          </span>
          <span class="stat-item">
            <svg class="icon-comment" viewBox="0 0 24 24" fill="currentColor">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"/>
            </svg>
            {{ comments }}
          </span>
        </div>
        <div class="card-actions">
          <button
            v-if="isAdmin"
            type="button"
            class="btn-pin"
            :class="{ active: !!pinned }"
            :disabled="pinning"
            @click="togglePin"
          >
            {{ pinned ? '取消置顶' : '置顶' }}
          </button>
          <button type="button" class="btn-detail" @click="goToDetail">查看详情</button>
          <button class="btn-config" @click="goToConfigurator">开始配置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, ref } from 'vue'
import { pinCustomScheme } from '../api'

const router = inject('router')

const props = defineProps({
  id: {
    type: [String, Number],
    default: '1'
  },
  title: {
    type: String,
    required: true
  },
  author: {
    type: String,
    required: true
  },
  likes: {
    type: String,
    required: true
  },
  comments: {
    type: String,
    required: true
  },
  imgSrc: {
    type: String,
    required: true
  },
  pinned: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['pinned-change'])

const isAdmin = ref((localStorage.getItem('role') || '').toLowerCase() === 'admin')
const pinning = ref(false)

const goToConfigurator = () => {
  const id = props.id ?? '1'
  router.push(`/configure?schemeId=${encodeURIComponent(String(id))}`)
}

const goToDetail = () => {
  const id = props.id ?? '1'
  router.push(`/scheme-detail?id=${encodeURIComponent(String(id))}`)
}

const togglePin = async () => {
  if (!props.id) return
  try {
    pinning.value = true
    await pinCustomScheme(props.id, { pinned: !props.pinned })
    emit('pinned-change', { id: props.id, pinned: !props.pinned })
  } catch (e) {
    console.error(e)
    alert(e?.message || '置顶操作失败')
  } finally {
    pinning.value = false
  }
}
</script>

<style scoped>
.package-card {
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid transparent;
}

.package-card--pinned {
  border-color: rgba(220, 38, 38, 0.55);
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.12), 0 1px 3px rgba(0, 0, 0, 0.08);
}

.hot-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 3;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #ffffff;
  background: linear-gradient(135deg, #ef4444, #f97316);
  border-radius: 999px;
  box-shadow: 0 10px 20px rgba(239, 68, 68, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.package-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-img-wrapper {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.package-card:hover .card-img {
  transform: scale(1.05);
}

.card-content {
  padding: 16px 20px 20px;
}

.card-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #1e293b;
}

.author {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #64748b;
}

.icon-heart {
  width: 16px;
  height: 16px;
  color: #ef4444;
}

.icon-comment {
  width: 16px;
  height: 16px;
  color: #94a3b8;
}

.btn-config {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  background-color: #2563eb;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-config:hover {
  background-color: #1d4ed8;
}

.card-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.clickable {
  cursor: pointer;
}

.btn-detail {
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-detail:hover {
  border-color: #2563eb;
  background: rgba(37, 99, 235, 0.06);
}

.btn-pin {
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #b91c1c;
  background: #fff;
  border: 1px solid rgba(185, 28, 28, 0.35);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-pin:hover:enabled {
  border-color: rgba(185, 28, 28, 0.65);
  background: rgba(254, 226, 226, 0.55);
}

.btn-pin.active {
  background: rgba(185, 28, 28, 0.1);
}

.btn-pin:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
