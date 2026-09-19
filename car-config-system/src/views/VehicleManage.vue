<template>
  <div class="vehicle-manage-page">
    <Header />

    <main class="vehicle-manage-main">
      <div class="manage-container">
        <!-- 页面标题区 -->
        <section class="section title-section">
          <h1 class="page-title">车型管理</h1>
          <button class="btn-primary" @click="openModal()">
            <span class="btn-icon">+</span> 新增车型
          </button>
        </section>

        <!-- 顶部操作栏 -->
        <section class="section action-bar">
          <div class="action-left">
            <button class="btn-outline" @click="triggerImport">
              批量导入
            </button>
            <button class="btn-outline" @click="downloadTemplate">
              <span class="btn-icon">📥</span> 下载模板
            </button>
            <input
              ref="importInput"
              type="file"
              accept=".csv,.xlsx,.xls"
              class="sr-only"
              @change="onImportFileChange"
            />
          </div>
          <div class="view-toggle">
            <span class="toggle-label">视图切换：</span>
            <button
              class="toggle-btn"
              :class="{ active: viewMode === 'card' }"
              @click="viewMode = 'card'"
            >
              卡片
            </button>
            <button
              class="toggle-btn"
              :class="{ active: viewMode === 'table' }"
              @click="viewMode = 'table'"
            >
              表格
            </button>
          </div>
        </section>

        <!-- 筛选搜索区 -->
        <section class="section filter-bar">
          <div class="search-wrap">
            <span class="search-icon">🔍</span>
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="搜索车型..."
              class="search-input"
            />
          </div>
          <select v-model="filters.brand" class="filter-select">
            <option value="">品牌</option>
            <option v-for="b in brandOptions" :key="b" :value="b">{{ b }}</option>
          </select>
          <select v-model="filters.bodyType" class="filter-select">
            <option value="">车身类型</option>
            <option v-for="l in bodyTypeOptions" :key="l" :value="l">{{ l }}</option>
          </select>
          <select v-model="filters.powerType" class="filter-select">
            <option value="">动力类型</option>
            <option v-for="p in powerTypeOptions" :key="p" :value="p">{{ p }}</option>
          </select>
          <button class="btn-outline" @click="resetFilters">重置筛选</button>
        </section>

        <!-- 车型列表展示区 -->
        <section class="section list-section">
          <!-- 卡片视图 -->
          <div v-if="viewMode === 'card'" class="card-grid">
            <div
              v-for="v in paginatedList"
              :key="v.id"
              class="vehicle-card"
              @click="openModal(v)"
            >
              <div class="card-cover">
                <img :src="v.img" :alt="v.name" />
              </div>
              <div class="card-body">
                <h3 class="card-name">{{ v.name }}</h3>
                <p class="card-meta">{{ v.brand }} · {{ v.level }}</p>
                <p class="card-price">指导价 ¥{{ v.price.toLocaleString() }}</p>
                <div class="card-actions">
                  <button class="btn-link" @click.stop="openModal(v)">编辑</button>
                  <button class="btn-link danger" @click.stop="removeVehicle(v)">删除</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 表格视图 -->
          <div v-else class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>车型名称</th>
                  <th>品牌</th>
                  <th>车身类型</th>
                  <th>指导价</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="v in paginatedList" :key="v.id">
                  <td>
                    <div class="cell-name">
                      <img :src="v.img" :alt="v.name" class="table-thumb" />
                      {{ v.name }}
                    </div>
                  </td>
                  <td>{{ v.brand }}</td>
                  <td>{{ v.level }}</td>
                  <td>¥{{ v.price.toLocaleString() }}</td>
                  <td>
                    <button class="btn-link" @click="openModal(v)">编辑</button>
                    <button class="btn-link danger" @click="removeVehicle(v)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页控制区 -->
          <div class="pagination">
            <button class="page-btn" :disabled="page <= 1" @click="page = Math.max(1, page - 1)">
              &lt;
            </button>
            <template v-for="p in pageNumbers" :key="p">
              <button
                v-if="p !== '...'"
                class="page-btn"
                :class="{ active: page === p }"
                @click="page = p"
              >
                {{ p }}
              </button>
              <span v-else class="page-ellipsis">...</span>
            </template>
            <button
              class="page-btn"
              :disabled="page >= totalPages"
              @click="page = Math.min(totalPages, page + 1)"
            >
              &gt;
            </button>
            <span class="page-size">
              每页显示
              <select v-model.number="pageSize" class="page-size-select">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
              条
            </span>
          </div>
        </section>
      </div>
    </main>

    <!-- 车型详情/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="modalVisible" class="modal-overlay" @click.self="closeModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2 class="modal-title">
              {{ editingVehicle ? `编辑车型：${editingVehicle.name}` : '新增车型' }}
            </h2>
            <button class="modal-close" @click="closeModal">✕</button>
          </div>
          <div class="modal-body">
            <div class="tab-content">
              <div class="form-row">
                <label>车型名称</label>
                <input v-model="form.modelName" type="text" placeholder="如：奥迪 A6L" />
              </div>
              <div class="form-row">
                <label>品牌</label>
                <select v-model="form.brand">
                  <option value="">请选择</option>
                  <option v-for="b in brandOptions" :key="b" :value="b">{{ b }}</option>
                </select>
              </div>
              <div class="form-row">
                <label>指导价（元）</label>
                <input v-model.number="form.guidePrice" type="number" placeholder="如：428000" />
              </div>
              <div class="form-row">
                <label>生产年份</label>
                <input v-model.number="form.productionYear" type="number" placeholder="如：2025" />
              </div>
              <div class="form-row">
                <label>动力类型</label>
                <select v-model="form.powerType">
                  <option value="">请选择</option>
                  <option v-for="p in powerTypeOptions" :key="p" :value="p">{{ p }}</option>
                </select>
              </div>
              <div class="form-row">
                <label>车身类型</label>
                <select v-model="form.bodyType">
                  <option value="">请选择</option>
                  <option v-for="l in bodyTypeOptions" :key="l" :value="l">{{ l }}</option>
                </select>
              </div>
              <div class="form-row">
                <label>车型图片路径</label>
                <input v-model="form.carImage" type="text" placeholder="如：carmodel/1.jpeg" />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-outline" @click="closeModal">取消</button>
            <button class="btn-primary" @click="saveVehicle">保存</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getCarModels, createCarModel, updateCarModel, deleteCarModel, importCarModels } from '../api'

