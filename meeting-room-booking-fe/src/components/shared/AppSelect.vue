<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ChevronDownIcon, MagnifyingGlassIcon, CheckIcon } from '@heroicons/vue/24/outline';

interface Option {
  id: number | string;
  name: string;
  capacity?: number;
  location?: string;
  [key: string]: any;
}

interface Props {
  modelValue: number | string | null;
  options: Option[];
  label?: string;
  placeholder?: string;
  searchPlaceholder?: string;
  error?: string;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Select an option',
  searchPlaceholder: 'Search...',
  error: '',
  disabled: false
});

const emit = defineEmits(['update:modelValue']);

const isOpen = ref(false);
const searchQuery = ref('');
const containerRef = ref<HTMLElement | null>(null);
const searchInputRef = ref<HTMLInputElement | null>(null);

const selectedOption = computed(() => {
  return props.options.find(opt => opt.id === props.modelValue) || null;
});

const filteredOptions = computed(() => {
  if (!searchQuery.value) return props.options;
  const query = searchQuery.value.toLowerCase();
  return props.options.filter(opt => 
    opt.name.toLowerCase().includes(query) || 
    (opt.location && opt.location.toLowerCase().includes(query))
  );
});

const toggleDropdown = () => {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    searchQuery.value = '';
    // Focus search input after transition starts
    setTimeout(() => {
      searchInputRef.value?.focus();
    }, 100);
  }
};

const selectOption = (option: Option) => {
  emit('update:modelValue', option.id);
  isOpen.value = false;
};

const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});
</script>

<template>
  <div class="flex flex-col gap-1.5 relative" ref="containerRef">
    <label v-if="label" class="text-sm font-medium text-gray-600 ml-1">
      {{ label }}
    </label>
    
    <div 
      @click="toggleDropdown"
      class="w-full px-4 py-3.5 bg-gray-50 border border-gray-200 rounded-2xl flex items-center justify-between cursor-pointer transition-all duration-300 group"
      :class="[
        { 'border-red-500 ring-red-100': error },
        { 'opacity-50 cursor-not-allowed bg-gray-100': disabled },
        isOpen ? 'ring-2 ring-viettel-red/30 border-viettel-red bg-white shadow-sm' : 'hover:border-gray-300'
      ]"
    >
      <div class="flex items-center gap-2">
        <span v-if="selectedOption" class="text-base text-gray-900 font-medium">{{ selectedOption.name }}</span>
        <span v-else class="text-base text-gray-400">{{ placeholder }}</span>
      </div>
      
      <ChevronDownIcon 
        class="w-5 h-5 text-gray-400 transition-transform duration-300 ease-out group-hover:text-gray-600"
        :class="{ 'rotate-180 text-gray-900': isOpen }"
      />
    </div>

    <!-- Dropdown Panel -->
    <Transition name="dropdown-slide">
      <div 
        v-if="isOpen" 
        class="absolute top-[calc(100%+8px)] left-0 right-0 z-50 bg-white border border-gray-100 rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.12)] overflow-hidden"
      >
        <!-- Search Input Wrapper -->
        <div class="p-3 border-b border-gray-50">
          <div class="relative group">
            <MagnifyingGlassIcon class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 transition-colors group-focus-within:text-viettel-red-dark" />
            <input 
              v-model="searchQuery"
              type="text"
              class="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-transparent rounded-xl text-sm transition-all focus:bg-white focus:border-gray-100 focus:ring-2 focus:ring-viettel-red/20 outline-none placeholder:text-gray-400"
              :placeholder="searchPlaceholder"
              @click.stop
              ref="searchInputRef"
            />
          </div>
        </div>

        <!-- Options List -->
        <div class="max-h-[280px] overflow-y-auto p-2 custom-scrollbar">
          <div v-if="filteredOptions.length > 0" class="flex flex-col gap-1">
            <div 
              v-for="option in filteredOptions" 
              :key="option.id"
              @click="selectOption(option)"
              class="group flex items-center justify-between px-4 py-3 rounded-2xl cursor-pointer transition-all duration-200"
              :class="[
                option.id === modelValue 
                  ? 'bg-viettel-red/10 text-gray-900' 
                  : 'hover:bg-gray-50 text-gray-700'
              ]"
            >
              <div class="flex flex-col gap-0.5">
                <span class="text-sm font-semibold">{{ option.name }}</span>
                <div class="flex items-center gap-2">
                  <span v-if="option.capacity" class="text-[11px] px-1.5 py-0.5 bg-gray-100 rounded-md text-gray-500 font-medium">
                    {{ option.capacity }} Seats
                  </span>
                  <span v-if="option.location" class="text-[11px] text-gray-400 truncate max-w-[150px]">
                    {{ option.location }}
                  </span>
                </div>
              </div>
              
              <div class="flex items-center">
                <CheckIcon v-if="option.id === modelValue" class="w-5 h-5 text-viettel-red-dark" />
                <div v-else class="w-5 h-5 rounded-full border border-gray-200 group-hover:border-viettel-red/50 transition-colors"></div>
              </div>
            </div>
          </div>
          
          <div v-else class="py-10 px-4 flex flex-col items-center justify-center gap-2 text-center">
            <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center text-gray-300">
              <MagnifyingGlassIcon class="w-6 h-6" />
            </div>
            <p class="text-sm text-gray-400 font-medium">No results found</p>
          </div>
        </div>
      </div>
    </Transition>

    <span v-if="error" class="text-xs text-red-500 ml-1 mt-0.5">
      {{ error }}
    </span>
  </div>
</template>

<style scoped>
.dropdown-slide-enter-active,
.dropdown-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dropdown-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.dropdown-slide-leave-to {
  opacity: 0;
  transform: translateY(-5px) scale(0.98);
}

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
