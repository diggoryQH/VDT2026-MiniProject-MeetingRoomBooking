<script setup lang="ts">
import AppInput from '../shared/AppInput.vue';

interface Props {
  modelValue: string;
  error?: string;
  label?: string;
}

withDefaults(defineProps<Props>(), {
  label: 'Email',
  error: ''
});

defineEmits(['update:modelValue']);
</script>

<template>
  <AppInput
    :label="label"
    :model-value="modelValue"
    type="email"
    placeholder="nguyenvana@gmail.com"
    :error="error"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>
