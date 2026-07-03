<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue';
import { XMarkIcon } from '@heroicons/vue/24/outline';
import AppInput from '@/components/shared/AppInput.vue';
import AppButton from '@/components/shared/AppButton.vue';
import AppSelect from '@/components/shared/AppSelect.vue';
import { useRoomStore } from '@/stores/roomStore';
import { useBookingStore } from '@/stores/bookingStore';
import { useUserStore } from '@/stores/userStore';
import { ExclamationCircleIcon } from '@heroicons/vue/24/outline';

interface Props {
  isOpen: boolean;
  selectedDate: string;
}

const props = defineProps<Props>();
const emit = defineEmits(['close', 'submit']);

const roomStore = useRoomStore();
const bookingStore = useBookingStore();
const userStore = useUserStore();

const usersByDepartment = computed(() => {
  const groups: Record<string, typeof userStore.directoryUsers> = {};
  userStore.directoryUsers.forEach(u => {
    const deptName = u.department_name || 'Khác';
    if (!groups[deptName]) groups[deptName] = [];
    groups[deptName].push(u);
  });
  return groups;
});

const isDepartmentSelected = (users: { id: number }[]) => {
  return users.length > 0 && users.every(u => form.attendee_ids.includes(u.id));
};

const toggleDepartment = (users: { id: number }[]) => {
  const allSelected = isDepartmentSelected(users);
  if (allSelected) {
    form.attendee_ids = form.attendee_ids.filter(id => !users.some(u => u.id === id));
  } else {
    const newIds = users.filter(u => !form.attendee_ids.includes(u.id)).map(u => u.id);
    form.attendee_ids.push(...newIds);
  }
};

const getDefaultTimes = (dateStr: string) => {
  const today = new Date();
  const selected = new Date(dateStr);
  
  if (selected.toDateString() === today.toDateString()) {
    const nextHour = today.getHours() + 1;
    if (nextHour < 23) {
      return {
        start: `${String(nextHour).padStart(2, '0')}:00`,
        end: `${String(nextHour + 1).padStart(2, '0')}:00`
      };
    }
  }
  return { start: '10:00', end: '11:00' };
};

const initialTimes = getDefaultTimes(props.selectedDate);

const form = reactive({
  title: '',
  description: '',
  room_id: null as number | null,
  attendee_count: 1,
  attendee_ids: [] as number[],
  date: props.selectedDate,
  startTime: initialTimes.start,
  endTime: initialTimes.end,
  status: 'pending',
  recurrenceType: 'NONE',
  recurrenceEndDate: ''
});

const isSubmitting = ref(false);
const submitError = ref('');
const formError = ref('');

onMounted(() => {
  roomStore.fetchRooms();
  userStore.fetchUserDirectory();
  bookingStore.clearError();
});

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    submitError.value = '';
    formError.value = '';
    bookingStore.clearError();
  }
});

watch(() => props.selectedDate, (newDate) => {
  form.date = newDate;
  const times = getDefaultTimes(newDate);
  form.startTime = times.start;
  form.endTime = times.end;
});

// Clear errors when user modifies the form (user intent to retry)
watch(() => form, () => {
  if (submitError.value || formError.value) {
    submitError.value = '';
    formError.value = '';
  }
}, { deep: true });

const handleSubmit = async () => {
  formError.value = '';
  submitError.value = '';
  
  if (!form.room_id) {
    formError.value = 'Vui lòng chọn phòng trước khi tạo lịch đặt.';
    return;
  }
  
  isSubmitting.value = true;
  
  const bookingData = {
    title: form.title,
    description: form.description,
    room_id: form.room_id,
    attendee_count: form.attendee_count,
    attendee_ids: form.attendee_ids,
    start_time: `${form.date} ${form.startTime}:00`,
    end_time: `${form.date} ${form.endTime}:00`,
    recurrence_type: form.recurrenceType,
    recurrence_end_date: form.recurrenceEndDate ? `${form.recurrenceEndDate} 23:59:59` : null
  };

  const result = await bookingStore.createBooking(bookingData);
  
  // Handle both boolean (old store code during HMR) and object (new store code)
  const isSuccess = typeof result === 'boolean' ? result : result?.success;
  const errorMsg = typeof result === 'boolean' ? bookingStore.error : result?.error;
  
  if (isSuccess) {
    emit('close');
    resetForm();
  } else {
    // Fallback to bookingStore.error if result doesn't have it (e.g. HMR didn't update store)
    submitError.value = errorMsg || bookingStore.error || 'Đã có lỗi xảy ra khi tạo lịch đặt.';
  }
  
  isSubmitting.value = false;
};

const resetForm = () => {
  const times = getDefaultTimes(props.selectedDate);
  form.title = '';
  form.description = '';
  form.room_id = null;
  form.attendee_count = 1;
  form.attendee_ids = [];
  form.startTime = times.start;
  form.endTime = times.end;
  form.recurrenceType = 'NONE';
  form.recurrenceEndDate = '';
  formError.value = '';
  submitError.value = '';
};
</script>

