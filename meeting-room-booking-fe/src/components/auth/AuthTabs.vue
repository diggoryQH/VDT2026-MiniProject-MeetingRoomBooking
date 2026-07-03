<script setup lang="ts">
interface Tab {
  value: string;
  label: string;
}

interface Props {
  modelValue: string;
  tabs: Tab[];
}

defineProps<Props>();
defineEmits(['update:modelValue']);
</script>

<template>
  <div class="bg-gray-100 rounded-3xl p-1 flex w-full">
    <button
      v-for="tab in tabs"
      :key="tab.value"
      class="flex-1 py-2.5 px-4 text-sm transition-all duration-200 cursor-pointer"
      :class="[
        modelValue === tab.value 
          ? 'bg-viettel-red text-white font-semibold rounded-3xl shadow-sm' 
          : 'text-gray-400 hover:text-gray-600'
      ]"
      @click="$emit('update:modelValue', tab.value)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>
