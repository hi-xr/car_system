import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
   proxy: {
      // 所有以 /api 开头的请求，都代理到后端
      '/api': {
        target: 'http://localhost:8888', // 后端地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api') // 保留 /api 前缀
      },
      // 让前端开发环境能访问后端静态文件（如数据库里的 car_image 指向 /uploads 或 /static）
      '/uploads': {
        target: 'http://localhost:8888',
        changeOrigin: true
      },
      '/static': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    } // 若 3000 被占用则自动尝试下一端口
  },
})
