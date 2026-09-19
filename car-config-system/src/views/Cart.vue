<template>
  <div class="cart-page">
    <Header />
    <main class="cart-main">
      <div class="container">
        <!-- 标题 + 标签 -->
        <header class="cart-header">
          <h1 class="cart-title">我的方案与购物车</h1>
          <nav class="cart-tabs">
            <button
              class="cart-tab"
              :class="{ active: activeTab === 'schemes' }"
              @click="activeTab = 'schemes'"
            >
              我的方案
            </button>
            <button
              class="cart-tab"
              :class="{ active: activeTab === 'cart' }"
              @click="activeTab = 'cart'"
            >
              购物车
              <span v-if="itemsCount">({{ itemsCount }})</span>
            </button>
          </nav>
        </header>

        <!-- 我的方案 Tab -->
        <section v-if="activeTab === 'schemes'">
          <section v-if="schemesLoading" class="state-box">
            正在加载我的方案...
          </section>
          <section v-else-if="schemesError" class="state-box state-error">
            {{ schemesError }}
          </section>
          <section v-else-if="!schemes.length" class="state-box state-empty">
            <p class="empty-text">
              还没有保存任何配置方案，先去「在线选装」中保存一个吧。
            </p>
            <button class="btn-primary" @click="goConfigure">开始选配</button>
          </section>
          <section v-else class="schemes-list">
            <article
              v-for="scheme in schemes"
              :key="scheme.schemeId"
              class="scheme-item"
            >
              <div class="scheme-main">
                <h2 class="scheme-title">{{ scheme.schemeName }}</h2>
                <p class="scheme-meta">
                  车型：{{ scheme.modelName || '未指定车型' }} ·
                  保存时间：{{ scheme.createdAt || '-' }}
                </p>
                <p class="scheme-desc" v-if="scheme.description">
                  {{ scheme.description }}
                </p>
              </div>
              <div class="scheme-side">
                <div class="scheme-price">
                  方案总价：
                  <span class="scheme-price-value">
                    ¥{{ (scheme.totalPrice || 0).toLocaleString() }}
                  </span>
                </div>
                <div class="scheme-actions">
                  <button class="btn-secondary" @click="viewSchemeDetail(scheme)">
                    查看详情
                  </button>
                  <button class="btn-secondary" @click="editScheme(scheme)">
                    编辑
                  </button>
                  <button class="btn-secondary" @click="addSchemeToCart(scheme)">
                    加入购物车
                  </button>
                  <button class="btn-secondary" @click="toggleShareScheme(scheme)">
                    {{ scheme.isPublic ? '取消分享' : '分享' }}
                  </button>
                  <button class="btn-text danger" @click="deleteScheme(scheme)">
                    删除方案
                  </button>
                </div>
              </div>
            </article>
          </section>
        </section>

        <!-- 购物车 Tab -->
        <section v-else>
          <!-- 加载 / 错误状态 -->
          <section v-if="cartLoading" class="state-box">
            正在加载购物车数据...
          </section>
          <section v-else-if="cartError" class="state-box state-error">
            {{ cartError }}
          </section>

          <!-- 正常内容 -->
          <section v-else>
            <section v-if="items.length" class="cart-content">
            <!-- 商品列表 -->
            <div class="cart-list">
              <article
                v-for="item in items"
                :key="item.id"
                class="cart-item"
              >
                <div class="item-select">
                  <input
                    type="checkbox"
                    v-model="selectedIds"
                    :value="item.id"
                  />
                </div>
                <div class="item-info">
                  <h2 class="item-title">{{ item.vehicleName }}</h2>
                  <p class="item-config">
                    配置方案：{{ item.configSummary }}
                  </p>
                  <p class="item-meta">
                    预计交付时间：{{ item.estimatedDelivery || summary.estimatedDelivery || '以门店确认为准' }}
                  </p>
                  <div class="item-footer">
                    <div class="item-price">
                      总价：
                      <span class="item-price-value">
                        ¥{{ (item.totalPrice || 0).toLocaleString() }}
                      </span>
                    </div>
                    <div class="item-actions">
                      <button class="btn-secondary" @click="editItem(item)">
                        编辑配置
                      </button>
                      <button class="btn-text danger" @click="removeItem(item)">
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            </div>

            <!-- 结算区 -->
            <aside class="cart-summary">
              <h2 class="summary-title">结算信息</h2>
              <div class="summary-row">
                <span class="label">商品总计</span>
                <span class="value">¥{{ payAmount.toLocaleString() }}</span>
              </div>
              <div class="summary-row">
                <span class="label">预计交付时间</span>
                <span class="value">
                  {{ summary.estimatedDelivery || '以门店最终通知为准' }}
                </span>
              </div>

              <div class="summary-actions">
                <button class="btn-secondary full" @click="goConfigure">
                  继续选配
                </button>
                <button class="btn-primary full" @click="goPay">
                  支付
                </button>
              </div>
            </aside>
          </section>

            <!-- 空购物车 -->
            <section v-else class="state-box state-empty">
              <p class="empty-text">
                购物车中还没有商品，先去配置一辆心仪的车型吧。
              </p>
              <button class="btn-primary" @click="goConfigure">开始选配</button>
            </section>
          </section>
        </section>
      </div>
    </main>
    <!-- 支付二维码弹窗（居中） -->
    <div v-if="showPayDialog" class="pay-dialog-mask">
      <div class="pay-dialog">
        <h2 class="pay-title">扫码支付</h2>
        <p class="pay-subtitle">
          请使用微信或支付宝扫码完成支付，系统会自动确认支付结果
        </p>
        <div class="pay-qrcodes">
          <div class="pay-channel">
            <div class="pay-channel-title">微信支付</div>
            <div class="pay-qrcode-box">
              <img
                class="pay-qrcode-img"
                src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=weixin://wxpay"
                alt="微信支付二维码"
              />
            </div>
          </div>
          <div class="pay-channel">
            <div class="pay-channel-title">支付宝支付</div>
            <div class="pay-qrcode-box">
              <img
                class="pay-qrcode-img"
                src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=alipays://platformapi/startapp"
                alt="支付宝支付二维码"
              />
            </div>
          </div>
        </div>
        <p class="pay-status-text">
          {{ paying ? '正在确认支付，请稍候...' : '等待支付中，请在手机上完成支付' }}
        </p>
        <div class="pay-actions">
          <button class="btn-secondary" @click="cancelPay" :disabled="paying">
            取消
          </button>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { getCart, addToCart, removeCartItem, getCustomSchemes, shareCustomScheme, unshareCustomScheme, createOrder } from '../api'

