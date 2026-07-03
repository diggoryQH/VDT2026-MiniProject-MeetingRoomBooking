<script setup lang="ts">
import { onMounted, ref, onUnmounted } from 'vue';
import { PlusIcon, PencilSquareIcon, TrashIcon, UserIcon } from '@heroicons/vue/24/outline';
import { useUserStore } from '@/stores/userStore';
import UserFormModal from '@/components/users/UserFormModal.vue';
import type { User } from '@/types/user';

const userStore = useUserStore();
const isModalOpen = ref(false);
const selectedUser = ref<User | null>(null);
const loader = ref<HTMLElement | null>(null);

let observer: IntersectionObserver | null = null;

onMounted(async () => {
  await userStore.fetchUsers();
  
  observer = new IntersectionObserver((entries) => {
    if (entries[0]?.isIntersecting && userStore.hasMore && !userStore.isLoading) {
      userStore.fetchMoreUsers();
    }
  }, { threshold: 0.1 });

  if (loader.value) {
    observer.observe(loader.value);
  }
});

onUnmounted(() => {
  if (observer) observer.disconnect();
});

const openCreateModal = () => {
  selectedUser.value = null;
  isModalOpen.value = true;
};

const openEditModal = (user: User) => {
  selectedUser.value = user;
  isModalOpen.value = true;
};

const confirmDelete = async (user: User) => {
  if (confirm(`Bạn có chắc chắn muốn xóa người dùng "${user.name}"?`)) {
    await userStore.deleteUser(user.id);
  }
};
</script>

<template>
  <div class="min-h-screen bg-white max-w-2xl mx-auto pb-24 px-4 pt-8">
    <div class="flex items-center justify-between mb-6 px-1">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 font-display">Người dùng</h1>
        <p class="text-gray-400 text-sm mt-0.5">Quản lý thành viên</p>
      </div>
      <button 
        @click="openCreateModal"
        class="bg-viettel-red text-white px-5 py-2.5 rounded-2xl font-bold text-sm hover:bg-viettel-red-dark transition-all flex items-center gap-1.5 shadow-sm active:scale-95"
      >
        <PlusIcon class="w-4 h-4" />
        Thêm
      </button>
    </div>

    <div v-if="userStore.users.length > 0" class="flex flex-col gap-4">
      <div 
        v-for="user in userStore.users" 
        :key="user.id"
        class="bg-white p-4 rounded-3xl border border-gray-100 flex items-center justify-between group hover:shadow-lg hover:shadow-gray-100/50 transition-all duration-300"
      >
        <div class="flex items-center gap-4 min-w-0">
          <div class="w-12 h-12 rounded-2xl bg-viettel-red/10 flex items-center justify-center text-viettel-red-dark group-hover:bg-viettel-red group-hover:text-gray-900 transition-all duration-500 shrink-0">
            <UserIcon class="w-6 h-6" />
          </div>
          <div class="flex flex-col min-w-0">
            <span class="text-lg font-bold text-gray-900 truncate">{{ user.name }}</span>
            <div class="flex items-center gap-2 text-sm text-gray-500 mt-0.5">
              <span class="bg-gray-100 px-2 py-0.5 rounded-lg font-medium text-[10px] uppercase tracking-wider shrink-0 text-gray-400">{{ user.role }}</span>
              <span class="bg-viettel-red/10 text-viettel-red-dark px-2 py-0.5 rounded-lg font-medium text-[10px] uppercase tracking-wider shrink-0" v-if="user.department_name">{{ user.department_name }}</span>
              <span class="w-1 h-1 rounded-full bg-gray-200 shrink-0"></span>
              <span class="truncate text-xs">{{ user.email }}</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-1">
          <button 
            @click="openEditModal(user)"
            class="p-3 rounded-2xl hover:bg-gray-50 text-gray-400 hover:text-gray-900 transition-all"
            title="Sửa thông tin"
          >
            <PencilSquareIcon class="w-5 h-5" />
          </button>
          <button 
            @click="confirmDelete(user)"
            class="p-3 rounded-2xl hover:bg-red-50 text-gray-400 hover:text-red-500 transition-all"
            title="Xóa người dùng"
          >
            <TrashIcon class="w-5 h-5" />
          </button>
        </div>
      </div>

      <div ref="loader" class="h-16 flex items-center justify-center">
        <div v-if="userStore.isLoading" class="animate-spin rounded-full h-6 w-6 border-b-2 border-viettel-red"></div>
        <span v-else-if="!userStore.hasMore" class="text-sm text-gray-400 font-medium italic">Hết danh sách</span>
      </div>
    </div>

    <div v-else-if="userStore.isLoading" class="flex flex-col gap-4">
      <div v-for="i in 5" :key="i" class="h-24 bg-gray-50 rounded-3xl animate-pulse border border-gray-100"></div>
    </div>

    <div v-else class="py-20 flex flex-col items-center justify-center text-center gap-4">
      <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center text-gray-300">
        <UserIcon class="w-10 h-10" />
      </div>
      <div>
        <h3 class="text-xl font-bold text-gray-900">Không tìm thấy người dùng</h3>
        <p class="text-gray-500 mt-1">Hãy bắt đầu bằng cách thêm thành viên mới</p>
      </div>
    </div>

    <UserFormModal 
      :is-open="isModalOpen" 
      :user="selectedUser" 
      @close="isModalOpen = false" 
    />
  </div>
</template>
