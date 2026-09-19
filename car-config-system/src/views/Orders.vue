<template>
  <div class="orders-page">
    <Header />
    <main class="orders-main">
      <div class="container">
        <!-- 标题 -->
        <header class="orders-header">
          <h1 class="orders-title">{{ isAdmin ? '订单管理' : '我的订单' }}</h1>
        </header>

        <!-- 筛选 Tabs -->
        <nav class="filter-tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: currentStatus === tab.value }"
            @click="changeStatus(tab.value)"
          >
            {{ tab.label }}
          </button>
        </nav>

        <!-- 加载 / 错误 / 空状态 -->
        <section v-if="loading" class="state-box">
          正在加载订单数据...
        </section>
        <section v-else-if="error" class="state-box state-error">
          {{ error }}
        </section>
        <section v-else-if="!orders.length" class="state-box state-empty">
          当前筛选条件下暂无订单。
        </section>

        <!-- 订单列表 -->
        <section v-else class="orders-list">
          <article
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            :class="{ 'order-card--has-message': isAdmin && order.hasMessage }"
          >
            <header class="order-header">
              <div class="order-meta">
                <p class="order-no">订单号：{{ order.orderNo }}</p>
                <p class="order-time">
                  下单时间：{{ order.orderTime }}
                </p>
              </div>
              <div class="order-status-wrap">
                <span v-if="isAdmin && order.hasMessage" class="message-badge">有留言</span>
                <span
                  class="order-status"
                  :class="order.status"
                >
                  {{ statusText(order) }}
                </span>
              </div>
            </header>

            <div class="order-body">
              <div v-if="isAdmin" class="order-user">
                <p class="order-user-line">
                  用户：<strong>{{ order.username || '未知' }}</strong>
                </p>
                <p class="order-user-line" v-if="order.phone">电话：{{ order.phone }}</p>
                <p class="order-user-line" v-if="order.email">邮箱：{{ order.email }}</p>
                <p class="order-user-line" v-if="order.address">地址：{{ order.address }}</p>
              </div>
              <p class="order-vehicle">车型：{{ order.vehicleName }}</p>
              <p v-if="order.configSummary" class="order-config">
                配置方案：{{ order.configSummary }}
              </p>
              <p v-if="order.estimatedDelivery" class="order-delivery">
                预计交付时间：{{ order.estimatedDelivery }}
              </p>
              <p class="order-amount">
                总金额：
                <span class="order-amount-value">
                  ¥{{ (order.totalAmount || 0).toLocaleString() }}
                </span>
              </p>
            </div>

            <footer class="order-footer">
              <div class="order-actions">
                <button class="btn-secondary" @click="viewDetail(order)">
                  查看详情
                </button>
                <template v-if="isAdmin">
                  <select class="status-select" v-model="order._nextStatus">
                    <option value="preparing">准备中</option>
                    <option value="producing">制造中</option>
                    <option value="completed">完成</option>
                    <option value="cancelled">已取消</option>
                  </select>
                  <button
                    class="btn-secondary"
                    :disabled="updatingId === order.id || normalizeStatus(order.status) === order._nextStatus"
                    @click="adminUpdateStatus(order)"
                  >
                    {{ updatingId === order.id ? '处理中...' : '更新状态' }}
                  </button>
                  <button class="btn-text" @click="openMessages(order)">查看留言</button>
                </template>
                <button
                  v-if="order.status === 'producing' || order.status === 'pending'"
                  class="btn-text"
                  @click="isAdmin ? contactUser(order) : contactService(order)"
                >
                  {{ isAdmin ? '联系用户' : '联系客服' }}
                </button>
                <button
                  v-else-if="order.status === 'delivered'"
                  class="btn-text"
                  @click="applyAfterSale(order)"
                >
                  申请售后
                </button>
              </div>
            </footer>
          </article>
        </section>
      </div>
    </main>
    <Footer />

    <!-- 管理员：联系用户弹窗 -->
    <div v-if="showContactDialog" class="dialog-backdrop" @click.self="closeContactDialog">
      <div class="dialog">
        <h3 class="dialog-title">联系用户</h3>
        <div class="dialog-body">
          <p class="dialog-line">用户：<strong>{{ contactUserInfo.username || '未知' }}</strong></p>
          <p class="dialog-line">
            电话：{{ contactUserInfo.phone || '无' }}
            <a
              v-if="contactUserInfo.phone"
              class="dialog-link"
              :href="`tel:${contactUserInfo.phone}`"
            >拨打</a>
            <button
              v-if="contactUserInfo.phone"
              class="dialog-link-btn"
              type="button"
              @click="copyText(contactUserInfo.phone)"
            >复制</button>
          </p>
          <p class="dialog-line">
            邮箱：{{ contactUserInfo.email || '无' }}
            <a
              v-if="contactUserInfo.email"
              class="dialog-link"
              :href="`mailto:${contactUserInfo.email}`"
            >发邮件</a>
            <button
              v-if="contactUserInfo.email"
              class="dialog-link-btn"
              type="button"
              @click="copyText(contactUserInfo.email)"
            >复制</button>
          </p>
          <p class="dialog-line">
            地址：{{ contactUserInfo.address || '无' }}
            <button
              v-if="contactUserInfo.address"
              class="dialog-link-btn"
              type="button"
              @click="copyText(contactUserInfo.address)"
            >复制</button>
          </p>
          <p class="dialog-hint">建议通过电话/邮箱与用户确认交付时间与需求。</p>
        </div>
        <div class="dialog-actions">
          <button class="btn-secondary" type="button" @click="closeContactDialog">关闭</button>
        </div>
      </div>
    </div>

    <!-- 普通用户：给管理员留言 -->
    <div v-if="showLeaveMessageDialog" class="dialog-backdrop" @click.self="closeLeaveMessageDialog">
      <div class="dialog">
        <h3 class="dialog-title">给管理员留言</h3>
        <div class="dialog-body">
          <p class="dialog-line">
            订单号：<strong>{{ leaveMessageOrder?.orderNo || '-' }}</strong>
          </p>
          <textarea
            v-model="leaveMessageText"
            class="dialog-textarea"
            placeholder="请输入要咨询/反馈的内容（例如交付时间、配置确认、地址变更等）..."
          ></textarea>
          <p class="dialog-hint">留言会发送给管理员，管理员会通过你预留的电话/邮箱联系你。</p>
        </div>
        <div class="dialog-actions">
          <button class="btn-secondary" type="button" @click="closeLeaveMessageDialog">取消</button>
          <button class="btn-secondary" type="button" :disabled="leavingMessage" @click="submitLeaveMessage">
            {{ leavingMessage ? '发送中...' : '发送留言' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 管理员：查看留言 -->
    <div v-if="showMessagesDialog" class="dialog-backdrop" @click.self="closeMessagesDialog">
      <div class="dialog dialog--wide">
        <h3 class="dialog-title">订单留言</h3>
        <div class="dialog-body">
          <p v-if="messagesLoading" class="dialog-hint">正在加载留言...</p>
          <p v-else-if="messagesError" class="dialog-hint">{{ messagesError }}</p>
          <ul v-else class="message-list">
            <li v-if="!orderMessages.length" class="message-empty">暂无留言</li>
            <li v-for="m in orderMessages" :key="m.id" class="message-item">
              <div class="message-meta">
                <strong>{{ m.username || '用户' }}</strong>
                <span class="message-time">{{ m.createTime }}</span>
              </div>
              <div class="message-content">{{ m.content }}</div>
            </li>
          </ul>
        </div>
        <div class="dialog-actions">
          <button class="btn-secondary" type="button" @click="closeMessagesDialog">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, computed } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { getOrders, updateOrderStatus, createSupportMessage, getSupportMessages } from '../api'

const router = inject('router')

const role = localStorage.getItem('role') || 'user'
const isAdmin = computed(() => String(role).toLowerCase() === 'admin')

const statusTabs = computed(() => {
  if (isAdmin.value) {
    return [
      { value: 'all', label: '全部' },
      { value: 'preparing', label: '准备中' },
      { value: 'producing', label: '制造中' },
      { value: 'completed', label: '完成' },
      { value: 'cancelled', label: '已取消' }
    ]
  }
  return [
    { value: 'all', label: '全部' },
    { value: 'pending', label: '待支付' },
    { value: 'preparing', label: '准备中' },
    { value: 'producing', label: '制造中' },
    { value: 'delivered', label: '已交付' },
    { value: 'completed', label: '完成' },
    { value: 'cancelled', label: '已取消' }
  ]
})

const currentStatus = ref('all')
const orders = ref([])
const loading = ref(false)
const error = ref('')
const updatingId = ref(null)
const showContactDialog = ref(false)
const contactUserInfo = ref({ username: '', phone: '', email: '', address: '' })
const showLeaveMessageDialog = ref(false)
const leaveMessageOrder = ref(null)
const leaveMessageText = ref('')
const leavingMessage = ref(false)
const showMessagesDialog = ref(false)
const messagesLoading = ref(false)
const messagesError = ref('')
const orderMessages = ref([])

function normalizeStatus(status) {
  const s = String(status || '').toLowerCase()
  if (s === 'delivered') return 'completed'
  if (s === 'pending') return 'preparing'
  return s
}

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const list = await getOrders(currentStatus.value)
    // 期待后端返回数组：[{ id, orderNo, vehicleName, orderTime, status, totalAmount, estimatedDelivery, statusText }]
    let mapped = (list || []).map((o) => ({
      ...o,
      _nextStatus: normalizeStatus(o.status) || 'preparing',
      hasMessage: false
    }))

    // 管理员：有留言订单置顶 + 红色标记
    if (isAdmin.value) {
      try {
        const msgs = await getSupportMessages()
        const set = new Set((msgs || []).map((m) => String(m.orderId)))
        mapped = mapped.map((o) => ({
          ...o,
          hasMessage: set.has(String(o.id))
        }))
        mapped.sort((a, b) => {
          const am = a.hasMessage ? 1 : 0
          const bm = b.hasMessage ? 1 : 0
          if (bm !== am) return bm - am
          // 次级排序：按下单时间倒序（无法解析时保持原顺序）
          const at = Date.parse(String(a.orderTime || '')) || 0
          const bt = Date.parse(String(b.orderTime || '')) || 0
          return bt - at
        })
      } catch (e) {
        // 留言拉取失败不影响订单展示
        console.error(e)
      }
    }

    orders.value = mapped
  } catch (e) {
    console.error(e)
    error.value = e.message || '订单数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function changeStatus(status) {
  if (currentStatus.value === status) return
  currentStatus.value = status
  loadOrders()
}

