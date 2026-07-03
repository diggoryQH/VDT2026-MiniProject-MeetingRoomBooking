<script setup lang="ts">
import { onMounted } from 'vue';
import { useNotificationStore } from '@/stores/notificationStore';
import { useAuthStore } from '@/stores/authStore';
import { 
  BellAlertIcon, 
  CheckCircleIcon,
  XCircleIcon,
  InformationCircleIcon,
  ClockIcon
} from '@heroicons/vue/24/outline';

const notificationStore = useNotificationStore();
const authStore = useAuthStore();

onMounted(() => {
  if (authStore.isAuthenticated) {
    notificationStore.fetchNotifications();
    notificationStore.fetchUnreadCount();
  }
});

const getIconForType = (type: string) => {
  switch (type) {
    case 'BOOKING_APPROVED': return CheckCircleIcon;
    case 'BOOKING_REJECTED': return XCircleIcon;
    case 'NEW_BOOKING': return InformationCircleIcon;
    case 'REMINDER': return ClockIcon;
    default: return BellAlertIcon;
  }
};

const getColorForType = (type: string) => {
  switch (type) {
    case 'BOOKING_APPROVED': return 'text-green-500 bg-green-50';
    case 'BOOKING_REJECTED': return 'text-red-500 bg-red-50';
    case 'NEW_BOOKING': return 'text-blue-500 bg-blue-50';
    case 'REMINDER': return 'text-yellow-500 bg-yellow-50';
    default: return 'text-gray-500 bg-gray-50';
  }
};

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr.replace(' ', 'T'));
  return date.toLocaleString('vi-VN', { 
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

const handleMarkAsRead = async (id: number) => {
  await notificationStore.markAsRead(id);
};

const handleMarkAllAsRead = async () => {
  await notificationStore.markAllAsRead();
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <!-- Header -->
    <div class="bg-white px-4 py-6 shadow-sm sticky top-0 z-10">
      <div class="max-w-2xl mx-auto flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Thông báo</h1>
          <p class="text-sm text-gray-500">Cập nhật thông tin phòng họp của bạn</p>
        </div>
        <button 
          v-if="notificationStore.unreadCount > 0"
          @click="handleMarkAllAsRead"
          class="text-sm font-bold text-viettel-red-dark hover:underline"
        >
          Đánh dấu tất cả đã đọc
        </button>
      </div>
    </div>

    <main class="max-w-2xl mx-auto px-4 mt-6">
      <div v-if="notificationStore.isLoading" class="flex flex-col gap-4">
        <div v-for="i in 4" :key="i" class="h-24 bg-gray-200 rounded-2xl animate-pulse"></div>
      </div>

      <div v-else-if="notificationStore.notifications.length > 0" class="flex flex-col gap-3">
        <div 
          v-for="notification in notificationStore.notifications" 
          :key="notification.id"
          @click="!notification.is_read && handleMarkAsRead(notification.id)"
          class="bg-white rounded-2xl p-4 shadow-sm border transition-all cursor-pointer relative overflow-hidden"
          :class="notification.is_read ? 'border-gray-100 opacity-70' : 'border-viettel-red/30 shadow-md'"
        >
          <div v-if="!notification.is_read" class="absolute top-0 left-0 w-1 h-full bg-viettel-red"></div>
          
          <div class="flex gap-4">
            <div class="shrink-0 mt-1">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="getColorForType(notification.type)">
                <component :is="getIconForType(notification.type)" class="w-6 h-6" />
              </div>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start gap-2">
                <h3 class="font-bold text-gray-900" :class="{'text-gray-700 font-semibold': notification.is_read}">
                  {{ notification.title }}
                </h3>
                <span class="text-xs text-gray-400 shrink-0 whitespace-nowrap">{{ formatTime(notification.created_at) }}</span>
              </div>
              <p class="text-sm text-gray-600 mt-1 leading-relaxed">{{ notification.message }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-16">
        <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-400">
          <BellAlertIcon class="w-10 h-10" />
        </div>
        <h3 class="text-lg font-bold text-gray-900">Không có thông báo</h3>
        <p class="text-gray-500 mt-1">Bạn đã xem hết thông báo!</p>
      </div>
    </main>
  </div>
</template>
