export interface User {
  id: number;
  name: string;
  username: string;
  email: string;
  role: string;
  department_id?: number;
  department_name?: string;
  created_at: string;
  updated_at: string;
  bookings: any[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refresh_token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  pendingVerificationEmail?: string | null;
}

export type AuthMode = 'login' | 'signup';
export type InputType = 'phone' | 'email';
