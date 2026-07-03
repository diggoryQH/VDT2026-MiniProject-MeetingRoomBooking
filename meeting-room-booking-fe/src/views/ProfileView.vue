<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import AppButton from '@/components/shared/AppButton.vue';
import { 
  PhoneIcon, 
  EnvelopeIcon, 
  CakeIcon, 
  ChevronRightIcon,
  BellIcon,
  ShieldCheckIcon,
  QuestionMarkCircleIcon,
  BuildingOfficeIcon
} from '@heroicons/vue/24/outline';

const router = useRouter();
const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
  router.push('/auth');
};

const profileInfo = computed(() => [
  { label: 'Tên đăng nhập', value: authStore.user?.username, icon: QuestionMarkCircleIcon },
  { label: 'Địa chỉ Email', value: authStore.user?.email, icon: EnvelopeIcon },
  { label: 'Phòng ban', value: authStore.user?.department_name, icon: BuildingOfficeIcon },
  { label: 'Vai trò', value: authStore.user?.role, icon: ShieldCheckIcon },
]);

const menuItems = [
  { label: 'Thông báo', icon: BellIcon },
  { label: 'Bảo mật & Quyền riêng tư', icon: ShieldCheckIcon },
  { label: 'Trung tâm trợ giúp', icon: QuestionMarkCircleIcon },
];
</script>

<template>
  <div class="min-h-screen bg-white max-w-2xl mx-auto pb-24 px-4 pt-8">
    <div class="mb-4">
      <h1 class="text-2xl font-bold font-display text-gray-900">Tài khoản cá nhân</h1>
    </div>

    <ProfileHeader :user="authStore.user" />

    <!-- Personal Information Card -->
    <div class="mt-8">
      <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3 px-1">Thông tin cá nhân</h3>
      <div class="bg-gray-50 rounded-3xl overflow-hidden border border-gray-100">
        <div 
          v-for="(item, index) in profileInfo" 
          :key="index"
          class="flex items-center gap-4 p-4 border-b border-gray-100 last:border-0"
        >
          <div class="w-10 h-10 rounded-2xl bg-white shadow-sm flex items-center justify-center text-gray-400">
            <component :is="item.icon" class="w-5 h-5" />
          </div>
          <div class="flex flex-col gap-0.5">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-tighter">{{ item.label }}</span>
            <span class="text-sm font-semibold text-gray-900">{{ item.value || 'Chưa cung cấp' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Settings Menu -->
    <div class="mt-8">
      <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3 px-1">Cài đặt</h3>
      <div class="flex flex-col gap-2">
        <button 
          v-for="(item, index) in menuItems" 
          :key="index"
          class="flex items-center justify-between p-4 bg-white border border-gray-100 rounded-2xl hover:bg-gray-50 transition-all group"
        >
          <div class="flex items-center gap-3">
            <component :is="item.icon" class="w-5 h-5 text-gray-400 group-hover:text-viettel-red-dark transition-colors" />
            <span class="text-sm font-semibold text-gray-700">{{ item.label }}</span>
          </div>
          <ChevronRightIcon class="w-4 h-4 text-gray-300" />
        </button>
      </div>
    </div>

    <!-- Logout Button -->
    <div class="mt-12 px-2">
      <AppButton 
        label="Đăng xuất" 
        variant="outline" 
        class="text-red-500 border-red-100 hover:bg-red-50 hover:border-red-200"
        @click="handleLogout" 
      />
    </div>
  </div>
</template>
