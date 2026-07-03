<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { 
  HomeIcon as HomeOutline, 
  CalendarDaysIcon as CalendarOutline, 
  UsersIcon as UsersOutline, 
  UserCircleIcon as UserOutline,
  BuildingOfficeIcon as BuildingOfficeOutline,
  BellAlertIcon as BellOutline
} from '@heroicons/vue/24/outline';
import { 
  HomeIcon as HomeSolid, 
  CalendarDaysIcon as CalendarSolid, 
  UsersIcon as UsersSolid, 
  UserCircleIcon as UserSolid,
  BuildingOfficeIcon as BuildingOfficeSolid,
  BellAlertIcon as BellSolid
} from '@heroicons/vue/24/solid';

const route = useRoute();

const authStore = useAuthStore();
import { useNotificationStore } from '@/stores/notificationStore';

const notificationStore = useNotificationStore();

const allNavItems = [
  { 
    label: 'Trang chủ', 
    path: '/', 
    outlineIcon: HomeOutline, 
    solidIcon: HomeSolid 
  },
  { 
    label: 'Lịch', 
    path: '/calendar', 
    outlineIcon: CalendarOutline, 
    solidIcon: CalendarSolid 
  },
  { 
    label: 'Phòng họp', 
    path: '/rooms', 
    outlineIcon: BuildingOfficeOutline, 
    solidIcon: BuildingOfficeSolid 
  },
  { 
    label: 'Thông báo', 
    path: '/notifications', 
    outlineIcon: BellOutline, 
    solidIcon: BellSolid 
  },
  { 
    label: 'Cá nhân', 
    path: '/profile', 
    outlineIcon: UserOutline, 
    solidIcon: UserSolid 
  },
  {
    label: 'Người dùng',
    path: '/users',
    outlineIcon: UsersOutline,
    solidIcon: UsersSolid,
    roles: ['SUPERADMIN', 'ADMIN']
  }
];

const navItems = computed(() => {
  return allNavItems.filter(item => {
    if (!item.roles) return true;
    return item.roles.includes(authStore.user?.role || '');
  });
});

const isActive = (path: string) => {
  if (path === '/' && route.path === '/') return true;
  if (path !== '/' && route.path.startsWith(path)) return true;
  return false;
};
</script>

<template>
  <nav class="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100 pb-safe">
    <div class="max-w-2xl mx-auto flex justify-around items-center h-16 px-4">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 min-w-[64px] transition-colors duration-200"
        :class="isActive(item.path) ? 'text-viettel-red' : 'text-gray-400'"
      >
        <div class="relative">
          <component 
            :is="isActive(item.path) ? item.solidIcon : item.outlineIcon" 
            class="w-6 h-6"
          />
          <div v-if="item.path === '/notifications' && notificationStore.unreadCount > 0" 
               class="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full border-2 border-white"></div>
        </div>
        <span class="text-[10px] font-semibold uppercase tracking-wider">
          {{ item.label }}
        </span>
        <div 
          v-if="isActive(item.path)" 
          class="w-1 h-1 rounded-full bg-viettel-red mt-[-2px]"
        ></div>
      </router-link>
    </div>
  </nav>
</template>

<style scoped>
.pb-safe {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
