<script setup lang="ts">
import { computed } from 'vue';
import { ArrowPathIcon } from '@heroicons/vue/24/outline';
import type { Booking } from '../../types/booking';
import { useRoomStore } from '../../stores/roomStore';

interface Props {
  booking: Booking;
}

const props = defineProps<Props>();
const roomStore = useRoomStore();

const timeRange = computed(() => {
  const start = props.booking.start_time.split(' ')[1]?.slice(0, 5) || '';
  const end = props.booking.end_time.split(' ')[1]?.slice(0, 5) || '';
  return `${start} - ${end}`;
});

const roomName = computed(() => {
  const room = roomStore.rooms.find(r => r.id === props.booking.room_id);
  return room ? room.name : `Phòng #${props.booking.room_id}`;
});
</script>

<template>
  <div 
    class="flex gap-4 py-4 px-3 rounded-2xl hover:bg-gray-50 transition-all duration-200 cursor-pointer group"
  >
    <div class="flex flex-col items-end min-w-[60px]">
      <span class="text-xs font-bold text-gray-900">{{ timeRange }}</span>
    </div>
    
    <div class="border-l-4 border-viettel-red pl-4 flex-1">
      <div class="flex items-center justify-between gap-2">
        <h3 class="text-sm font-bold text-gray-900 group-hover:text-viettel-red-dark transition-colors">
          {{ booking.description }}
        </h3>
        <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-md bg-gray-50 text-gray-400">
          {{ booking.status }}
        </span>
      </div>
      <p class="text-xs text-gray-500 mt-1 font-medium">
        {{ roomName }}
      </p>
    </div>
  </div>
</template>
