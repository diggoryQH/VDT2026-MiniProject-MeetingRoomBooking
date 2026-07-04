<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useBookingStore } from '@/stores/bookingStore';
import { useRoomStore } from '@/stores/roomStore';
import { useAuthStore } from '@/stores/authStore';
import BottomNav from '@/components/layout/BottomNav.vue';
import AppButton from '@/components/shared/AppButton.vue';
import BookingFilterBar from '@/components/shared/BookingFilterBar.vue';
import { 
  CheckCircleIcon, XCircleIcon, DocumentMagnifyingGlassIcon,
  DocumentArrowDownIcon
} from '@heroicons/vue/24/outline';
import apiClient from '@/api/axios';

const bookingStore = useBookingStore();
const roomStore = useRoomStore();
const authStore = useAuthStore();

const filters = ref({
  status: '',
  roomId: null as number | null,
  startDate: '',
  endDate: ''
});

const fetchPage = async (page: number) => {
  const payload: any = { ...filters.value };
  if (payload.startDate) payload.startDate += ' 00:00:00';
  if (payload.endDate) payload.endDate += ' 23:59:59';
  await bookingStore.fetchBookings(page, 20, false, payload);
};

const loadBookings = async () => {
  await fetchPage(1);
};

onMounted(async () => {
  await roomStore.fetchRooms();
  await loadBookings();
});

const handleFilterChange = (newFilters: any) => {
  filters.value = newFilters;
  fetchPage(1);
};

const canApprove = (booking: any): boolean => {
  const role = authStore.user?.role;
  if (role === 'ADMIN' || role === 'SUPERADMIN') return true;
  if (role === 'APPROVER') {
    const userDeptId = authStore.user?.department_id;
    return userDeptId != null && booking.room_department_id === userDeptId;
  }
  return false;
};

const handleApprove = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn duyệt lịch này?')) {
    await bookingStore.approveBooking(id);
    loadBookings();
  }
};

const handleReject = async (id: number) => {
  const reason = prompt('Vui lòng nhập lý do từ chối:');
  if (reason !== null) {
    await bookingStore.rejectBooking(id, reason);
    loadBookings();
  }
};

const handleExport = async () => {
  try {
    const params: any = { ...filters.value };
    if (params.startDate) params.startDate += ' 00:00:00';
    if (params.endDate) params.endDate += ' 23:59:59';

    const response = await apiClient.get('/api/v1/export/bookings', {
      params,
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `bookings_export_${new Date().toISOString().split('T')[0]}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (err: any) {
    console.error('Failed to export', err);
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

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  const [date, time] = dateStr.split(' ');
  return `${time?.substring(0, 5) || ''} - ${date}`;
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'bg-yellow-100 text-yellow-800';
    case 'APPROVED': return 'bg-green-100 text-green-800';
    case 'REJECTED': return 'bg-red-100 text-red-800';
    case 'CANCELLED': return 'bg-gray-100 text-gray-800';
    case 'CHECKED_IN': return 'bg-blue-100 text-blue-800';
    case 'COMPLETED': return 'bg-purple-100 text-purple-800';
    case 'NO_SHOW': return 'bg-orange-100 text-orange-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'PENDING': return 'Chờ duyệt';
    case 'APPROVED': return 'Đã duyệt';
    case 'REJECTED': return 'Từ chối';
    case 'CANCELLED': return 'Đã hủy';
    case 'CHECKED_IN': return 'Đã check-in';
    case 'COMPLETED': return 'Hoàn thành';
    case 'NO_SHOW': return 'Không đến';
    default: return status;
  }
};
</script>


<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <!-- Header -->
    <header class="bg-white/80 backdrop-blur-lg sticky top-0 z-30 border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <h1 class="text-xl font-bold text-gray-900 font-display">Quản lý Lịch đặt phòng</h1>
        <AppButton 
          variant="outline" 
          size="sm" 
          @click="handleExport"
          label="Xuất Excel"
        >
          <template #icon>
            <DocumentArrowDownIcon class="w-5 h-5" />
          </template>
          Xuất Excel
        </AppButton>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 py-8">
      <!-- Filter Bar -->
      <BookingFilterBar 
        :rooms="roomStore.rooms"
        @filter="handleFilterChange"
      />

      <!-- Table -->
      <div class="mt-8 bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-gray-50 text-gray-600 text-sm border-b border-gray-100">
                <th class="p-4 font-medium">Tiêu đề</th>
                <th class="p-4 font-medium">Người đặt</th>
                <th class="p-4 font-medium">Phòng</th>
                <th class="p-4 font-medium">Thời gian</th>
                <th class="p-4 font-medium">Người duyệt</th>
                <th class="p-4 font-medium">Trạng thái</th>
                <th class="p-4 font-medium text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="booking in bookingStore.bookings" 
                :key="booking.id"
                class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
              >
                <td class="p-4">
                  <div class="font-medium text-gray-900">{{ booking.title || booking.description }}</div>
                  <div class="text-xs text-gray-500">{{ booking.attendee_count }} người</div>
                </td>
                <td class="p-4 text-gray-600">{{ booking.user_name }}</td>
                <td class="p-4 text-gray-600">{{ booking.room_name }}</td>
                <td class="p-4">
                  <div class="text-gray-900">{{ formatDateTime(booking.start_time) }}</div>
                  <div class="text-gray-500 text-sm">đến {{ formatDateTime(booking.end_time) }}</div>
                </td>
                <td class="p-4 text-gray-600">{{ booking.approved_by_name || '-' }}</td>
                <td class="p-4">
                  <span 
                    class="px-2.5 py-1 rounded-full text-xs font-medium"
                    :class="getStatusColor(booking.status)"
                  >
                    {{ getStatusLabel(booking.status) }}
                  </span>
                </td>
                <td class="p-4 flex justify-end gap-2">
                  <template v-if="booking.status === 'PENDING' && canApprove(booking)">
                    <button 
                      @click="handleApprove(booking.id)"
                      class="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                      title="Duyệt"
                    >
                      <CheckCircleIcon class="w-5 h-5" />
                    </button>
                    <button 
                      @click="handleReject(booking.id)"
                      class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Từ chối"
                    >
                      <XCircleIcon class="w-5 h-5" />
                    </button>
                  </template>
                </td>
              </tr>
              <tr v-if="bookingStore.bookings.length === 0">
                <td colspan="7" class="p-8 text-center text-gray-500">
                  <div class="flex flex-col items-center justify-center gap-2">
                    <DocumentMagnifyingGlassIcon class="w-12 h-12 text-gray-300" />
                    <p>Không tìm thấy lịch đặt nào</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- Pagination -->
        <div class="p-4 border-t border-gray-100 flex items-center justify-between" v-if="bookingStore.totalPages > 1">
          <span class="text-sm text-gray-500">Trang {{ bookingStore.currentPage }} / {{ bookingStore.totalPages }}</span>
          <div class="flex gap-2">
            <AppButton 
              variant="outline" 
              size="sm" 
              :disabled="bookingStore.currentPage === 1"
              @click="fetchPage(bookingStore.currentPage - 1)"
              label="Trước"
            >
              Trước
            </AppButton>
            <AppButton 
              variant="outline" 
              size="sm" 
              :disabled="bookingStore.currentPage === bookingStore.totalPages"
              @click="fetchPage(bookingStore.currentPage + 1)"
              label="Sau"
            >
              Sau
            </AppButton>
          </div>
        </div>
      </div>
    </main>

    <BottomNav />
  </div>
</template>