const router = inject('router')

const activeTab = ref('cart')

// 我的方案列表
const schemes = ref([])
const schemesLoading = ref(false)
const schemesError = ref('')

// 购物车列表数据（从后端 / 数据库获取）
const items = ref([])
// 结算摘要信息（总价、整体预计交付时间等，来自后端）
const summary = ref({
  totalAmount: 0,
  estimatedDelivery: ''
})
const cartLoading = ref(false)
const cartError = ref('')

const itemsCount = computed(() => items.value.length)

// 勾选的购物车项 ID
const selectedIds = ref([])

// 支付弹窗相关状态
const showPayDialog = ref(false)
const paying = ref(false)

// 支付轮询 / 定时句柄
let payTimer = null

function isLoggedIn() {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  return !!(token || username)
}

// 商品总价：优先用后端 summary.totalAmount，没有则前端求和
const totalAmount = computed(() => {
  if (summary.value && typeof summary.value.totalAmount === 'number') {
    return summary.value.totalAmount
  }
  return items.value.reduce(
    (sum, item) => sum + (item.totalPrice || 0),
    0
  )
})

// 结算金额：严格等于“当前勾选商品”的总价；未勾选则视为 0
const payAmount = computed(() => {
  if (!selectedIds.value.length) return 0
  const idSet = new Set(selectedIds.value)
  return items.value.reduce((sum, item) => {
    if (!idSet.has(item.id)) return sum
    return sum + (item.totalPrice || 0)
  }, 0)
})

async function loadCart() {
  cartLoading.value = true
  cartError.value = ''
  try {
    const { items: its, summary: sum } = await getCart()
    items.value = its || []
    summary.value = sum || {
      totalAmount: 0,
      estimatedDelivery: ''
    }
  } catch (e) {
    console.error(e)
    cartError.value = e.message || '购物车数据加载失败，请稍后重试'
  } finally {
    cartLoading.value = false
  }
}

