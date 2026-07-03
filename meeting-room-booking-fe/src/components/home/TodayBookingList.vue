<script setup lang="ts">
import { CalendarDaysIcon } from '@heroicons/vue/24/outline';
import BookingCard from '@/components/calendar/BookingCard.vue';
import type { Booking } from '@/types/booking';

interface Props {
  bookings: Booking[];
  isLoading: boolean;
}

defineProps<Props>();
</script>

<template>
  <div class="flex flex-col gap-4 mt-8">
    <div class="flex items-center justify-between px-1">
      <h2 class="text-xl font-bold font-display text-gray-900">Lịch hôm nay</h2>
      <span v-if="bookings.length > 0" class="text-xs font-bold text-gray-400 uppercase tracking-widest bg-gray-50 px-2 py-1 rounded-lg">
        {{ bookings.length }} cuộc họp
      </span>
    </div>

    <!-- Loading State (Skeleton) -->
    <div v-if="isLoading" class="flex flex-col gap-3">
      <div v-for="i in 3" :key="i" class="h-24 bg-gray-50 rounded-3xl animate-pulse relative overflow-hidden">
        <div class="absolute inset-0 bg-linear-to-r from-transparent via-white/50 to-transparent -translate-x-full animate-[shimmer_1.5s_infinite]"></div>
        <div class="flex flex-col gap-2 p-4">
          <div class="h-4 w-3/4 bg-gray-200 rounded-lg"></div>
          <div class="h-3 w-1/2 bg-gray-100 rounded-lg"></div>
        </div>
      </div>
    </div>

    <!-- Bookings List -->
    <div v-else-if="bookings.length > 0" class="flex flex-col gap-2">
      <BookingCard 
        v-for="booking in bookings" 
        :key="booking.id" 
        :booking="booking"
        class="bg-white border border-gray-50 shadow-sm hover:shadow-md transition-shadow duration-300"
      />
    </div>

    <!-- Empty State -->
    <div v-else class="flex flex-col items-center text-center py-16 px-6 bg-gray-50 rounded-3xl border border-dashed border-gray-200">
      <div class="w-20 h-20 bg-white rounded-full shadow-sm flex items-center justify-center mb-6">
        <CalendarDaysIcon class="w-10 h-10 text-gray-200" />
      </div>
      <h3 class="text-lg font-bold text-gray-900 font-display">Không có cuộc họp nào hôm nay!</h3>
      <p class="text-sm text-gray-400 mt-2 max-w-[200px]">
        Lịch của bạn trống — hãy tận hưởng thời gian rảnh nhé! 🎉
      </p>
    </div>
  </div>
</template>
