// src/api/index.js

// 通用 Result 解析工具
// 约定后端统一返回：{ success:boolean, code:number, msg:string, data:any }
export async function requestJson(url, options = {}) {
  const token = localStorage.getItem('token')
  const headers = { ...(options.headers || {}) }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(url, {
    credentials: 'include',
    ...options,
    headers
  })
  const result = await res.json().catch(() => ({}))

  if (!res.ok || !result || result.success === false || (typeof result.code === 'number' && result.code !== 200)) {
    const msg = (result && result.msg) || '请求失败'
    throw new Error(msg)
  }

  return result.data
}

// ================= 认证相关 =================
export async function apiLogin({ username, password, role, remember, employeeId }) {
  const payload = { username, password, role, remember }
  if (employeeId != null && employeeId !== '') {
    payload.employeeId = employeeId
  }
  const data = await requestJson('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

export async function apiRegister({ username, phone, role, employeeId, password, address }) {
  const data = await requestJson('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, phone, role, employeeId, password, address })
  })
  return data
}

// 当前登录用户信息（用于个人信息页）
export async function getCurrentUser() {
  const username = localStorage.getItem('username')
  const url = username ? `/api/users/current?username=${encodeURIComponent(username)}` : '/api/users/current'
  const data = await requestJson(url)
  return data.user || data
}

// 上传头像，file 为 File 对象
export async function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('avatar', file)
  const token = localStorage.getItem('token')
  const headers = {}
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const username = localStorage.getItem('username')
  const url = username ? `/api/users/avatar?username=${encodeURIComponent(username)}` : '/api/users/avatar'
  const res = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: formData
  })
  const result = await res.json().catch(() => ({}))
  if (!res.ok || result.success === false || (typeof result.code === 'number' && result.code !== 200)) {
    throw new Error(result.msg || '上传头像失败')
  }
  return result.data || {}
}

// 更新个人信息（Profile.vue 依赖）
export async function updateProfile(payload) {
  const username = localStorage.getItem('username')
  const url = username ? `/api/users/profile?username=${encodeURIComponent(username)}` : '/api/users/profile'
  const data = await requestJson(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload || {})
  })
  return data
}

// ================= 车型相关 =================
export async function getCarModels() {
  const data = await requestJson('/api/car-models')
  const list = data.models || data.carModels || data || []
  return Array.isArray(list) ? list : []
}

export async function getCarModelDetail(modelId) {
  const data = await requestJson(`/api/car-models/${modelId}`)
  return data.model || data
}