const viewMode = ref('card') // card | table
const modalVisible = ref(false)
const editingVehicle = ref(null)
const page = ref(1)
const pageSize = ref(20)

const brandOptions = ['奥迪', '宝马', '奔驰', '特斯拉', '比亚迪', '理想汽车', '蔚来', '丰田', '本田', '吉利']
const bodyTypeOptions = ['轿车', 'SUV', 'MPV']
const powerTypeOptions = ['燃油', '电动', '混动']

function toImgUrl(path) {
  if (!path) return 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400&q=80&fit=crop'
  if (path.startsWith('http')) return path
  return path.startsWith('/') ? path : `/${path}`
}

const filters = reactive({
  keyword: '',
  brand: '',
  bodyType: '',
  powerType: ''
})

const form = reactive({
  modelName: '',
  brand: '',
  guidePrice: '',
  productionYear: '',
  powerType: '',
  bodyType: '',
  carImage: ''
})

const vehicleList = ref([])

const loading = ref(false)
const error = ref('')

async function loadVehicles() {
  loading.value = true
  error.value = ''
  try {
    const list = await getCarModels()
    vehicleList.value = (Array.isArray(list) ? list : []).map((m) => ({
      id: m.modelId,
      name: m.modelName || m.name,
      brand: m.brand,
      level: m.bodyType,
      price: Number(m.guidePrice) || 0,
      productionYear: m.productionYear,
      powerType: m.powerType,
      standardCount: 0,
      status: 'on',
      img: toImgUrl(m.carImage),
      carImage: m.carImage
    }))
  } catch (e) {
    console.error(e)
    error.value = e.message || '车型数据加载失败'
    vehicleList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVehicles()
})

const filteredList = computed(() => {
  let list = vehicleList.value
  if (filters.keyword.trim()) {
    const k = filters.keyword.toLowerCase()
    list = list.filter(
      (v) =>
        v.name.toLowerCase().includes(k) ||
        v.brand.toLowerCase().includes(k) ||
        v.level.toLowerCase().includes(k)
    )
  }
  if (filters.brand) list = list.filter((v) => v.brand === filters.brand)
  if (filters.bodyType) list = list.filter((v) => v.level === filters.bodyType)
  if (filters.powerType) list = list.filter((v) => v.powerType === filters.powerType)
  return list
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredList.value.length / pageSize.value))
)
const paginatedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})
const pageNumbers = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  if (page.value <= 4) return [1, 2, 3, 4, 5, '...', total]
  if (page.value >= total - 3) return [1, '...', total - 4, total - 3, total - 2, total - 1, total]
  return [1, '...', page.value - 1, page.value, page.value + 1, '...', total]
})

function resetFilters() {
  filters.keyword = ''
  filters.brand = ''
  filters.bodyType = ''
  filters.powerType = ''
}

function openModal(vehicle = null) {
  editingVehicle.value = vehicle
  if (vehicle) {
    form.modelName = vehicle.name
    form.brand = vehicle.brand
    form.guidePrice = vehicle.price
    form.productionYear = vehicle.productionYear
    form.powerType = vehicle.powerType
    form.bodyType = vehicle.level
    form.carImage = vehicle.carImage || ''
  } else {
    form.modelName = ''
    form.brand = ''
    form.guidePrice = ''
    form.productionYear = new Date().getFullYear()
    form.powerType = ''
    form.bodyType = ''
    form.carImage = ''
  }
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  editingVehicle.value = null
}

async function saveVehicle() {
  const payload = {
    modelName: form.modelName,
    brand: form.brand,
    guidePrice: form.guidePrice,
    productionYear: form.productionYear,
    powerType: form.powerType,
    bodyType: form.bodyType,
    carImage: form.carImage || null
  }

  try {
    const id = editingVehicle.value && editingVehicle.value.id
    if (id) {
      await updateCarModel(id, payload)
    } else {
      await createCarModel(payload)
    }
    await loadVehicles()
    closeModal()
  } catch (e) {
    console.error(e)
    alert(e.message || '保存车型失败，请稍后重试')
  }
}

