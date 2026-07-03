<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useDepartmentStore } from '@/stores/departmentStore';
import { useRoomStore } from '@/stores/roomStore';
import AppButton from '@/components/shared/AppButton.vue';

const emit = defineEmits(['filter']);
const departmentStore = useDepartmentStore();
const roomStore = useRoomStore();

const filters = ref({
  status: '',
  roomId: null as number | null,
  startDate: '',
  endDate: ''
});

onMounted(async () => {
  await departmentStore.fetchDepartments();
  await roomStore.fetchRooms();
});

const applyFilters = () => {
  const payload: any = {};
  if (filters.value.status) payload.status = filters.value.status;
  if (filters.value.roomId) payload.roomId = filters.value.roomId;
  if (filters.value.startDate) payload.startDate = filters.value.startDate;
  if (filters.value.endDate) payload.endDate = filters.value.endDate;
  
  emit('filter', payload);
};

const clearFilters = () => {
  filters.value = {
    status: '',
    roomId: null,
    startDate: '',
    endDate: ''
  };
  emit('filter', {});
};
</script>

<template>
  <div class="bg-white p-4 rounded-3xl shadow-sm border border-gray-100 mb-6">
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <!-- Status -->
      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1.5">Trạng thái</label>
        <select v-model="filters.status" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-viettel-red outline-none">
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chờ duyệt</option>
          <option value="APPROVED">Đã duyệt</option>
          <option value="REJECTED">Từ chối</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
      </div>

      <!-- Room -->
      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1.5">Phòng</label>
        <select v-model="filters.roomId" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-viettel-red outline-none">
          <option :value="null">Tất cả phòng</option>
          <option v-for="room in roomStore.rooms" :key="room.id" :value="room.id">
            {{ room.name }}
          </option>
        </select>
      </div>

      <!-- Date Range -->
      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1.5">Từ ngày</label>
        <input v-model="filters.startDate" type="date" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-viettel-red outline-none" />
      </div>
      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-1.5">Đến ngày</label>
        <input v-model="filters.endDate" type="date" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-viettel-red outline-none" />
      </div>
    </div>

    <div class="flex justify-end gap-2 mt-4 pt-4 border-t border-gray-100">
      <button @click="clearFilters" class="px-4 py-2 text-sm font-semibold text-gray-500 hover:text-gray-800 transition-colors">
        Xóa bộ lọc
      </button>
      <AppButton label="Áp dụng" size="sm" @click="applyFilters" />
    </div>
  </div>
</template>
