import { ref, computed } from 'vue';

export const useValidation = () => {
  const errors = ref<Record<string, string>>({});

  const validateEmail = (email: string) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) return 'Email is required';
    if (!re.test(email)) return 'Please enter a valid email address';
    return '';
  };

  const validatePhone = (phone: string) => {
    const re = /^\+?[\d\s-]{10,15}$/;
    if (!phone) return 'Phone number is required';
    if (!re.test(phone)) return 'Please enter a valid phone number';
    return '';
  };

  const validatePassword = (password: string) => {
    if (!password) return 'Password is required';
    if (password.length < 8) return 'Password must be at least 8 characters';
    if (!/[A-Z]/.test(password)) return 'Password must contain at least one uppercase letter';
    if (!/[0-9]/.test(password)) return 'Password must contain at least one number';
    return '';
  };

  const validateRequired = (value: string, fieldName: string) => {
    if (!value || value.trim() === '') return `${fieldName} is required`;
    return '';
  };

  const validateBirthDate = (date: string) => {
    if (!date) return 'Birth date is required';
    const birthDate = new Date(date);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    if (age < 13) return 'You must be at least 13 years old';
    return '';
  };

  const isValid = computed(() => Object.values(errors.value).every(err => err === ''));

  const clearErrors = () => {
    errors.value = {};
  };

  return {
    errors,
    isValid,
    validateEmail,
    validatePhone,
    validatePassword,
    validateRequired,
    validateBirthDate,
    clearErrors
  };
};
