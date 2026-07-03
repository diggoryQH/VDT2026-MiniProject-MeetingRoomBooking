<script setup lang="ts">
interface Props {
  label: string;
  modelValue: string | number;
  type?: string;
  placeholder?: string;
  error?: string;
  disabled?: boolean;
  id?: string;
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  error: '',
  disabled: false
});

defineEmits(['update:modelValue']);
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label v-if="label" :for="id" class="text-sm font-medium text-gray-600 ml-1">
      {{ label }}
    </label>
    <div class="relative">
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-base text-gray-900 placeholder-gray-400 outline-none transition-all focus:ring-2 focus:ring-viettel-red focus:border-viettel-red disabled:opacity-50 disabled:bg-gray-100"
        :class="{ 'border-red-500 focus:ring-red-200': error }"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
      <slot name="suffix" />
    </div>
    <span v-if="error" class="text-xs text-red-500 ml-1 mt-0.5">
      {{ error }}
    </span>
  </div>
</template>
