<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { XMarkIcon, ExclamationCircleIcon } from '@heroicons/vue/24/outline';
import AppInput from '@/components/shared/AppInput.vue';
import AppButton from '@/components/shared/AppButton.vue';
import AppSelect from '@/components/shared/AppSelect.vue';
import { useUserStore } from '@/stores/userStore';
import { useDepartmentStore } from '@/stores/departmentStore';
import type { User, UserRequest } from '@/types/user';

interface Props {
  isOpen: boolean;
  user: User | null;
}

const props = defineProps<Props>();
const emit = defineEmits(['close']);

const userStore = useUserStore();
const isSubmitting = ref(false);

const form = reactive({
  name: '',
  email: '',
  username: '',
  password: '',
  role: 'EMPLOYEE' as User['role'],
  department_id: null as number | null
});

const departmentStore = useDepartmentStore();

const roles = [
  { id: 'SUPERADMIN', name: 'Super Admin' },
  { id: 'EMPLOYEE', name: 'Employee' }
];

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    userStore.clearError();
    if (props.user) {
      form.name = props.user.name;
      form.email = props.user.email;
      form.username = props.user.username;
      form.role = props.user.role;
      form.department_id = props.user.department_id || null;
      form.password = ''; 
    } else {
      resetForm();
    }
    departmentStore.fetchDepartments();
  }
});

watch(() => form, () => {
  if (userStore.error) {
    userStore.clearError();
  }
}, { deep: true });

const handleSubmit = async () => {
  isSubmitting.value = true;
  
  let success = false;
  if (props.user) {
    const updateData: UserRequest = { ...form };
    if (!updateData.password) delete updateData.password;
    success = await userStore.updateUser(props.user.id, updateData);
  } else {
    success = await userStore.createUser(form);
  }

  if (success) {
    emit('close');
    resetForm();
  }
  
  isSubmitting.value = false;
};

const resetForm = () => {
  form.name = '';
  form.email = '';
  form.username = '';
  form.password = '';
  form.role = 'EMPLOYEE';
  form.department_id = null;
};
</script>

<template>
  <Transition name="fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('close')"></div>

      <div class="relative w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-y-auto max-h-[calc(100vh-2rem)] glass animate-in fade-in zoom-in duration-300 custom-scrollbar">
        <div class="p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900 font-display">
              {{ user ? 'Sửa người dùng' : 'Thêm người dùng' }}
            </h2>
            <button @click="emit('close')" class="p-2 rounded-full hover:bg-gray-100 text-gray-400 transition-colors">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>

          <!-- Error Message -->
          <Transition name="slide-down">
            <div v-if="userStore.error" class="mb-6 p-4 rounded-2xl bg-red-50 border border-red-100 flex items-start gap-4 animate-shake">
              <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                <ExclamationCircleIcon class="w-6 h-6 text-red-600" />
              </div>
              <div class="flex flex-col gap-0.5 pt-0.5">
                <span class="text-sm font-bold text-red-900">Lỗi! Đã có lỗi xảy ra</span>
                <p class="text-sm text-red-600 leading-relaxed">{{ userStore.error }}</p>
              </div>
            </div>
          </Transition>

          <form @submit.prevent="handleSubmit" class="flex flex-col gap-4">
            <AppInput label="Họ và tên" v-model="form.name" placeholder="Nguyễn Văn A" required />
            <AppInput label="Email" v-model="form.email" type="email" placeholder="nguyenvana@example.com" required />
            <AppInput label="Tên đăng nhập" v-model="form.username" placeholder="nguyenvana" required />
            <AppInput 
              label="Mật khẩu" 
              v-model="form.password" 
              type="password" 
              :placeholder="user ? 'Để trống nếu không muốn đổi' : 'Nhập mật khẩu'" 
              :required="!user" 
            />
            
            <div class="grid grid-cols-2 gap-4">
              <AppSelect 
                label="Vai trò" 
                v-model="form.role" 
                :options="roles" 
                placeholder="Chọn vai trò" 
                required 
              />
              
              <AppSelect 
                label="Phòng ban" 
                v-model="form.department_id" 
                :options="departmentStore.departments" 
                placeholder="Chọn phòng ban" 
              />
            </div>

            <div class="mt-4 flex gap-3">
              <AppButton label="Hủy" variant="outline" @click="emit('close')" />
              <AppButton 
                :label="user ? 'Cập nhật' : 'Tạo mới'" 
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
</style>
