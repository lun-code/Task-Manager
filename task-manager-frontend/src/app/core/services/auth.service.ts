import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  login(request: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => { // tap es opcional, pero es mejor porque cualquier componente que llame a login() automáticamente tendrá el token guardado, sin tener que recordar hacerlo manualmente.
        localStorage.setItem('token', response.token);
      })
    );
  }

  register(request: RegisterRequest) {
    return this.http.post<UserResponse>(`${this.apiUrl}/signup`, request);
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  getMe() {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }
}