<template>
  <div class="vehicle-list-page">
    <Header />
    <main class="vehicle-main">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">全部车型</h1>
          <div class="search-wrap">
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="按名称搜索车型..."
            />
          </div>
        </div>

        <section v-if="loading" class="state-box">
          正在加载车型数据...
        </section>
        <section v-else-if="error" class="state-box state-error">
          {{ error }}
        </section>
        <section v-else>
          <div class="vehicle-grid">
            <VehicleCard
              v-for="car in filteredVehicles"
              :key="car.modelId"
              :model-id="car.modelId"
              :title="car.modelName"
              :price="formatPrice(car.guidePrice)"
              :img-src="car.imgUrl || defaultImg"
            />
          </div>
          <section v-if="!filteredVehicles.length" class="state-box state-empty">
            {{ searchKeyword ? '未找到匹配的车型' : '暂无车型数据' }}
          </section>
        </section>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import VehicleCard from '../components/VehicleCard.vue'
import { getCarModels } from '../api/carModels.js'

const defaultImg =
  'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&q=80'

const vehicleList = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const error = ref('')

/** 按名称筛选车型 */
const filteredVehicles = computed(() => {
  const list = vehicleList.value || []
  const kw = (searchKeyword.value || '').trim().toLowerCase()
  if (!kw) return list
  return list.filter((car) =>
    String(car.modelName || '').toLowerCase().includes(kw)
  )
})

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

const mapToCard = (raw) => {
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

const loadCarModels = async () => {
  loading.value = true
  error.value = ''
  try {
    const list = await getCarModels()
    vehicleList.value = list.map(mapToCard)
  } catch (e) {
    console.error(e)
    error.value = '车型数据加载失败，请稍后重试'
    vehicleList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCarModels()
})
</script>

<style scoped>
.vehicle-list-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.vehicle-main {
  flex: 1;
  padding: 40px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.search-wrap {
  flex-shrink: 0;
}

.search-input {
  width: 240px;
  padding: 10px 16px;
  font-size: 14px;
  color: #1e293b;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s ease;
}

.search-input::placeholder {
  color: #94a3b8;
}

.search-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.state-box {
  margin-top: 24px;
  padding: 32px;
  text-align: center;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 14px;
}

.state-error {
  color: #dc2626;
  background: #fef2f2;
  border-color: #fecaca;
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
  .vehicle-main {
    padding: 24px 0 40px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .page-title {
    font-size: 22px;
  }

  .vehicle-grid {
    grid-template-columns: 1fr;
  }
}
</style>

