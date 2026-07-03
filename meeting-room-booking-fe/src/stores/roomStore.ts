import { defineStore } from 'pinia';
import type { RoomState, Room } from '../types/room';
import apiClient from '../api/axios';

export const useRoomStore = defineStore('room', {
  state: (): RoomState => ({
    rooms: [],
    isLoading: false,
    error: null,
  }),

  actions: {
    async fetchRooms(filters: Record<string, any> = {}) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/api/v1/rooms', {
          params: { limit: 100, ...filters }
        });
        this.rooms = response.data.data.content;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch rooms';
      } finally {
        this.isLoading = false;
      }
    },

    async createRoom(roomData: any) {
      this.error = null;
      try {
        const response = await apiClient.post('/api/v1/rooms', roomData);
        this.rooms.push(response.data.data);
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to create room';
        return false;
      }
    },

    async updateRoom(id: number, roomData: any) {
      this.error = null;
      try {
        const response = await apiClient.patch(`/api/v1/rooms/${id}`, roomData);
        const idx = this.rooms.findIndex(r => r.id === id);
        if (idx !== -1) this.rooms[idx] = response.data.data;
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to update room';
        return false;
      }
    },

    async deleteRoom(id: number) {
      this.error = null;
      try {
        await apiClient.delete(`/api/v1/rooms/${id}`);
        this.rooms = this.rooms.filter(r => r.id !== id);
        return true;
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to delete room';
        return false;
      }
    }
  }
});
