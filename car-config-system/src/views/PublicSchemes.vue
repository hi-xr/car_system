<template>
  <div class="public-schemes-page">
    <Header />
    <main class="main">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">已分享方案</h1>
          <p class="page-subtitle">浏览所有公开分享的配置方案</p>
        </div>

        <section v-if="loading" class="state-box">正在加载方案...</section>
        <section v-else-if="error" class="state-box state-error">{{ error }}</section>

        <section v-else>
          <div class="scheme-grid">
            <PackageCard
              v-for="s in schemes"
              :key="String(s.schemeId)"
              :id="s.schemeId"
              :title="s.schemeName || '未命名方案'"
              :author="s.authorText"
              :likes="String(s.likesCount ?? 0)"
              :comments="String(s.copiesCount ?? 0)"
              :imgSrc="s.imgSrc"
              :pinned="!!s.pinned"
              @pinned-change="loadSchemes"
            />
          </div>

          <div v-if="!schemes.length" class="state-box state-empty">
            暂无已分享方案
          </div>
        </section>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import PackageCard from '../components/PackageCard.vue'
import { getCustomSchemes } from '../api'

const loading = ref(false)
const error = ref('')
const schemes = ref([])

const defaultImg =
  'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=900&q=80&fit=crop'

function guessSchemeImg(s) {
  const raw = s.carImage ?? s.car_image ?? ''
  if (raw) {
    const str = String(raw).trim()
    if (str.startsWith('http://') || str.startsWith('https://')) return str
    const clean = str.replace(/^\/+/, '')
    return clean ? `/${clean}` : defaultImg
  }
  return defaultImg
}

function mapScheme(raw) {
  return {
    schemeId: raw.schemeId ?? raw.scheme_id ?? raw.id,
    schemeName: raw.schemeName ?? raw.scheme_name ?? raw.shareTitle ?? '',
    modelId: raw.modelId ?? raw.model_id ?? '',
    likesCount: raw.likesCount ?? raw.likes_count ?? 0,
    copiesCount: raw.copiesCount ?? raw.copies_count ?? 0,
    pinned: raw.pinned ?? raw.isPinned ?? raw.is_pinned ?? false,
    authorText: raw.userName ? `@${raw.userName}` : raw.username ? `@${raw.username}` : (raw.userId ? `用户${raw.userId}` : '匿名用户'),
    imgSrc: guessSchemeImg(raw)
  }
}

async function loadSchemes() {
  loading.value = true
  error.value = ''
  try {
    const list = await getCustomSchemes({ onlyPublic: true })
    const mapped = (Array.isArray(list) ? list : []).map(mapScheme).filter((x) => x.schemeId)
    mapped.sort((a, b) => {
      const ap = a.pinned ? 1 : 0
      const bp = b.pinned ? 1 : 0
      if (bp !== ap) return bp - ap
      return Number(b.likesCount || 0) - Number(a.likesCount || 0)
    })
    schemes.value = mapped
  } catch (e) {
    console.error(e)
    error.value = e?.message || '方案加载失败，请稍后重试'
    schemes.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSchemes()
})
</script>

<style scoped>
.public-schemes-page {
  min-height: 100vh;
  background: #ffffff;
  color: #111827;
}

.main {
  min-height: calc(100vh - 64px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 20px 72px;
}

.page-header {
  text-align: center;
  margin: 10px 0 24px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
}

.scheme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.state-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 18px;
  color: #6b7280;
  text-align: center;
}

.state-error {
  border-color: #fecaca;
  color: #b91c1c;
  background: #fff7f7;
}

.state-empty {
  margin-top: 18px;
}

@media (max-width: 992px) {
  .scheme-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .scheme-grid {
    grid-template-columns: 1fr;
  }
}
</style>

