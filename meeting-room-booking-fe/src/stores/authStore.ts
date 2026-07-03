import axios from 'axios';
import { defineStore } from 'pinia';
import type { AuthState, User } from '../types/auth';
import apiClient from '../api/axios';
import router from '../router';

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('access_token'),
    refresh_token: localStorage.getItem('refresh_token'),
    isAuthenticated: !!localStorage.getItem('access_token'),
    isLoading: false,
    error: null,
  }),

  actions: {
    async login(credentials: any) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.post('/api/v1/auth/login', credentials);
        const { access_token, refresh_token } = response.data.data;
        
        this.token = access_token;
        this.refresh_token = refresh_token;
        this.isAuthenticated = true;
        
        localStorage.setItem('access_token', access_token);
        localStorage.setItem('refresh_token', refresh_token);
        
        await this.getCurrentUser();
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Login failed';
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    async refreshToken() {
      console.log('refreshToken called, current refresh_token:', this.refresh_token);
      if (!this.refresh_token) return false;
      
      try {
        const refreshUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/v1/auth/refresh`;
        console.log('Sending refresh request to:', refreshUrl);
        const response = await axios.post(refreshUrl, {
          refresh_token: this.refresh_token
        });
        console.log('Refresh response:', response.status, response.data);
        const { access_token, refresh_token } = response.data.data;
        
        this.token = access_token;
        this.refresh_token = refresh_token;
        
        localStorage.setItem('access_token', access_token);
        localStorage.setItem('refresh_token', refresh_token);
        
        return true;
      } catch (err) {
        this.logout();
        return false;
      }
    },

    async getCurrentUser() {
      try {
        const response = await apiClient.get('/api/v1/users/current');
        this.user = response.data.data;
      } catch (err: any) {
        console.error('Failed to fetch current user', err);
        if (err.response?.status === 401) {
          this.logout();
        }
      }
    },

    logout(errorMessage?: string) {
      this.user = null;
      this.token = null;
      this.refresh_token = null;
      this.isAuthenticated = false;
      this.error = errorMessage || null;
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      
      router.push('/auth');
    }
  }
});
