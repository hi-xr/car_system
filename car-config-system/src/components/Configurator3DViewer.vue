<template>
  <div class="model-display">
    <iframe
      ref="iframeRef"
      title="Model X 2024"
      frameborder="0"
      allowfullscreen
      allow="autoplay; fullscreen; xr-spatial-tracking"
      class="model-iframe"
    ></iframe>
    <div v-if="loading" class="model-loading">加载 3D 模型中...</div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  // 每个车型对应的 Sketchfab 模型 UID，由父组件传入
  modelUid: { type: String, default: '' },
  colorHex: { type: String, default: '#1e3a5f' },
  wheelId: { type: String, default: '21turb' }
})

const iframeRef = ref(null)
const loading = ref(true)
let api = null
let materialsCache = []
let bodyMaterials = []
let wheelMaterials = []
let wheelNodesMap = {}
let wheelNodesWithMatrix = [] // { instanceID, originalMatrix } 用于按尺寸缩放

// 默认占位模型 UID，当当前车型没有配置 modelUid 时使用
// 该 UID 来自 Sketchfab 上公开的 Low-Poly Car 模型
// https://sketchfab.com/3d-models/low-poly-car-fcdb0c27f7d04d47a518a249ae7093a2
const DEFAULT_MODEL_UID = 'fcdb0c27f7d04d47a518a249ae7093a2'

// 轮毂选项对应的颜色（灰阶模拟不同轮毂质感）
const WHEEL_COLORS = {
  '19std': '#4a4a4a',   // 19英寸标准
  '21turb': '#333333',  // 21英寸涡轮
  '22sport': '#5a5a5a'  // 22英寸运动
}

// 轮毂选项对应的尺寸比例（以 21 英寸为基准 1.0）
const WHEEL_SCALE = {
  '19std': 19 / 21,   // 约 0.905
  '21turb': 1.0,
  '22sport': 22 / 21  // 约 1.048
}

// 对部分模型，直接将所有材质都视为“车身”，以保证换色生效
// 这些 UID 与 Configurator.vue 中的部分车型一一对应
const FORCE_FULL_BODY_UIDS = new Set([
  // 当前项目中需要“整车一起变色”的车型 UID
  'fcdb0c27f7d04d47a518a249ae7093a2', // 奥迪 A6L
  '141c3279892b429ba42c0b98ba88ae6b', // 宝马 530Li
  '4d69b9aa808c42bc88226837f9c809b7', // 奔驰 E300L
  'b7b32eaca80d460c9338197e2c9d1408', // 特斯拉 Model Y
  '8558a51b5e4e4adb840751020000eca6'  // 蔚来 ET5T
])

function loadSketchfabScript() {
  return new Promise((resolve) => {
    if (window.Sketchfab) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = 'https://unpkg.com/@sketchfab/viewer-api@1.12.1/viewer-api.js'
    script.onload = resolve
    document.head.appendChild(script)
  })
}

function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  if (!result) return [0.5, 0.5, 0.5]
  return [
    parseInt(result[1], 16) / 255,
    parseInt(result[2], 16) / 255,
    parseInt(result[3], 16) / 255
  ]
}

function isWheelMaterial(name) {
  if (!name || typeof name !== 'string') return false
  const n = name.toLowerCase()
  return /wheel|rim|tire|tyre|alloy|hub|轮毂|轮胎/.test(n)
}

function applyMaterialColor(mat, color, apiRef) {
  if (!mat.channels) return
  const channel = mat.channels.AlbedoPBR || mat.channels.DiffuseColor || mat.channels.DiffusePBR
  if (!channel) return
  const rgb = hexToRgb(color)
  const updated = JSON.parse(JSON.stringify(mat))
  if (updated.channels.AlbedoPBR) {
    updated.channels.AlbedoPBR.enable = 1
    updated.channels.AlbedoPBR.color = rgb
  }
  if (updated.channels.DiffuseColor) {
    updated.channels.DiffuseColor.enable = 1
    updated.channels.DiffuseColor.color = rgb
  }
  if (updated.channels.DiffusePBR) {
    updated.channels.DiffusePBR.enable = 1
    updated.channels.DiffusePBR.color = rgb
  }
  if (apiRef) apiRef.setMaterial(updated)
}

function setBodyColor(hex) {
  if (!api || !bodyMaterials.length) return
  bodyMaterials.forEach((mat) => applyMaterialColor(mat, hex, api))
}

function applyScaleToMatrix(matrix, scale) {
  if (!matrix || matrix.length < 16) return matrix
  const m = [...matrix]
  // 列主序 4x4：缩放影响左上 3x3，索引 0,1,2,4,5,6,8,9,10
  const idx = [0, 1, 2, 4, 5, 6, 8, 9, 10]
  idx.forEach((i) => { m[i] *= scale })
  return m
}

