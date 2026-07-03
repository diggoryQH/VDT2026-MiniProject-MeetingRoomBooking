<script setup lang="ts">
import { ref } from 'vue';
import { ExclamationCircleIcon } from '@heroicons/vue/24/outline';
import PasswordInput from './PasswordInput.vue';
import AppButton from '../shared/AppButton.vue';
import { useAuthStore } from '../../stores/authStore';

const emit = defineEmits(['switch-to-register', 'forgot-password', 'success']);

const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const rememberMe = ref(false);

const handleSubmit = async () => {
  if (!username.value || !password.value) return;

  try {
    await authStore.login({
      username: username.value,
      password: password.value
    });

    if (authStore.isAuthenticated) {
      emit('success');
    }
  } catch (err) {
    // Error handled in store
  }
};
</script>

<template>
  <div class="flex flex-col gap-6">
    <div class="text-center">
      <h1 class="text-2xl font-bold text-gray-900">Chào mừng trở lại</h1>
      <p class="text-sm text-gray-400 mt-1">Đăng nhập để truy cập tài khoản của bạn</p>
    </div>

    <form @submit.prevent="handleSubmit" class="flex flex-col gap-5">
      <div class="flex flex-col gap-2">
        <label class="text-sm font-medium text-gray-700">Tên đăng nhập</label>
        <input
          v-model="username"
          type="text"
          placeholder="Nhập tên đăng nhập"
          class="w-full px-4 py-3 rounded-2xl bg-gray-50 border border-transparent focus:bg-white focus:border-gray-200 focus:ring-0 transition-all outline-hidden text-sm"
          required
        />
      </div>

      <PasswordInput
        v-model="password"
      />

      <Transition name="slide-down">
        <div v-if="authStore.error" class="p-4 rounded-2xl bg-red-50 border border-red-100 flex items-start gap-4 animate-shake">
          <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
            <ExclamationCircleIcon class="w-6 h-6 text-red-600" />
          </div>
          <div class="flex flex-col gap-0.5 pt-0.5">
            <span class="text-sm font-bold text-red-900">Lỗi!</span>
            <p class="text-sm text-red-600 leading-relaxed">{{ authStore.error }}</p>
          </div>
        </div>
      </Transition>

      <div class="flex items-center justify-between">
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            v-model="rememberMe"
            class="w-4 h-4 rounded border-gray-300 text-viettel-red focus:ring-viettel-red"
          />
          <span class="text-sm text-gray-600">Ghi nhớ đăng nhập</span>
        </label>
      </div>

      <AppButton
        type="submit"
        label="Đăng nhập"
        :disabled="authStore.isLoading"
      />

      <div class="text-center mt-2">
        <p class="text-sm text-gray-500">
          Chưa có tài khoản? 
          <button type="button" @click="emit('switch-to-register')" class="text-viettel-red font-bold hover:underline">
            Đăng ký
          </button>
        </p>
      </div>
    </form>
  </div>
</template>

<style scoped>
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
</style>