function statusText(order) {
  if (order.statusText) return order.statusText
  const s = normalizeStatus(order.status)
  switch (s) {
    case 'preparing':
      return '准备中'
    case 'producing':
      return order.estimatedDelivery
        ? `制造中（预计${order.estimatedDelivery}交付）`
        : '制造中'
    case 'completed':
      return '完成'
    case 'cancelled':
      return '已取消'
    default:
      return '未知状态'
  }
}

function viewDetail(order) {
  const schemeId = order.schemeId
  if (!schemeId) {
    return
  }
  // 跳转到已有的方案详情页，展示完整配置
  router.push(`/scheme-detail?id=${schemeId}`)
}

function contactService(order) {
  // 普通用户：给管理员留言
  leaveMessageOrder.value = order || null
  leaveMessageText.value = ''
  showLeaveMessageDialog.value = true
}

function contactUser(order) {
  contactUserInfo.value = {
    username: order?.username || '',
    phone: order?.phone || '',
    email: order?.email || '',
    address: order?.address || ''
  }
  showContactDialog.value = true
}

function closeContactDialog() {
  showContactDialog.value = false
}

function closeLeaveMessageDialog() {
  showLeaveMessageDialog.value = false
  leaveMessageOrder.value = null
  leaveMessageText.value = ''
}

