<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  userName: string;
}

defineProps<Props>();

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return 'Chào buổi sáng';
  if (hour < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
});

const todayDateLabel = computed(() => {
  return new Date().toLocaleDateString('vi-VN', { 
    weekday: 'long', 
    day: 'numeric', 
    month: 'long', 
    year: 'numeric' 
  });
});
</script>

<template>
  <div class="flex flex-col gap-1">
    <h1 class="text-2xl font-bold font-display text-gray-900">
      {{ greeting }}, <span class="text-viettel-red-dark">{{ userName }}</span> 👋
    </h1>
    <p class="text-sm text-gray-400 font-medium">
      {{ todayDateLabel }}
    </p>
  </div>
</template>