<template>
  <Transition name="fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div 
        class="absolute inset-0 bg-black/40 backdrop-blur-sm" 
        @click="emit('close')"
      ></div>

      <!-- Modal Content -->
      <div class="relative w-full max-w-lg max-h-[85vh] overflow-y-auto bg-white rounded-3xl shadow-2xl glass animate-in fade-in zoom-in duration-300 custom-scrollbar">
        <div class="p-6 sm:p-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900 font-display">Lịch đặt mới</h2>
            <button 
              @click="emit('close')"
              class="p-2 rounded-full hover:bg-gray-100 text-gray-400 transition-colors"
            >
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>

          <!-- Error Message -->
          <Transition name="slide-down">
            <div v-if="submitError || formError" class="mb-6 p-4 rounded-2xl bg-red-50 border border-red-100 flex items-start gap-4 animate-shake">
              <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                <ExclamationCircleIcon class="w-6 h-6 text-red-600" />
              </div>
              <div class="flex flex-col gap-0.5 pt-0.5">
                <span class="text-sm font-bold text-red-900">Lỗi! Đã có lỗi xảy ra</span>
                <p class="text-sm text-red-600 leading-relaxed">{{ formError || submitError }}</p>
              </div>
            </div>
          </Transition>

          <form @submit.prevent="handleSubmit" class="flex flex-col gap-4">
            <AppInput 
              label="Tiêu đề cuộc họp" 
              v-model="form.title" 
              placeholder="vd: Daily Standup" 
              required
            />
            
            <AppInput 
              label="Mô tả cuộc họp" 
              v-model="form.description" 
              placeholder="Nội dung chi tiết..." 
              required
            />

            <div class="grid grid-cols-2 gap-4">
              <AppSelect 
                label="Phòng"
                v-model="form.room_id"
                :options="roomStore.rooms"
                placeholder="Chọn phòng"
                required
              />

              <AppInput 
                label="Số người tham gia" 
                v-model="form.attendee_count" 
                type="number"
                min="1"
                required
              />
            </div>

            <div class="flex flex-col gap-2">
              <label class="block text-sm font-semibold text-gray-700">Mời người tham gia nội bộ</label>
              <div class="max-h-48 overflow-y-auto border border-gray-200 rounded-xl p-3 bg-gray-50 custom-scrollbar">
                <div v-for="(users, deptName) in usersByDepartment" :key="deptName" class="mb-3 last:mb-0">
                  <div class="flex items-center justify-between mb-2 sticky top-0 bg-gray-50 py-1 z-10 border-b border-gray-100">
                    <span class="font-bold text-xs text-gray-500 uppercase tracking-wider">{{ deptName }}</span>
                    <button type="button" @click.prevent="toggleDepartment(users)" class="text-xs font-semibold text-viettel-red hover:text-viettel-red-dark transition-colors">
                      {{ isDepartmentSelected(users) ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}
                    </button>
                  </div>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <label v-for="user in users" :key="user.id" class="flex items-center gap-2 text-sm text-gray-700 cursor-pointer hover:bg-gray-100 p-1.5 rounded-lg transition-colors border border-transparent hover:border-gray-200">
                      <input type="checkbox" :value="user.id" v-model="form.attendee_ids" class="rounded text-primary focus:ring-primary w-4 h-4" />
                      <span class="truncate flex-1" :title="user.name + ' (' + user.username + ')'">{{ user.name }} ({{ user.username }})</span>
                    </label>
                  </div>
                </div>
                <div v-if="Object.keys(usersByDepartment).length === 0" class="text-sm text-gray-500 italic p-2 text-center">Đang tải danh sách...</div>
              </div>
            </div>

            <AppInput 
              label="Ngày" 
              v-model="form.date"
              type="date"
              required
            />

            <div class="grid grid-cols-2 gap-4">
              <AppInput 
                label="Thời gian bắt đầu" 
                v-model="form.startTime" 
                type="time" 
                required
              />
              <AppInput 
                label="Thời gian kết thúc" 
                v-model="form.endTime" 
                type="time" 
                required
              />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <AppSelect 
                label="Lặp lại"
                v-model="form.recurrenceType"
                :options="[{id:'NONE',name:'Không lặp lại'},{id:'DAILY',name:'Hàng ngày'},{id:'WEEKLY',name:'Hàng tuần'},{id:'MONTHLY',name:'Hàng tháng'}]"
                required
              />
              <AppInput 
                v-if="form.recurrenceType !== 'NONE'"
                label="Ngày kết thúc lặp" 
                v-model="form.recurrenceEndDate" 
                type="date"
                required
              />
            </div>

            <div class="mt-4 flex gap-3">
              <AppButton 
                label="Hủy" 
                variant="outline" 
                @click="emit('close')" 
              />
              <AppButton 
                label="Tạo lịch đặt" 
                type="submit" 
                :is-loading="isSubmitting"
              />
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.glass {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

.animate-in {
  animation-duration: 0.3s;
  animation-fill-mode: both;
}

@keyframes zoom-in {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.zoom-in {
  animation-name: zoom-in;
}

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

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

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