async function submitLeaveMessage() {
  const order = leaveMessageOrder.value
  const content = leaveMessageText.value.trim()
  if (!order?.id) return
  if (!content) {
    alert('请输入留言内容')
    return
  }
  try {
    leavingMessage.value = true
    await createSupportMessage({
      orderId: order.id,
      orderNo: order.orderNo,
      content
    })
    alert('留言已发送，管理员会尽快联系你')
    closeLeaveMessageDialog()
  } catch (e) {
    console.error(e)
    alert(e?.message || '留言发送失败')
  } finally {
    leavingMessage.value = false
  }
}

async function openMessages(order) {
  if (!order?.id) return
  showMessagesDialog.value = true
  messagesLoading.value = true
  messagesError.value = ''
  orderMessages.value = []
  try {
    const list = await getSupportMessages({ orderId: order.id })
    orderMessages.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error(e)
    messagesError.value = e?.message || '留言加载失败'
  } finally {
    messagesLoading.value = false
  }
}

function closeMessagesDialog() {
  showMessagesDialog.value = false
  orderMessages.value = []
  messagesError.value = ''
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(String(text || ''))
    alert('已复制')
  } catch {
    // 兼容不支持 clipboard 的环境
    const el = document.createElement('textarea')
    el.value = String(text || '')
    el.setAttribute('readonly', '')
    el.style.position = 'fixed'
    el.style.left = '-9999px'
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
    alert('已复制')
  }
}

