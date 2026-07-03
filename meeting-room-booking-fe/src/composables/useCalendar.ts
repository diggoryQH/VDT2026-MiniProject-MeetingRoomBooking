import { ref, computed } from 'vue';

export const useCalendar = () => {
  const currentDate = ref(new Date());

  const currentWeekDays = computed(() => {
    const days = [];
    const startOfWeek = new Date(currentDate.value);
    const day = startOfWeek.getDay();
    startOfWeek.setDate(startOfWeek.getDate() - day); // Set to Sunday

    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(startOfWeek.getDate() + i);
      days.push(date);
    }
    return days;
  });

  const currentMonthLabel = computed(() => {
    return currentDate.value.toLocaleString('default', { month: 'long', year: 'numeric' });
  });

  const goToNextWeek = () => {
    const nextWeek = new Date(currentDate.value);
    nextWeek.setDate(nextWeek.getDate() + 7);
    currentDate.value = nextWeek;
  };

  const goToPreviousWeek = () => {
    const prevWeek = new Date(currentDate.value);
    prevWeek.setDate(prevWeek.getDate() - 7);
    currentDate.value = prevWeek;
  };

  const selectDate = (date: Date) => {
    currentDate.value = date;
  };

  const isToday = (date: Date) => {
    const today = new Date();
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  };

  const isSelected = (date: Date) => {
    return date.getDate() === currentDate.value.getDate() &&
           date.getMonth() === currentDate.value.getMonth() &&
           date.getFullYear() === currentDate.value.getFullYear();
  };

  const formatDateLabel = (date: Date) => {
    return `${date.getDate()} ${date.toLocaleString('default', { month: 'short' })}`;
  };

  const getDayLabel = (date: Date) => {
    const today = new Date();
    const tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);

    if (date.toDateString() === today.toDateString()) return 'Today';
    if (date.toDateString() === tomorrow.toDateString()) return 'Tomorrow';
    if (date.toDateString() === yesterday.toDateString()) return 'Yesterday';
    
    return date.toLocaleString('default', { weekday: 'long' });
  };

  const formatDateKey = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  return {
    currentDate,
    currentWeekDays,
    currentMonthLabel,
    goToNextWeek,
    goToPreviousWeek,
    selectDate,
    isToday,
    isSelected,
    formatDateLabel,
    getDayLabel,
    formatDateKey
  };
};