function setWheelAppearance(wheelId) {
  if (!api) return
  const color = WHEEL_COLORS[wheelId] || WHEEL_COLORS['21turb']
  const scale = WHEEL_SCALE[wheelId] ?? 1
  const hasVariantNodes = Object.values(wheelNodesMap).some((arr) => arr.length > 0)

  // 方式1: 若模型有 19/21/22 变体节点，通过 hide/show 切换
  if (hasVariantNodes) {
    Object.keys(wheelNodesMap).forEach((key) => {
      wheelNodesMap[key].forEach((id) => {
        api[key === wheelId ? 'show' : 'hide'](id, () => {})
      })
    })
  } else {
    // 方式2: 若无变体节点，对轮毂节点应用尺寸缩放（19/21/22 英寸视觉区分）
    wheelNodesWithMatrix.forEach(({ instanceID, originalMatrix }) => {
      const scaledMatrix = applyScaleToMatrix(originalMatrix, scale)
      api.setMatrix(instanceID, scaledMatrix, () => {})
    })
  }

  // 方式3: 通过材质颜色更新轮毂外观
  if (wheelMaterials.length) {
    wheelMaterials.forEach((mat) => applyMaterialColor(mat, color, api))
  }
}

function updateModelAppearance() {
  setBodyColor(props.colorHex)
  setWheelAppearance(props.wheelId)
}

function isWheelNode(name) {
  if (!name || typeof name !== 'string') return false
  return /wheel|rim|轮毂|轮胎|tire|tyre/i.test(name)
}

function buildWheelNodesMap(nodes) {
  const map = { '19std': [], '21turb': [], '22sport': [] }
  const patterns = {
    '19std': /19|std|standard/i,
    '21turb': /21|turb|turbine/i,
    '22sport': /22|sport/i
  }
  nodes.forEach((node) => {
    const name = (node.name || '').toString()
    if (!isWheelNode(name)) return
    Object.keys(patterns).forEach((key) => {
      if (patterns[key].test(name)) map[key].push(node.instanceID)
    })
  })
  return map
}

function loadWheelMatrices(nodes, callback) {
  const wheelNodes = nodes.filter((n) => isWheelNode((n.name || '').toString()))
  if (!wheelNodes.length) {
    callback()
    return
  }
  let pending = wheelNodes.length
  wheelNodes.forEach((node) => {
    api.getMatrix(node.instanceID, (err, matrix) => {
      if (!err && matrix) {
        wheelNodesWithMatrix.push({ instanceID: node.instanceID, originalMatrix: [...matrix] })
      }
      if (--pending === 0) callback()
    })
  })
}

function initViewer() {
  if (!iframeRef.value || !window.Sketchfab) return
  const uid = props.modelUid || DEFAULT_MODEL_UID
  const client = new window.Sketchfab('1.12.1', iframeRef.value)
  client.init(uid, {
    autostart: 1,
    ui_controls: 0,
    ui_infos: 0,
    ui_stop: 0,
    ui_watermark: 0,
    ui_settings: 0,
    success: (viewerApi) => {
      api = viewerApi
      api.start(() => {
        api.addEventListener('viewerready', () => {
          loading.value = false
          api.getMaterialList((err, materials) => {
            if (!err && materials && materials.length) {
              materialsCache = materials
              bodyMaterials = materials.filter((m) => !isWheelMaterial(m.name))
              wheelMaterials = materials.filter((m) => isWheelMaterial(m.name))

              // 针对某些模型，直接将全部材质视为车身，以保证车漆颜色变化明显
              if (FORCE_FULL_BODY_UIDS.has(props.modelUid)) {
                bodyMaterials = materials
                wheelMaterials = []
              } else {
                // 通用轮毂/车身识别逻辑
                // 若没有识别到轮毂材质，尝试将非首材质当作轮毂（常见：车身+轮毂）
                if (!wheelMaterials.length && materials.length > 1) {
                  bodyMaterials = materials.slice(0, 1)
                  wheelMaterials = materials.slice(1)
                } else if (!wheelMaterials.length) {
                  // 单材质或无法区分：全部当车身，轮毂切换时整体应用轮毂色调
                  bodyMaterials = materials
                  wheelMaterials = []
                }
              }
              updateModelAppearance()
            }
          })
          api.getNodeMap((err, nodes) => {
            if (!err && nodes && nodes.length) {
              wheelNodesMap = buildWheelNodesMap(nodes)
              // 加载轮毂节点原始矩阵，用于后续按尺寸缩放
              loadWheelMatrices(nodes, () => updateModelAppearance())
            }
          })
        })
      })
    },
    error: () => {
      loading.value = false
      console.warn('Sketchfab 加载失败')
    }
  })
}

watch(
  () => props.colorHex,
  () => {
    updateModelAppearance()
  }
)

watch(
  () => props.wheelId,
  () => {
    updateModelAppearance()
  }
)

onMounted(async () => {
  await loadSketchfabScript()
  initViewer()
})
</script>

<style scoped>
.model-display {
  flex: 1;
  min-height: 400px;
  position: relative;
  background: #cbd5e1;
}

.model-iframe {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border: none;
}

.model-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
}
</style>
