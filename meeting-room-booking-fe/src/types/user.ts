export interface User {
  id: number;
  name: string;
  email: string;
  username: string;
  role: 'ADMIN' | 'SUPERADMIN' | 'APPROVER' | 'EMPLOYEE';
  department_id?: number;
  department_name?: string;
  created_at?: string;
  updated_at?: string;
}

export interface UserRequest {
  name: string;
  email: string;
  username: string;
  password?: string;
  role: 'ADMIN' | 'APPROVER' | 'EMPLOYEE';
  department_id?: number;
}

export interface UserState {
  users: User[];
  directoryUsers: User[];
  isLoading: boolean;
  error: string | null;
  currentPage: number;
  totalPages: number;
  hasMore: boolean;
}
