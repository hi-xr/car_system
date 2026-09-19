<template>
  <div class="about-page">
    <Header />
    <main class="about-main">
      <div class="container">
        <h1>关于我们</h1>
        <p class="about-desc">
          本系统用于演示「汽车个性化选配」的完整流程，从浏览车型、在线配置，到加入购物车、下单购车。
        </p>

        <!-- 新手指引（图文卡片） -->
        <section class="guide-card">
          <h2 class="guide-title">新手快速上手指引</h2>

          <div class="guide-grid">
            <div class="guide-item">
              <div class="guide-icon">
                <span class="step-index">1</span>
                <svg viewBox="0 0 24 24" class="icon">
                  <path
                    d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4 0-7 2-7 4.5A1.5 1.5 0 0 0 6.5 20h11a1.5 1.5 0 0 0 1.5-1.5C19 16 16 14 12 14Z"
                  />
                </svg>
              </div>
              <div class="guide-text">
                <h3>注册并登录账号</h3>
                <p class="guide-paragraph">
                  首次使用建议先注册并登录，这样你的配置与订单才能被系统记录下来。
                </p>
                <ul class="guide-bullets">
                  <li>右上角点击「注册」，填写用户名/手机号/地址并设置密码。</li>
                  <li>登录后可使用「在线选装」「保存方案」「加入购物车」「下单」等功能。</li>
                  <li>建议完善个人资料，便于后续订单沟通与交付联系。</li>
                </ul>
              </div>
            </div>

            <div class="guide-item">
              <div class="guide-icon">
                <span class="step-index">2</span>
                <svg viewBox="0 0 24 24" class="icon">
                  <path
                    d="M3 5.5A1.5 1.5 0 0 1 4.5 4h15A1.5 1.5 0 0 1 21 5.5v11A1.5 1.5 0 0 1 19.5 18h-15A1.5 1.5 0 0 1 3 16.5Zm10.25 0h-3.5L8 9h8Z"
                  />
                </svg>
              </div>
              <div class="guide-text">
                <h3>浏览全系车型</h3>
                <p class="guide-paragraph">
                  在开始选配前，先了解不同车型的基础信息与指导价。
                </p>
                <ul class="guide-bullets">
                  <li>进入「全系车型」，可按车型名称快速搜索。</li>
                  <li>点击车型卡片/详情页查看图片、指导价等信息，确定要选配的车型。</li>
                </ul>
              </div>
            </div>

            <div class="guide-item">
              <div class="guide-icon">
                <span class="step-index">3</span>
                <svg viewBox="0 0 24 24" class="icon">
                  <path
                    d="M4 5a2 2 0 0 1 2-2h4l2 2h6a2 2 0 0 1 2 2v2H4Zm0 4h18v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2Z"
                  />
                </svg>
              </div>
              <div class="guide-text">
                <h3>在线定制专属配置</h3>
                <p class="guide-paragraph">
                  进入「在线选装」后，系统会提供外观/内饰/性能/科技等配置项，你可以自由组合。
                </p>
                <ul class="guide-bullets">
                  <li>先选择车型，再在不同标签页里勾选配置项。</li>
                  <li>右侧会显示“已选配置清单”和当前总价，方便你随时核对。</li>
                  <li>部分配置存在互斥/依赖规则，若冲突系统会提示并自动回退。</li>
                  <li>你也可以从「热门配置方案」点击“开始配置”，系统会自动带出该方案的默认配置。</li>
                </ul>
              </div>
            </div>

            <div class="guide-item">
              <div class="guide-icon">
                <span class="step-index">4</span>
                <svg viewBox="0 0 24 24" class="icon">
                  <path
                    d="M7 6h14l-1.5 9h-11zM5 6h1.5L9 18H6L5 6Zm3 13a1.5 1.5 0 1 0 1.5 1.5A1.5 1.5 0 0 0 8 19Zm8 0a1.5 1.5 0 1 0 1.5 1.5A1.5 1.5 0 0 0 16 19Z"
                  />
                </svg>
              </div>
              <div class="guide-text">
                <h3>保存方案或加入购物车</h3>
                <p class="guide-paragraph">
                  当你对当前配置比较满意时，有两种常用方式继续下一步。
                </p>
                <ul class="guide-bullets">
                  <li>点击「保存方案」：用于暂存当前配置，后续可继续修改优化。</li>
                  <li>点击「加入购物车」：用于合并购买清单，适合马上准备下单。</li>
                  <li>建议先保存方案再加入购物车，避免忘记配置细节。</li>
                </ul>
              </div>
            </div>

            <div class="guide-item">
              <div class="guide-icon">
                <span class="step-index">5</span>
                <svg viewBox="0 0 24 24" class="icon">
                  <path
                    d="M5 4h14v2H5Zm2 4h10v2H7Zm-2 4h14v2H5Zm2 4h6v2H7Z"
                  />
                </svg>
              </div>
              <div class="guide-text">
                <h3>提交订单并跟踪进度</h3>
                <p class="guide-paragraph">
                  在购物车确认信息无误后即可下单，随后可以在订单页面跟踪进度。
                </p>
                <ul class="guide-bullets">
                  <li>进入「购物车」检查车型、配置方案摘要与金额，点击下单。</li>
                  <li>普通用户在「我的订单」查看订单状态（准备中/制造中/完成/已取消）。</li>
                  <li>如需咨询进度，点击订单上的「联系客服」给管理员留言（管理员会通过电话/邮箱联系你）。</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="guide-extra">
            <h3>小贴士</h3>
            <ul>
              <li>普通用户：建议完善「个人信息」中的电话/邮箱/地址，便于管理员联系确认交付。</li>
              <li>下单后可在「我的订单」持续关注状态变化，必要时通过“联系客服”留言咨询进度。</li>
              <li>在「个人信息」页可以查看账号资料，并上传头像。</li>
            </ul>
          </div>
        </section>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.about-main {
  flex: 1;
  padding: 60px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.about-desc {
  color: #64748b;
  font-size: 15px;
}

.guide-card {
  margin-top: 32px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 24px 24px 28px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.guide-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
  margin-bottom: 16px;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f9fafb;
}

.guide-icon {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2563eb;
  flex-shrink: 0;
}

.icon {
  width: 22px;
  height: 22px;
  fill: currentColor;
}

.step-index {
  position: absolute;
  top: -6px;
  left: -6px;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.guide-text h3 {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.guide-text p {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.guide-paragraph {
  margin: 0 0 8px 0;
}

.guide-bullets {
  margin: 0;
  padding-left: 18px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.65;
}

.guide-bullets li {
  margin: 4px 0;
}

.guide-extra h3 {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 12px 0 6px;
}

.guide-extra ul {
  padding-left: 18px;
  margin: 0;
  font-size: 14px;
  color: #4b5563;
}

@media (max-width: 768px) {
  .guide-grid {
    grid-template-columns: 1fr;
  }
}
</style>

