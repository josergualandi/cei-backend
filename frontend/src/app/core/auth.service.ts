import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, tap } from 'rxjs';

interface LoginRequest { email: string; senha: string; }
interface LoginResponse { tokenType: string; accessToken: string; expiresIn: number; roles: string[]; }
export interface RequestTokenPayload { email: string; telefone: string; tipoPessoa: 'CPF' | 'CNPJ'; numeroDocumento: string; }
export interface ConfirmRegistrationPayload { email: string; nome: string; senha: string; token: string; tipoPessoa?: 'CPF' | 'CNPJ'; numeroDocumento?: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private readonly tokenKey = 'auth_token';
  private readonly rolesKey = 'auth_roles';

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, req).pipe(
      tap(res => {
        localStorage.setItem(this.tokenKey, res.accessToken);
        if (Array.isArray(res.roles)) {
          localStorage.setItem(this.rolesKey, JSON.stringify(res.roles));
        }
      })
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
    localStorage.removeItem(this.rolesKey);
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get isAuthenticated(): boolean {
    return !!this.token;
  }

  get roles(): string[] {
    try { return JSON.parse(localStorage.getItem(this.rolesKey) || '[]') as string[]; } catch { return []; }
  }

  hasRole(role: string): boolean { return this.roles.some(r => r.toUpperCase() === role.toUpperCase()); }
  get isMaster(): boolean { return this.hasRole('MASTER') || this.hasRole('ADMIN_MAIN'); }
}
