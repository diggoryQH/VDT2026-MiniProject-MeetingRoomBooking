<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoomStore } from '@/stores/roomStore';
import { useAuthStore } from '@/stores/authStore';
import { useDepartmentStore } from '@/stores/departmentStore';
import AppButton from '@/components/shared/AppButton.vue';
import RoomFormModal from './RoomFormModal.vue';
import { 
  PlusIcon, 
  PencilSquareIcon, 
  TrashIcon, 
  UserGroupIcon, 
  MapPinIcon 
} from '@heroicons/vue/24/outline';

const roomStore = useRoomStore();
const authStore = useAuthStore();
const departmentStore = useDepartmentStore();

const isModalOpen = ref(false);
const editingRoomId = ref<number | null>(null);

const filterDepartment = ref<number | null>(null);
const searchName = ref('');

onMounted(async () => {
  await departmentStore.fetchDepartments();
  fetchRooms();
});

const fetchRooms = () => {
  roomStore.fetchRooms({
    name: searchName.value || undefined,
    departmentId: filterDepartment.value || undefined
  });
};

const handleEdit = (id: number) => {
  editingRoomId.value = id;
  isModalOpen.value = true;
};

const handleDelete = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn xóa phòng này không?')) {
    await roomStore.deleteRoom(id);
  }
};

const openCreateModal = () => {
  editingRoomId.value = null;
  isModalOpen.value = true;
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <!-- Header -->
    <div class="bg-white px-4 py-6 shadow-sm">
      <div class="max-w-2xl mx-auto flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Phòng họp</h1>
          <p class="text-sm text-gray-500">Quản lý phòng họp</p>
        </div>
        <AppButton v-if="authStore.user?.role === 'ADMIN' || authStore.user?.role === 'SUPERADMIN'"
          label="Thêm phòng"
          size="sm"
          @click="openCreateModal"
        >
          <template #icon>
            <PlusIcon class="w-4 h-4" />
          </template>
        </AppButton>
      </div>
    </div>

    <main class="max-w-2xl mx-auto px-4 mt-6">
      <!-- Filters -->
      <div class="flex gap-2 mb-6 bg-white p-3 rounded-2xl shadow-sm">
        <input 
          v-model="searchName"
          @keyup.enter="fetchRooms"
          type="text" 
          placeholder="Tìm kiếm phòng..." 
          class="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-viettel-red focus:border-transparent outline-none"
        />
        <select 
          v-model="filterDepartment"
          @change="fetchRooms"
          class="bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-viettel-red focus:border-transparent outline-none appearance-none"
        >
          <option :value="null">Tất cả phòng ban</option>
          <option v-for="dept in departmentStore.departments" :key="dept.id" :value="dept.id">
            {{ dept.name }}
          </option>
        </select>
      </div>

      <!-- Room List -->
      <div v-if="roomStore.isLoading" class="flex flex-col gap-4">
        <div v-for="i in 3" :key="i" class="h-32 bg-gray-200 rounded-2xl animate-pulse"></div>
      </div>

      <div v-else-if="roomStore.rooms.length > 0" class="flex flex-col gap-4">
        <div 
          v-for="room in roomStore.rooms" 
          :key="room.id"
          class="bg-white rounded-3xl p-5 shadow-sm border border-gray-100 flex flex-col gap-4"
        >
          <div class="flex justify-between items-start">
            <div>
              <h3 class="text-lg font-bold text-gray-900">{{ room.name }}</h3>
              <span class="inline-block mt-1 px-2 py-0.5 bg-gray-100 text-gray-600 text-[10px] font-bold uppercase rounded-md">
                {{ room.department_name || 'Phòng dùng chung' }}
              </span>
            </div>
            <span 
              class="px-2 py-0.5 text-xs font-bold uppercase rounded-full"
              :class="room.is_available ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
            >
              {{ room.is_available ? 'Khả dụng' : 'Không khả dụng' }}
            </span>
          </div>

          <div class="flex items-center gap-4 text-sm text-gray-500">
            <div class="flex items-center gap-1">
              <MapPinIcon class="w-4 h-4 text-gray-400" />
              <span>{{ room.location }}</span>
            </div>
            <div class="flex items-center gap-1">
              <UserGroupIcon class="w-4 h-4 text-gray-400" />
              <span>{{ room.capacity }} người</span>
            </div>
          </div>

          <div v-if="room.equipment" class="text-xs text-gray-500 border-t border-gray-100 pt-3">
            <span class="font-semibold text-gray-700">Thiết bị:</span> {{ room.equipment }}
          </div>

          <!-- Admin Actions -->
          <div v-if="authStore.user?.role === 'ADMIN' || authStore.user?.role === 'SUPERADMIN'" 
               class="flex gap-2 border-t border-gray-100 pt-3">
            <button 
              @click="handleEdit(room.id)"
              class="flex-1 flex items-center justify-center gap-1 py-2 text-sm font-semibold text-gray-600 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
            >
              <PencilSquareIcon class="w-4 h-4" /> Sửa
            </button>
            <button 
              @click="handleDelete(room.id)"
              class="flex-1 flex items-center justify-center gap-1 py-2 text-sm font-semibold text-red-600 bg-red-50 rounded-xl hover:bg-red-100 transition-colors"
            >
              <TrashIcon class="w-4 h-4" /> Xóa
            </button>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12 text-gray-500">
        Không tìm thấy phòng nào phù hợp.
      </div>
    </main>

    <RoomFormModal 
      v-if="isModalOpen"
      :is-open="isModalOpen"
      :room-id="editingRoomId"
      @close="isModalOpen = false"
      @saved="fetchRooms"
    />
  </div>
</template>
