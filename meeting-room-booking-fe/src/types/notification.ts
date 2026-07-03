export interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  is_read: boolean;
  booking_id?: number;
  created_at: string;
}
