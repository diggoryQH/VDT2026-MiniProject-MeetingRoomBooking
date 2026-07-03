<script setup lang="ts">
import AppInput from '../shared/AppInput.vue';

interface Props {
  modelValue: string;
  error?: string;
  label?: string;
}

withDefaults(defineProps<Props>(), {
  label: 'Số điện thoại',
  error: ''
});

defineEmits(['update:modelValue']);
</script>

<template>
  <div class="relative">
    <AppInput
      :label="label"
      :model-value="modelValue"
      placeholder="0987 654 321"
      :error="error"
      class="pl-16"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <div class="absolute left-4 top-[42px] -translate-y-1/2 flex items-center gap-1.5 text-base font-medium text-gray-900 border-r border-gray-200 pr-3 pointer-events-none">
      <span class="text-xl leading-none">🇻🇳</span>
      <span class="text-sm">+84</span>
    </div>
  </div>
</template>

<style scoped>
:deep(input) {
  padding-left: 5rem !important;
}
</style>
