<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import viLocale from '@fullcalendar/core/locales/vi';
import { PlusIcon, ArrowDownTrayIcon } from '@heroicons/vue/24/solid';
import { useBookingStore } from '@/stores/bookingStore';
import { useAuthStore } from '@/stores/authStore';
import CreateBookingModal from '@/components/calendar/CreateBookingModal.vue';
import BookingDetailModal from '@/components/calendar/BookingDetailModal.vue';
import BookingFilterBar from '@/components/shared/BookingFilterBar.vue';
import apiClient from '@/api/axios';

const bookingStore = useBookingStore();
const authStore = useAuthStore();

const isCreateModalOpen = ref(false);
const isDetailModalOpen = ref(false);
const selectedBooking = ref<any>(null);
const selectedDateForCreate = ref(new Date().toISOString().split('T')[0]);
const currentViewStart = ref('');
const currentViewEnd = ref('');
const currentFilters = ref<Record<string, any>>({});

onMounted(() => {
  // Initial load will be triggered by FullCalendar's datesSet event
});

const getEventColor = (status: string) => {
  switch (status.toUpperCase()) {
    case 'PENDING': return '#eab308'; // yellow-500
    case 'APPROVED': return '#22c55e'; // green-500
    case 'REJECTED': return '#ef4444'; // red-500
    case 'CANCELLED': return '#9ca3af'; // gray-400
    default: return '#3b82f6'; // blue-500
  }
};

const fetchCalendarData = async () => {
  if (!currentViewStart.value || !currentViewEnd.value) return;
  
  const params: any = { ...currentFilters.value };
  if (params.startDate) {
    params.startDate += ' 00:00:00';
  } else {
    params.startDate = currentViewStart.value + ' 00:00:00';
  }
  
  if (params.endDate) {
    params.endDate += ' 23:59:59';
  } else {
    params.endDate = currentViewEnd.value + ' 23:59:59';
  }
  
  await bookingStore.fetchBookings(1, 500, false, params);
};

const handleDatesSet = (arg: any) => {
  currentViewStart.value = arg.startStr.split('T')[0];
  // arg.end is exclusive in fullcalendar, so subtract 1 day to get the visual end
  const end = new Date(arg.end);
  end.setDate(end.getDate() - 1);
  currentViewEnd.value = end.toISOString().split('T')[0];
  
  fetchCalendarData();
};

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  locale: viLocale,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  events: bookingStore.bookings.map(b => ({
    id: String(b.id),
    title: b.description + (b.room_name ? ` (${b.room_name})` : ''),
    start: b.start_time.replace(' ', 'T'),
    end: b.end_time.replace(' ', 'T'),
    backgroundColor: getEventColor(b.status),
    borderColor: getEventColor(b.status),
    extendedProps: { ...b }
  })),
  eventClick: (info: any) => {
    selectedBooking.value = info.event.extendedProps;
    isDetailModalOpen.value = true;
  },
  dateClick: (info: any) => {
    selectedDateForCreate.value = info.dateStr.split('T')[0];
    isCreateModalOpen.value = true;
  },
  height: 'auto',
  slotMinTime: '08:00:00',
  slotMaxTime: '20:00:00',
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  },
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  },
  datesSet: handleDatesSet
}));

const onFilter = (filters: any) => {
  currentFilters.value = filters;
  fetchCalendarData();
};

const exportToExcel = async () => {
  try {
    const params: any = { ...currentFilters.value };
    if (params.startDate) {
      params.startDate += ' 00:00:00';
    } else if (currentViewStart.value) {
      params.startDate = currentViewStart.value + ' 00:00:00';
    }
    
    if (params.endDate) {
      params.endDate += ' 23:59:59';
    } else if (currentViewEnd.value) {
      params.endDate = currentViewEnd.value + ' 23:59:59';
    }
    
    const response = await apiClient.get('/api/v1/export/bookings', {
      params,
      responseType: 'blob'
    });
    
    // Create a download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    link.setAttribute('download', `bookings_${date}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (err: any) {
    console.error('Export failed', err);
    let errorMessage = 'Vui lòng thử lại.';
    if (err.response?.data instanceof Blob) {
      try {
        const text = await err.response.data.text();
        const errorData = JSON.parse(text);
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (e) {}
    } else if (err.message) {
      errorMessage = err.message;
    }
    alert('Lỗi xuất file: ' + errorMessage);
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-32">
    <!-- Header -->
    <div class="bg-white px-4 py-6 shadow-sm sticky top-0 z-20">
      <div class="max-w-4xl mx-auto flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Lịch</h1>
          <p class="text-sm text-gray-500">Xem và quản lý phòng họp</p>
        </div>
        
        <div class="flex gap-2">
          <button 
            @click="exportToExcel"
            class="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 font-bold text-sm transition-colors shadow-sm"
          >
            <ArrowDownTrayIcon class="w-4 h-4" /> Xuất file
          </button>
        </div>
      </div>
    </div>

    <main class="max-w-4xl mx-auto px-4 mt-6">
      <BookingFilterBar @filter="onFilter" />

      <div class="bg-white p-4 md:p-6 rounded-3xl shadow-sm border border-gray-100">
        <div v-if="bookingStore.isLoading && bookingStore.bookings.length === 0" class="h-96 flex items-center justify-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-viettel-red"></div>
        </div>
        <FullCalendar v-else :options="calendarOptions" />
      </div>
    </main>

    <!-- FAB -->
    <div class="fixed bottom-24 left-1/2 -translate-x-1/2 w-full max-w-4xl pointer-events-none z-40">
      <div class="relative w-full h-full flex justify-end px-8">
        <button 
          @click="isCreateModalOpen = true"
          class="w-14 h-14 bg-viettel-red text-white rounded-full shadow-lg flex items-center justify-center hover:bg-viettel-red-dark hover:scale-105 transition-all duration-300 pointer-events-auto focus:outline-none focus:ring-4 focus:ring-viettel-red/30"
          aria-label="Thêm lịch đặt"
        >
          <PlusIcon class="w-8 h-8" />
        </button>
      </div>
    </div>

    <CreateBookingModal 
      :is-open="isCreateModalOpen" 
      :selected-date="selectedDateForCreate" 
      @close="isCreateModalOpen = false"
      @created="fetchCalendarData"
    />

    <!-- Detail Modal -->
    <BookingDetailModal
      :is-open="isDetailModalOpen"
      :booking="selectedBooking"
      @close="isDetailModalOpen = false"
      @updated="fetchCalendarData"
    />
  </div>
</template>

<style>
/* FullCalendar custom styles */
.fc {
  font-family: inherit;
}
.fc-toolbar-title {
  font-size: 1.25rem !important;
  font-weight: 700 !important;
}
.fc-button-primary {
  background-color: #f3f4f6 !important;
  border-color: #e5e7eb !important;
  color: #374151 !important;
  text-transform: capitalize !important;
  font-weight: 600 !important;
  border-radius: 0.5rem !important;
}
.fc-button-primary:not(:disabled):active,
.fc-button-primary:not(:disabled).fc-button-active {
  background-color: #EE0033 !important;
  border-color: #EE0033 !important;
  color: #FFFFFF !important;
}
.fc-event {
  cursor: pointer;
  border-radius: 4px;
  padding: 2px 4px;
}
</style>
