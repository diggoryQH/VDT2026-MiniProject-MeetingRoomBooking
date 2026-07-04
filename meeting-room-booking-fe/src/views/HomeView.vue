<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useBookingStore } from '@/stores/bookingStore';
import WelcomeBanner from '@/components/home/WelcomeBanner.vue';
import TodayBookingList from '@/components/home/TodayBookingList.vue';
import NotificationBell from '@/components/home/NotificationBell.vue';
import PendingBookingsModal from '@/components/home/PendingBookingsModal.vue';
import { useNotificationStore } from '@/stores/notificationStore';
import { ref } from 'vue';

const authStore = useAuthStore();
const bookingStore = useBookingStore();
const notificationStore = useNotificationStore();

const isNotificationModalOpen = ref(false);

onMounted(async () => {
  if (!authStore.user) {
    await authStore.getCurrentUser();
  }
  await bookingStore.fetchBookings();
  
  if (authStore.user?.role === 'SUPERADMIN' || authStore.user?.role === 'ADMIN' || authStore.user?.role === 'APPROVER') {
    notificationStore.fetchPendingBookings();
  }
});

const todayKey = computed(() => {
  const today = new Date();
  return today.toISOString().split('T')[0] || '';
});

const todayBookings = computed(() => {
  return bookingStore.getBookingsByDate(todayKey.value);
});

const displayName = computed(() => authStore.user?.name || 'User');
</script>

<template>
  <div class="min-h-screen bg-white max-w-2xl mx-auto pb-24 px-4 pt-8">
    <div class="flex items-start justify-between mb-2">
      <WelcomeBanner :user-name="displayName" />
      <NotificationBell 
        v-if="authStore.user?.role === 'SUPERADMIN' || authStore.user?.role === 'ADMIN' || authStore.user?.role === 'APPROVER'"
        :count="notificationStore.pendingCount"
        @click="isNotificationModalOpen = true"
      />
    </div>
    
    <TodayBookingList 
      :bookings="todayBookings" 
      :is-loading="bookingStore.isLoading" 
    />
    
    <!-- Quick Stats or Actions (Optional visual flair) -->
    <div class="grid grid-cols-2 gap-4 mt-8">
      <div class="p-4 bg-viettel-red-light rounded-3xl border border-viettel-red/10">
        <span class="text-xs font-bold text-viettel-red uppercase tracking-wider">Phòng họp</span>
        <p class="text-xl font-bold text-gray-900 mt-1">Khả dụng</p>
      </div>
      <router-link to="/calendar" class="p-4 bg-gray-50 rounded-3xl border border-gray-100 flex flex-col justify-between">
        <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">Lịch</span>
        <p class="text-xl font-bold text-gray-900 mt-1">Xem tất cả</p>
      </router-link>
    </div>

    <!-- Modals -->
    <PendingBookingsModal 
      :is-open="isNotificationModalOpen"
      @close="isNotificationModalOpen = false"
    />
  </div>
</template>
