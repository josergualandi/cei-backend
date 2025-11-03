import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
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
    const email = (this.email || '').trim();
    const senha = this.senha || '';
    this.auth.login({ email, senha }).subscribe({
  next: () => this.router.navigateByUrl('/home'),
      error: (err) => {
        // Mensagens mais específicas conforme o status
        if (err?.status === 401) {
          this.error = 'E-mail ou senha incorretos.';
        } else if (err?.status === 0) {
          this.error = 'Não foi possível conectar ao servidor. Verifique se a API (8081) está em execução.';
        } else if (err?.error?.detail) {
          this.error = err.error.detail;
        } else {
          this.error = 'Falha ao entrar. Tente novamente.';
        }
        this.loading = false;
      }
    });
  }
}
