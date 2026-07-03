<script setup lang="ts">
interface Props {
  label: string;
  variant?: 'primary' | 'outline' | 'social';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  isLoading?: boolean;
  fullWidth?: boolean;
  type?: 'button' | 'submit' | 'reset';
}

withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  isLoading: false,
  fullWidth: true,
  type: 'button'
});

defineEmits(['click']);
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || isLoading"
    class="flex items-center justify-center gap-2 transition-all duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed relative"
    :class="[
      variant === 'primary' ? 'bg-viettel-red text-white font-semibold rounded-3xl hover:bg-viettel-red-dark' : '',
      variant === 'outline' ? 'bg-white border border-gray-200 text-gray-700 font-medium rounded-3xl hover:bg-gray-50' : '',
      variant === 'social' ? 'bg-white border border-gray-200 text-gray-700 font-medium rounded-xl hover:bg-gray-50' : '',
      size === 'sm' ? 'py-2 px-4 text-sm' : 'py-3 px-4',
      fullWidth ? 'w-full' : ''
    ]"
    @click="$emit('click')"
  >
    <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-inherit rounded-inherit">
      <div class="w-5 h-5 border-2 border-gray-900/30 border-t-gray-900 rounded-full animate-spin"></div>
    </div>

    <div class="flex items-center gap-2" :class="{ 'opacity-0': isLoading }">
      <slot name="icon" />
      <span>{{ label }}</span>
    </div>
  </button>
</template>
