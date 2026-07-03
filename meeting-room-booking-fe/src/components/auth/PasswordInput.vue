<script setup lang="ts">
import { ref } from 'vue';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/outline';
import AppInput from '../shared/AppInput.vue';

interface Props {
  label?: string;
  modelValue: string;
  placeholder?: string;
  error?: string;
  id?: string;
}

withDefaults(defineProps<Props>(), {
  label: 'Mật khẩu',
  placeholder: '••••••••',
  error: '',
  id: 'password'
});

defineEmits(['update:modelValue']);

const showPassword = ref(false);
const toggleVisibility = () => {
  showPassword.value = !showPassword.value;
};
</script>

<template>
  <AppInput
    :id="id"
    :label="label"
    :model-value="modelValue"
    :type="showPassword ? 'text' : 'password'"
    :placeholder="placeholder"
    :error="error"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #suffix>
      <button
        type="button"
        class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
        @click="toggleVisibility"
      >
        <EyeIcon v-if="!showPassword" class=" w-5 h-5" />
        <EyeSlashIcon v-else class="w-5 h-5" />
      </button>
    </template>
  </AppInput>
</template>
