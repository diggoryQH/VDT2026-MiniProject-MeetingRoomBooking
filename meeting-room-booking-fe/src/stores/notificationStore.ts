import { defineStore } from 'pinia';
import type { Notification } from '../types/notification';
import apiClient from '../api/axios';

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [] as Notification[],
    unreadCount: 0,
    pendingBookings: [] as any[],
    pendingCount: 0,
    isLoading: false,
    error: null as string | null,
  }),

  getters: {
    hasPending: (state) => state.unreadCount > 0,
  },

  actions: {
    async fetchNotifications(page = 1, limit = 20) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/api/v1/notifications', {
          params: { page, limit }
        });
        this.notifications = response.data.data.content;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch notifications';
      } finally {
        this.isLoading = false;
      }
    },

    async fetchUnreadCount() {
      try {
        const response = await apiClient.get('/api/v1/notifications/unread-count');
        this.unreadCount = response.data.data.count;
      } catch (err: any) {
        console.error('Failed to fetch unread count', err);
      }
    },

    async markAsRead(id: number) {
      try {
        await apiClient.patch(`/api/v1/notifications/${id}/read`);
        const notification = this.notifications.find(n => n.id === id);
        if (notification) notification.is_read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to mark as read';
      }
    },

    async markAllAsRead() {
      try {
        await apiClient.patch('/api/v1/notifications/read-all');
        this.notifications.forEach(n => n.is_read = true);
        this.unreadCount = 0;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to mark all as read';
      }
    },

    // Legacy compatibility - fetch pending bookings for approval modal
    async fetchPendingBookings() {
      try {
        const response = await apiClient.get('/api/v1/bookings', {
          params: { status: 'PENDING', limit: 50 }
        });
        this.pendingBookings = response.data.data.content;
        this.pendingCount = this.pendingBookings.length;
        return this.pendingBookings;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch pending bookings';
        return [];
      }
    },
    
    async approveBooking(id: number) {
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/approve`);
        this.pendingBookings = this.pendingBookings.filter(b => b.id !== id);
        this.pendingCount = this.pendingBookings.length;
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to approve booking';
        return false;
      }
    },

    async rejectBooking(id: number, reason?: string) {
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/reject`, { reason });
        this.pendingBookings = this.pendingBookings.filter(b => b.id !== id);
        this.pendingCount = this.pendingBookings.length;
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to reject booking';
        return false;
      }
    },

    clearError() {
      this.error = null;
    }
  }
});
