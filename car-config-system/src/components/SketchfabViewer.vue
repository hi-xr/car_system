<!-- src/components/SketchfabViewer.vue -->
<template>
    <div class="sketchfab-viewer">
      <!-- 3D模型展示区域 -->
      <div class="model-container">
        <!-- 动态嵌入Sketchfab模型 -->
        <iframe
          :src="currentModelUrl"
          :title="currentModelTitle"
          frameborder="0"
          allowfullscreen
          allow="autoplay; fullscreen; xr-spatial-tracking"
          class="sketchfab-iframe"
        ></iframe>
      </div>
      
      <!-- 控制面板 -->
      <div class="controls">
        <!-- 颜色选择 -->
        <div class="color-picker">
          <h4>车身颜色</h4>
          <div class="color-grid">
            <button
              v-for="color in colorOptions"
              :key="color.name"
              class="color-btn"
              :style="{ backgroundColor: color.hex }"
              :class="{ active: selectedColor === color.name }"
              @click="changeColor(color)"
              :title="color.name"
            >
              <span class="color-name">{{ color.name }}</span>
            </button>
          </div>
        </div>
        
        <!-- 模型选择 -->
        <div class="model-picker">
          <h4>车型选择</h4>
          <div class="model-grid">
            <button
              v-for="model in modelOptions"
              :key="model.id"
              class="model-btn"
              :class="{ active: selectedModel === model.id }"
              @click="changeModel(model)"
            >
              <img :src="model.thumbnail" :alt="model.name" />
              <span>{{ model.name }}</span>
            </button>
          </div>
        </div>
        
        <!-- 视角控制 -->
        <div class="view-controls">
          <h4>视角控制</h4>
          <div class="view-buttons">
            <button @click="setCameraView('front')">前视</button>
            <button @click="setCameraView('side')">侧视</button>
            <button @click="setCameraView('back')">后视</button>
            <button @click="setCameraView('top')">俯视</button>
            <button @click="resetView">重置</button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue'
  
  // 颜色选项
  const colorOptions = [
    { name: '烈焰红', hex: '#e74c3c', value: 'red' },
    { name: '深海蓝', hex: '#3498db', value: 'blue' },
    { name: '珍珠白', hex: '#ecf0f1', value: 'white' },
    { name: '曜石黑', hex: '#2c3e50', value: 'black' },
    { name: '活力橙', hex: '#e67e22', value: 'orange' },
    { name: '森林绿', hex: '#27ae60', value: 'green' }
  ]
  
  // 模型选项（使用可用的 Sketchfab 汽车模型）
  const modelOptions = [
    {
      id: 'lowpoly',
      name: '低多边形汽车',
      embedUrl: 'https://sketchfab.com/3d-models/low-poly-car-fcdb0c27f7d04d47a518a249ae7093a2/embed',
      thumbnail: 'https://media.sketchfab.com/models/fcdb0c27f7d04d47a518a249ae7093a2/thumbnails/9f50e32bc0b64ea6bf339ca7de1a7b4b/da72db04d6c14ed2b90a8fc0b7e6bf4e.jpeg'
    },
    {
      id: 'sports',
      name: '运动跑车',
      embedUrl: 'https://sketchfab.com/models/bfe9258d95b64970b7f9a1d65c4e3f97/embed',
      thumbnail: 'https://media.sketchfab.com/models/bfe9258d95b64970b7f9a1d65c4e3f97/thumbnails/9a9f1e8f0c404bfcbf1370a560ae7a39/9dcaa48d1e234264b5e6a125c123e29c.jpeg'
    },
    {
      id: 'classic',
      name: '经典老爷车',
      embedUrl: 'https://sketchfab.com/models/70e5de1e9c8245e7a1bd4e3bc3192b6e/embed',
      thumbnail: 'https://media.sketchfab.com/models/70e5de1e9c8245e7a1bd4e3bc3192b6e/thumbnails/85e4d9d577614bd9ae635c69569c4b75/5121aad92a55472f8c6be9b15bc5b010.jpeg'
    },
    {
      id: 'carsFlo',
      name: 'Flo 汽车',
      embedUrl: 'https://sketchfab.com/models/2d5b2127c113479ab4c1dce9b62845e8/embed',
      thumbnail: 'https://media.sketchfab.com/models/2d5b2127c113479ab4c1dce9b62845e8/thumbnails/0e0ff3c52a2941c487a80569419b03c1/ec24e7990a11426d8d4250733bd0f7d8.jpeg'
    }
  ]
  
  // 当前选择的模型和颜色（默认使用可用的低多边形汽车）
  const selectedModel = ref('lowpoly')
  const selectedColor = ref('烈焰红')
  
  // 当前模型URL（带参数）
  const currentModelUrl = computed(() => {
    const baseUrl = modelOptions.find(m => m.id === selectedModel.value)?.embedUrl || modelOptions[0].embedUrl
    
    // 添加URL参数控制模型表现
    const params = new URLSearchParams({
      autostart: 1,           // 自动开始播放
      ui_controls: 1,         // 显示UI控制
      ui_infos: 0,            // 隐藏信息面板
      ui_stop: 0,             // 隐藏停止按钮
      ui_watermark: 0,        // 隐藏水印
      ui_settings: 0,         // 隐藏设置按钮
      ui_help: 0,             // 隐藏帮助按钮
      ui_fullscreen: 0,       // 隐藏全屏按钮
      ui_annotations: 0,      // 隐藏标注
      // 注意：不能直接控制颜色，需要模型本身支持材质切换
    })
    
    return `${baseUrl}?${params.toString()}`
  })
  
  // 当前模型标题
  const currentModelTitle = computed(() => {
    return modelOptions.find(m => m.id === selectedModel.value)?.name || '3D模型'
  })
  
  // 改变颜色
  const changeColor = (color) => {
    selectedColor.value = color.name
    // 这里可以触发父组件的事件，用于更新价格等
    emit('colorChange', color)
    
    // 显示提示
    console.log(`已选择颜色: ${color.name}`)
    // 注意：Sketchfab嵌入无法直接改变模型颜色，这里只是演示
    // 如果要真正改变颜色，需要找支持颜色参数的模型或使用其他方案
  }
  
  // 改变模型
  const changeModel = (model) => {
    selectedModel.value = model.id
    emit('modelChange', model)
    console.log(`已切换模型: ${model.name}`)
  }
  
  // 设置相机视角
  const setCameraView = (view) => {
    // 通过postMessage向iframe发送控制指令
    const iframe = document.querySelector('.sketchfab-iframe')
    if (iframe && iframe.contentWindow) {
      const message = {
        type: 'view',
        view: view
      }
      iframe.contentWindow.postMessage(JSON.stringify(message), '*')
    }
    console.log(`切换视角: ${view}`)
  }
  
  // 重置视角
  const resetView = () => {
    const iframe = document.querySelector('.sketchfab-iframe')
    if (iframe && iframe.contentWindow) {
      const message = {
        type: 'reset'
      }
      iframe.contentWindow.postMessage(JSON.stringify(message), '*')
    }
  }
  
  // 定义事件
  const emit = defineEmits(['colorChange', 'modelChange'])
  
  // 组件挂载后
  onMounted(() => {
    // 监听来自Sketchfab的消息（可选）
    window.addEventListener('message', (event) => {
      if (event.data && typeof event.data === 'string') {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'modelLoaded') {
            console.log('Sketchfab模型加载完成')
          }
        } catch (e) {
          // 不是JSON消息，忽略
        }
      }
    })
  })
  </script>
  
  <style scoped>
  .sketchfab-viewer {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .model-container {
    width: 100%;
    height: 500px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    border: 1px solid #e0e0e0;
  }
  
  .sketchfab-iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
  
  .controls {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  }
  
  .controls h4 {
    margin: 0 0 15px 0;
    color: #333;
    font-size: 16px;
  }
  
  .color-picker,
  .model-picker,
  .view-controls {
    margin-bottom: 25px;
  }
  
  .color-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
  }
  
  .color-btn {
    height: 60px;
    border: 3px solid transparent;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }
  
  .color-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
  
  .color-btn.active {
    border-color: #3498db;
    box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.3);
  }
  
  .color-name {
    color: white;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
    font-size: 12px;
    font-weight: bold;
  }
  
  .model-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 15px;
  }
  
  .model-btn {
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    padding: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: white;
    text-align: center;
  }
  
  .model-btn:hover {
    border-color: #3498db;
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(52, 152, 219, 0.2);
  }
  
  .model-btn.active {
    border-color: #3498db;
    background: #f0f9ff;
  }
  
  .model-btn img {
    width: 100%;
    height: 60px;
    object-fit: contain;
    margin-bottom: 8px;
    border-radius: 4px;
  }
  
  .model-btn span {
    font-size: 12px;
    color: #333;
    display: block;
  }
  
  .view-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .view-buttons button {
    padding: 8px 16px;
    border: 1px solid #3498db;
    background: white;
    color: #3498db;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .view-buttons button:hover {
    background: #3498db;
    color: white;
  }
  
  @media (max-width: 768px) {
    .model-container {
      height: 400px;
    }
    
    .color-grid,
    .model-grid {
      grid-template-columns: repeat(3, 1fr);
    }
  }
  </style>