function applyAfterSale(order) {
  // 这里可以跳转到售后申请页
  console.log('apply after sale for order', order.orderNo)
}

async function adminUpdateStatus(order) {
  if (!order?.id) return
  try {
    updatingId.value = order.id
    const target = order._nextStatus || 'preparing'
    await updateOrderStatus(order.id, target)
    order.status = target
    await loadOrders()
  } catch (e) {
    console.error(e)
    alert(e?.message || '更新订单状态失败')
  } finally {
    updatingId.value = null
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.orders-main {
  flex: 1;
  padding: 40px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.orders-header {
  margin-bottom: 16px;
}

.orders-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-tab {
  padding: 6px 14px;
  font-size: 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab:hover {
  color: #111827;
}

.filter-tab.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.state-box {
  margin-top: 16px;
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

.orders-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 14px 16px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.order-status-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.message-badge {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 999px;
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  font-weight: 600;
}

.order-card--has-message {
  border-color: rgba(185, 28, 28, 0.35);
  box-shadow: 0 0 0 1px rgba(185, 28, 28, 0.18);
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-no,
.order-time {
  margin: 0;
  font-size: 13px;
  color: #4b5563;
}

.order-status {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e5e7eb;
  color: #374151;
}

.order-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.order-status.producing {
  background: #dbeafe;
  color: #1d4ed8;
}

.order-status.delivered {
  background: #dcfce7;
  color: #166534;
}

.order-status.cancelled {
  background: #fee2e2;
  color: #b91c1c;
}

.order-status.preparing {
  background: #fef3c7;
  color: #92400e;
}

.order-status.completed {
  background: #dcfce7;
  color: #166534;
}

.order-body {
  border-top: 1px dashed #e5e7eb;
  padding-top: 8px;
  margin-top: 4px;
}

.order-user {
  margin-bottom: 6px;
  padding: 8px 10px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 10px;
}

.order-user-line {
  margin: 2px 0;
  font-size: 12px;
  color: #334155;
}

.order-vehicle,
.order-config,
.order-delivery,
.order-amount {
  margin: 4px 0;
  font-size: 13px;
  color: #4b5563;
}

.order-vehicle {
  font-weight: 500;
  color: #111827;
}

.order-amount-value {
  font-size: 16px;
  font-weight: 600;
  color: #2563eb;
}

.order-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.order-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.status-select {
  height: 32px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  padding: 0 10px;
  font-size: 13px;
  color: #111827;
  background: #ffffff;
}

.btn-secondary,
.btn-text {
  font-size: 13px;
  border-radius: 999px;
  padding: 6px 14px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
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
  color: #2563eb;
}

.btn-text:hover {
  color: #1d4ed8;
}

.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.dialog {
  width: 460px;
  max-width: 92vw;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.25);
  padding: 16px 18px 14px;
}

.dialog--wide {
  width: 640px;
}

.dialog-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.dialog-body {
  font-size: 13px;
  color: #374151;
}

.dialog-line {
  margin: 6px 0;
  line-height: 1.6;
}

.dialog-link {
  margin-left: 10px;
  font-size: 12px;
  color: #2563eb;
  text-decoration: none;
}

.dialog-link:hover {
  text-decoration: underline;
}

.dialog-link-btn {
  margin-left: 8px;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  cursor: pointer;
}

.dialog-link-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.dialog-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.dialog-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-textarea {
  width: 100%;
  min-height: 110px;
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  outline: none;
  font-size: 13px;
  color: #111827;
  resize: vertical;
}

.dialog-textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.message-list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 12px;
  padding: 10px 12px;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #334155;
  margin-bottom: 6px;
}

.message-time {
  color: #64748b;
  flex-shrink: 0;
}

.message-content {
  font-size: 13px;
  color: #111827;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.message-empty {
  padding: 12px 10px;
  text-align: center;
  color: #6b7280;
  border: 1px dashed #e5e7eb;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-footer {
    justify-content: flex-start;
  }
}
</style>
