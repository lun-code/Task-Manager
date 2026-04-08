export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  createdAt: string;
}