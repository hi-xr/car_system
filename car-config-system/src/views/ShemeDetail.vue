<template>
  <div class="scheme-detail-page">
    <Header />
    <main class="scheme-main">
      <div class="container" v-if="detail">
        <!-- 标题区：方案名 + 作者 + 时间 -->
        <section class="header-card">
          <div class="title-row">
            <div>
              <p class="scheme-label">定制方案</p>
              <h1>{{ detail.schemeName }}</h1>
            </div>
            <div class="header-actions">
              <button class="btn-config" @click="addToCart">
                加入购物车
              </button>
              <div class="header-actions-column">
                <button
                  class="btn-like"
                  :class="{ 'btn-like--active': isLiked }"
                  @click="toggleLike"
                >
                  <span class="btn-like__icon" aria-hidden="true">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                    </svg>
                  </span>
                  <span class="btn-like__text">{{ isLiked ? '已点赞' : '点赞' }}</span>
                  <span class="btn-like__count">{{ detail.likesCount || 0 }}</span>
                </button>
              </div>
            </div>
          </div>

          <p class="meta">
            作者：{{ detail.authorName }} · 创建于：{{ detail.createdAt }}
          </p>
          <p class="meta">车型：{{ detail.modelName }}</p>

          <div class="stats">
            <span class="stat-item primary">
              总价：¥{{ formatPrice(detail.totalPrice) }}
            </span>
          </div>
        </section>

        <!-- 方案介绍 -->
        <section class="intro-card">
          <h2>方案介绍</h2>
          <p class="intro-text">
            {{ detail.shareDescription || '暂无介绍' }}
          </p>
        </section>

        <!-- 配件列表 -->
        <section class="items-card">
          <div class="section-header">
            <h2>配置明细</h2>
            <span class="items-count">
              共 {{ detail.items.length }} 个配件
            </span>
          </div>
          <table class="items-table">
            <thead>
              <tr>
                <th>分类</th>
                <th>名称</th>
                <th>加价</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in detail.items" :key="item.id">
                <td>{{ item.category }}</td>
                <td>{{ item.name }}</td>
                <td>¥{{ formatPrice(item.price) }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <!-- 评论区（放在配置明细下方） -->
        <section class="comments-card">
          <h2>评论</h2>
          <ul class="comment-list">
            <li
              v-for="c in comments"
              :key="c.commentId"
              class="comment-item"
              :style="{ marginLeft: `${(c._indent || 0) * 18}px` }"
            >
              <div class="comment-avatar" aria-hidden="true">
                <img
                  v-if="c.avatar"
                  class="comment-avatar-img"
                  :src="c.avatar"
                  :alt="c.userName"
                  loading="lazy"
                  referrerpolicy="no-referrer"
                />
                <span v-else class="comment-avatar-text">
                  {{ String(c.userName || '匿').slice(0, 1) }}
                </span>
              </div>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-name">{{ c.userName }}</span>
                  <span class="comment-dot">·</span>
                  <span class="comment-time">{{ c.createdAt }}</span>
                </div>
                <p class="comment-content">{{ c.content }}</p>
                <div class="comment-actions">
                  <button
                    class="comment-action"
                    :class="{ active: isCommentLiked(c.commentId) }"
                    @click="toggleCommentLike(c)"
                    :disabled="!isLoggedIn()"
                  >
                    赞 {{ c.likeCount || 0 }}
                  </button>
                  <button
                    class="comment-action"
                    @click="startReply(c)"
                    :disabled="!isLoggedIn()"
                  >
                    回复
                  </button>
                  <button
                    v-if="canManageComment(c)"
                    class="comment-action comment-action--muted"
                    @click="startEditComment(c)"
                    :disabled="!isLoggedIn()"
                  >
                    编辑
                  </button>
                  <button
                    v-if="canManageComment(c)"
                    class="comment-action comment-action--danger"
                    @click="handleDeleteComment(c)"
                    :disabled="!isLoggedIn()"
                  >
                    删除
                  </button>
                </div>
              </div>
            </li>
            <li v-if="!comments.length" class="comment-empty">
              暂无评论，快来抢沙发吧～
            </li>
          </ul>
          <div class="comment-form">
            <div v-if="replyTo || editingCommentId" class="reply-banner">
              <span class="reply-text">
                <template v-if="editingCommentId">
                  正在编辑评论
                </template>
                <template v-else>
                  回复 <strong>{{ replyTo.userName || '匿名用户' }}</strong>
                </template>
              </span>
              <button class="reply-cancel" @click="cancelCompose">取消</button>
            </div>
            <textarea
              v-model="newComment"
              class="comment-textarea"
              placeholder="说点什么，分享你的看法..."
              @focus="ensureLoginForComment"
            ></textarea>
            <div class="comment-form-actions">
              <span class="comment-hint">文明发言，共建良好氛围</span>
              <button
                class="btn-comment"
                :disabled="!newComment.trim() || !isLoggedIn()"
                @click="submitComment"
              >
                发布评论
              </button>
            </div>
          </div>
        </section>

        <!-- 编辑弹窗 -->
        <div v-if="showEdit" class="edit-dialog-backdrop">
          <div class="edit-dialog">
            <h3>编辑方案</h3>
            <div class="edit-dialog-body">
              <label class="edit-field">
                <span>方案标题</span>
                <input v-model="editSchemeName" type="text" />
              </label>
              <label class="edit-field">
                <span>方案介绍</span>
                <textarea v-model="editShareDescription"></textarea>
              </label>
              <div class="edit-items-block">
                <div class="edit-items-header">
                  <span class="edit-items-title">配置明细</span>
                  <button class="btn-items-link" @click="openAddItemDialog">增加配件</button>
                </div>
                <div class="edit-items-body">
                  <table class="items-table items-table--compact">
                    <thead>
                      <tr>
                        <th>分类</th>
                        <th>名称</th>
                        <th>加价</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="item in detail.items" :key="item.id">
                        <td>{{ item.category }}</td>
                        <td>{{ item.name }}</td>
                        <td>¥{{ formatPrice(item.price) }}</td>
                        <td>
                          <button class="link-btn danger" @click="removeItem(item)">移除</button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            <div class="edit-actions">
              <button class="btn-secondary" @click="closeEditDialog">取消</button>
              <button class="btn-config" @click="saveEdit">保存</button>
            </div>
          </div>
        </div>

        <!-- 增加配件弹窗 -->
        <div v-if="showAddItem" class="edit-dialog-backdrop">
          <div class="edit-dialog">
            <h3>增加配件</h3>
            <p class="options-hint" v-if="optionsError">{{ optionsError }}</p>
            <p class="options-hint" v-else>从配置选项中选择要增加的配件：</p>
            <div class="options-list">
              <div
                v-for="opt in allOptions"
                :key="opt.id"
                class="option-row"
              >
                <div class="option-main">
                  <span class="option-name">{{ opt.category }} · {{ opt.name }}</span>
                  <span class="option-price">¥{{ formatPrice(opt.extraPrice || 0) }}</span>
                </div>
                <button
                  class="link-btn"
                  :disabled="detail.items.some(i => i.id === opt.id)"
                  @click="addItem(opt)"
                >
                  {{ detail.items.some(i => i.id === opt.id) ? '已添加' : '添加' }}
                </button>
              </div>
            </div>
            <div class="edit-actions">
              <button class="btn-secondary" @click="closeAddItemDialog">取消</button>
              <button class="btn-config" @click="closeAddItemDialog">保存</button>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="loading">
        正在加载方案详情...
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import {
  getCustomSchemeDetail,
  getSchemeComments,
  likeScheme as apiLikeScheme,
  addSchemeComment,
  likeSchemeComment,
  unlikeSchemeComment,
  updateSchemeComment,
  deleteSchemeComment,
  updateCustomScheme,
  addToCart as apiAddToCart,
  getExteriorOptions,
  getInteriorOptions,
  getPerformanceOptions,
  getTechOptions
} from '../api'

const router = inject('router')

const detail = ref(null)
const showEdit = ref(false)
const editSchemeName = ref('')
const editShareDescription = ref('')
const editingItems = ref(false)
const showAddItem = ref(false)
const allOptions = ref([])
const optionsError = ref('')
const comments = ref([])
const newComment = ref('')
const isLiked = ref(false)
const replyTo = ref(null)
const editingCommentId = ref(null)

const formatPrice = (value) => {
  if (value == null) return '0'
  const num = Number(value) || 0
  return num.toLocaleString()
}

const getQueryParam = (name) => {
  const search = window.location.search
  const params = new URLSearchParams(search)
  return params.get(name)
}

const addToCart = async () => {
  if (!detail.value) return

  try {
    // 使用后端返回的车型图片 carImage（model_image），再做前端路径规范化
    let imageUrl = ''
    const rawImg = detail.value.carImage
    if (rawImg) {
      const s = String(rawImg).trim()
      if (s.startsWith('http://') || s.startsWith('https://')) {
        imageUrl = s
      } else {
        const clean = s.replace(/^\/+/, '')
        imageUrl = clean ? `/${clean}` : ''
      }
    }
    await apiAddToCart({
      schemeId: detail.value.schemeId,
      schemeName: detail.value.schemeName,
      totalPrice: detail.value.totalPrice,
      modelName: detail.value.modelName,
      imageUrl,
      configSummary: detail.value.schemeName
    })
    // 加入成功后跳转到购物车页面
    router.push('/cart')
  } catch (e) {
    console.error(e)
    alert('加入购物车失败，请稍后重试')
  }
}

function getSchemeLikeKey(schemeId) {
  const username = localStorage.getItem('username') || 'guest'
  return `${username}:${String(schemeId)}`
}

function getSchemeLikeMap() {
  const raw = localStorage.getItem('schemeLikes') || '{}'
  try {
    const map = JSON.parse(raw)
    return map && typeof map === 'object' ? map : {}
  } catch {
    return {}
  }
}

const toggleLike = async () => {
  if (!detail.value) return
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  const schemeId = detail.value.schemeId
  const map = getSchemeLikeMap()
  const key = getSchemeLikeKey(schemeId)
  const currentlyLiked = !!map[key]
  // 方案点赞：可点赞/可取消（与评论点赞一致的切换体验）
  map[key] = !currentlyLiked
  localStorage.setItem('schemeLikes', JSON.stringify(map))
  isLiked.value = !currentlyLiked
  const delta = currentlyLiked ? -1 : 1
  const current = Number(detail.value.likesCount || 0)
  detail.value.likesCount = Math.max(0, current + delta)
  try {
    const res = await apiLikeScheme(schemeId, { liked: !currentlyLiked })
    // 以数据库为准同步（解决本地状态与后端不一致导致的“已点赞”弹窗）
    if (res && typeof res === 'object') {
      if (typeof res.likesCount === 'number') {
        detail.value.likesCount = res.likesCount
      }
      if (typeof res.liked === 'boolean') {
        isLiked.value = res.liked
        map[key] = res.liked
        localStorage.setItem('schemeLikes', JSON.stringify(map))
      }
    }
  } catch (e) {
    console.error(e)
    // 接口失败则回滚，避免“看起来点了但实际没加”
    map[key] = currentlyLiked
    localStorage.setItem('schemeLikes', JSON.stringify(map))
    isLiked.value = currentlyLiked
    detail.value.likesCount = Math.max(0, current)
    alert(e?.message || '点赞失败，请稍后重试')
  }
}

const openEditDialog = () => {
  if (!detail.value) return
  editSchemeName.value = detail.value.schemeName || ''
  editShareDescription.value = detail.value.shareDescription || ''
  showEdit.value = true
}

const closeEditDialog = () => {
  showEdit.value = false
}

const saveEdit = async () => {
  if (!detail.value) return
  try {
    const itemIds = detail.value.items.map((i) => i.id)
    await updateCustomScheme(detail.value.schemeId, {
      schemeName: editSchemeName.value,
      shareDescription: editShareDescription.value,
      selectedOptionItemIds: itemIds,
      totalPrice: detail.value.totalPrice
    })
    detail.value.schemeName = editSchemeName.value
    detail.value.shareDescription = editShareDescription.value
    showEdit.value = false
  } catch (e) {
    console.error(e)
    alert('保存失败，请稍后重试')
  }
}

const toggleEditItems = async () => {
  editingItems.value = !editingItems.value
  if (editingItems.value && !allOptions.value.length) {
    await loadAllOptions()
  }
}

const openAddItemDialog = async () => {
  if (!allOptions.value.length) {
    await loadAllOptions()
  }
  showAddItem.value = true
}

const closeAddItemDialog = () => {
  showAddItem.value = false
}

const loadAllOptions = async () => {
  optionsError.value = ''
  try {
    const [exterior, interior, performance, tech] = await Promise.all([
      getExteriorOptions(),
      getInteriorOptions(),
      getPerformanceOptions(),
      getTechOptions()
    ])
    const normalize = (list, categoryLabel) =>
      (list || []).map((x) => ({
        id: x.id,
        name: x.name,
        category: categoryLabel,
        extraPrice: x.extraPrice || x.optionPrice || 0
      }))
    allOptions.value = [
      ...normalize(exterior, '外观'),
      ...normalize(interior, '内饰'),
      ...normalize(performance, '性能'),
      ...normalize(tech, '科技')
    ]
  } catch (e) {
    console.error(e)
    optionsError.value = '配置选项加载失败，请稍后重试'
  }
}

const addItem = (opt) => {
  if (!detail.value) return
  if (detail.value.items.some((i) => i.id === opt.id)) return
  const price = Number(opt.extraPrice || 0)
  detail.value.items.push({
    id: opt.id,
    category: opt.category,
    name: opt.name,
    price
  })
  detail.value.totalPrice += price
}

const removeItem = (item) => {
  if (!detail.value) return
  detail.value.items = detail.value.items.filter((i) => i.id !== item.id)
  const price = Number(item.price || 0)
  detail.value.totalPrice -= price
}

const saveItemsEdit = async () => {
  if (!detail.value) return
  try {
    const itemIds = detail.value.items.map((i) => i.id)
    await updateCustomScheme(detail.value.schemeId, {
      selectedOptionItemIds: itemIds,
      totalPrice: detail.value.totalPrice
    })
    editingItems.value = false
    showAddItem.value = false
    alert('配置明细已保存')
  } catch (e) {
    console.error(e)
    alert('保存配置明细失败，请稍后重试')
  }
}

function isLoggedIn() {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  return !!(token || username)
}

function redirectToLogin() {
  const redirect = window.location.pathname + window.location.search
  router.push(`/login?redirect=${encodeURIComponent(redirect)}`)
}

function ensureLoginForComment() {
  if (!isLoggedIn()) {
    redirectToLogin()
  }
}

function normalizeAvatarPath(path) {
  if (!path) return ''
  const s = String(path).trim()
  if (!s) return ''
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const clean = s.replace(/^\/+/, '')
  return `/${clean}`
}

function startReply(c) {
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  editingCommentId.value = null
  replyTo.value = c
}

function cancelCompose() {
  replyTo.value = null
  editingCommentId.value = null
  newComment.value = ''
}

function getCommentLikeMap() {
  const raw = localStorage.getItem('commentLikes') || '{}'
  try {
    const map = JSON.parse(raw)
    return map && typeof map === 'object' ? map : {}
  } catch {
    return {}
  }
}

function isCommentLiked(commentId) {
  const map = getCommentLikeMap()
  const username = localStorage.getItem('username') || ''
  return !!map[`${username}:${String(commentId)}`]
}

async function toggleCommentLike(c) {
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  if (!detail.value) return
  const commentId = c?.commentId
  if (!commentId) return

  const username = localStorage.getItem('username') || ''
  const key = `${username}:${String(commentId)}`
  const map = getCommentLikeMap()
  const currentlyLiked = !!map[key]
  map[key] = !currentlyLiked
  localStorage.setItem('commentLikes', JSON.stringify(map))

  // 乐观更新（与方案点赞一致：再点一次取消）
  const delta = currentlyLiked ? -1 : 1
  const current = Number(c.likeCount || 0)
  c.likeCount = Math.max(0, current + delta)

  try {
    if (currentlyLiked) {
      await unlikeSchemeComment(detail.value.schemeId, commentId)
    } else {
      await likeSchemeComment(detail.value.schemeId, commentId)
    }
    // 以数据库为准，刷新一次评论列表
    await loadComments(detail.value.schemeId)
  } catch (e) {
    console.error(e)
    // 回滚本地状态与数量
    map[key] = currentlyLiked
    localStorage.setItem('commentLikes', JSON.stringify(map))
    c.likeCount = Math.max(0, current)
  }
}

const submitComment = async () => {
  const content = newComment.value.trim()
  if (!detail.value) return
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  if (!content) return

  // 编辑已有评论
  if (editingCommentId.value) {
    try {
      await updateSchemeComment(detail.value.schemeId, editingCommentId.value, {
        content
      })
      await loadComments(detail.value.schemeId)
      editingCommentId.value = null
      newComment.value = ''
    } catch (e) {
      console.error(e)
      alert(e?.message || '编辑评论失败，请稍后重试')
    }
    return
  }

  const username = localStorage.getItem('username') || ''
  const localComment = {
    commentId: Date.now(),
    userName: username || '匿名用户',
    createdAt: new Date().toLocaleString(),
    content,
    likeCount: 0,
    parentId: replyTo.value?.commentId || 0,
    rootId: replyTo.value?.rootId || 0,
    floorLevel: (replyTo.value?.floorLevel || 0) + 1
  }

  comments.value.unshift(localComment)
  newComment.value = ''
  replyTo.value = null

  try {
    await addSchemeComment(detail.value.schemeId, {
      userName: username,
      content,
      parentId: localComment.parentId || 0
    })
    // 发表成功后刷新一次，拿到真实 commentId/时间/楼层
    await loadComments(detail.value.schemeId)
  } catch (e) {
    console.error(e)
  }
}

const loadComments = async (schemeId) => {
  try {
    const list = await getSchemeComments(schemeId)
    const mapped = list.map((c) => ({
      commentId: c.commentId ?? c.comment_id ?? c.id,
      userName: c.userName ?? c.user_name ?? c.username ?? '匿名用户',
      avatar: normalizeAvatarPath(c.avatar ?? c.userAvatar ?? c.user_avatar ?? ''),
      createdAt: c.createdAt ?? c.createTime ?? c.create_time ?? '',
      content: c.content ?? c.commentContent ?? c.comment_content ?? '',
      likeCount: c.likeCount ?? c.like_count ?? 0,
      parentId: c.parentId ?? c.parent_id ?? 0,
      rootId: c.rootId ?? c.root_id ?? 0,
      floorLevel: c.floorLevel ?? c.floor_level ?? 1
    }))

    // 展示：按 parentId/rootId 做简单缩进（顶级在前，回复跟在父评论后）
    const byId = new Map(mapped.map((x) => [String(x.commentId), x]))
    const children = new Map()
    mapped.forEach((x) => {
      const pid = String(x.parentId || 0)
      if (!children.has(pid)) children.set(pid, [])
      children.get(pid).push(x)
    })
    // 时间倒序列表里，children 仍按 createdAt/原顺序；这里保持接口顺序
    const result = []
    function dfs(node, level) {
      node._indent = level
      result.push(node)
      const kids = children.get(String(node.commentId)) || []
      kids.forEach((k) => dfs(k, Math.min(level + 1, 5)))
    }
    const roots = children.get('0') || children.get('') || []
    roots.forEach((r) => dfs(r, 0))
    // 兜底：如果接口 parentId 都为空（旧数据），直接用 mapped
    comments.value = result.length ? result : mapped
  } catch (e) {
    console.error(e)
    comments.value = []
  }
}

function canManageComment(c) {
  const username = localStorage.getItem('username') || ''
  if (!username) return false
  return String(c.userName || '') === username
}

function startEditComment(c) {
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  editingCommentId.value = c.commentId
  replyTo.value = null
  newComment.value = c.content || ''
}

async function handleDeleteComment(c) {
  if (!detail.value || !c?.commentId) return
  if (!isLoggedIn()) {
    redirectToLogin()
    return
  }
  if (!window.confirm('确定要删除这条评论吗？')) return
  try {
    await deleteSchemeComment(detail.value.schemeId, c.commentId)
    await loadComments(detail.value.schemeId)
    if (editingCommentId.value === c.commentId) {
      editingCommentId.value = null
      newComment.value = ''
    }
  } catch (e) {
    console.error(e)
    alert(e?.message || '删除评论失败，请稍后重试')
  }
}

const loadLikeState = (schemeId) => {
  const map = getSchemeLikeMap()
  const key = getSchemeLikeKey(schemeId)
  isLiked.value = !!map[key]
}

const buildMockData = (schemeId) => {
  // 这里使用静态示例数据，你可以替换为后端接口请求
  const baseDetail = {
    schemeId,
    schemeName: '尊享豪华内饰套装',
    authorName: '@DavidMiller',
    createdAt: '2023-05-21 14:32',
    modelName: '2023 款豪华版',
    totalPrice: 32800,
    likesCount: 1248,
    copiesCount: 56,
    shareDescription:
      '通过真皮座椅、氛围灯和高级音响系统的组合，为您打造尊享级别的豪华驾乘体验。',
    items: [
      {
        id: 1,
        category: '内饰',
        name: '真皮电动调节座椅套装',
        price: 12800
      },
      {
        id: 2,
        category: '内饰',
        name: '64 色车内氛围灯',
        price: 4800
      },
      {
        id: 3,
        category: '科技',
        name: '高级环绕音响系统',
        price: 8800
      },
      {
        id: 4,
        category: '舒适',
        name: '前后排座椅加热 / 通风',
        price: 6400
      }
    ]
  }

  const baseComments = [
    {
      commentId: 1,
      userName: '小李',
      createdAt: '2023-06-02 09:15',
      content: '已经按这个方案下单了，氛围灯和音响效果都很不错！'
    },
    {
      commentId: 2,
      userName: '车迷阿强',
      createdAt: '2023-06-05 18:47',
      content: '个人感觉加个电动座椅记忆功能会更完美。'
    }
  ]

  return { detail: baseDetail, comments: baseComments }
}

const loadDetail = async (schemeId) => {
  try {
    const raw = await getCustomSchemeDetail(schemeId)

    const items =
      raw.items || raw.schemeItems || raw.scheme_items || []

    detail.value = {
      schemeId: raw.schemeId ?? raw.scheme_id ?? schemeId,
      schemeName: raw.schemeName ?? raw.scheme_name ?? '',
      authorName:
        raw.authorName ?? raw.userName ?? raw.username ?? '未知用户',
      createdAt:
        raw.createdAt ?? raw.createTime ?? raw.create_time ?? '',
      modelId: raw.modelId ?? raw.model_id ?? '',
      modelName: raw.modelName ?? raw.model_name ?? '',
      carImage: raw.carImage ?? raw.car_image ?? '',
      totalPrice: raw.totalPrice ?? raw.total_price ?? 0,
      likesCount: raw.likesCount ?? raw.likes_count ?? 0,
      shareDescription:
        raw.shareDescription ?? raw.share_description ?? '',
      items: items.map((it) => ({
        id: it.id,
        category: it.category ?? it.categoryName ?? it.category_name ?? '',
        name: it.name ?? it.itemName ?? it.accessory_name ?? '',
        price: it.price ?? it.extraPrice ?? it.extra_price ?? 0
      }))
    }
  } catch (e) {
    console.error(e)
    const { detail: d } = buildMockData(schemeId)
    detail.value = d
  }
}

onMounted(async () => {
  const schemeId = getQueryParam('id') || '1'
  await loadDetail(schemeId)
  await loadComments(schemeId)
  loadLikeState(schemeId)
  const editFlag = getQueryParam('edit')
  if (editFlag === '1') {
    showEdit.value = true
  }
})
</script>

<style scoped>
.scheme-detail-page {
  min-height: 100vh;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
}

.scheme-main {
  flex: 1;
  padding: 32px 0 48px;
}

.container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 24px 24px 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  margin-bottom: 24px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.scheme-label {
  font-size: 12px;
  color: #4f46e5;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 6px;
}

h1 {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.meta {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

.stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.stat-item {
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 999px;
  background-color: #f1f5f9;
  color: #0f172a;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.stat-item.primary {
  background-color: rgba(37, 99, 235, 0.08);
  color: #1d4ed8;
  font-weight: 600;
}

/* 点赞按钮：心形图标 + 胶囊样式 */
.btn-like {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease, transform 0.15s ease;
}

.btn-like .btn-like__icon {
  display: inline-flex;
  color: #94a3b8;
  transition: color 0.2s ease, transform 0.2s ease;
}

.btn-like .btn-like__icon svg {
  display: block;
}

.btn-like .btn-like__count {
  font-variant-numeric: tabular-nums;
  min-width: 1.2em;
}

.btn-like:hover {
  background-color: #fff1f2;
  border-color: #fecaca;
  color: #be123c;
}

.btn-like:hover .btn-like__icon {
  color: #f43f5e;
  transform: scale(1.1);
}

/* 已点赞状态 */
.btn-like.btn-like--active {
  background-color: rgba(244, 63, 94, 0.1);
  border-color: rgba(244, 63, 94, 0.35);
  color: #be123c;
}

.btn-like.btn-like--active .btn-like__icon {
  color: #e11d48;
}

.btn-like.btn-like--active .btn-like__icon svg {
  fill: currentColor;
}

.btn-config {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  border-radius: 999px;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.4);
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-config:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.55);
}

.btn-secondary {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  color: #111827;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.intro-card,
.items-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 20px 24px 24px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  margin-bottom: 20px;
}

.btn-items-link {
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 6px;
}

.btn-items-link.primary {
  font-weight: 600;
}

.link-btn.danger {
  color: #dc2626;
}

.edit-items-block {
  margin-top: 16px;
}

.edit-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.edit-items-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.items-table--compact {
  margin-top: 4px;
}

.edit-items-body {
  max-height: 220px;
  overflow-y: auto;
  padding-right: 4px;
}

.options-list {
  max-height: 320px;
  overflow-y: auto;
  margin-top: 8px;
}

.option-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px solid #e5e7eb;
}

