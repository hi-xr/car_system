<template>
  <section class="hero">
    <div class="hero-overlay"></div>
    <div class="hero-container">
      <div class="hero-content">
        <h1>打造您的专属座驾</h1>
        <p>从外观到内饰，从性能到科技，全方位定制您的理想车型。超过1000种<br>个性化配置选择，满足您的独特品味。</p>
        <div class="hero-actions">
          <router-link to="/configure" class="btn-primary-hero">立即定制</router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
// 立即定制按钮通过 router-link 跳转到 /configure
</script>

<style scoped>
.hero {
  height: 480px;
  background-image: url('https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1920&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to right, rgba(30, 41, 59, 0.85) 0%, rgba(30, 41, 59, 0.6) 50%, rgba(30, 41, 59, 0.3) 100%);
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
}

.hero-content {
  max-width: 550px;
}

.hero-content h1 {
  font-size: 42px;
  line-height: 1.2;
  margin-bottom: 20px;
  color: #fff;
  font-weight: 700;
}

.hero-content p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 32px;
  line-height: 1.8;
}

.btn-primary-hero {
  display: inline-block;
  background-color: #2563eb;
  color: #fff;
  padding: 14px 28px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary-hero:hover {
  background-color: #1d4ed8;
}

.btn-secondary-hero {
  background-color: transparent;
  color: #fff;
  padding: 14px 28px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary-hero:hover {
  border-color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .hero {
    height: 400px;
  }
  
  .hero-content h1 {
    font-size: 28px;
  }
  
  .hero-content p {
    font-size: 14px;
  }
  
  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
