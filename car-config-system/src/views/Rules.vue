<template>
  <div class="rules-page">
    <Header />
    <main class="rules-main">
      <div class="container">
        <!-- 规则筛选 + 列表（统一表格布局） -->
        <section class="rules-card">
          <header class="rules-card-header">
            <h2 class="section-title">配置规则</h2>
            <div class="rules-toolbar">
              <label class="toolbar-label">
                车系
                <select v-model="filterSeries">
                  <option value="">全部</option>
                  <option v-for="s in seriesOptions" :key="s" :value="s">
                    {{ s }}
                  </option>
                </select>
              </label>
              <label class="toolbar-label">
                车型
                <select v-model="filterModel">
                  <option value="">全部</option>
                  <option
                    v-for="m in currentModelOptions"
                    :key="m"
                    :value="m"
                  >
                    {{ m }}
                  </option>
                </select>
              </label>
              <button class="btn-primary" @click="createRule">
                + 新建规则
              </button>
              <span class="rules-count">共 {{ filteredRules.length }} 条</span>
            </div>
          </header>
            <table class="rules-table">
              <thead>
                <tr>
                  <th>规则 ID</th>
                  <th>规则类型</th>
                  <th>车系 / 车型</th>
                  <th>配置项 A</th>
                  <th>配置项 B</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="rule in filteredRules" :key="rule.ruleId ?? rule.id">
                  <td>{{ rule.ruleId ?? rule.id }}</td>
                  <td>{{ typeLabelMap[rule.ruleType ?? rule.type] ?? rule.typeLabel ?? rule.ruleType }}</td>
                  <td>{{ rule.series ?? rule.brand }} / {{ rule.model ?? rule.modelName }}</td>
                  <td>{{ optionItemMap.get(Number(rule.itemAId ?? rule.itemA)) ?? rule.itemA ?? '-' }}</td>
                  <td>{{ optionItemMap.get(Number(rule.itemBId ?? rule.itemB)) ?? rule.itemB ?? '-' }}</td>
                  <td>
                    <span
                      class="badge"
                      :class="(rule.enabled !== false) ? 'badge-on' : 'badge-off'"
                    >
                      {{ (rule.enabled !== false) ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn-link" @click="editRule(rule)">编辑</button>
                    <button class="btn-link" @click="toggleRule(rule)">
                      {{ (rule.enabled !== false) ? '禁用' : '启用' }}
                    </button>
                    <button class="btn-link danger" @click="deleteRule(rule)">
                      删除
                    </button>
                  </td>
                </tr>
                <tr v-if="!filteredRules.length">
                  <td colspan="7" class="empty-cell">
                    当前条件下暂无规则。
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

        <!-- 规则新增 / 编辑表单弹窗 -->
        <div v-if="showRuleModal" class="modal-overlay">
          <div class="modal-box">
            <header class="modal-header">
              <h3 class="modal-title">
                {{ editingRuleId ? '编辑规则' : '新建规则' }}
              </h3>
              <button class="modal-close" @click="closeRuleModal">✕</button>
            </header>
            <div class="modal-body">
              <div class="form-row">
                <label>规则类型</label>
                <select v-model="ruleForm.type">
                  <option
                    v-for="opt in ruleTypeOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div class="form-row">
                <label>车系（品牌）</label>
                <select v-model="ruleForm.series">
                  <option value="">请选择</option>
                  <option v-for="s in seriesOptions" :key="s" :value="s">
                    {{ s }}
                  </option>
                </select>
              </div>
              <div class="form-row">
                <label>车型</label>
                <select v-model="ruleForm.model">
                  <option value="">请选择</option>
                  <option
                    v-for="m in ruleFormModelOptions"
                    :key="m"
                    :value="m"
                  >
                    {{ m }}
                  </option>
                </select>
              </div>
              <div class="form-row">
                <label>搜索配置项</label>
                <input
                  v-model="optionItemSearch"
                  type="text"
                  placeholder="输入名称筛选（如：车漆、座椅）"
                />
              </div>
              <div class="form-row">
                <label>配置项 A</label>
                <select v-model="ruleForm.itemAId">
                  <option value="">请选择配置项</option>
                  <option
                    v-for="o in filteredOptionItems"
                    :key="o.id"
                    :value="o.id"
                  >
                    {{ o.name }} (¥{{ (o.price ?? 0).toLocaleString() }})
                  </option>
                </select>
              </div>
              <div class="form-row">
                <label>配置项 B</label>
                <select v-model="ruleForm.itemBId">
                  <option value="">请选择配置项</option>
                  <option
                    v-for="o in filteredOptionItems"
                    :key="'b-' + o.id"
                    :value="o.id"
                  >
                    {{ o.name }} (¥{{ (o.price ?? 0).toLocaleString() }})
                  </option>
                </select>
              </div>
              <div class="form-row checkbox-row">
                <label class="checkbox-label">
                  <input v-model="ruleForm.enabled" type="checkbox" />
                  启用
                </label>
              </div>
            </div>
            <footer class="modal-footer">
              <button class="btn-secondary" @click="closeRuleModal">
                取消
              </button>
              <button class="btn-primary" @click="submitRule">
                保存
              </button>
            </footer>
          </div>
        </div>

        <!-- 分类标签页 -->
        <nav class="category-tabs">
          <button
            v-for="tab in categoryTabs"
            :key="tab.id"
            class="category-tab"
            :class="{ active: activeCategory === tab.id }"
            @click="activeCategory = tab.id"
          >
            {{ tab.label }}
          </button>
        </nav>

        <!-- 配置项 / 配件管理（统一表格布局） -->
        <section class="access-list-card">
          <header class="access-header">
            <h2 class="section-title">配置项管理 - {{ currentCategoryLabel }}</h2>
            <div class="rules-toolbar">
              <button class="btn-primary" @click="createAccessory">
                + 新增配件
              </button>
              <span class="rules-count">共 {{ filteredAccessories.length }} 项</span>
            </div>
          </header>
          <table class="access-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>名称</th>
                <th>种类</th>
                <th>库存数量</th>
                <th>价格</th>
                <th>必选项</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="acc in filteredAccessories" :key="acc.id">
                <td>{{ acc.id }}</td>
                <td>{{ acc.name }}</td>
                <td>{{ categoryLabel(acc.category) }}</td>
                <td>{{ acc.stockQty ?? 0 }}</td>
                <td>¥{{ acc.price.toLocaleString() }}</td>
                <td>{{ acc.required ? '是' : '否' }}</td>
                <td>
                  <button class="btn-link" @click="editAccessory(acc)">
                    编辑
                  </button>
                  <button class="btn-link danger" @click="deleteAccessory(acc)">
                    删除
                  </button>
                </td>
              </tr>
              <tr v-if="!filteredAccessories.length">
                <td colspan="7" class="empty-cell">
                  当前分类下暂无配置项。
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <!-- 配件新增 / 编辑表单弹窗 -->
        <div v-if="showAccessoryModal" class="modal-overlay">
          <div class="modal-box">
            <header class="modal-header">
              <h3 class="modal-title">
                {{ editingAccessoryId ? '编辑配件' : '新增配件' }}
              </h3>
              <button class="modal-close" @click="closeAccessoryModal">✕</button>
            </header>
            <div class="modal-body">
              <div class="form-row">
                <label>名称</label>
                <input
                  v-model="accessoryForm.name"
                  type="text"
                  placeholder="请输入配件名称"
                />
              </div>
              <div class="form-row">
                <label>种类</label>
                <select v-model="accessoryForm.category">
                  <option
                    v-for="tab in categoryTabs"
                    :key="tab.id"
                    :value="tab.id"
                  >
                    {{ tab.label }}
                  </option>
                </select>
              </div>
              <div class="form-row">
                <label>库存数量</label>
                <input
                  v-model.number="accessoryForm.stockQty"
                  type="number"
                  min="0"
                  placeholder="请输入库存数量"
                />
              </div>
              <div class="form-row">
                <label>价格（元）</label>
                <input
                  v-model.number="accessoryForm.price"
                  type="number"
                  min="0"
                  placeholder="请输入价格"
                />
              </div>
              <div class="form-row checkbox-row">
                <label class="checkbox-label">
                  <input
                    v-model="accessoryForm.required"
                    type="checkbox"
                  />
                  是否为必选项
                </label>
              </div>
            </div>
            <footer class="modal-footer">
              <button class="btn-secondary" @click="closeAccessoryModal">
                取消
              </button>
              <button class="btn-primary" @click="submitAccessory">
                保存
              </button>
            </footer>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import {
  getAccessories,
  createAccessory as apiCreateAccessory,
  updateAccessory as apiUpdateAccessory,
  deleteAccessory as apiDeleteAccessory,
  getConfigRulesMeta,
  getConfigRules,
  createConfigRule,
  updateConfigRule,
  deleteConfigRule,
  getOptionItems
} from '../api'

// 下拉数据（来自数据库 car_model + option_item）
const meta = ref({ brands: [], modelsByBrand: {}, optionItems: [] })
const metaLoading = ref(false)

const seriesOptions = computed(() => meta.value.brands || [])
const modelMap = computed(() => meta.value.modelsByBrand || {})
const optionItems = ref([])
const optionItemSearch = ref('')

const filteredOptionItems = computed(() => {
  const list = optionItems.value || []
  const kw = (optionItemSearch.value || '').trim().toLowerCase()
  if (!kw) return list
  return list.filter((o) => (o.name || '').toLowerCase().includes(kw))
})

const filterSeries = ref('')
const filterModel = ref('')

const currentModelOptions = computed(() => {
  if (!filterSeries.value) return []
  return modelMap.value[filterSeries.value] || []
})

// 规则列表（来自数据库 config_rule）
const rules = ref([])
const rulesLoading = ref(false)

const filteredRules = computed(() =>
  rules.value.filter((r) => {
    const series = r.series ?? r.brand
    const model = r.model ?? r.modelName
    if (filterSeries.value && series !== filterSeries.value) return false
    if (filterModel.value && model !== filterModel.value) return false
    return true
  })
)

// 配置项名称映射（用于表格展示）
const optionItemMap = computed(() => {
  const map = new Map()
  for (const o of optionItems.value) {
    if (o.id != null) map.set(Number(o.id), o.name || '')
    if (o.itemId != null) map.set(Number(o.itemId), o.name || '')
  }
  return map
})

// 规则类型选项（与表格「规则类型」列对应）
const ruleTypeOptions = [
  { value: 'mutex', label: '互斥' },
  { value: 'depend', label: '依赖' },
  { value: 'compatible', label: '兼容' }
]

const typeLabelMap = { mutex: '互斥', depend: '依赖', compatible: '兼容' }

// 规则表单弹窗
const showRuleModal = ref(false)
const editingRuleId = ref(null)
const ruleForm = ref({
  type: 'mutex',
  series: '',
  model: '',
  itemAId: null,
  itemBId: null,
  enabled: true
})

const ruleFormModelOptions = computed(() => {
  if (!ruleForm.value.series) return []
  return modelMap.value[ruleForm.value.series] || []
})

function resetRuleForm() {
  ruleForm.value = {
    type: 'mutex',
    series: '',
    model: '',
    itemAId: null,
    itemBId: null,
    enabled: true
  }
  optionItemSearch.value = ''
}

function createRule() {
  editingRuleId.value = null
  resetRuleForm()
  showRuleModal.value = true
}

function editRule(rule) {
  editingRuleId.value = rule.ruleId ?? rule.id
  ruleForm.value = {
    type: rule.ruleType ?? rule.type ?? 'mutex',
    series: rule.series ?? rule.brand ?? '',
    model: rule.model ?? rule.modelName ?? '',
    itemAId: rule.itemAId ?? rule.itemA ?? null,
    itemBId: rule.itemBId ?? rule.itemB ?? null,
    enabled: rule.enabled !== false
  }
  showRuleModal.value = true
}

function closeRuleModal() {
  showRuleModal.value = false
}

async function submitRule() {
  const f = ruleForm.value
  const itemAId = (f.itemAId != null && f.itemAId !== '') ? Number(f.itemAId) : null
  const itemBId = (f.itemBId != null && f.itemBId !== '') ? Number(f.itemBId) : null
  if (!itemAId || !itemBId) {
    alert('请选择配置项 A 和配置项 B')
    return
  }
  const payload = {
    ruleType: f.type,
    brand: f.series || undefined,
    modelName: f.model || undefined,
    itemAId,
    itemBId,
    enabled: !!f.enabled
  }
  try {
    if (editingRuleId.value) {
      await updateConfigRule(editingRuleId.value, payload)
    } else {
      await createConfigRule(payload)
    }
    showRuleModal.value = false
    await loadRules()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function toggleRule(rule) {
  const id = rule.ruleId ?? rule.id
  const newEnabled = !(rule.enabled !== false)
  try {
    await updateConfigRule(id, {
      ruleType: rule.ruleType ?? rule.type,
      itemAId: rule.itemAId ?? rule.itemA,
      itemBId: rule.itemBId ?? rule.itemB,
      brand: rule.series ?? rule.brand,
      modelName: rule.model ?? rule.modelName,
      enabled: newEnabled
    })
    await loadRules()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

async function deleteRule(rule) {
  if (!window.confirm('确定删除该规则吗？')) return
  const id = rule.ruleId ?? rule.id
  try {
    await deleteConfigRule(id)
    await loadRules()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

// 分类标签 & 配件列表
const categoryTabs = [
  { id: 'exterior', label: '外观' },
  { id: 'interior', label: '内饰' },
  { id: 'tech', label: '科技' },
  { id: 'performance', label: '性能' }
]

const activeCategory = ref('exterior')

// 配件从后端 / 数据库读取
const accessories = ref([])
const accessoriesLoading = ref(false)
const accessoriesError = ref('')

async function loadMeta() {
  metaLoading.value = true
  try {
    const data = await getConfigRulesMeta()
    meta.value = data
    optionItems.value = data.optionItems || []
  } catch (e) {
    console.error(e)
  } finally {
    metaLoading.value = false
  }
}

async function loadRules() {
  rulesLoading.value = true
  try {
    const list = await getConfigRules()
    rules.value = list
  } catch (e) {
    console.error(e)
  } finally {
    rulesLoading.value = false
  }
}

async function loadAccessories() {
  accessoriesLoading.value = true
  accessoriesError.value = ''
  try {
    const data = await getAccessories()
    accessories.value = Array.isArray(data) ? data : data.items || []
  } catch (e) {
    console.error(e)
    accessoriesError.value = e.message || '配件数据加载失败，请稍后重试'
  } finally {
    accessoriesLoading.value = false
  }
}

onMounted(() => {
  loadMeta()
  loadRules()
  loadAccessories()
})

const filteredAccessories = computed(() =>
  accessories.value.filter((a) => a.category === activeCategory.value)
)

const currentCategoryLabel = computed(
  () => categoryTabs.find((t) => t.id === activeCategory.value)?.label || ''
)

// 分类中文名
function categoryLabel(category) {
  return categoryTabs.find((t) => t.id === category)?.label || category
}

// 配件表单弹窗
const showAccessoryModal = ref(false)
const editingAccessoryId = ref(null)
const accessoryForm = ref({
  name: '',
  category: 'exterior',
  stockQty: 0,
  price: 0,
  required: false
})

function resetAccessoryForm() {
  accessoryForm.value = {
    name: '',
    category: activeCategory.value,
    stockQty: 0,
    price: 0,
    required: false
  }
}

function createAccessory() {
  editingAccessoryId.value = null
  resetAccessoryForm()
  showAccessoryModal.value = true
}

function editAccessory(acc) {
  editingAccessoryId.value = acc.id
  accessoryForm.value = {
    name: acc.name,
    category: acc.category || activeCategory.value,
    stockQty: acc.stockQty ?? 0,
    price: acc.price ?? 0,
    required: !!acc.required
  }
  showAccessoryModal.value = true
}

function closeAccessoryModal() {
  showAccessoryModal.value = false
}

async function submitAccessory() {
  const payload = {
    name: accessoryForm.value.name,
    category: accessoryForm.value.category,
    stockQty: accessoryForm.value.stockQty ?? 0,
    price: accessoryForm.value.price ?? 0,
    required: !!accessoryForm.value.required
  }

  try {
    if (editingAccessoryId.value) {
      await apiUpdateAccessory(editingAccessoryId.value, payload)
    } else {
      await apiCreateAccessory(payload)
    }
    showAccessoryModal.value = false
    await loadAccessories()
  } catch (e) {
    console.error(e)
    alert(e.message || '保存配件失败')
  }
}

function deleteAccessory(acc) {
  if (!window.confirm(`确定删除配件「${acc.name}」吗？`)) return
  apiDeleteAccessory(acc.id)
    .then(() => loadAccessories())
    .catch((e) => console.error(e))
}
</script>

<style scoped>
.rules-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.rules-main {
  flex: 1;
  padding: 40px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.rules-card,
.access-list-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 14px 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.form-group label {
  font-size: 13px;
  color: #4b5563;
}

.form-group select {
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 14px;
}

.btn-primary {
  background: #2563eb;
  color: #ffffff;
  border: none;
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.full {
  width: 100%;
  text-align: center;
}

.rules-card-header,
.access-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.rules-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4b5563;
}

.toolbar-label select {
  height: 32px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 13px;
}

.rules-count {
  font-size: 13px;
  color: #6b7280;
}

.rules-table,
.access-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.rules-table th,
.rules-table td,
.access-table th,
.access-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

.rules-table th,
.access-table th {
  background: #f8fafc;
  font-weight: 500;
  color: #475569;
}

.rules-table tbody tr:hover,
.access-table tbody tr:hover {
  background: #f9fafb;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.badge-on {
  background: #dcfce7;
  color: #166534;
}

.badge-off {
  background: #fee2e2;
  color: #b91c1c;
}

.badge-warn {
  background: #fef3c7;
  color: #92400e;
}

.btn-link {
  background: transparent;
  border: none;
  padding: 0;
  margin-right: 8px;
  font-size: 13px;
  color: #2563eb;
  cursor: pointer;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #dc2626;
}

.empty-cell {
  padding: 16px 10px;
  text-align: center;
  color: #9ca3af;
}

.category-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.category-tab {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: transparent;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.category-tab:hover {
  color: #111827;
}

.category-tab.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.35);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.modal-close {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
}

.modal-close:hover {
  background: #f1f5f9;
  color: #111827;
}

.modal-body {
  padding: 14px 16px 4px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.form-row label {
  font-size: 13px;
  color: #4b5563;
}

.form-row input,
.form-row select {
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 14px;
}

.checkbox-row {
  margin-top: 4px;
}

.checkbox-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4b5563;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 10px 16px 12px;
  border-top: 1px solid #e2e8f0;
  background: #f9fafb;
}

.btn-secondary {
  background: #ffffff;
  color: #111827;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
}

.btn-secondary:hover {
  border-color: #2563eb;
  color: #2563eb;
}
</style>

