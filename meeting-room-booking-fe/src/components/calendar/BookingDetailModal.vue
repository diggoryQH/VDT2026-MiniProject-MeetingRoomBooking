<script setup lang="ts">
import { ref } from 'vue';
import { 
  XMarkIcon, 
  CalendarIcon, 
  ClockIcon, 
  MapPinIcon, 
  UserCircleIcon,
  CheckCircleIcon,
  XCircleIcon,
  TrashIcon
} from '@heroicons/vue/24/outline';
import { useAuthStore } from '@/stores/authStore';
import { useBookingStore } from '@/stores/bookingStore';

interface Props {
  isOpen: boolean;
  booking: any;
}

const props = defineProps<Props>();
const emit = defineEmits(['close', 'updated']);

const authStore = useAuthStore();
const bookingStore = useBookingStore();
const isSubmitting = ref(false);
const rejectReason = ref('');
const isRejecting = ref(false);

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString.replace(' ', 'T'));
  return date.toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
};

const formatTime = (timeString: string) => {
  if (!timeString) return '';
  return timeString.split(' ')[1]?.slice(0, 5) || '';
};

const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'APPROVED': return 'bg-green-100 text-green-800 border-green-200';
    case 'REJECTED': return 'bg-red-100 text-red-800 border-red-200';
    case 'CANCELLED': return 'bg-gray-100 text-gray-800 border-gray-200';
    default: return 'bg-blue-100 text-blue-800 border-blue-200';
  }
};

const getStatusText = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'Chờ duyệt';
    case 'APPROVED': return 'Đã duyệt';
    case 'REJECTED': return 'Từ chối';
    case 'CANCELLED': return 'Đã hủy';
    default: return status;
  }
};

const canApproveReject = () => {
  const role = authStore.user?.role;
  return role === 'ADMIN' || role === 'SUPERADMIN' || role === 'APPROVER';
};

const canCancel = () => {
  const role = authStore.user?.role;
  if (role === 'ADMIN' || role === 'SUPERADMIN') return true;
  return authStore.user?.id === props.booking?.user_id;
};

