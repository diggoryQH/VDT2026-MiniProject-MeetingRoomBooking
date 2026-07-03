export interface Room {
  id: number;
  name: string;
  capacity: number;
  location: string;
  equipment?: string;
  is_available: boolean;
  department_id?: number;
  department_name?: string;
  created_at: string;
  updated_at: string;
}

export interface RoomState {
  rooms: Room[];
  isLoading: boolean;
  error: string | null;
}