.option-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.option-name {
  font-size: 13px;
  color: #111827;
}

.option-price {
  font-size: 12px;
  color: #6b7280;
}

.intro-text {
  font-size: 14px;
  line-height: 1.8;
  color: #334155;
  margin-top: 10px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.items-count {
  font-size: 13px;
  color: #64748b;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.items-table th,
.items-table td {
  padding: 10px 8px;
  text-align: left;
}

.items-table thead th {
  font-weight: 600;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

.items-table tbody tr:nth-child(even) {
  background-color: #f8fafc;
}

.items-table tbody tr:hover {
  background-color: #eff6ff;
}

.comment-list {
  list-style: none;
  padding: 0;
  margin: 0 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc, #ffffff);
  border: 1px solid #e2e8f0;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.22);
  flex: 0 0 auto;
  user-select: none;
  overflow: hidden;
}

.comment-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.comment-avatar-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 2px;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.comment-dot,
.comment-time {
  font-size: 12px;
  color: #64748b;
}

.comment-content {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.7;
  margin: 6px 0 0 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.comment-action {
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #334155;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.comment-action:hover {
  border-color: #2563eb;
  color: #2563eb;
  background-color: rgba(37, 99, 235, 0.06);
}

.comment-action.active {
  border-color: rgba(244, 63, 94, 0.35);
  color: #be123c;
  background-color: rgba(244, 63, 94, 0.08);
}

.comment-action:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.comment-action--muted {
  border-color: #e2e8f0;
  color: #64748b;
}

.comment-action--danger {
  border-color: #fecaca;
  color: #dc2626;
}

.comment-empty {
  font-size: 13px;
  color: #94a3b8;
  padding: 16px 14px;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 14px;
  text-align: center;
}

.reply-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 10px;
}

.reply-text {
  font-size: 13px;
  color: #334155;
  min-width: 0;
}

.reply-cancel {
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 6px;
}

.reply-cancel:hover {
  text-decoration: underline;
}

.comment-form {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #e2e8f0;
}

.comment-textarea {
  width: 100%;
  min-height: 92px;
  padding: 12px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  outline: none;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.comment-textarea::placeholder {
  color: #94a3b8;
}

.comment-textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.comment-form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.comment-hint {
  font-size: 12px;
  color: #64748b;
}

.btn-comment {
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  border-radius: 999px;
  border: none;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.25);
  transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  white-space: nowrap;
}

.btn-comment:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.32);
}

.btn-comment:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 640px) {
  .comment-form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-comment {
    width: 100%;
  }
}

.edit-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 40;
}

.edit-dialog {
  width: 420px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 22px 18px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.25);
  display: flex;
  flex-direction: column;
}

.edit-dialog h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #0f172a;
}

.edit-dialog-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.edit-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 10px;
}

.edit-field span {
  font-size: 13px;
  color: #64748b;
}

.edit-field input,
.edit-field textarea {
  width: 100%;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  outline: none;
  resize: vertical;
}

.edit-field input:focus,
.edit-field textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.loading {
  max-width: 1120px;
  margin: 40px auto;
  padding: 0 20px;
  font-size: 14px;
  color: #64748b;
}

@media (max-width: 768px) {
  .scheme-main {
    padding-top: 20px;
  }

  .header-card {
    padding: 18px 16px 16px;
  }

  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  h1 {
    font-size: 22px;
  }

  .btn-config {
    align-self: stretch;
    width: 100%;
    justify-content: center;
    text-align: center;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .header-actions-column {
    width: 100%;
    flex-direction: row;
    gap: 10px;
  }

  .header-actions-column .btn-secondary,
  .header-actions-column .btn-like {
    flex: 1;
    justify-content: center;
  }
}
</style>