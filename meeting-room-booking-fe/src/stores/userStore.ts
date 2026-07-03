import { defineStore } from 'pinia';
import type { User, UserRequest, UserState } from '../types/user';
import apiClient from '../api/axios';

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    users: [],
    directoryUsers: [],
    isLoading: false,
    error: null,
    currentPage: 0,
    totalPages: 0,
    hasMore: true,
  }),

  actions: {
    async fetchUsers(page = 1, limit = 10, append = false) {
      if (this.isLoading) return;

      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/api/v1/users', {
          params: { page, limit }
        });
        
        const { content, page: pageInfo } = response.data.data;

        if (append) {
          this.users = [...this.users, ...content];
        } else {
          this.users = content;
        }

        this.currentPage = pageInfo.number + 1;
        this.totalPages = pageInfo.totalPages;
        this.hasMore = this.currentPage < this.totalPages;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch users';
      } finally {
        this.isLoading = false;
      }
    },

    async fetchUserDirectory() {
      try {
        const response = await apiClient.get('/api/v1/users/directory');
        this.directoryUsers = response.data.data;
      } catch (err: any) {
        console.error('Failed to fetch user directory', err);
      }
    },

    async fetchMoreUsers() {
      if (!this.hasMore || this.isLoading) return;
      await this.fetchUsers(this.currentPage + 1, 10, true);
    },

    async createUser(userData: UserRequest) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.post('/api/v1/users', userData);
        const newUser = response.data.data;
        this.users.unshift(newUser);
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to create user';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    async updateUser(id: number, userData: UserRequest) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.patch(`/api/v1/users/${id}`, userData);
        const updatedUser = response.data.data;
        const index = this.users.findIndex(u => u.id === id);
        if (index !== -1) {
          this.users[index] = updatedUser;
        }
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to update user';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    async deleteUser(id: number) {
      this.isLoading = true;
      this.error = null;
      try {
        await apiClient.delete(`/api/v1/users/${id}`);
        this.users = this.users.filter(u => u.id !== id);
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to delete user';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    clearError() {
      this.error = null;
    }
  }
});
