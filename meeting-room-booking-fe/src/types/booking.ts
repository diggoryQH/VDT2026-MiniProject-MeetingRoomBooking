export interface Booking {
  id: number;
  user_id: number;
  user_name?: string;
  room_id: number;
  room_name?: string;
  room_department_id?: number;
  start_time: string;
  end_time: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  description: string;
  approved_by?: number;
  approved_by_name?: string;
  attendee_ids?: number[];
  cancel_reason?: string;
  created_at: string;
  updated_at: string;
}

export interface DayScheduleData {
  date: Date;
  dateLabel: string;
  dayLabel: string;
  bookings: Booking[];
}
