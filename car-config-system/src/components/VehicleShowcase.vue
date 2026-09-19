<template>
  <section class="vehicle-showcase">
    <div class="container">
      <h2>探索我们的全系车型</h2>
      <div v-if="loading" class="state-box">正在加载车型数据...</div>
      <div v-else-if="error" class="state-box state-error">{{ error }}</div>
      <div v-else>
        <div class="vehicle-grid">
          <VehicleCard
            v-for="car in displayedVehicles"
            :key="car.modelId"
            :model-id="car.modelId"
            :title="car.modelName"
            :price="formatPrice(car.guidePrice)"
            :img-src="car.imgUrl || defaultImg"
          />
        </div>
        <div v-if="!vehicleList.length" class="state-box state-empty">
          暂无车型数据
        </div>
        <div v-else class="more-btn">
          <router-link to="/vehicles" class="btn-more">查看更多车型</router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VehicleCard from './VehicleCard.vue'
import { getCarModels } from '../api/carModels.js'

const defaultImg = 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=600&q=80'

const vehicleList = ref([])
const loading = ref(false)
const error = ref('')

/** 首页只展示前 6 款车型，其余通过「查看更多车型」查看 */
const displayedVehicles = computed(() => vehicleList.value.slice(0, 6))

function getFallbackList() {
  return [
    { modelId: 1, modelName: '奥迪 A6L', brand: '奥迪', guidePrice: 428000, productionYear: '2025', powerType: '燃油', bodyType: '轿车', imgUrl: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=600&q=80' },
    { modelId: 2, modelName: '宝马 530Li', brand: '宝马', guidePrice: 459000, productionYear: '2025', powerType: '燃油', bodyType: '轿车', imgUrl: 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=600&q=80' },
    { modelId: 3, modelName: '特斯拉 Model Y', brand: '特斯拉', guidePrice: 329900, productionYear: '2025', powerType: '电动', bodyType: 'SUV', imgUrl: 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=600&q=80' }
  ]
}

const formatPrice = (value) => {
  if (value == null) return '0'
  const num = Number(value) || 0
  return '¥' + num.toLocaleString()
}

/** 标准化车型图片路径：数据库 car_image 如 carmodel/1.jpeg → /carmodel/1.jpeg */
function normalizeCarImagePath(path) {
  if (!path) return ''
  const s = String(path).trim()
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const clean = s.replace(/^\/+/, '')
  return clean ? `/${clean}` : ''
}

function mapToCard(raw) {
  const rawImg = raw.carImage ?? raw.car_image ?? raw.imgUrl ?? raw.img_url ?? raw.imageUrl ?? raw.image_url ?? ''
  return {
    modelId: raw.modelId ?? raw.model_id,
    modelName: raw.modelName ?? raw.model_name ?? '',
    brand: raw.brand ?? '',
    guidePrice: raw.guidePrice ?? raw.guide_price ?? 0,
    productionYear: raw.productionYear ?? raw.production_year ?? '',
    powerType: raw.powerType ?? raw.power_type ?? '',
    bodyType: raw.bodyType ?? raw.body_type ?? '',
    imgUrl: normalizeCarImagePath(rawImg)
  }
}

async function loadCarModels() {
  loading.value = true
  error.value = ''
  try {
    const list = await getCarModels()
    vehicleList.value = list.map(mapToCard)
  } catch (e) {
    console.error(e)
    vehicleList.value = getFallbackList()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCarModels()
})
</script>

<style scoped>
.vehicle-showcase {
  padding: 80px 0;
  background-color: #fff;
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

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.more-btn {
  margin-top: 40px;
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

.state-box {
  padding: 32px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
  background: #f8fafc;
  border-radius: 12px;
}

.state-error {
  color: #dc2626;
  background: #fef2f2;
}

.state-empty {
  color: #94a3b8;
}

@media (max-width: 992px) {
  .vehicle-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .vehicle-grid {
    grid-template-columns: 1fr;
  }
  
  h2 {
    font-size: 24px;
    margin-bottom: 32px;
  }
}
</style>
