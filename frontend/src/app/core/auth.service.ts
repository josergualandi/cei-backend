import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, tap } from 'rxjs';

interface LoginRequest { email: string; senha: string; }
interface LoginResponse { tokenType: string; accessToken: string; expiresIn: number; roles: string[]; }
export interface RequestTokenPayload { email: string; telefone: string; }
export interface ConfirmRegistrationPayload { email: string; nome: string; senha: string; token: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private readonly tokenKey = 'auth_token';

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, req).pipe(
      tap(res => localStorage.setItem(this.tokenKey, res.accessToken))
    );
  }

  requestRegistrationToken(payload: RequestTokenPayload) {
    return this.http.post(`${environment.apiBaseUrl}/auth/register/request-token`, payload);
  }

  confirmRegistration(payload: ConfirmRegistrationPayload) {
    return this.http.post(`${environment.apiBaseUrl}/auth/register/confirm`, payload);
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get isAuthenticated(): boolean {
    return !!this.token;
  }
}
