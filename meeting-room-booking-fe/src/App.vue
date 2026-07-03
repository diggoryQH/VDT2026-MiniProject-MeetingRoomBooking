<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import BottomNav from '@/components/layout/BottomNav.vue'

const route = useRoute()

// Only show bottom nav on authenticated (non-guest) pages
const showBottomNav = computed(() => {
  return route.meta.auth === true
})
</script>

<template>
  <div class="min-h-screen bg-white">
    <RouterView />
    <BottomNav v-if="showBottomNav" />
  </div>
</template>
