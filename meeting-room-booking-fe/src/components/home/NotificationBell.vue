<script setup lang="ts">
import { BellIcon } from '@heroicons/vue/24/outline';

interface Props {
  count: number;
}

defineProps<Props>();
defineEmits(['click']);
</script>

<template>
  <button 
    @click="$emit('click')"
    class="relative p-2 rounded-2xl hover:bg-gray-50 transition-all text-gray-500 hover:text-gray-900 active:scale-95"
    aria-label="Xem thông báo"
  >
    <BellIcon class="w-7 h-7" />
    
    <!-- Badge -->
    <Transition name="scale">
      <div 
        v-if="count > 0" 
        class="absolute top-1.5 right-1.5 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center border-2 border-white shadow-sm pointer-events-none"
      >
        {{ count > 9 ? '9+' : count }}
      </div>
    </Transition>
  </button>
</template>

<style scoped>
.scale-enter-active,
.scale-leave-active {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.scale-enter-from,
.scale-leave-to {
  transform: scale(0);
}
</style>