async function loadSchemes() {
  schemesLoading.value = true
  schemesError.value = ''
  try {
    const username = localStorage.getItem('username') || ''
    if (!username) {
      // “我的方案”只展示自己创建的方案，需要登录态
      schemes.value = []
      schemesError.value = '请先登录后查看自己的方案'
      return
    }

    const list = await getCustomSchemes({ username })
    schemes.value = (Array.isArray(list) ? list : []).map((raw) => ({
      schemeId: raw.schemeId ?? raw.scheme_id ?? raw.id,
      schemeName: raw.schemeName ?? raw.scheme_name ?? '未命名方案',
      modelName: raw.modelName ?? raw.model_name ?? '',
      totalPrice: raw.totalPrice ?? raw.total_price ?? 0,
      createdAt: raw.createdAt ?? raw.createTime ?? raw.create_time ?? '',
      isPublic: !!(raw.isPublic ?? raw.is_public),
      description:
        raw.shareDescription ??
        raw.share_description ??
        raw.description ??
        ''
    }))
  } catch (e) {
    console.error(e)
    schemesError.value = e?.message || '方案数据加载失败，请稍后重试'
  } finally {
    schemesLoading.value = false
  }
}

onMounted(() => {
  if (window.location.hash === '#schemes') {
    activeTab.value = 'schemes'
  }
  loadSchemes()
  loadCart()
})

function goConfigure() {
  router.push('/configure')
}

function goPay() {
  if (!selectedIds.value.length) {
    alert('请先选择至少一个要支付的商品')
    return
  }
  const idSet = new Set(selectedIds.value)
  const selectedItems = items.value.filter((it) => idSet.has(it.id))
  if (!selectedItems.length) {
    alert('请选择有效的商品')
    return
  }
  showPayDialog.value = true
  // 打开弹窗后自动进入“支付确认中”，模拟由系统自动判断支付结果
  paying.value = true
  if (payTimer) {
    clearTimeout(payTimer)
    payTimer = null
  }
  // 这里用定时器模拟“系统检查支付状态”，真实环境可改为轮询后端支付状态接口
  payTimer = setTimeout(() => {
    confirmPay()
  }, 3000)
}

async function confirmPay() {
  if (!selectedIds.value.length) return
  const idSet = new Set(selectedIds.value)
  const selectedItems = items.value.filter((it) => idSet.has(it.id))
  if (!selectedItems.length) return
  paying.value = true
  try {
    await createOrder({
      username: localStorage.getItem('username') || '',
      items: selectedItems,
      totalAmount: payAmount.value
    })
    alert('支付成功，已生成订单')
    // 不再删除购物车中的商品，仅清空本次勾选
    selectedIds.value = []
    showPayDialog.value = false
    router.push('/orders')
  } catch (e) {
    console.error(e)
    cartError.value = e?.message || '下单失败，请稍后重试'
  } finally {
    paying.value = false
    if (payTimer) {
      clearTimeout(payTimer)
      payTimer = null
    }
  }
}

function cancelPay() {
  if (paying.value) return
  if (payTimer) {
    clearTimeout(payTimer)
    payTimer = null
  }
  showPayDialog.value = false
}


function editItem(item) {
  // 使用方案 ID 进入方案详情页的编辑模式，直接看到车型与可编辑配置
  const schemeId = item.schemeId
  if (!schemeId) {
    router.push('/configure')
    return
  }
  router.push(`/scheme-detail?id=${schemeId}&edit=1`)
}

async function removeItem(item) {
  // 先本地删除以保证交互流畅，再尝试通知后端删除
  items.value = items.value.filter((x) => x.id !== item.id)

  try {
    await removeCartItem(item.id)
    // 删除成功后刷新汇总信息
    await loadCart()
  } catch (e) {
    console.error(e)
    // 简单提示，实际可根据后端返回做更细致的处理
    cartError.value = e.message || '删除失败，请刷新后重试'
  }
}

function viewSchemeDetail(scheme) {
  const id = scheme.schemeId
  if (!id) return
  router.push(`/scheme-detail?id=${id}`)
}

function editScheme(scheme) {
  const id = scheme.schemeId
  if (!id) return
  router.push(`/scheme-detail?id=${id}&edit=1`)
}

async function addSchemeToCart(scheme) {
  const payload = {
    schemeId: scheme.schemeId,
    schemeName: scheme.schemeName,
    totalPrice: scheme.totalPrice,
    modelName: scheme.modelName
  }
  try {
    await addToCart(payload)
    await loadCart()
    activeTab.value = 'cart'
  } catch (e) {
    console.error(e)
    cartError.value = e.message || '加入购物车失败，请稍后重试'
  }
}

async function deleteScheme(scheme) {
  if (!window.confirm(`确定删除方案「${scheme.schemeName}」吗？`)) return
  try {
    const id = scheme.schemeId
    const res = await fetch(`/api/custom-schemes/${id}`, { method: 'DELETE' })
    const result = await res.json().catch(() => ({}))
    if (!res.ok || (result && result.success === false) || (typeof (result && result.code) === 'number' && result.code !== 200)) {
      throw new Error((result && result.msg) || '删除方案失败')
    }
    await loadSchemes()
  } catch (e) {
    console.error(e)
    schemesError.value = e.message || '删除方案失败，请稍后重试'
  }
}

