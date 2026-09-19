<template>
  <div class="configure-page">
    <Header />

    <div class="configure-main">
      <div class="configure-container">
        <!-- 顶部步骤条 -->
        <header class="steps-header">
          <div class="steps-title">在线选配</div>
          <div class="steps-flow">
            <div class="step-item active">
              <span class="step-index">1</span>
              <span class="step-label">选择车型</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
              <span class="step-index">2</span>
              <span class="step-label">定制配置</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
              <span class="step-index">3</span>
              <span class="step-label">生成结果</span>
            </div>
          </div>
        </header>

        <div class="configure-layout">
          <!-- 第一行 50% | 50%：选择您的车型 + 车辆预览 -->
          <section class="panel panel-vehicles">
            <div class="panel-header">
              <span class="panel-title">选择您的车型</span>
              <div class="vehicle-filters">
                <input
                  v-model="vehicleSearchKeyword"
                  class="vehicle-search"
                  type="text"
                  placeholder="搜索车型名称..."
                />
                <div class="vehicle-price-filter">
                  <input
                    v-model="vehiclePriceMin"
                    class="vehicle-price-input"
                    type="number"
                    min="0"
                    placeholder="最低价"
                  />
                  <span class="price-sep">-</span>
                  <input
                    v-model="vehiclePriceMax"
                    class="vehicle-price-input"
                    type="number"
                    min="0"
                    placeholder="最高价"
                  />
                </div>
              </div>
            </div>
            <div class="vehicle-grid">
              <button
                v-for="v in pagedVehicles"
                :key="v.id"
                class="vehicle-card"
                :class="{ active: selectedVehicle === v.id }"
                @click="selectedVehicle = v.id"
              >
                <img :src="v.img" :alt="v.name" class="vehicle-img" />
                <div class="vehicle-info">
                  <h4>{{ v.name }}</h4>
                  <p class="vehicle-price">¥{{ v.price.toLocaleString() }}</p>
                </div>
              </button>
            </div>
            <div
              v-if="filteredVehicles.length > vehiclePageSize"
              class="vehicle-pagination"
            >
              <button
                class="page-btn"
                :disabled="vehicleCurrentPage === 1"
                @click="changeVehiclePage(vehicleCurrentPage - 1)"
              >
                上一页
              </button>
              <span class="page-info">
                第 {{ vehicleCurrentPage }} / {{ totalVehiclePages }} 页
              </span>
              <button
                class="page-btn"
                :disabled="vehicleCurrentPage === totalVehiclePages"
                @click="changeVehiclePage(vehicleCurrentPage + 1)"
              >
                下一页
              </button>
            </div>
          </section>

          <section class="panel panel-preview">
            <h3 class="preview-title">车辆预览</h3>
            <div class="preview-box">
              <!-- key 绑定保证切换车型时强制重新挂载 3D 组件 -->
              <Configurator3DViewer
                :key="currentModelUid"
                :model-uid="currentModelUid"
                :color-hex="currentExteriorHex"
                :wheel-id="config.wheel"
              />
            </div>
          </section>

          <!-- 第二行 2/3 | 1/3：配置选项 + 已选配置清单 -->
          <section class="panel panel-options">
            <div class="panel-header">
              <span class="panel-title">配置选项</span>
            </div>
            <div class="options-tabs">
              <button
                v-for="tab in optionTabs"
                :key="tab.id"
                class="options-tab"
                :class="{ active: activeTab === tab.id }"
                @click="activeTab = tab.id"
              >
                {{ tab.label }}
              </button>
            </div>

            <div class="options-body">
              <!-- 外观配置（分为必选 / 非必选两栏） -->
              <div v-if="activeTab === 'exterior'">
                <div class="exterior-columns">
                  <div class="option-group">
                    <h4>必选项</h4>
                    <label class="checkbox-option select-all">
                      <input
                        type="checkbox"
                        :checked="exteriorRequiredOptions.length > 0 && exteriorRequiredOptions.every(o => config.exteriorExtras.includes(o.id))"
                        @change="toggleExteriorRequiredAll($event.target.checked)"
                      />
                      <span class="option-name">全选必选项</span>
                    </label>
                    <div class="option-list">
                      <label
                        v-for="opt in exteriorRequiredOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.exteriorExtras"
                          @change="onExteriorOptionToggle(opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="option-group">
                    <h4>可选项</h4>
                    <div class="option-list">
                      <label
                        v-for="opt in exteriorOptionalOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.exteriorExtras"
                          @change="onExteriorOptionToggle(opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 内饰配置 -->
              <div v-else-if="activeTab === 'interior'">
                <div class="exterior-columns">
                  <div class="option-group">
                    <h4>必选项</h4>
                    <label class="checkbox-option select-all">
                      <input
                        type="checkbox"
                        :checked="interiorRequiredOptions.length > 0 && interiorRequiredOptions.every(o => config.interiorExtras.includes(o.id))"
                        @change="toggleInteriorRequiredAll($event.target.checked)"
                      />
                      <span class="option-name">全选必选项</span>
                    </label>
                    <div class="option-list">
                      <label
                        v-for="opt in interiorRequiredOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.interiorExtras"
                          @change="onOptionToggle('interior', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="option-group">
                    <h4>可选项</h4>
                    <div class="option-list">
                      <label
                        v-for="opt in interiorOptionalOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.interiorExtras"
                          @change="onOptionToggle('interior', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 性能配置 -->
              <div v-else-if="activeTab === 'performance'">
                <div class="exterior-columns">
                  <div class="option-group">
                    <h4>必选项</h4>
                    <label class="checkbox-option select-all">
                      <input
                        type="checkbox"
                        :checked="performanceRequiredOptions.length > 0 && performanceRequiredOptions.every(o => config.performanceExtras.includes(o.id))"
                        @change="togglePerformanceRequiredAll($event.target.checked)"
                      />
                      <span class="option-name">全选必选项</span>
                    </label>
                    <div class="option-list">
                      <label
                        v-for="opt in performanceRequiredOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.performanceExtras"
                          @change="onOptionToggle('performance', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="option-group">
                    <h4>可选项</h4>
                    <div class="option-list">
                      <label
                        v-for="opt in performanceOptionalOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.performanceExtras"
                          @change="onOptionToggle('performance', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 科技配置 -->
              <div v-else-if="activeTab === 'tech'">
                <div class="exterior-columns">
                  <div class="option-group">
                    <h4>必选项</h4>
                    <label class="checkbox-option select-all">
                      <input
                        type="checkbox"
                        :checked="techRequiredOptions.length > 0 && techRequiredOptions.every(o => config.techExtras.includes(o.id))"
                        @change="toggleTechRequiredAll($event.target.checked)"
                      />
                      <span class="option-name">全选必选项</span>
                    </label>
                    <div class="option-list">
                      <label
                        v-for="opt in techRequiredOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.techExtras"
                          @change="onOptionToggle('tech', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="option-group">
                    <h4>可选项</h4>
                    <div class="option-list">
                      <label
                        v-for="opt in techOptionalOptions"
                        :key="opt.id"
                        class="checkbox-option"
                      >
                        <input
                          type="checkbox"
                          :value="opt.id"
                          v-model="config.techExtras"
                          @change="onOptionToggle('tech', opt, $event.target.checked)"
                        />
                        <span class="option-name">{{ opt.name }}</span>
                        <span class="option-price">+¥{{ (opt.extraPrice || 0).toLocaleString() }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section class="panel panel-checklist">
            <h3 class="checklist-title">已选配置清单</h3>
            <div class="checklist-body">
          <p class="checklist-item">车型：{{ selectedVehicleName }}</p>
          <p class="checklist-item">
            外观配置（必选项）：
            {{ selectedExteriorRequiredNames.length ? selectedExteriorRequiredNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            外观额外配置（非必选项）：
            {{ selectedExteriorOptionalNames.length ? selectedExteriorOptionalNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            内饰配置（必选项）：
            {{ selectedInteriorRequiredNames.length ? selectedInteriorRequiredNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            内饰额外配置（非必选项）：
            {{ selectedInteriorOptionalNames.length ? selectedInteriorOptionalNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            性能配置（必选项）：
            {{ selectedPerformanceRequiredNames.length ? selectedPerformanceRequiredNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            性能额外配置（非必选项）：
            {{ selectedPerformanceOptionalNames.length ? selectedPerformanceOptionalNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            科技配置（必选项）：
            {{ selectedTechRequiredNames.length ? selectedTechRequiredNames.join('、') : '无' }}
          </p>
          <p class="checklist-item">
            科技额外配置（非必选项）：
            {{ selectedTechOptionalNames.length ? selectedTechOptionalNames.join('、') : '无' }}
          </p>
            </div>
            <div class="checklist-total">
              <span>当前总价</span>
              <span class="checklist-total-value">¥{{ totalPrice.toLocaleString() }}</span>
            </div>
            <div class="price-actions">
              <button class="btn-secondary" @click="saveScheme">保存方案</button>
              <button class="btn-primary" @click="addSchemeToCart">加入购物车</button>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, inject } from 'vue'
import Header from '../components/Header.vue'
import Configurator3DViewer from '../components/Configurator3DViewer.vue'
import {
  getCarModels,
  getExteriorOptions,
  getInteriorOptions,
  getPerformanceOptions,
  getTechOptions,
  getConfigRules,
  getConfigRulesMeta,
  getCustomSchemeDetail,
  addToCart,
  createOrder as apiCreateOrder
} from '../api'

const router = inject('router')

function isLoggedIn() {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  return !!(token || username)
}

function redirectToLogin() {
  const redirect = window.location.pathname + window.location.search
  router.push(`/login?redirect=${encodeURIComponent(redirect)}`)
}

function requireLoginOrRedirect() {
  if (!isLoggedIn()) {
    redirectToLogin()
    return false
  }
  return true
}

const activeTab = ref('exterior')

const optionTabs = [
  { id: 'exterior', label: '外观配置' },
  { id: 'interior', label: '内饰配置' },
  { id: 'performance', label: '性能配置' },
  { id: 'tech', label: '科技配置' }
]

// 默认 3D 模型 UID（当某个车型没有单独配置 modelUid 时使用）
// 对应 Sketchfab 上公开的 Low-Poly Car 模型：
// https://sketchfab.com/3d-models/low-poly-car-fcdb0c27f7d04d47a518a249ae7093a2
const DEFAULT_MODEL_UID = 'fcdb0c27f7d04d47a518a249ae7093a2'

// 如果后端 car_model 表暂时没有提供 model_uid 字段，
// 就按车型 ID 在前端做一个兜底映射，保证“每辆车一个模型”
const FALLBACK_MODEL_UID_MAP = {
  // 这里仅是“前端兜底映射”，保证每个 model_id 对应一个不同的公开模型 UID
  // 以后如果后端 car_model 表提供了 model_uid 字段，会优先使用后端的值
  '1': 'fcdb0c27f7d04d47a518a249ae7093a2', // 奥迪 A6L
  '2': '141c3279892b429ba42c0b98ba88ae6b', // 宝马 530Li
  '3': '4d69b9aa808c42bc88226837f9c809b7', // 奔驰 E300L
  '4': 'b7b32eaca80d460c9338197e2c9d1408', // 特斯拉 Model Y
  '5': '18278b4bf0ec4002a27cc7a93290fb10', // 比亚迪 汉 DM-i
  '6': '73463b48a8994a9ba70dc4a275c40f4f', // 理想 L7
  '7': '8558a51b5e4e4adb840751020000eca6', // 蔚来 ET5T
  // 为丰田和本田更换两个之前未使用过的模型 UID
  '8': '93971323324243468f24d7da9d18f617', // 丰田 RAV4
  // 本田 CR-V 使用一个单车模型（Low Poly Small Car）
  // https://sketchfab.com/3d-models/low-poly-small-car-ebe7c5e98a7448b5abb2eaf0cb22b766
  '9': 'ebe7c5e98a7448b5abb2eaf0cb22b766', // 本田 CR-V
  '10': '6e624fba16914888bd435fb697f4a2b9' // 吉利 星瑞  → Low poly car
}

const defaultVehicleImg = 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=600&q=80'

const vehicleOptions = ref([])
const loadingVehicles = ref(false)
const vehiclesError = ref('')
const selectedVehicle = ref('')

// 车型筛选 / 分页
const vehicleSearchKeyword = ref('')
const vehiclePriceMin = ref('')
const vehiclePriceMax = ref('')
const vehiclePageSize = 9
const vehicleCurrentPage = ref(1)

function normalizeCarImagePath(path) {
  if (!path) return ''
  const s = String(path)
  // 绝对地址（含域名）的情况，直接用
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  // 其余情况统一走前端 public 目录：
  // public/carmodel/1.jpeg  ←→  /carmodel/1.jpeg
  const clean = s.replace(/^\/+/, '') // 去掉可能的前导斜杠
  return `/${clean}` // 例如 carmodel/1.jpeg → /carmodel/1.jpeg
}

function mapCarModelToCard(raw) {
  const modelId = raw.modelId ?? raw.model_id ?? raw.id
  const modelName = raw.modelName ?? raw.model_name ?? raw.name ?? ''
  const guidePrice = raw.guidePrice ?? raw.guide_price ?? raw.price ?? 0
  const carImage = raw.carImage ?? raw.car_image ?? raw.imgUrl ?? raw.img_url ?? raw.img ?? ''
  const dbModelUid = raw.modelUid ?? raw.model_uid
  const fallbackUid = FALLBACK_MODEL_UID_MAP[String(modelId ?? '')]
  return {
    id: String(modelId ?? ''),
    name: String(modelName || ''),
    price: Number(guidePrice) || 0,
    img: normalizeCarImagePath(carImage) || defaultVehicleImg,
    // 优先使用后端给的 model_uid，其次用前端兜底映射，最后用默认 UID
    modelUid: dbModelUid || fallbackUid || DEFAULT_MODEL_UID
  }
}

async function loadCarModels() {
  loadingVehicles.value = true
  vehiclesError.value = ''
  try {
    const list = await getCarModels()
    vehicleOptions.value = (list || []).map(mapCarModelToCard).filter((x) => x.id)
    if (!selectedVehicle.value && vehicleOptions.value.length) {
      selectedVehicle.value = vehicleOptions.value[0].id
    }
  } catch (e) {
    console.error(e)
    vehiclesError.value = '车型数据加载失败，请稍后重试'
    vehicleOptions.value = []
  } finally {
    loadingVehicles.value = false
  }
}

const filteredVehicles = computed(() => {
  let list = vehicleOptions.value || []
  const keyword = vehicleSearchKeyword.value.trim()
  if (keyword) {
    const lower = keyword.toLowerCase()
    list = list.filter((v) => String(v.name || '').toLowerCase().includes(lower))
  }
  const min = Number(vehiclePriceMin.value)
  const max = Number(vehiclePriceMax.value)
  const hasMin = !Number.isNaN(min) && vehiclePriceMin.value !== ''
  const hasMax = !Number.isNaN(max) && vehiclePriceMax.value !== ''
  if (hasMin) {
    list = list.filter((v) => (v.price || 0) >= min)
  }
  if (hasMax) {
    list = list.filter((v) => (v.price || 0) <= max)
  }
  return list
})

const totalVehiclePages = computed(() => {
  if (!filteredVehicles.value.length) return 1
  return Math.ceil(filteredVehicles.value.length / vehiclePageSize)
})

const pagedVehicles = computed(() => {
  const page = Math.min(
    Math.max(1, vehicleCurrentPage.value),
    totalVehiclePages.value || 1
  )
  const start = (page - 1) * vehiclePageSize
  return filteredVehicles.value.slice(start, start + vehiclePageSize)
})

function changeVehiclePage(page) {
  if (page < 1 || page > totalVehiclePages.value) return
  vehicleCurrentPage.value = page
}

const exteriorOptions = ref([])       // category_id = 1 的全部外观配置
const exteriorColors = ref([])        // 外观中作为车漆颜色的子集（供 3D 使用）
const interiorColors = ref([])        // category_id = 2, 作为主内饰颜色
const interiorOptions = ref([])       // 其它内饰附加项
const wheelOptions = ref([])          // 外观中作为轮毂的子集（供 3D / 价格使用）
const performanceOptions = ref([])    // category_id = 3
const techOptions = ref([])           // category_id = 4

// 配置规则（mutex 互斥 / depend 依赖），用于选配时校验
const configRules = ref([])
const optionNameMap = ref(new Map())  // id -> name，用于规则提示文案

const config = reactive({
  exteriorColor: '',
  interiorColor: '',
  wheel: '',
  exteriorExtras: [],
  interiorExtras: [],
  performanceExtras: [],
  techExtras: []
})

// 当前选中车型的基础车价
const basePrice = computed(() => {
  const v = (vehicleOptions.value || []).find((item) => item.id === selectedVehicle.value)
  return v ? v.price : 0
})
const loadingOptions = ref(false)
const optionsError = ref('')

async function loadConfigOptions() {
  loadingOptions.value = true
  optionsError.value = ''
  try {
    const [exteriorList, interiorList, performanceList, techList] = await Promise.all([
      getExteriorOptions(),
      getInteriorOptions(),
      getPerformanceOptions(),
      getTechOptions()
    ])

    const normalizedExterior = (exteriorList || []).map((x) => {
      const name = String(x.name || '')
      let hex = x.hex
      if (!hex && (name.includes('车漆') || name.includes('车身颜色') || /色车漆/.test(name))) {
        hex = pickColorHexByName(name)
      }
      return { ...x, hex }
    })

    exteriorOptions.value = normalizedExterior
    exteriorColors.value = normalizedExterior.filter((x) => !String(x.name || '').includes('轮毂'))
    wheelOptions.value = normalizedExterior
      .filter((x) => String(x.name || '').includes('轮毂'))
      .map((x) => ({
        id: String(x.id),
        name: x.name,
        extraPrice: x.extraPrice || 0,
        thumb: x.thumb || 'linear-gradient(135deg,#4b5563,#9ca3af)'
      }))
    interiorColors.value = (interiorList || []).slice(0, 1)
    interiorOptions.value = (interiorList || []).slice(1)
    performanceOptions.value = performanceList || []
    techOptions.value = techList || []

    if (!config.exteriorColor && exteriorColors.value.length) {
      config.exteriorColor = exteriorColors.value[0].name
    }
    if (!config.interiorColor && interiorColors.value.length) {
      config.interiorColor = interiorColors.value[0].name
    }
    if (!config.wheel && wheelOptions.value.length) {
      config.wheel = wheelOptions.value[0].id
    }

    // 加载配置规则与配置项名称映射（用于选配校验提示）
    try {
      const [rules, meta] = await Promise.all([
        getConfigRules({ enabled: true }),
        getConfigRulesMeta()
      ])
      configRules.value = Array.isArray(rules) ? rules : []
      const map = new Map()
      const items = meta?.optionItems || []
      items.forEach((it) => {
        const id = it.itemId ?? it.item_id ?? it.id
        const name = it.itemName ?? it.item_name ?? it.name ?? ''
        if (id != null) map.set(Number(id), String(name || '未知'))
      })
      const allOpts = [
        exteriorOptions.value,
        interiorColors.value,
        interiorOptions.value,
        performanceOptions.value,
        techOptions.value
      ]
      allOpts.forEach((arr) => {
        (arr || []).forEach((o) => {
          const id = o.id != null ? Number(o.id) : null
          if (id != null && !map.has(id)) map.set(id, String(o.name || '未知'))
        })
      })
      optionNameMap.value = map
    } catch (_) {
      configRules.value = []
    }
  } catch (e) {
    console.error(e)
    optionsError.value = '配置选项加载失败，请稍后重试'
  } finally {
    loadingOptions.value = false
  }
}

onMounted(() => {
  // 未登录不允许在线选配
  if (!requireLoginOrRedirect()) return
  initWithSchemePresetIfAny()
})

function getQueryParam(name) {
  try {
    const params = new URLSearchParams(window.location.search || '')
    return params.get(name)
  } catch {
    return null
  }
}

async function fetchSchemePreset() {
  const schemeId = getQueryParam('schemeId')
  if (!schemeId) return null
  try {
    const raw = await getCustomSchemeDetail(schemeId)
    const items = raw?.items || raw?.schemeItems || raw?.scheme_items || []
    const optionItemIds = items
      .map((it) => it?.id ?? it?.itemId ?? it?.item_id)
      .filter((x) => x != null)
      .map((x) => Number(x))
      .filter((n) => !Number.isNaN(n))
    const modelId = raw?.modelId ?? raw?.model_id ?? ''
    return {
      schemeId,
      modelId: modelId != null ? String(modelId) : '',
      optionItemIds
    }
  } catch (e) {
    console.error(e)
    return null
  }
}

function applySchemePreset(preset) {
  if (!preset) return

  // 车型：优先按方案车型选中（先设置，避免 loadCarModels 覆盖默认）
  if (preset.modelId) {
    selectedVehicle.value = String(preset.modelId)
  }

  // 清空已有勾选
  config.exteriorExtras = []
  config.interiorExtras = []
  config.performanceExtras = []
  config.techExtras = []

  const selectedSet = new Set((preset.optionItemIds || []).map((n) => Number(n)))

  // 先按“选项属于哪个分类列表”来归类，避免依赖 scheme 明细的 category 文案
  const exteriorIds = (exteriorOptions.value || [])
    .map((o) => Number(o.id))
    .filter((id) => selectedSet.has(id))
  const interiorIds = ([...(interiorOptions.value || []), ...(interiorColors.value || [])] || [])
    .map((o) => Number(o.id))
    .filter((id) => selectedSet.has(id))
  const performanceIds = (performanceOptions.value || [])
    .map((o) => Number(o.id))
    .filter((id) => selectedSet.has(id))
  const techIds = (techOptions.value || [])
    .map((o) => Number(o.id))
    .filter((id) => selectedSet.has(id))

  // 使用现有规则校验逻辑逐个加入；若冲突则跳过并在最后提示
  const failed = []
  function addAll(category, ids) {
    for (const id of ids) {
      const arr = config[`${category}Extras`]
      if (!arr) continue
      const beforeLen = arr.length
      arr.push(id)
      const result = checkConfigRule(getAllSelectedIds())
      if (!result.ok) {
        removeFromExtras(arr, id)
        failed.push({ id, message: result.message })
      } else if (arr.length === beforeLen) {
        // no-op
      }
    }
  }

  addAll('exterior', exteriorIds)
  addAll('interior', interiorIds)
  addAll('performance', performanceIds)
  addAll('tech', techIds)

  // 外观颜色/轮毂联动：从已选外观里找一个“颜色项/轮毂项”作为当前展示
  const selectedExterior = (exteriorOptions.value || []).filter((o) =>
    config.exteriorExtras.includes(o.id)
  )
  const pickedColor = selectedExterior.find((o) => o?.hex)
  if (pickedColor?.name) {
    config.exteriorColor = pickedColor.name
  }
  const pickedWheel = selectedExterior.find(
    (o) => o?.thumb || String(o?.name || '').includes('轮毂')
  )
  if (pickedWheel?.id != null) {
    config.wheel = String(pickedWheel.id)
  }

  if (failed.length) {
    const msg = failed[0]?.message || '部分配置项与规则冲突，已自动忽略'
    alert(msg)
  }
}

async function initWithSchemePresetIfAny() {
  const preset = await fetchSchemePreset()
  // 并行加载：车型 + 选项
  await Promise.all([loadCarModels(), loadConfigOptions()])
  if (preset) {
    applySchemePreset(preset)
  }
}

// 所有已选配置的加价之和（不区分必选 / 非必选）
const configExtrasPrice = computed(() => {
  let sum = 0

  ;(exteriorOptions.value || []).forEach((o) => {
    if (config.exteriorExtras.includes(o.id)) {
      sum += o.extraPrice || 0
    }
  })

  ;(interiorOptions.value || []).forEach((o) => {
    if (config.interiorExtras.includes(o.id)) {
      sum += o.extraPrice || 0
    }
  })

  ;(performanceOptions.value || []).forEach((o) => {
    if (config.performanceExtras.includes(o.id)) {
      sum += o.extraPrice || 0
    }
  })

  ;(techOptions.value || []).forEach((o) => {
    if (config.techExtras.includes(o.id)) {
      sum += o.extraPrice || 0
    }
  })

  return sum
})

// 清单里只展示纯名称，不附加“金属漆 / 高级内饰”等后缀
const exteriorColorLabel = computed(() => config.exteriorColor || '')
const wheelLabel = computed(() => wheelOptions.value.find(w => w.id === config.wheel)?.name || '')
const interiorColorLabel = computed(() => config.interiorColor || '')
const currentVehicle = computed(
  () => (vehicleOptions.value || []).find((v) => v.id === selectedVehicle.value) || (vehicleOptions.value || [])[0]
)

const selectedVehicleName = computed(() => currentVehicle.value?.name || '')

// 已选配置清单（只用于前端展示）
// 每一类再区分“必选项 / 非必选项”
const selectedExteriorRequiredNames = computed(() =>
  (exteriorRequiredOptions.value || [])
    .filter((o) => config.exteriorExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedExteriorOptionalNames = computed(() =>
  (exteriorOptionalOptions.value || [])
    .filter((o) => config.exteriorExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedInteriorRequiredNames = computed(() =>
  (interiorRequiredOptions.value || [])
    .filter((o) => config.interiorExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedInteriorOptionalNames = computed(() =>
  (interiorOptionalOptions.value || [])
    .filter((o) => config.interiorExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedPerformanceRequiredNames = computed(() =>
  (performanceRequiredOptions.value || [])
    .filter((o) => config.performanceExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedPerformanceOptionalNames = computed(() =>
  (performanceOptionalOptions.value || [])
    .filter((o) => config.performanceExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedTechRequiredNames = computed(() =>
  (techRequiredOptions.value || [])
    .filter((o) => config.techExtras.includes(o.id))
    .map((o) => o.name)
)

const selectedTechOptionalNames = computed(() =>
  (techOptionalOptions.value || [])
    .filter((o) => config.techExtras.includes(o.id))
    .map((o) => o.name)
)

// 当前车型对应的 3D 模型 UID
const currentModelUid = computed(() => currentVehicle.value?.modelUid || DEFAULT_MODEL_UID)

// 总价 = 车型价格 + 所有已选配置加价
const totalPrice = computed(() => {
  return basePrice.value + configExtrasPrice.value
})

// 外观配置：按必选 / 非必选拆分
const exteriorRequiredOptions = computed(() =>
  (exteriorOptions.value || []).filter((x) => x.required === 1 || x.required === true)
)
const exteriorOptionalOptions = computed(() =>
  (exteriorOptions.value || []).filter((x) => !x.required || x.required === 0)
)

// 内饰配置：按必选 / 非必选拆分
const interiorRequiredOptions = computed(() =>
  (interiorOptions.value || []).filter((x) => x.required === 1 || x.required === true)
)
const interiorOptionalOptions = computed(() =>
  (interiorOptions.value || []).filter((x) => !x.required || x.required === 0)
)

// 性能配置：按必选 / 非必选拆分
const performanceRequiredOptions = computed(() =>
  (performanceOptions.value || []).filter((x) => x.required === 1 || x.required === true)
)
const performanceOptionalOptions = computed(() =>
  (performanceOptions.value || []).filter((x) => !x.required || x.required === 0)
)

// 科技配置：按必选 / 非必选拆分
const techRequiredOptions = computed(() =>
  (techOptions.value || []).filter((x) => x.required === 1 || x.required === true)
)
const techOptionalOptions = computed(() =>
  (techOptions.value || []).filter((x) => !x.required || x.required === 0)
)

function pickColorHexByName(name) {
  if (!name) return '#1e3a5f'
  if (name.includes('红')) return '#b91c1c'
  if (name.includes('黑')) return '#111827'
  if (name.includes('白')) return '#f3f4f6'
  if (name.includes('灰')) return '#6b7280'
  if (name.includes('蓝')) return '#1d4ed8'
  return '#1e3a5f'
}

function addIdsWithRuleCheck(category, ids) {
  const arr = config[`${category}Extras`]
  if (!arr) return
  const toAdd = ids.filter((id) => !arr.includes(id))
  for (const id of toAdd) {
    arr.push(id)
    const result = checkConfigRule(getAllSelectedIds())
    if (!result.ok) {
      removeFromExtras(arr, id)
      alert(result.message)
      return
    }
  }
}

function toggleExteriorRequiredAll(checked) {
  const ids = exteriorRequiredOptions.value.map((o) => o.id)
  if (checked) {
    addIdsWithRuleCheck('exterior', ids)
  } else {
    config.exteriorExtras = config.exteriorExtras.filter((id) => !ids.includes(id))
  }
}

function toggleInteriorRequiredAll(checked) {
  const ids = interiorRequiredOptions.value.map((o) => o.id)
  if (checked) {
    addIdsWithRuleCheck('interior', ids)
  } else {
    config.interiorExtras = config.interiorExtras.filter((id) => !ids.includes(id))
  }
}

function togglePerformanceRequiredAll(checked) {
  const ids = performanceRequiredOptions.value.map((o) => o.id)
  if (checked) {
    addIdsWithRuleCheck('performance', ids)
  } else {
    config.performanceExtras = config.performanceExtras.filter((id) => !ids.includes(id))
  }
}

function toggleTechRequiredAll(checked) {
  const ids = techRequiredOptions.value.map((o) => o.id)
  if (checked) {
    addIdsWithRuleCheck('tech', ids)
  } else {
    config.techExtras = config.techExtras.filter((id) => !ids.includes(id))
  }
}

function getAllSelectedIds() {
  const ids = [
    ...config.exteriorExtras,
    ...config.interiorExtras,
    ...config.performanceExtras,
    ...config.techExtras
  ]
  return ids.map((id) => Number(id)).filter((n) => !Number.isNaN(n))
}

function getOptionName(id) {
  return optionNameMap.value.get(Number(id)) ??
    optionNameMap.value.get(String(id)) ??
    `选项${id}`
}

function checkConfigRule(selectedIds) {
  const set = new Set(selectedIds)
  const rules = configRules.value || []
  for (const r of rules) {
    const type = r.ruleType ?? r.type
    const a = r.itemAId ?? r.item_a_id ?? null
    const b = r.itemBId ?? r.item_b_id ?? null
    if (a == null || b == null) continue
    const aNum = Number(a)
    const bNum = Number(b)
    if (type === 'mutex') {
      if (set.has(aNum) && set.has(bNum)) {
        return { ok: false, message: `「${getOptionName(a)}」和「${getOptionName(b)}」互斥，不能同时选择` }
      }
    } else if (type === 'depend') {
      if (set.has(aNum) && !set.has(bNum)) {
        return { ok: false, message: `「${getOptionName(a)}」必须依赖于「${getOptionName(b)}」` }
      }
    }
  }
  return { ok: true }
}

function removeFromExtras(arr, optId) {
  const id = optId != null ? Number(optId) : null
  if (id == null) return
  const idx = arr.findIndex((x) => Number(x) === id)
  if (idx >= 0) arr.splice(idx, 1)
}

function onOptionToggle(category, opt, checked) {
  if (!checked) return true
  const arr = config[`${category}Extras`]
  if (!arr) return true
  const selected = getAllSelectedIds()
  const result = checkConfigRule(selected)
  if (!result.ok) {
    removeFromExtras(arr, opt.id)
    alert(result.message)
    return false
  }
  return true
}

function onExteriorOptionToggle(opt, checked) {
  if (checked && !onOptionToggle('exterior', opt, checked)) return


  // 如果是带颜色的外观项（有 hex），联动 3D 车漆颜色
  if (opt.hex) {
    if (checked) {
      config.exteriorColor = opt.name
    } else if (config.exteriorColor === opt.name) {
      // 取消当前颜色时，尝试切换到其他已选颜色；否则退回第一个颜色
      const selectedColor = exteriorOptions.value.find(
        (x) => x.hex && config.exteriorExtras.includes(x.id)
      )
      if (selectedColor) {
        config.exteriorColor = selectedColor.name
      } else if (exteriorColors.value.length) {
        config.exteriorColor = exteriorColors.value[0].name
      }
    }
  }

  // 如果是带轮毂缩略图的项（有 thumb），联动 3D 轮毂
  if (opt.thumb || String(opt.name || '').includes('轮毂')) {
    if (checked) {
      config.wheel = String(opt.id)
    } else if (String(config.wheel) === String(opt.id)) {
      // 取消当前轮毂时，尝试切换到其他已选轮毂；否则退回默认第一个轮毂
      const selectedWheel = exteriorOptions.value.find(
        (x) =>
          (x.thumb || String(x.name || '').includes('轮毂')) &&
          config.exteriorExtras.includes(x.id)
      )
      if (selectedWheel) {
        config.wheel = String(selectedWheel.id)
      } else if (wheelOptions.value.length) {
        config.wheel = wheelOptions.value[0].id
      }
    }
  }
}

// 当前外观颜色对应的 hex，用于 3D 模型颜色联动
const currentExteriorHex = computed(() => {
  const c = exteriorColors.value.find((x) => x.name === config.exteriorColor)
  return c ? c.hex : '#1e3a5f'
})

function buildSchemePayload() {
  const vehicle = currentVehicle.value

  const exteriorExtras = (exteriorOptions.value || [])
    .filter((o) => config.exteriorExtras.includes(o.id))
    .map((o) => ({ id: o.id, name: o.name, extraPrice: o.extraPrice || 0 }))

  const interiorExtras = (interiorOptions.value || [])
    .filter((o) => config.interiorExtras.includes(o.id))
    .map((o) => ({ id: o.id, name: o.name, extraPrice: o.extraPrice || 0 }))

  const performanceExtras = (performanceOptions.value || [])
    .filter((o) => config.performanceExtras.includes(o.id))
    .map((o) => ({ id: o.id, name: o.name, extraPrice: o.extraPrice || 0 }))

  const techExtras = (techOptions.value || [])
    .filter((o) => config.techExtras.includes(o.id))
    .map((o) => ({ id: o.id, name: o.name, extraPrice: o.extraPrice || 0 }))

  return {
    username: localStorage.getItem('username') || '',
    vehicleId: vehicle.id,
    vehicleName: vehicle.name,
    vehicleImg: vehicle.img,
    basePrice: basePrice.value,
    exteriorColor: config.exteriorColor,
    interiorColor: config.interiorColor,
    wheelId: config.wheel,
    totalPrice: totalPrice.value,
    exteriorExtras,
    interiorExtras,
    performanceExtras,
    techExtras,

    // 仅用于后端写入 scheme_option（方案-配件关联）
    exteriorItemIds: config.exteriorExtras,
    interiorItemIds: config.interiorExtras,
    performanceItemIds: config.performanceExtras,
    techItemIds: config.techExtras
  }
}

async function saveScheme() {
  if (!requireLoginOrRedirect()) return
  const payload = buildSchemePayload()
  // 进入长表单页面完善方案命名/是否分享/备注后再保存
  sessionStorage.setItem('pendingSchemePayload', JSON.stringify(payload))
  router.push('/scheme-save')
}

async function addSchemeToCart() {
  try {
    const payload = buildSchemePayload()
    // 购物车展示所需摘要信息
    payload.configSummary = payload.schemeName || `${payload.vehicleName} 个性化配置方案`
    payload.imageUrl = payload.vehicleImg
    await addToCart(payload)
    alert('已加入购物车')
    router.push('/cart')
  } catch (e) {
    console.error(e)
    alert(e.message || '加入购物车失败，请稍后重试')
  }
}

async function createOrder() {
  try {
    const payload = buildSchemePayload()
    await apiCreateOrder(payload)
    router.push('/orders')
  } catch (e) {
    console.error(e)
    alert(e.message || '下单失败，请稍后重试')
  }
}
</script>

<style scoped>
.configure-page {
  min-height: 100vh;
  background: #ffffff;
  color: #111827;
}

.configure-main {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 64px);
}

.configure-container {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 20px 72px;
}

.steps-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.steps-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.steps-flow {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #6b7280;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.step-item.active .step-index {
  background: #3b82f6;
  color: #ffffff;
}

.step-item.active .step-label {
  color: #111827;
}

.step-index {
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.step-label {
  font-size: 13px;
}

.step-divider {
  width: 40px;
  height: 1px;
  background: #374151;
}

.configure-layout {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  grid-template-rows: minmax(340px, 1fr) minmax(280px, 1fr);
  gap: 16px;
}

.panel-vehicles {
  grid-column: 1 / 4;
  grid-row: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-preview {
  grid-column: 4 / 7;
  grid-row: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-options {
  grid-column: 1 / 5;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-checklist {
  grid-column: 5 / 7;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 12px 14px 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
}

.vehicle-filters {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vehicle-search {
  width: 180px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  font-size: 12px;
  outline: none;
}

.vehicle-search:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.18);
}

.vehicle-price-filter {
  display: flex;
  align-items: center;
  gap: 4px;
}

.vehicle-price-input {
  width: 90px;
  padding: 6px 8px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  font-size: 12px;
  outline: none;
}

.vehicle-price-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.18);
}

.price-sep {
  font-size: 12px;
  color: #9ca3af;
}

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.vehicle-card {
  width: 100%;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.vehicle-card.active {
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.35);
}

.vehicle-card:hover {
  border-color: #3b82f6;
}

.vehicle-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.vehicle-info {
  padding: 8px 10px 10px;
}

.vehicle-info h4 {
  font-size: 14px;
  color: #111827;
  margin: 0 0 2px 0;
  line-height: 1.3;
}

.vehicle-price {
  font-size: 13px;
  color: #2563eb;
}

.vehicle-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  color: #4b5563;
}

.page-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.page-btn:hover:enabled {
  border-color: #3b82f6;
  color: #2563eb;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  min-width: 90px;
  text-align: center;
}

.options-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.options-tab {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.options-tab.active {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.08);
  color: #1f2937;
}

.options-body {
  margin-top: 4px;
}

.option-group {
  margin-bottom: 12px;
}

.option-group h4 {
  font-size: 12px;
  color: #374151;
  margin-bottom: 6px;
}

.color-swatches {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.color-swatch {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.color-swatch:hover {
  transform: translateY(-1px);
}

.color-swatch.active {
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.35);
}

.option-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

.exterior-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #374151;
}

.checkbox-option input {
  margin-right: 4px;
}

.checkbox-option.select-all {
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}

.option-name {
  flex: 1;
}

.option-price {
  font-size: 12px;
  color: #2563eb;
}

.wheel-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.wheel-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.wheel-btn.active {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.06);
}

.wheel-thumb {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.wheel-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.wheel-name {
  font-size: 13px;
  color: #111827;
}

.wheel-price {
  font-size: 12px;
  color: #2563eb;
}

.option-placeholder {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}

.panel-preview .preview-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin: 0 0 8px 0;
}

.panel-preview .preview-box {
  flex: 1;
  min-height: 220px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.panel-preview .preview-box :deep(.model-display) {
  height: 100%;
}

.checklist-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin: 0 0 8px 0;
}

.checklist-body {
  flex: 1;
  margin-bottom: 8px;
}

.checklist-item {
  font-size: 12px;
  color: #4b5563;
  margin: 4px 0;
}

.checklist-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
  font-size: 13px;
  color: #111827;
}

.checklist-total-value {
  font-size: 18px;
  font-weight: 600;
  color: #3b82f6;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 6px;
}

.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #1f2937;
}

.price-total-value {
  font-size: 18px;
  font-weight: 600;
  color: #3b82f6;
}

.price-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.btn-primary,
.btn-secondary,
.btn-text {
  font-size: 13px;
  border-radius: 999px;
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: #ffffff;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #ffffff;
  color: #111827;
  border: 1px solid #e5e7eb;
}

.btn-secondary:hover {
  border-color: #3b82f6;
}

.btn-text {
  background: transparent;
  color: #6b7280;
}

.btn-text:hover {
  color: #111827;
}

@media (max-width: 1100px) {
  .configure-layout {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto;
  }
  .panel-vehicles { grid-column: 1 / -1; }
  .panel-preview { grid-column: 1 / -1; }
  .panel-options { grid-column: 1 / -1; }
  .panel-checklist { grid-column: 1 / -1; }
  .vehicle-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .configure-container {
    padding: 16px 14px 72px;
  }
  .configure-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  .panel-vehicles,
  .panel-preview,
  .panel-options,
  .panel-checklist {
    grid-column: 1;
    grid-row: auto;
  }
  .vehicle-grid {
    grid-template-columns: 1fr;
  }
}
</style>
