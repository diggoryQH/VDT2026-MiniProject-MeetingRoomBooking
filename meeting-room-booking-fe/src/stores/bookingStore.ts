import { defineStore } from 'pinia';
import type { Booking } from '../types/booking';
import apiClient from '../api/axios';

export const useBookingStore = defineStore('booking', {
  state: () => ({
    bookings: [] as Booking[],
    calendarBookings: [] as Booking[],
    isLoading: false,
    error: null as string | null,
    currentPage: 0,
    totalPages: 0,
    hasMore: true,
  }),

  actions: {
    async fetchBookings(page = 1, limit = 10, append = false, filters: Record<string, any> = {}) {
      if (this.isLoading) return;
      
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/api/v1/bookings', {
          params: { page, limit, ...filters }
        });
        
        const { content, page: pageInfo } = response.data.data;
        
        if (append) {
          this.bookings = [...this.bookings, ...content];
        } else {
          this.bookings = content;
        }
        
        this.currentPage = pageInfo.number + 1;
        this.totalPages = pageInfo.totalPages;
        this.hasMore = this.currentPage < this.totalPages;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch bookings';
      } finally {
        this.isLoading = false;
      }
    },

    async fetchCalendarBookings(start: string, end: string) {
      try {
        const response = await apiClient.get('/api/v1/bookings/calendar', {
          params: { start, end }
        });
        this.calendarBookings = response.data.data;
      } catch (err: any) {
        console.error('Failed to fetch calendar bookings', err);
      }
    },

    async fetchMoreBookings() {
      if (!this.hasMore || this.isLoading) return;
      await this.fetchBookings(this.currentPage + 1, 10, true);
    },

    async createBooking(bookingData: any) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.post('/api/v1/bookings', bookingData);
        const newBooking = response.data.data;
        this.bookings.unshift(newBooking);
        return { success: true, error: null };
      } catch (err: any) {
        const errorMsg = err.response?.data?.message || 'Failed to create booking';
        this.error = errorMsg;
        return { success: false, error: errorMsg };
      } finally {
        this.isLoading = false;
      }
    },

    async approveBooking(id: number) {
      this.error = null;
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/approve`);
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) this.bookings[idx].status = 'APPROVED';
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to approve booking';
        return false;
      }
    },

    async rejectBooking(id: number, reason?: string) {
      this.error = null;
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/reject`, { reason });
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) this.bookings[idx].status = 'REJECTED';
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to reject booking';
        return false;
      }
    },

    async cancelBooking(id: number, reason?: string) {
      this.error = null;
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/cancel`, { reason });
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) {
          this.bookings[idx].status = 'CANCELLED';
          this.bookings[idx].cancel_reason = reason;
        }
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to cancel booking';
        return false;
      }
    },

    async checkInBooking(id: number) {
      this.error = null;
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/checkin`);
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) this.bookings[idx].status = 'CHECKED_IN';
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to check-in';
        return false;
      }
    },

    async checkOutBooking(id: number) {
      this.error = null;
      try {
        await apiClient.patch(`/api/v1/bookings/${id}/checkout`);
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) this.bookings[idx].status = 'COMPLETED';
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to check-out';
        return false;
      }
    },

    clearError() {
      this.error = null;
    },

    getBookingsByDate(date: string) {
      return this.bookings.filter(b => b.start_time.startsWith(date));
    }
  }
});