async function toggleShareScheme(scheme) {
  const id = scheme?.schemeId
  if (!id) return
  try {
    if (scheme.isPublic) {
      if (!window.confirm(`确定取消分享方案「${scheme.schemeName}」吗？`)) return
      await unshareCustomScheme(id)
    } else {
      await shareCustomScheme(id)
    }
    await loadSchemes()
  } catch (e) {
    console.error(e)
    schemesError.value = e?.message || '操作失败，请稍后重试'
  }
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.cart-main {
  flex: 1;
  padding: 40px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.cart-header {
  margin-bottom: 20px;
}

.cart-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.cart-tabs {
  margin-top: 12px;
  display: inline-flex;
  gap: 8px;
  padding: 4px;
  background: #e5e7eb;
  border-radius: 999px;
}

.cart-tab {
  border: none;
  background: transparent;
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 999px;
  cursor: pointer;
  color: #4b5563;
  transition: all 0.2s;
}

.cart-tab.active {
  background: #2563eb;
  color: #ffffff;
}

.cart-title span {
  font-size: 18px;
  color: #64748b;
  margin-left: 4px;
}

.cart-content {
  display: grid;
  grid-template-columns: minmax(0, 3fr) minmax(260px, 1.4fr);
  gap: 20px;
  align-items: flex-start;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cart-item {
  display: flex;
  gap: 16px;
  padding: 14px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.item-select {
  display: flex;
  align-items: center;
  padding-right: 4px;
}

.item-image {
  width: 160px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #e5e7eb;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.item-config {
  font-size: 13px;
  color: #4b5563;
  margin: 2px 0 4px 0;
}

.item-meta {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.item-footer {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.item-price {
  font-size: 13px;
  color: #4b5563;
}

.item-price-value {
  font-size: 18px;
  font-weight: 600;
  color: #2563eb;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.cart-summary {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 18px 16px 16px;
}

.summary-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px dashed #e5e7eb;
}

.summary-row:last-of-type {
  border-bottom: none;
  margin-bottom: 8px;
}

.summary-row .label {
  color: #6b7280;
}

.summary-row .value {
  color: #111827;
  font-weight: 500;
}

.summary-actions {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-primary,
.btn-secondary,
.btn-text {
  font-size: 14px;
  border-radius: 999px;
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: #ffffff;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-secondary {
  background: #ffffff;
  color: #111827;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.btn-text {
  background: transparent;
  color: #64748b;
  padding-inline: 0;
}

.btn-text:hover {
  color: #111827;
}

.btn-text.danger {
  color: #dc2626;
}

.btn-text.danger:hover {
  color: #b91c1c;
}

.full {
  width: 100%;
  justify-content: center;
  text-align: center;
}

.state-box {
  margin-top: 20px;
  padding: 24px;
  text-align: center;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  color: #4b5563;
  font-size: 14px;
}

.state-error {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fef2f2;
}

.state-empty .empty-text {
  margin-bottom: 10px;
  color: #6b7280;
}

.schemes-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scheme-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 16px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.scheme-main {
  flex: 1;
}

.scheme-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.scheme-meta {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 6px 0;
}

.scheme-desc {
  font-size: 13px;
  color: #4b5563;
  margin: 0;
}

.scheme-side {
  min-width: 190px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.scheme-price {
  font-size: 13px;
  color: #4b5563;
}

.scheme-price-value {
  font-size: 16px;
  font-weight: 600;
  color: #2563eb;
}

.scheme-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

/* 支付弹窗样式 */
.pay-dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.pay-dialog {
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.35);
  padding: 24px 28px 20px;
}

.pay-title {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.pay-subtitle {
  margin: 0 0 16px 0;
  font-size: 13px;
  color: #6b7280;
}

.pay-qrcodes {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 12px;
}

.pay-channel-title {
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 6px;
}

.pay-qrcode-box {
  height: 150px;
  border-radius: 12px;
  border: 1px dashed #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.pay-qrcode-img {
  max-width: 130px;
  max-height: 130px;
}

.pay-status-text {
  margin: 6px 0 10px 0;
  font-size: 12px;
  color: #6b7280;
}

.pay-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 900px) {
  .cart-content {
    grid-template-columns: 1fr;
  }

  .cart-summary {
    order: -1;
  }

  .scheme-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .scheme-side {
    align-items: flex-start;
  }
}
</style>