async function saveDraft() {
  await saveVehicle()
}

async function saveAndPublish() {
  await saveVehicle()
}

async function removeVehicle(v) {
  if (!window.confirm(`确定删除车型「${v.name}」吗？`)) return
  try {
    await deleteCarModel(v.id)
    await loadVehicles()
  } catch (e) {
    console.error(e)
    alert(e.message || '删除失败')
  }
}

// ===== 批量导入（Excel） =====
const importInput = ref(null)
const importing = ref(false)

function triggerImport() {
  if (importing.value) return
  importInput.value?.click()
}

async function onImportFileChange(e) {
  const file = e?.target?.files?.[0]
  if (!file) return
  try {
    importing.value = true
    const res = await importCarModels(file)
    const ok = Number(res.successCount || 0)
    const bad = Number(res.failedCount || 0)
    await loadVehicles()
    if (bad > 0) {
      const first = (res.errors || [])[0]
      alert(`导入完成：成功 ${ok} 条，失败 ${bad} 条。` + (first ? `\n第 ${first.row} 行：${first.message}` : ''))
    } else {
      alert(`导入成功：共 ${ok} 条`)
    }
  } catch (err) {
    console.error(err)
    alert(err?.message || '导入失败')
  } finally {
    importing.value = false
    // 允许重复选择同一文件
    if (importInput.value) importInput.value.value = ''
  }
}

function downloadTemplate() {
  const header = ['modelName(车型名称)', 'brand(品牌)', 'guidePrice(指导价)', 'productionYear(生产年份)', 'powerType(动力类型)', 'bodyType(车身类型)', 'carImage(图片路径)']
  const csv = header.join(',') + '\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'car_models_template.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.vehicle-manage-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.vehicle-manage-main {
  flex: 1;
  padding: 24px 0 40px;
}

.manage-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.section {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: #2563eb;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  color: #475569;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.btn-icon {
  font-size: 16px;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.action-left {
  display: flex;
  gap: 10px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.toggle-label {
  font-size: 13px;
  color: #64748b;
  margin-right: 8px;
}

.toggle-btn {
  padding: 6px 12px;
  font-size: 13px;
  color: #64748b;
  background: #f1f5f9;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  margin-left: 4px;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-wrap {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200px;
  max-width: 320px;
  height: 40px;
  padding: 0 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.search-icon {
  margin-right: 8px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

.filter-select {
  height: 40px;
  padding: 0 12px;
  font-size: 14px;
  color: #475569;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-width: 100px;
  cursor: pointer;
}

.list-section {
  min-height: 360px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.vehicle-card {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.vehicle-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.card-cover {
  position: relative;
  height: 140px;
  background: #e2e8f0;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
}

.card-status.on {
  background: #059669;
}

.card-status.draft {
  background: #64748b;
}

.card-body {
  padding: 14px;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.card-meta {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 4px 0;
}

.card-price {
  font-size: 14px;
  font-weight: 500;
  color: #2563eb;
  margin: 0 0 4px 0;
}

.card-count {
  font-size: 12px;
  color: #94a3b8;
  margin: 0 0 10px 0;
}

.card-actions {
  display: flex;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.btn-link {
  font-size: 13px;
  color: #2563eb;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #dc2626;
}

.table-wrap {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th,
.data-table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.data-table th {
  font-weight: 600;
  color: #475569;
  background: #f8fafc;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.cell-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-thumb {
  width: 48px;
  height: 32px;
  object-fit: cover;
  border-radius: 4px;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
}

.badge.on {
  background: #d1fae5;
  color: #059669;
}

.badge.off {
  background: #fee2e2;
  color: #dc2626;
}

.badge.draft {
  background: #f1f5f9;
  color: #64748b;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  font-size: 14px;
  color: #475569;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #2563eb;
  color: #2563eb;
}

.page-btn.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-ellipsis {
  padding: 0 4px;
  color: #94a3b8;
}

.page-size {
  margin-left: 24px;
  font-size: 13px;
  color: #64748b;
}

.page-size-select {
  margin: 0 6px;
  padding: 4px 8px;
  font-size: 13px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  cursor: pointer;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.modal-box {
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #64748b;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.modal-tabs {
  display: flex;
  gap: 4px;
  padding: 12px 20px 0;
  border-bottom: 1px solid #e2e8f0;
}

.modal-tab {
  padding: 10px 16px;
  font-size: 14px;
  color: #64748b;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-tab:hover {
  color: #1e293b;
}

.modal-tab.active {
  color: #2563eb;
  font-weight: 500;
  border-bottom-color: #2563eb;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.form-row input,
.form-row select {
  height: 40px;
  padding: 0 12px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.form-row input:focus,
.form-row select:focus {
  outline: none;
  border-color: #2563eb;
}

.tab-placeholder {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  padding: 24px 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}
</style>
