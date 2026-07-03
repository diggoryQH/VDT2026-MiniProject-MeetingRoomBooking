<script setup lang="ts">
interface Props {
  days: Date[];
  selectedDate: Date;
}

const props = defineProps<Props>();
const emit = defineEmits(['select']);

const isToday = (date: Date) => {
  const today = new Date();
  return date.toDateString() === today.toDateString();
};

const isSelected = (date: Date) => {
  return date.toDateString() === props.selectedDate.toDateString();
};

const getDayInitial = (date: Date) => {
  return date.toLocaleString('vi-VN', { weekday: 'narrow' });
};
</script>

<template>
  <div class="grid grid-cols-7 gap-1 py-2 px-1">
    <div 
      v-for="date in days" 
      :key="date.toISOString()"
      class="flex flex-col items-center"
    >
      <span class="text-xs font-medium text-gray-400 uppercase mb-2">
        {{ getDayInitial(date) }}
      </span>
      <button
        @click="emit('select', date)"
        class="w-10 h-10 flex items-center justify-center rounded-full text-sm font-semibold transition-all duration-200"
        :class="[
          isToday(date) 
            ? 'bg-viettel-red text-white shadow-sm' 
            : isSelected(date)
              ? 'bg-gray-100 text-gray-900'
              : 'text-gray-700 hover:bg-gray-50'
        ]"
      >
        {{ date.getDate() }}
      </button>
    </div>
  </div>
</template>
