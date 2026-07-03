<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeftIcon, ShieldCheckIcon } from '@heroicons/vue/24/outline';
import OtpInput from '../components/auth/OtpInput.vue';
import AppButton from '../components/shared/AppButton.vue';
import { useAuthStore } from '../stores/authStore';

const router = useRouter();
const authStore = useAuthStore();
const otpCode = ref('');

const handleConfirm = async () => {
  if (otpCode.value.length !== 4) return;
  
  const success = await authStore.verifyOtp(otpCode.value);
  if (success) {
    router.push('/');
  }
};

const handleBack = () => {
  router.push('/auth');
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white p-6">
    <div class="w-full max-w-md relative">
      <!-- Back Button -->
      <button 
        @click="handleBack"
        class="absolute -top-12 left-0 p-2 text-gray-400 hover:text-gray-600 transition-colors"
      >
        <ChevronLeftIcon class="w-6 h-6" />
      </button>

      <div class="flex flex-col items-center text-center gap-6">
        <!-- Icon -->
        <div class="w-20 h-20 bg-viettel-red-light rounded-full flex items-center justify-center">
          <ShieldCheckIcon class="w-10 h-10 text-viettel-red" />
        </div>

        <div class="flex flex-col gap-2">
          <h1 class="text-2xl font-bold text-gray-900">Xác thực</h1>
          <p class="text-sm font-semibold text-gray-900">Mã xác thực</p>
          <p class="text-sm text-gray-400">
            Nhập mã xác thực chúng tôi đã gửi tới 
            <span class="text-gray-900 font-medium">{{ authStore.pendingVerificationEmail || 'email' }}</span>
          </p>
        </div>

        <OtpInput v-model="otpCode" @complete="handleConfirm" />

        <div class="w-full mt-4">
          <AppButton
            label="Xác nhận"
            :disabled="otpCode.length !== 4 || authStore.isLoading"
            @click="handleConfirm"
          />
        </div>

        <p class="text-sm text-gray-400">
          Chưa nhận được mã? 
          <button 
            @click="authStore.resendOtp"
            class="text-viettel-red font-semibold hover:text-viettel-red-dark ml-1"
          >
            Gửi lại
          </button>
        </p>
      </div>
    </div>
  </div>
</template>
