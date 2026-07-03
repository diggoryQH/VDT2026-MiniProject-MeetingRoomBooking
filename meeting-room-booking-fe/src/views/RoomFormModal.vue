<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoomStore } from '@/stores/roomStore';
import { useDepartmentStore } from '@/stores/departmentStore';
import AppButton from '@/components/shared/AppButton.vue';
import { XMarkIcon } from '@heroicons/vue/24/outline';

const props = defineProps<{
  isOpen: boolean;
  roomId: number | null;
}>();

const emit = defineEmits(['close', 'saved']);

const roomStore = useRoomStore();
const departmentStore = useDepartmentStore();

const formData = ref({
  name: '',
  capacity: 10,
  location: '',
  equipment: '',
  is_available: true,
  department_id: null as number | null
});

onMounted(() => {
  if (props.roomId) {
    const room = roomStore.rooms.find(r => r.id === props.roomId);
    if (room) {
      formData.value = {
        name: room.name,
        capacity: room.capacity,
        location: room.location,
        equipment: room.equipment || '',
        is_available: room.is_available,
        department_id: room.department_id || null
      };
    }
  }
});

const isSubmitting = ref(false);

const submit = async () => {
  isSubmitting.value = true;
  
  // Convert empty string or "null" to null for department_id
  const payload = {
    ...formData.value,
    department_id: formData.value.department_id ? Number(formData.value.department_id) : null
  };

  let success = false;
  if (props.roomId) {
    success = await roomStore.updateRoom(props.roomId, payload);
  } else {
    success = await roomStore.createRoom(payload);
  }

  isSubmitting.value = false;
  
  if (success) {
    emit('saved');
    emit('close');
  }
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('close')"></div>
    
    <div class="relative w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-gray-900">{{ roomId ? 'Sửa phòng' : 'Thêm phòng mới' }}</h2>
          <button @click="emit('close')" class="p-2 rounded-full hover:bg-gray-100 text-gray-400">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>

        <div v-if="roomStore.error" class="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-xl">
          {{ roomStore.error }}
        </div>

        <form @submit.prevent="submit" class="flex flex-col gap-4">
          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">Tên phòng</label>
            <input v-model="formData.name" type="text" required class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-gray-700 mb-1">Sức chứa</label>
              <input v-model.number="formData.capacity" type="number" min="1" required class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red" />
            </div>
            <div>
              <label class="block text-sm font-bold text-gray-700 mb-1">Trạng thái</label>
              <select v-model="formData.is_available" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red">
                <option :value="true">Khả dụng</option>
                <option :value="false">Không khả dụng</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">Vị trí</label>
            <input v-model="formData.location" type="text" required class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red" />
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">Phòng ban quản lý</label>
            <select v-model="formData.department_id" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red">
              <option :value="null">Dùng chung (Tất cả phòng ban)</option>
              <option v-for="dept in departmentStore.departments" :key="dept.id" :value="dept.id">
                {{ dept.name }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">Thiết bị (cách nhau bởi dấu phẩy)</label>
            <textarea v-model="formData.equipment" rows="2" placeholder="vd: Máy chiếu, Bảng trắng, Họp trực tuyến" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 outline-none focus:ring-2 focus:ring-viettel-red resize-none"></textarea>
          </div>

          <div class="mt-4">
            <AppButton type="submit" class="w-full" :label="roomId ? 'Lưu thay đổi' : 'Tạo phòng'" :is-loading="isSubmitting" />
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