export async function createCarModel(payload) {
  return requestJson('/api/car-models', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export async function updateCarModel(id, payload) {
  return requestJson(`/api/car-models/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export async function importCarModels(file) {
  const form = new FormData()
  form.append('file', file)
  const token = localStorage.getItem('token')
  const headers = {}
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch('/api/car-models/import', {
    method: 'POST',
    credentials: 'include',
    headers,
    body: form
  })
  const result = await res.json().catch(() => ({}))
  if (!res.ok || result.success === false || (typeof result.code === 'number' && result.code !== 200)) {
    throw new Error(result.msg || '导入失败')
  }
  return result.data || {}
}

export async function deleteCarModel(id) {
  return requestJson(`/api/car-models/${id}`, { method: 'DELETE' })
}

/** 热门配置：公开方案 + 标配方案（车型推荐配置），用于首页展示 */
export async function getPopularSchemes() {
  // 直接拉取所有已分享（公开）方案；避免后端未部署 /popular 时首页空白
  const data = await requestJson('/api/custom-schemes?onlyPublic=true')
  return data.schemes || []
}

// ================= 配置选项 / 规则 / 配件 =================
export async function getExteriorOptions() {
  const data = await requestJson('/api/options/exterior')
  return Array.isArray(data) ? data : []
}

export async function getInteriorOptions() {
  const data = await requestJson('/api/options/interior')
  return Array.isArray(data) ? data : []
}

export async function getPerformanceOptions() {
  const data = await requestJson('/api/options/performance')
  return Array.isArray(data) ? data : []
}

export async function getTechOptions() {
  const data = await requestJson('/api/options/tech')
  return Array.isArray(data) ? data : []
}

export async function getAccessories() {
  const data = await requestJson('/api/accessories')
  return Array.isArray(data) ? data : data.items || []
}

export async function createAccessory(payload) {
  const data = await requestJson('/api/accessories', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

export async function updateAccessory(id, payload) {
  const data = await requestJson(`/api/accessories/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

export async function deleteAccessory(id) {
  await requestJson(`/api/accessories/${id}`, { method: 'DELETE' })
}

// ================= 配置规则 =================
export async function getConfigRulesMeta() {
  const data = await requestJson('/api/config-rules/meta')
  return data || { brands: [], modelsByBrand: {}, optionItems: [] }
}

export async function getConfigRules(params = {}) {
  const sp = new URLSearchParams()
  if (params.ruleType) sp.set('ruleType', params.ruleType)
  if (params.series) sp.set('series', params.series)
  if (params.model) sp.set('model', params.model)
  if (params.brand) sp.set('brand', params.brand)
  if (params.modelName) sp.set('modelName', params.modelName)
  if (params.enabled != null) sp.set('enabled', String(params.enabled))
  if (params.itemAId) sp.set('itemAId', params.itemAId)
  if (params.itemBId) sp.set('itemBId', params.itemBId)
  const qs = sp.toString()
  const url = qs ? `/api/config-rules?${qs}` : '/api/config-rules'
  const data = await requestJson(url)
  return Array.isArray(data) ? data : data.list || []
}

export async function createConfigRule(payload) {
  return requestJson('/api/config-rules', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export async function updateConfigRule(id, payload) {
  return requestJson(`/api/config-rules/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export async function deleteConfigRule(id) {
  return requestJson(`/api/config-rules/${id}`, { method: 'DELETE' })
}

// 配置项（用于规则中选择 itemA/itemB，支持按名称搜索）
export async function getOptionItems({ categoryId, keyword } = {}) {
  const sp = new URLSearchParams()
  if (categoryId != null) sp.set('categoryId', categoryId)
  if (keyword) sp.set('keyword', keyword)
  const qs = sp.toString()
  const url = qs ? `/api/option-items?${qs}` : '/api/option-items'
  const data = await requestJson(url)
  return Array.isArray(data) ? data : []
}

// ================= 自定义方案 =================
export async function getCustomSchemes({ username, onlyPublic } = {}) {
  const params = new URLSearchParams()
  if (username) params.set('username', username)
  if (onlyPublic !== undefined) params.set('onlyPublic', String(!!onlyPublic))
  const qs = params.toString()
  const url = qs ? `/api/custom-schemes?${qs}` : '/api/custom-schemes'
  const data = await requestJson(url)
  return data.schemes || data || []
}

export async function createCustomScheme(payload) {
  const data = await requestJson('/api/custom-schemes', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

export async function getCustomSchemeDetail(id) {
  const data = await requestJson(`/api/custom-schemes/${id}`)
  return data.scheme || data
}

export async function getSchemeComments(id) {
  const data = await requestJson(`/api/custom-schemes/${id}/comments`)
  return data.comments || data || []
}

export async function updateCustomScheme(id, payload) {
  const data = await requestJson(`/api/custom-schemes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data || {}
}

export async function shareCustomScheme(id) {
  const data = await requestJson(`/api/custom-schemes/${id}/share`, {
    method: 'POST'
  })
  return data || {}
}

export async function unshareCustomScheme(id) {
  const data = await requestJson(`/api/custom-schemes/${id}/unshare`, {
    method: 'POST'
  })
  return data || {}
}

export async function likeScheme(id, { liked } = {}) {
  const username = localStorage.getItem('username') || ''
  const data = await requestJson(`/api/custom-schemes/${id}/like`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, liked })
  })
  return data || {}
}

export async function pinCustomScheme(id, { pinned } = {}) {
  const username = localStorage.getItem('username') || ''
  const qs = username ? `?username=${encodeURIComponent(username)}` : ''
  const data = await requestJson(`/api/custom-schemes/${id}/pin${qs}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pinned: !!pinned })
  })
  return data || {}
}

export async function addSchemeComment(id, payload = {}) {
  const {
    userId,
    username: usernameRaw,
    userName,
    content,
    parentId,
    rootId,
    floorLevel
  } = payload || {}
  const username = usernameRaw || userName || localStorage.getItem('username') || ''
  const data = await requestJson(`/api/custom-schemes/${id}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    // 对应后端 comment 表：user_id/scheme_id/content/parent_id/root_id/floor_level...
    // 这里先支持最常用的顶级评论：content + username（后端会根据 username 解析 userId）
    body: JSON.stringify({
      userId,
      username,
      userName: username,
      content,
      parentId,
      rootId,
      floorLevel
    })
  })
  return data || {}
}

export async function likeSchemeComment(schemeId, commentId) {
  const username = localStorage.getItem('username') || ''
  const data = await requestJson(
    `/api/custom-schemes/${schemeId}/comments/${commentId}/like`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username })
    }
  )
  return data || {}
}

