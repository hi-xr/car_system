<template>
  <div class="vehicle-detail-page">
    <Header />
    <main class="vehicle-main">
      <div class="container" v-if="detail">
        <h1 class="page-title">{{ detail.modelName }}</h1>

        <section class="detail-layout">
          <!-- 左侧：车辆外观图 -->
          <div class="detail-left">
            <div class="image-card">
              <img :src="detail.imgUrl || defaultImg" :alt="detail.modelName" class="vehicle-img">
              <div class="image-caption">车辆外观</div>
            </div>
          </div>

          <!-- 右侧：规格参数 -->
          <div class="detail-right">
            <div class="spec-list">
              <div class="spec-row">
                <span class="spec-label">车辆型号</span>
                <span class="spec-value">{{ detail.modelName }}</span>
              </div>
              <div class="spec-row" v-if="detail.engineParams">
                <span class="spec-label">发动机参数</span>
                <span class="spec-value">{{ detail.engineParams }}</span>
              </div>
              <div class="spec-row" v-if="detail.bodyDimensions">
                <span class="spec-label">车身尺寸</span>
                <span class="spec-value">{{ detail.bodyDimensions }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">官方指导价</span>
                <span class="spec-value price">{{ formatPrice(detail.guidePrice) }}万元起</span>
              </div>
              <div class="spec-row" v-if="detail.configHighlights">
                <span class="spec-label">配置亮点</span>
                <span class="spec-value">{{ detail.configHighlights }}</span>
              </div>
              <div class="spec-row" v-if="detail.brand">
                <span class="spec-label">品牌</span>
                <span class="spec-value">{{ detail.brand }}</span>
              </div>
              <div class="spec-row" v-if="detail.productionYear">
                <span class="spec-label">生产年份</span>
                <span class="spec-value">{{ detail.productionYear }}</span>
              </div>
              <div class="spec-row" v-if="detail.powerType">
                <span class="spec-label">动力类型</span>
                <span class="spec-value">{{ detail.powerType }}</span>
              </div>
              <div class="spec-row" v-if="detail.bodyType">
                <span class="spec-label">车身类型</span>
                <span class="spec-value">{{ detail.bodyType }}</span>
              </div>
            </div>
            <button class="btn-config" @click="goToConfigure">
              开始配置
            </button>
          </div>
        </section>
      </div>
      <div v-else-if="loadError" class="loading state-error">
        {{ loadError }}
      </div>
      <div v-else class="loading">
        正在加载车型详情...
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { getCarModelDetail } from '../api/carModels.js'

const router = inject('router')

const defaultImg = 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&q=80'

const detail = ref(null)
const loadError = ref('')

const formatPrice = (value) => {
  if (value == null) return '0'
  const num = Number(value) || 0
  return (num / 10000).toFixed(2)
}

const getQueryParam = (name) => {
  const params = new URLSearchParams(window.location.search)
  return params.get(name)
}

const goToConfigure = () => {
  router.push('/configure')
}

/** 标准化车型图片路径：数据库 car_image 如 carmodel/1.jpeg → /carmodel/1.jpeg */
function normalizeCarImagePath(path) {
  if (!path) return ''
  const s = String(path).trim()
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const clean = s.replace(/^\/+/, '')
  return clean ? `/${clean}` : ''
}

const loadDetail = async (modelId) => {
  loadError.value = ''
  try {
    const raw = await getCarModelDetail(modelId)
    const rawImg = raw.carImage ?? raw.car_image ?? raw.imgUrl ?? raw.img_url ?? raw.imageUrl ?? raw.image_url ?? ''

    detail.value = {
      modelName: raw.modelName ?? raw.model_name ?? '',
      brand: raw.brand ?? '',
      guidePrice: raw.guidePrice ?? raw.guide_price ?? 0,
      productionYear: raw.productionYear ?? raw.production_year ?? '',
      powerType: raw.powerType ?? raw.power_type ?? '',
      bodyType: raw.bodyType ?? raw.body_type ?? '',
      imgUrl: normalizeCarImagePath(rawImg),
      engineParams: raw.engineParams ?? raw.engine_params ?? '',
      bodyDimensions: raw.bodyDimensions ?? raw.body_dimensions ?? '',
      configHighlights: raw.configHighlights ?? raw.config_highlights ?? ''
    }
  } catch (e) {
    console.error(e)
    loadError.value = '车型详情加载失败，请稍后重试'
    detail.value = null
  }
}

onMounted(() => {
  const modelId = getQueryParam('id')
  if (!modelId) {
    loadError.value = '缺少车型 ID'
    detail.value = null
    return
  }
  loadDetail(modelId)
})
</script>

<style scoped>
.vehicle-detail-page {
  min-height: 100vh;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
}

.vehicle-main {
  flex: 1;
  padding: 32px 0 48px;
}

.container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 24px 0;
  text-align: center;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 32px;
  align-items: start;
}

.detail-left {
  position: sticky;
  top: 24px;
}

.image-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  border: 1px solid #e2e8f0;
}

.vehicle-img {
  width: 100%;
  aspect-ratio: 4/3;
  object-fit: cover;
  display: block;
}

.image-caption {
  padding: 12px 16px;
  background: #1e293b;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.detail-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.spec-list {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #e2e8f0;
}

.spec-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  font-size: 12px;
  color: #64748b;
}

.spec-value {
  font-size: 15px;
  font-weight: 500;
  color: #0f172a;
}

.spec-value.price {
  font-size: 18px;
  font-weight: 600;
  color: #2563eb;
}

.btn-config {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  border-radius: 10px;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.4);
  transition: all 0.2s ease;
  width: 100%;
}

.btn-config:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.55);
}

.loading {
  max-width: 1120px;
  margin: 40px auto;
  padding: 0 20px;
  font-size: 14px;
  color: #64748b;
}

.loading.state-error {
  color: #dc2626;
}

@media (max-width: 768px) {
  .vehicle-main {
    padding-top: 20px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 20px;
  }

  .detail-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .detail-left {
    position: static;
  }
}
</style>
