<script setup lang="ts">
interface Props {
  text?: string;
}

withDefaults(defineProps<Props>(), {
  text: 'Or Sign In With'
});
</script>

<template>
  <div class="flex items-center gap-4 my-4">
    <div class="flex-1 h-px bg-gray-200"></div>
    <span v-if="text" class="text-xs text-gray-400 font-medium whitespace-nowrap">
      {{ text }}
    </span>
    <div class="flex-1 h-px bg-gray-200"></div>
  </div>
</template>
