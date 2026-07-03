<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/vue/24/outline';

interface Props {
  monthLabel: string;
}

defineProps<Props>();
defineEmits(['prev', 'next']);
</script>

<template>
  <header class="flex items-center justify-between px-2 py-4">
    <h1 class="text-2xl font-bold text-gray-900 font-display">
      {{ monthLabel }}
    </h1>
    <div class="flex gap-2">
      <button 
        @click="$emit('prev')"
        class="p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-600 focus:outline-none focus:ring-2 focus:ring-viettel-red"
        aria-label="Tuần trước"
      >
        <ChevronLeftIcon class="w-6 h-6" />
      </button>
      <button 
        @click="$emit('next')"
        class="p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-600 focus:outline-none focus:ring-2 focus:ring-viettel-red"
        aria-label="Tuần sau"
      >
        <ChevronRightIcon class="w-6 h-6" />
      </button>
    </div>
  </header>
</template>
