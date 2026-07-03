import { defineStore } from 'pinia';
import type { Department } from '../types/department';
import apiClient from '../api/axios';

export const useDepartmentStore = defineStore('department', {
  state: () => ({
    departments: [] as Department[],
    isLoading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchDepartments() {
      if (this.departments.length > 0) return; // Cache
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/api/v1/departments');
        this.departments = response.data.data;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch departments';
      } finally {
        this.isLoading = false;
      }
    }
  }
});
