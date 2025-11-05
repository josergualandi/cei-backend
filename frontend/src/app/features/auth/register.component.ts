import { Component, inject, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, ConfirmRegistrationPayload, RequestTokenPayload } from '../../core/auth.service';
import { SnackbarService } from '../../shared/snackbar/snackbar.service';
import { EmpresasService } from '../empresas/empresas.service';
import { debounceTime, distinctUntilChanged, filter, switchMap, of, catchError } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnDestroy, AfterViewInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private empresas = inject(EmpresasService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackbar = inject(SnackbarService);

  step: 1 | 2 = 1;
  loading = false;
  // Timers
  resendCooldown = 0; // segundos restantes para habilitar "Reenviar"
  private cooldownInterval?: any;
  tokenExpiresIn = 0; // em segundos (ex.: 600 = 10 minutos)
  private tokenInterval?: any;

  formRequest = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', [Validators.required]],
    tipoPessoa: ['CNPJ', [Validators.required]],
    documento: ['', [Validators.required]],
  });

  formConfirm = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
    token: ['', [Validators.required]],
  });

  ngOnInit(){
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) {
      this.formRequest.patchValue({ email });
    }

    // Validators dinâmicos para CPF/CNPJ
  const docCtrl = this.formRequest.get('documento');
  docCtrl?.addValidators(this.numeroDocumentoValidator);
  // Garante reavaliação imediata de validade após adicionar validators
  docCtrl?.updateValueAndValidity({ emitEvent: false });

    // Reaplicar máscara ao trocar tipoPessoa
    this.formRequest.get('tipoPessoa')?.valueChanges.subscribe(tp => {
      const ctrl = this.formRequest.get('documento');
      if (!ctrl) return;
      ctrl.setValue('', { emitEvent: false });
      ctrl.updateValueAndValidity({ emitEvent: false });
    });

    // Checagem de existência (debounced)
    this.formRequest.get('documento')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter(() => this.formRequest.get('documento')?.valid ?? false),
      switchMap(() => {
        const tp = (this.formRequest.value.tipoPessoa || 'CNPJ').toUpperCase() as 'CPF'|'CNPJ';
        const digits = this.onlyDigits(this.formRequest.value.documento || '');
        if (!digits) return of({ exists: false });
        return this.empresas.exists(tp, digits).pipe(catchError(() => of({ exists: false })));
      })
    ).subscribe(res => {
      this.docExists = !!res?.exists;
    });
  }

  ngOnDestroy() {
    this.stopTimers();
  }

  ngAfterViewInit(): void {
    // Alguns navegadores preenchem campos via autofill sem disparar eventos de input.
    // Sincroniza valores do DOM com o FormGroup após a renderização.
    setTimeout(() => this.syncAutofilledInputs(), 200);
  }

  private stopTimers(){
    if (this.cooldownInterval) { clearInterval(this.cooldownInterval); this.cooldownInterval = undefined; }
    if (this.tokenInterval) { clearInterval(this.tokenInterval); this.tokenInterval = undefined; }
  }

  private startCooldown(seconds = 60){
    this.resendCooldown = seconds;
    if (this.cooldownInterval) clearInterval(this.cooldownInterval);
    this.cooldownInterval = setInterval(() => {
      this.resendCooldown = Math.max(0, this.resendCooldown - 1);
      if (this.resendCooldown === 0) {
        clearInterval(this.cooldownInterval);
        this.cooldownInterval = undefined;
      }
    }, 1000);
  }

  private startTokenTimer(seconds = 600){
    this.tokenExpiresIn = seconds;
    if (this.tokenInterval) clearInterval(this.tokenInterval);
    this.tokenInterval = setInterval(() => {
      this.tokenExpiresIn = Math.max(0, this.tokenExpiresIn - 1);
      if (this.tokenExpiresIn === 0) {
        clearInterval(this.tokenInterval);
        this.tokenInterval = undefined;
      }
    }, 1000);
  }

  formatSeconds(s: number): string {
    const m = Math.floor((s || 0) / 60).toString().padStart(2, '0');
    const ss = Math.floor((s || 0) % 60).toString().padStart(2, '0');
    return `${m}:${ss}`;
  }

  onRequestToken(){
    if (this.formRequest.invalid) return;
    if (this.docExists) {
      this.snackbar.info('Documento já cadastrado. Utilize outro documento ou faça login.');
      return;
    }
    this.loading = true;
    const raw = this.formRequest.getRawValue() as any;
    const payload: RequestTokenPayload = { email: raw.email, telefone: raw.telefone };
    this.auth.requestRegistrationToken(payload).subscribe({
      next: () => {
        this.snackbar.success('Enviamos um código por e-mail e SMS.');
        this.step = 2;
        this.loading = false;
        // Inicia timers: cooldown para reenvio e expiração de 10 min (espelhando backend)
        this.startCooldown(60);
        this.startTokenTimer(600);
      },
      error: (err) => {
        const status = err?.status;
        const detail = err?.error?.detail || 'Não foi possível enviar o código. Tente novamente.';
        if (status === 409 && detail === 'usuario.ja.existe') {
          this.snackbar.info('E-mail já cadastrado. Faça login.');
          const email = this.formRequest.value.email || '';
          this.router.navigate(['/login'], { queryParams: email ? { email } : undefined });
        } else {
          this.snackbar.error(detail);
        }
        this.loading = false;
      }
    });
  }

  onConfirm(){
    if (this.formConfirm.invalid || this.formRequest.invalid) return;
    this.loading = true;
    const email = this.formRequest.value.email!;
    const payload: ConfirmRegistrationPayload = {
      email,
      nome: this.formConfirm.value.nome!,
      senha: this.formConfirm.value.senha!,
      token: this.formConfirm.value.token!,
    };
    this.auth.confirmRegistration(payload).subscribe({
      next: () => {
        this.snackbar.success('Cadastro confirmado! Faça login.');
        this.router.navigate(['/login'], { queryParams: { email } });
      },
      error: (err) => {
        const status = err?.status;
        const detail = err?.error?.detail || 'Não foi possível confirmar o cadastro. Verifique o código e tente novamente.';
        if (status === 409 && detail === 'usuario.ja.existe') {
          this.snackbar.info('E-mail já cadastrado. Faça login.');
          this.router.navigate(['/login'], { queryParams: { email } });
        } else {
          this.snackbar.error(detail);
        }
        this.loading = false;
      }
    });
  }

  onResendCode(){
    if (this.resendCooldown > 0 || this.loading) return;
    if (this.formRequest.invalid) return;
    this.loading = true;
    const raw = this.formRequest.getRawValue() as any;
    const payload: RequestTokenPayload = { email: raw.email, telefone: raw.telefone };
    this.auth.requestRegistrationToken(payload).subscribe({
      next: () => {
        this.snackbar.success('Código reenviado.');
        this.loading = false;
        this.startCooldown(60);
        this.startTokenTimer(600); // reinicia expiração
      },
      error: (err) => {
        const status = err?.status;
        const detail = err?.error?.detail || 'Não foi possível reenviar o código.';
        if (status === 409 && detail === 'usuario.ja.existe') {
          this.snackbar.info('E-mail já cadastrado. Faça login.');
          const email = this.formRequest.value.email || '';
          this.router.navigate(['/login'], { queryParams: email ? { email } : undefined });
        } else {
          this.snackbar.error(detail);
        }
        this.loading = false;
      }
    });
  }

  backToLogin(){
    const email = this.formRequest.value.email || '';
    this.router.navigate(['/login'], { queryParams: email ? { email } : undefined });
  }

  // ===== Telefone (BR +55) e Documento (CPF/CNPJ) =====
  docExists = false;

  onTelefoneInput(ev: Event){
    const input = ev.target as HTMLInputElement;
    const digits = this.onlyDigits(input.value);
    // Formata como +55 (DD) 9XXXX-XXXX ou +55 (DD) XXXX-XXXX
    let d = digits;
    if (d.startsWith('55')) d = d.substring(2);
    d = d.substring(0, 11);
    let out = '';
    if (d.length <= 10) {
      // (DD) XXXX-XXXX
      out = d
        .replace(/(\d{2})(\d)/, '+55 ($1) $2')
        .replace(/(\d{2}) (\d{4})(\d)/, '+55 ($1) $2-$3')
        .replace(/(\d{2}) (\d{4})-(\d{4}).*/, '+55 ($1) $2-$3');
    } else {
      // (DD) 9XXXX-XXXX
      out = d
        .replace(/(\d{2})(\d)/, '+55 ($1) $2')
        .replace(/(\d{2}) (\d{5})(\d)/, '+55 ($1) $2-$3')
        .replace(/(\d{2}) (\d{5})-(\d{4}).*/, '+55 ($1) $2-$3');
    }
    input.value = out;
    const telCtrl = this.formRequest.get('telefone');
    telCtrl?.setValue(out, { emitEvent: false });
    // Garante que o estado de validade/disabled do botão seja atualizado imediatamente
    telCtrl?.updateValueAndValidity({ emitEvent: false });
  }

  onDocumentoInput(ev: Event){
    const input = ev.target as HTMLInputElement;
    const tp = (this.formRequest.value.tipoPessoa || 'CNPJ').toUpperCase();
    const digits = this.onlyDigits(input.value);
    input.value = this.maskDocumento(tp, digits);
    this.formRequest.get('documento')?.setValue(input.value, { emitEvent: false });
    this.formRequest.get('documento')?.updateValueAndValidity({ emitEvent: false });
  }

  private syncAutofilledInputs(){
    const emailEl = document.querySelector('input[formControlName="email"]') as HTMLInputElement | null;
    const telEl = document.querySelector('input[formControlName="telefone"]') as HTMLInputElement | null;
    const docEl = document.querySelector('input[formControlName="documento"]') as HTMLInputElement | null;

    if (emailEl) {
      const ctrl = this.formRequest.get('email');
      const v = (emailEl.value || '').trim();
      if (v && ctrl?.value !== v) {
        ctrl?.setValue(v, { emitEvent: true });
        ctrl?.updateValueAndValidity();
      }
    }

    if (telEl) {
      // Reusa a lógica de máscara/validação do próprio input handler
      this.onTelefoneInput({ target: telEl } as any as Event);
    }

    if (docEl) {
      this.onDocumentoInput({ target: docEl } as any as Event);
    }

    // Atualiza o form como um todo após sincronizar
    this.formRequest.updateValueAndValidity();
  }

  getDocumentoPlaceholder(): string {
    const tp = (this.formRequest.value.tipoPessoa || 'CNPJ').toUpperCase();
    return tp === 'CPF' ? '000.000.000-00' : '00.000.000/0000-00';
  }

  private onlyDigits(v: string): string { return (v || '').replace(/\D+/g, ''); }
  private maskDocumento(tipo: string, digits: string): string {
    if (tipo === 'CNPJ') {
      const d = digits.substring(0,14);
      return d
        .replace(/(\d{2})(\d)/, '$1.$2')
        .replace(/(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/(\d{2})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3/$4')
        .replace(/(\d{2})\.(\d{3})\.(\d{3})\/(\d{4})(\d{1,2}).*/, '$1.$2.$3/$4-$5');
    }
    if (tipo === 'CPF') {
      const d = digits.substring(0,11);
      return d
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/(\d{3})\.(\d{3})\.(\d{3})(\d{1,2}).*/, '$1.$2.$3-$4');
    }
    return digits;
  }

  private isValidCPF(cpf: string): boolean {
    const d = this.onlyDigits(cpf);
    if (!d || d.length !== 11 || /^([0-9])\1+$/.test(d)) return false;
    let sum = 0;
    for (let i=0;i<9;i++) sum += parseInt(d.charAt(i)) * (10 - i);
    let r = 11 - (sum % 11); if (r >= 10) r = 0; if (r !== parseInt(d.charAt(9))) return false;
    sum = 0;
    for (let i=0;i<10;i++) sum += parseInt(d.charAt(i)) * (11 - i);
    r = 11 - (sum % 11); if (r >= 10) r = 0; return r === parseInt(d.charAt(10));
  }

  private isValidCNPJ(cnpj: string): boolean {
    const d = this.onlyDigits(cnpj);
    if (!d || d.length !== 14 || /^([0-9])\1+$/.test(d)) return false;
    const calc = (len: number) => {
      let sum = 0;
      let pos = len - 7;
      for (let i = len; i >= 1; i--) {
        sum += parseInt(d.charAt(len - i)) * pos--;
        if (pos < 2) pos = 9;
      }
      const r = sum % 11;
      return r < 2 ? 0 : 11 - r;
    };
    const dv1 = calc(12);
    if (dv1 !== parseInt(d.charAt(12))) return false;
    const dv2 = calc(13);
    return dv2 === parseInt(d.charAt(13));
  }

  private numeroDocumentoValidator = (ctrl: AbstractControl) => {
    const tipo = (this.formRequest.get('tipoPessoa')?.value || 'CNPJ').toUpperCase();
    const val = ctrl.value || '';
    if (!val) return { required: true };
    if (tipo === 'CPF') return this.isValidCPF(val) ? null : { cpfInvalido: true };
    if (tipo === 'CNPJ') return this.isValidCNPJ(val) ? null : { cnpjInvalido: true };
    return null;
  };
}
