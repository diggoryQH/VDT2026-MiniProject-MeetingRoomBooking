<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';

interface Props {
  modelValue: string;
  length?: number;
}

const props = withDefaults(defineProps<Props>(), {
  length: 4
});

const emit = defineEmits(['update:modelValue', 'complete']);

const digits = ref<string[]>(new Array(props.length).fill(''));
const inputRefs = ref<HTMLInputElement[]>([]);

const handleInput = (index: number, event: Event) => {
  const input = event.target as HTMLInputElement;
  const value = input.value.slice(-1); // Only take the last character
  
  if (value && !/^\d$/.test(value)) {
    digits.value[index] = '';
    return;
  }

  digits.value[index] = value;
  emitUpdate();

  if (value && index < props.length - 1) {
    inputRefs.value[index + 1]?.focus();
  }
};

const handleKeyDown = (index: number, event: KeyboardEvent) => {
  if (event.key === 'Backspace' && !digits.value[index] && index > 0) {
    inputRefs.value[index - 1]?.focus();
  }
};

const emitUpdate = () => {
  const code = digits.value.join('');
  emit('update:modelValue', code);
  if (code.length === props.length) {
    emit('complete', code);
  }
};

watch(() => props.modelValue, (newVal) => {
  if (!newVal) {
    digits.value = new Array(props.length).fill('');
  }
});

onMounted(() => {
  inputRefs.value[0]?.focus();
});
</script>

<template>
  <div class="flex justify-between gap-4">
    <input
      v-for="(digit, index) in digits"
      :key="index"
      :ref="(el: any) => inputRefs[index] = el"
      type="text"
      inputmode="numeric"
      maxlength="1"
      :value="digit"
      class="w-14 h-14 bg-white border border-gray-200 rounded-xl text-center text-xl font-bold text-gray-900 outline-none transition-all focus:ring-2 focus:ring-viettel-red focus:border-viettel-red focus:bg-viettel-red-light"
      @input="handleInput(index, $event)"
      @keydown="handleKeyDown(index, $event)"
    />
  </div>
</template>
