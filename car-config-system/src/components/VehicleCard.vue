<template>
  <div class="vehicle-card">
    <div class="card-img-wrapper">
      <img :src="imgSrc" :alt="title" class="card-img">
    </div>
    <div class="card-content">
      <h3>{{ title }}</h3>
      <p class="price">起售价 {{ price }}</p>
      <button class="btn-view-detail" @click="goToDetail">查看详情</button>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'

const router = inject('router')

const props = defineProps({
  modelId: {
    type: [String, Number],
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  price: {
    type: [String, Number],
    required: true
  },
  imgSrc: {
    type: String,
    required: true
  }
})

const goToDetail = () => {
  if (props.modelId) {
    router.push(`/vehicle-detail?id=${props.modelId}`)
  }
}
</script>

<style scoped>
.vehicle-card {
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.vehicle-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-img-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f8fafc;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.vehicle-card:hover .card-img {
  transform: scale(1.05);
}

.card-content {
  padding: 20px;
}

.card-content h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1e293b;
}

.price {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 16px;
}

.btn-view-detail {
  width: 100%;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #2563eb;
  background-color: transparent;
  border: 1px solid #2563eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-view-detail:hover {
  background-color: #2563eb;
  color: #fff;
}
</style>
