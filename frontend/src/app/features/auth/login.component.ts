import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  template: `
  <div class="auth-container">
    <h2>Entrar</h2>
    <form (ngSubmit)="onSubmit()" #f="ngForm">
      <label>Email</label>
      <input name="email" [(ngModel)]="email" type="email" required />
      <label>Senha</label>
      <input name="senha" [(ngModel)]="senha" type="password" required />
      <button type="submit" [disabled]="f.invalid || loading">Entrar</button>
      <div class="error" *ngIf="error">{{error}}</div>
    </form>
  </div>
  `,
  styles: [`
    .auth-container { max-width: 360px; margin: 64px auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
    label { display:block; margin-top: 12px; font-weight: 600; }
    input { width: 100%; padding: 10px; margin-top: 6px; border: 1px solid #ccc; border-radius: 6px; }
    button { margin-top: 16px; width: 100%; padding: 10px; border:none; background:#1976d2; color:#fff; border-radius:6px; cursor:pointer; }
    .error { margin-top: 12px; color: #c62828; }
  `]
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  senha = '';
  loading = false;
  error: string | null = null;

  onSubmit() {
    this.loading = true;
    this.error = null;
    this.auth.login({ email: this.email, senha: this.senha }).subscribe({
      next: () => this.router.navigateByUrl('/empresas'),
      error: () => { this.error = 'Credenciais inválidas'; this.loading = false; }
    });
  }
}
