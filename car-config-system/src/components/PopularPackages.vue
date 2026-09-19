<template>
  <section class="popular-packages">
    <div class="container">
      <h2>热门配置方案</h2>
      <div class="package-grid">
        <PackageCard
          v-for="s in displayedSchemes"
          :key="String(s.schemeId)"
          :id="s.schemeId"
          :title="s.schemeName || '未命名方案'"
          :author="s.authorText"
          :likes="String(s.likesCount ?? 0)"
          :comments="String(s.copiesCount ?? 0)"
          :imgSrc="s.imgSrc"
          :pinned="!!s.pinned"
          @pinned-change="onPinnedChange"
        />
      </div>
      <div class="more-btn">
        <router-link to="/schemes" class="btn-more">查看更多热门方案</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
import PackageCard from './PackageCard.vue'
import { ref, computed, onMounted } from 'vue'
import { getPopularSchemes } from '../api'

const schemes = ref([])

const displayedSchemes = computed(() => (schemes.value || []).slice(0, 3))

function normalizeCarImagePath(path) {
  if (!path) return ''
  const s = String(path).trim()
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const clean = s.replace(/^\/+/, '')
  return clean ? `/${clean}` : ''
}

const defaultImg =
  'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&q=80&fit=crop'

function mapScheme(raw) {
  const img = normalizeCarImagePath(raw.carImage ?? raw.car_image ?? '')
  return {
    schemeId: raw.schemeId ?? raw.scheme_id ?? raw.id,
    schemeName: raw.schemeName ?? raw.scheme_name ?? raw.shareTitle ?? '',
    likesCount: raw.likesCount ?? raw.likes_count ?? 0,
    copiesCount: raw.copiesCount ?? raw.copies_count ?? 0,
    pinned: raw.pinned ?? raw.isPinned ?? raw.is_pinned ?? false,
    authorText: raw.userName ? `@${raw.userName}` : raw.username ? `@${raw.username}` : (raw.userId ? `用户${raw.userId}` : '匿名用户'),
    imgSrc: img || defaultImg
  }
}

async function loadPopularSchemes() {
  try {
    const list = await getPopularSchemes()
    const mapped = (Array.isArray(list) ? list : [])
      .map(mapScheme)
      .filter((x) => x.schemeId)
    // 置顶优先，其次按点赞排序
    mapped.sort((a, b) => {
      const ap = a.pinned ? 1 : 0
      const bp = b.pinned ? 1 : 0
      if (bp !== ap) return bp - ap
      return Number(b.likesCount || 0) - Number(a.likesCount || 0)
    })
    schemes.value = mapped
  } catch (e) {
    console.error(e)
    schemes.value = []
  }
}

function onPinnedChange() {
  // 置顶状态变更后刷新热门方案列表
  loadPopularSchemes()
}

onMounted(() => {
  loadPopularSchemes()
})
</script>

<style scoped>
.popular-packages {
  padding: 80px 0;
  background-color: #f8fafc;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h2 {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 48px;
  color: #1e293b;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 48px;
}

.more-btn {
  text-align: center;
}

.btn-more {
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 500;
  color: #2563eb;
  background-color: transparent;
  border: 1px solid #2563eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-more:hover {
  background-color: #2563eb;
  color: #fff;
}

@media (max-width: 992px) {
  .package-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .package-grid {
    grid-template-columns: 1fr;
  }
  
  h2 {
    font-size: 24px;
    margin-bottom: 32px;
  }
}
</style>
