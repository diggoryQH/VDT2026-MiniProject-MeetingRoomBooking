<script setup lang="ts">
import { ref } from 'vue';
import AuthTabs from './AuthTabs.vue';
import PhoneInput from './PhoneInput.vue';
import EmailInput from './EmailInput.vue';
import PasswordInput from './PasswordInput.vue';
import AppButton from '../shared/AppButton.vue';
import AppInput from '../shared/AppInput.vue';
import AppDateInput from '../shared/AppDateInput.vue';
import { useValidation } from '../../composables/useValidation';
import { useAuthStore } from '../../stores/authStore';
import type { AuthMode } from '../../types/auth';

const emit = defineEmits(['switch-to-login', 'success']);

const authStore = useAuthStore();
const { errors, validateEmail, validatePhone, validatePassword, validateRequired, validateBirthDate, clearErrors } = useValidation();

const authMode = ref<AuthMode>('signup');
const firstName = ref('');
const lastName = ref('');
const email = ref('');
const birthDate = ref('');
const phone = ref('');
const password = ref('');

const authTabs = [
  { value: 'signup', label: 'Đăng ký' },
  { value: 'login', label: 'Đăng nhập' }
];

const handleSubmit = async () => {
  clearErrors();
  
  errors.value.firstName = validateRequired(firstName.value, 'First name');
  errors.value.lastName = validateRequired(lastName.value, 'Last name');
  errors.value.email = validateEmail(email.value);
  errors.value.birthDate = validateBirthDate(birthDate.value);
  errors.value.phone = validatePhone(phone.value);
  errors.value.password = validatePassword(password.value);

  const hasErrors = Object.values(errors.value).some(err => err !== '');
  if (hasErrors) return;

  await authStore.register({
    firstName: firstName.value,
    lastName: lastName.value,
    email: email.value,
    birthDate: birthDate.value,
    phone: phone.value,
    password: password.value
  });

  emit('success');
};

const handleTabChange = (value: string) => {
  if (value === 'login') {
    emit('switch-to-login');
  }
};
</script>

<template>
  <div class="flex flex-col gap-6">
    <div class="text-center">
      <h1 class="text-2xl font-bold text-gray-900">Bắt đầu ngay</h1>
      <p class="text-sm text-gray-400 mt-1 max-w-[280px] mx-auto">
        Tạo tài khoản hoặc đăng nhập để khám phá ứng dụng
      </p>
    </div>

    <AuthTabs 
      :model-value="authMode" 
      :tabs="authTabs" 
      @update:model-value="handleTabChange"
    />

    <form @submit.prevent="handleSubmit" class="flex flex-col gap-4">
      <div class="flex gap-4">
        <AppInput
          label="Tên"
          v-model="firstName"
          placeholder="Nam"
          :error="errors.firstName"
          class="flex-1"
        />
        <AppInput
          label="Họ"
          v-model="lastName"
          placeholder="Nguyễn"
          :error="errors.lastName"
          class="flex-1"
        />
      </div>

      <EmailInput
        v-model="email"
        :error="errors.email"
      />

      <AppDateInput
        label="Ngày sinh"
        v-model="birthDate"
        :error="errors.birthDate"
      />

      <PhoneInput
        v-model="phone"
        :error="errors.phone"
      />

      <PasswordInput
        label="Mật khẩu"
        v-model="password"
        :error="errors.password"
      />

      <div class="mt-2">
        <AppButton
          type="submit"
          label="Đăng ký"
          :disabled="authStore.isLoading"
        />
      </div>
    </form>
  </div>
</template>
