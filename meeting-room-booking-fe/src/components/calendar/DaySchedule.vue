<script setup lang="ts">
import BookingCard from './BookingCard.vue';
import type { DayScheduleData } from '../../types/booking';

interface Props {
  dayData: DayScheduleData;
  index: number;
}

defineProps<Props>();
</script>

<template>
  <section 
    class="py-6 border-b border-gray-100 last:border-0 stagger-item"
    :style="{ animationDelay: `${index * 0.1}s` }"
  >
    <div class="flex items-baseline gap-2 mb-4 px-2">
      <h2 class="text-xl font-bold text-gray-900 font-display">
        {{ dayData.dateLabel }}
      </h2>
      <span 
        class="text-lg font-medium"
        :class="dayData.dayLabel === 'Today' ? 'text-viettel-red' : 'text-gray-400'"
      >
        {{ dayData.dayLabel }}
      </span>
    </div>

    <div v-if="dayData.bookings.length > 0" class="flex flex-col gap-2">
      <BookingCard 
        v-for="booking in dayData.bookings" 
        :key="booking.id" 
        :booking="booking" 
      />
    </div>
    
    <div v-else class="px-2 py-4">
      <p class="text-sm text-gray-400 italic">Không có cuộc họp nào</p>
    </div>
  </section>
</template>
