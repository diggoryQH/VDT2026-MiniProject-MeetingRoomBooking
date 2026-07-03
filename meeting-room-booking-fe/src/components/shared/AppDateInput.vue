<script setup lang="ts">
import { CalendarIcon } from '@heroicons/vue/24/outline';

interface Props {
  label: string;
  modelValue: string;
  error?: string;
  disabled?: boolean;
}

defineProps<Props>();
defineEmits(['update:modelValue']);
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label class="text-sm font-medium text-gray-600 ml-1">
      {{ label }}
    </label>
    <div class="relative">
      <input
        type="date"
        :value="modelValue"
        :disabled="disabled"
        class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-base text-gray-900 placeholder-gray-400 outline-none transition-all focus:ring-2 focus:ring-viettel-red focus:border-viettel-red disabled:opacity-50 appearance-none"
        :class="{ 'border-red-500 focus:ring-red-200': error }"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
      <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
        <CalendarIcon class="w-5 h-5" />
      </div>
    </div>
    <span v-if="error" class="text-xs text-red-500 ml-1 mt-0.5">
      {{ error }}
    </span>
  </div>
</template>

<style scoped>
input::-webkit-calendar-picker-indicator {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: auto;
  height: auto;
  color: transparent;
  background: transparent;
}
</style>
