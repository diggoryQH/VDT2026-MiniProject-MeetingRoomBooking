<script setup lang="ts">
import { ref } from 'vue';
import { 
  XMarkIcon, 
  ExclamationCircleIcon, 
  CheckCircleIcon, 
  CalendarIcon, 
  MapPinIcon 
} from '@heroicons/vue/24/outline';
import AppButton from '@/components/shared/AppButton.vue';
import { useNotificationStore } from '@/stores/notificationStore';
import { useAuthStore } from '@/stores/authStore';

interface Props {
  isOpen: boolean;
}

defineProps<Props>();
const emit = defineEmits(['close']);

const notificationStore = useNotificationStore();
const authStore = useAuthStore();

const canApprove = (booking: any): boolean => {
  const role = authStore.user?.role;
  if (role === 'ADMIN' || role === 'SUPERADMIN') return true;
  if (role === 'APPROVER') {
    const userDeptId = authStore.user?.department_id;
    return userDeptId != null && booking.room_department_id === userDeptId;
  }
  return false;
};
const approvingId = ref<number | null>(null);

const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return 'N/A';
  // Input: "2026-05-10 09:00:00"
  const date = new Date(dateTimeStr.replace(' ', 'T'));
  return date.toLocaleString('default', { 
    day: 'numeric', 
    month: 'short', 
    hour: '2-digit', 
    minute: '2-digit' 
  });
};

const handleApprove = async (id: number) => {
  approvingId.value = id;
  await notificationStore.approveBooking(id);
  approvingId.value = null;
};

const handleReject = async (id: number) => {
  approvingId.value = id;
  await notificationStore.rejectBooking(id);
  approvingId.value = null;
};
</script>

<template>
  <Transition name="fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('close')"></div>

      <!-- Modal Content -->
      <div class="relative w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-y-auto max-h-[calc(100vh-4rem)] glass animate-in fade-in zoom-in duration-300 custom-scrollbar">
        <div class="p-6">
          <!-- Header -->
          <div class="flex items-center justify-between mb-6">
            <div>
              <h2 class="text-2xl font-bold text-gray-900 font-display">Chờ phê duyệt</h2>
              <p class="text-sm text-gray-400 mt-0.5">Phê duyệt hoặc xem xét yêu cầu</p>
            </div>
            <button @click="emit('close')" class="p-2 rounded-full hover:bg-gray-100 text-gray-400 transition-colors">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>

          <!-- Error Alert -->
          <Transition name="slide-down">
            <div v-if="notificationStore.error" class="mb-6 p-4 rounded-2xl bg-red-50 border border-red-100 flex items-start gap-4 animate-shake">
              <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                <ExclamationCircleIcon class="w-6 h-6 text-red-600" />
              </div>
              <div class="flex flex-col gap-0.5 pt-0.5">
                <span class="text-sm font-bold text-red-900">Phê duyệt thất bại</span>
                <p class="text-sm text-red-600 leading-relaxed">{{ notificationStore.error }}</p>
              </div>
            </div>
          </Transition>

          <!-- Bookings List -->
          <div v-if="notificationStore.pendingBookings.length > 0" class="flex flex-col gap-4">
            <div 
              v-for="booking in notificationStore.pendingBookings" 
              :key="booking.id"
              class="bg-gray-50 rounded-2xl p-4 border border-gray-100 hover:border-viettel-red/30 transition-all group"
            >
              <div class="flex flex-col gap-3">
                <div class="flex justify-between items-start gap-2">
                  <h3 class="font-bold text-gray-900 leading-tight">{{ booking.description }}</h3>
                  <span class="px-2 py-0.5 bg-yellow-100 text-yellow-700 text-[10px] font-bold uppercase rounded-md shrink-0">Chờ xử lý</span>
                </div>
                
                <div class="flex flex-col gap-2">
                  <div class="flex items-center gap-2 text-xs text-gray-500">
                    <CalendarIcon class="w-4 h-4 text-gray-400" />
                    <span>{{ formatDateTime(booking.start_time) }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-xs text-gray-500">
                    <MapPinIcon class="w-4 h-4 text-gray-400" />
                    <span>Phòng #{{ booking.room_id }}</span>
                  </div>
                </div>

                <div v-if="canApprove(booking)" class="pt-2 border-t border-gray-200/60 mt-1 flex gap-2">
                  <AppButton 
                    label="Từ chối" 
                    size="sm"
                    variant="outline"
                    class="!text-red-500 !border-red-200 hover:!bg-red-50"
                    :is-loading="approvingId === booking.id"
                    @click="handleReject(booking.id)"
                  />
                  <AppButton 
                    label="Phê duyệt yêu cầu" 
                    size="sm"
                    :is-loading="approvingId === booking.id"
                    @click="handleApprove(booking.id)"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else-if="!notificationStore.isLoading" class="py-12 flex flex-col items-center justify-center text-center gap-4">
            <div class="w-20 h-20 bg-viettel-red/10 rounded-full flex items-center justify-center text-viettel-red-dark">
              <CheckCircleIcon class="w-10 h-10" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-900">Đã hoàn tất!</h3>
              <p class="text-sm text-gray-400 mt-1">Không có yêu cầu nào đang chờ phê duyệt.</p>
            </div>
            <button 
              @click="emit('close')"
              class="mt-2 text-sm font-bold text-viettel-red-dark hover:underline"
            >
              Về trang chủ
            </button>
          </div>

          <!-- Loading State -->
          <div v-else class="flex flex-col gap-4">
            <div v-for="i in 3" :key="i" class="h-40 bg-gray-50 rounded-2xl animate-pulse border border-gray-100"></div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.glass {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.animate-in {
  animation-duration: 0.3s;
  animation-fill-mode: both;
}

@keyframes zoom-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.zoom-in { animation-name: zoom-in; }

.animate-shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease-out;
}

.slide-down-enter-from { opacity: 0; transform: translateY(-20px); }
.slide-down-leave-to { opacity: 0; transform: translateY(-10px); }

.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #E5E7EB;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #D1D5DB;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
