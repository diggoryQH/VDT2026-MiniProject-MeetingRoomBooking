<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import LoginForm from '@/components/auth/LoginForm.vue';
import RegisterForm from '@/components/auth/RegisterForm.vue';
import type { AuthMode } from '@/types/auth';

const router = useRouter();
const authMode = ref<AuthMode>('login');

const handleSuccess = () => {
  if (authMode.value === 'login') {
    router.push('/');
  } else {
    router.push('/verify');
  }
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-white sm:bg-gray-50 p-4">
    <div class="w-full max-w-md bg-white rounded-3xl sm:shadow-xl sm:p-10 p-4 transition-all duration-300">
      <Transition name="fade" mode="out-in">
        <LoginForm
          v-if="authMode === 'login'"
          @switch-to-register="authMode = 'signup'"
          @success="handleSuccess"
        />
        <RegisterForm
          v-else
          @switch-to-login="authMode = 'login'"
          @success="handleSuccess"
        />
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