const handleApprove = async () => {
  if (!props.booking) return;
  isSubmitting.value = true;
  try {
    await bookingStore.approveBooking(props.booking.id);
    emit('updated');
    emit('close');
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};

const handleReject = async () => {
  if (!props.booking) return;
  isSubmitting.value = true;
  try {
    await bookingStore.rejectBooking(props.booking.id, rejectReason.value);
    emit('updated');
    emit('close');
    isRejecting.value = false;
    rejectReason.value = '';
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};

const isCanceling = ref(false);
const cancelReason = ref('');

const handleCancel = async () => {
  if (!props.booking) return;
  isSubmitting.value = true;
  try {
    await bookingStore.cancelBooking(props.booking.id, cancelReason.value);
    emit('updated');
    emit('close');
    isCanceling.value = false;
    cancelReason.value = '';
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};

const handleCheckIn = async () => {
  if (!props.booking) return;
  isSubmitting.value = true;
  try {
    await bookingStore.checkInBooking(props.booking.id);
    emit('updated');
    emit('close');
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};

const handleCheckOut = async () => {
  if (!props.booking) return;
  isSubmitting.value = true;
  try {
    await bookingStore.checkOutBooking(props.booking.id);
    emit('updated');
    emit('close');
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div v-if="isOpen && booking" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm" @click="emit('close')"></div>
    
    <div class="relative w-full max-w-md max-h-[90vh] overflow-y-auto bg-white rounded-3xl shadow-2xl glass animate-in fade-in zoom-in duration-300">
      <div class="p-6">
        <div class="flex flex-col mb-4">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold text-gray-900 font-display truncate pr-4">
              {{ booking.title || booking.description }}
            </h2>
            <button 
              @click="emit('close')"
              class="p-2 rounded-full hover:bg-gray-100 text-gray-400 transition-colors shrink-0"
            >
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          <p v-if="booking.title" class="text-gray-500 text-sm mt-1">{{ booking.description }}</p>
        </div>

        <div class="flex items-center gap-2 mb-6">
          <span :class="['px-3 py-1 text-xs font-bold uppercase tracking-wider rounded-full border', getStatusColor(booking.status)]">
            {{ getStatusText(booking.status) }}
          </span>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-3">
            <MapPinIcon class="w-5 h-5 text-gray-400 shrink-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-gray-500">Phòng họp</p>
              <p class="text-base font-bold text-gray-900">{{ booking.room_name || `Phòng #${booking.room_id}` }}</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <CalendarIcon class="w-5 h-5 text-gray-400 shrink-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-gray-500">Ngày</p>
              <p class="text-base font-bold text-gray-900">{{ formatDate(booking.start_time) }}</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <ClockIcon class="w-5 h-5 text-gray-400 shrink-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-gray-500">Thời gian</p>
              <p class="text-base font-bold text-gray-900">
                {{ formatTime(booking.start_time) }} - {{ formatTime(booking.end_time) }}
              </p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <UserCircleIcon class="w-5 h-5 text-gray-400 shrink-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-gray-500">Người đặt</p>
              <p class="text-base font-bold text-gray-900">{{ booking.user_name || 'Người dùng' }}</p>
              <p class="text-xs text-gray-500 mt-0.5">{{ booking.attendee_count || 1 }} người tham gia</p>
            </div>
          </div>
          
          <div v-if="booking.status === 'CANCELLED' && booking.cancel_reason" class="bg-gray-50 p-3 rounded-xl border border-gray-100">
            <p class="text-sm font-medium text-gray-500 mb-1">Lý do hủy</p>
            <p class="text-sm text-gray-700 italic">"{{ booking.cancel_reason }}"</p>
          </div>
          <div v-else-if="booking.status === 'REJECTED' && booking.reject_reason" class="bg-red-50 p-3 rounded-xl border border-red-100">
            <p class="text-sm font-medium text-red-500 mb-1">Lý do từ chối</p>
            <p class="text-sm text-red-700 italic">"{{ booking.reject_reason }}"</p>
          </div>
        </div>

        <!-- Actions -->
        <div class="mt-8 pt-6 border-t border-gray-100 flex flex-col gap-3">
          <!-- Reject Reason Input -->
          <div v-if="isRejecting" class="mb-2 animate-in slide-in-from-top-2">
            <input 
              v-model="rejectReason"
              type="text"
              placeholder="Lý do từ chối (tùy chọn)"
              class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-viettel-red focus:border-viettel-red outline-none text-sm"
              @keyup.enter="handleReject"
            />
            <div class="flex gap-2 mt-2">
              <button 
                @click="isRejecting = false"
                class="flex-1 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 rounded-lg transition-colors border border-gray-200"
              >
                Hủy
              </button>
              <button 
                @click="handleReject"
                :disabled="isSubmitting"
                class="flex-1 py-2 text-sm font-bold text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors disabled:opacity-50"
              >
                Xác nhận từ chối
              </button>
            </div>
          </div>

          <!-- Cancel Reason Input -->
          <div v-if="isCanceling" class="mb-2 animate-in slide-in-from-top-2">
            <input 
              v-model="cancelReason"
              type="text"
              placeholder="Lý do hủy (tùy chọn)"
              class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-gray-400 outline-none text-sm"
              @keyup.enter="handleCancel"
            />
            <div class="flex gap-2 mt-2">
              <button 
                @click="isCanceling = false"
                class="flex-1 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 rounded-lg transition-colors border border-gray-200"
              >
                Trở lại
              </button>
              <button 
                @click="handleCancel"
                :disabled="isSubmitting"
                class="flex-1 py-2 text-sm font-bold text-white bg-gray-600 hover:bg-gray-700 rounded-lg transition-colors disabled:opacity-50"
              >
                Xác nhận hủy
              </button>
            </div>
          </div>

          <!-- Action Buttons -->
          <template v-else>
            <div v-if="booking.status === 'PENDING' && canApproveReject()" class="grid grid-cols-2 gap-3">
              <button 
                @click="handleApprove"
                :disabled="isSubmitting"
                class="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm bg-green-600 text-white hover:bg-green-700 transition-colors disabled:opacity-50"
              >
                <CheckCircleIcon class="w-5 h-5" /> Duyệt
              </button>
              <button 
                @click="isRejecting = true"
                :disabled="isSubmitting"
                class="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm bg-red-50 text-red-700 hover:bg-red-100 transition-colors disabled:opacity-50"
              >
                <XCircleIcon class="w-5 h-5" /> Từ chối
              </button>
            </div>

            <!-- Check-in button -->
            <button 
              v-if="booking.status === 'APPROVED' && canCancel()"
              @click="handleCheckIn"
              :disabled="isSubmitting"
              class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm text-white bg-blue-600 hover:bg-blue-700 transition-colors disabled:opacity-50"
            >
              <CheckCircleIcon class="w-5 h-5" /> Check-in
            </button>
            
            <!-- Check-out button -->
            <button 
              v-if="booking.status === 'CHECKED_IN' && canCancel()"
              @click="handleCheckOut"
              :disabled="isSubmitting"
              class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm text-white bg-purple-600 hover:bg-purple-700 transition-colors disabled:opacity-50"
            >
              <CheckCircleIcon class="w-5 h-5" /> Check-out
            </button>

            <button 
              v-if="(booking.status === 'PENDING' || booking.status === 'APPROVED') && canCancel()"
              @click="isCanceling = true"
              :disabled="isSubmitting"
              class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors disabled:opacity-50"
            >
              <TrashIcon class="w-5 h-5" /> Hủy lịch đặt
            </button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