export async function unlikeSchemeComment(schemeId, commentId) {
  const username = localStorage.getItem('username') || ''
  const data = await requestJson(
    `/api/custom-schemes/${schemeId}/comments/${commentId}/unlike`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username })
    }
  )
  return data || {}
}

export async function updateSchemeComment(schemeId, commentId, { content }) {
  const username = localStorage.getItem('username') || ''
  const data = await requestJson(
    `/api/custom-schemes/${schemeId}/comments/${commentId}`,
    {
      // 某些部署环境可能不支持 PUT，后端提供 POST 兼容路由
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, content })
    }
  )
  return data || {}
}

export async function deleteSchemeComment(schemeId, commentId) {
  const username = localStorage.getItem('username') || ''
  const data = await requestJson(
    // 某些部署环境可能不支持 DELETE，后端提供 POST 兼容路由
    `/api/custom-schemes/${schemeId}/comments/${commentId}/delete`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username })
    }
  )
  return data || {}
}

// ================= 购物车 =================
export async function getCart() {
  const username = localStorage.getItem('username') || ''
  const url = username ? `/api/cart?username=${encodeURIComponent(username)}` : '/api/cart'
  const data = await requestJson(url)
  return {
    items: data.items || [],
    summary: data.summary || { totalAmount: 0, estimatedDelivery: '' }
  }
}

export async function addToCart(payload) {
  const username = localStorage.getItem('username') || ''
  const body = { ...(payload || {}), username }
  const data = await requestJson('/api/cart', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  return data || {}
}

export async function removeCartItem(id) {
  const username = localStorage.getItem('username') || ''
  const qs = username ? `?username=${encodeURIComponent(username)}` : ''
  const data = await requestJson(`/api/cart/${id}${qs}`, { method: 'DELETE' })
  return data || {}
}

// ================= 订单 =================
export async function getOrders(status = 'all') {
  const username = localStorage.getItem('username') || ''
  const params = new URLSearchParams()
  params.set('status', status || 'all')
  if (username) params.set('username', username)
  const qs = params.toString()
  const url = qs ? `/api/orders?${qs}` : '/api/orders'
  const data = await requestJson(url)
  return data.orders || data || []
}

export async function updateOrderStatus(id, status) {
  const username = localStorage.getItem('username') || ''
  const qs = username ? `?username=${encodeURIComponent(username)}` : ''
  const data = await requestJson(`/api/orders/${id}/status${qs}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status })
  })
  return data || {}
}

// ================= 订单留言（普通用户 -> 管理员） =================
export async function createSupportMessage({ orderId, orderNo, content }) {
  const username = localStorage.getItem('username') || ''
  const qs = username ? `?username=${encodeURIComponent(username)}` : ''
  const data = await requestJson(`/api/support-messages${qs}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ orderId, orderNo, content })
  })
  return data || {}
}

export async function getSupportMessages({ orderId } = {}) {
  const username = localStorage.getItem('username') || ''
  const params = new URLSearchParams()
  if (username) params.set('username', username)
  if (orderId != null) params.set('orderId', String(orderId))
  const qs = params.toString()
  const data = await requestJson(qs ? `/api/support-messages?${qs}` : '/api/support-messages')
  return data.messages || []
}

export async function createOrder(payload) {
  const data = await requestJson('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

// ================= 后台车型管理 =================
export async function getAdminVehicles() {
  const data = await requestJson('/api/vehicles')
  const list = data.vehicles || data || []
  return Array.isArray(list) ? list : []
}

export async function saveVehicle(payload, id) {
  const url = id ? `/api/vehicles/${id}` : '/api/vehicles'
  const method = id ? 'PUT' : 'POST'
  const data = await requestJson(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  return data
}

export async function updateVehicleStatus(id, status) {
  const data = await requestJson(`/api/vehicles/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status })
  })
  return data || {}
}