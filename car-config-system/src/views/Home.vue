<template>
  <div class="home">
    <Header />
    <HeroSection />
    <VehicleShowcase />
    <PopularPackages />
    <Footer />
  </div>
</template>

<script setup>
import Header from '../components/Header.vue'
import HeroSection from '../components/HeroSection.vue'
import VehicleShowcase from '../components/VehicleShowcase.vue'
import PopularPackages from '../components/PopularPackages.vue'
import Footer from '../components/Footer.vue'
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
}
</style>
