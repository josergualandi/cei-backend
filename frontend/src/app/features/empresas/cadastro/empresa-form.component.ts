import { Component, computed, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmpresasService, EmpresaCreateDto, EmpresaDto } from '../empresas.service';
import { AuthService } from '../../../core/auth.service';
import { SnackbarService } from '../../../shared/snackbar/snackbar.service';
import { finalize } from 'rxjs/operators';

type Mode = 'create' | 'edit' | 'view';

@Component({
  standalone: true,
  selector: 'app-empresa-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './empresa-form.component.html',
  styleUrls: ['./empresa-form.component.scss']
})
export class EmpresaFormComponent {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(EmpresasService);
  private auth = inject(AuthService);
  private snackbar = inject(SnackbarService);

  mode: Mode = 'create';
  id: number | null = null;
  saving = false;

  form = this.fb.group({
    tipoPessoa: ['CNPJ', Validators.required],
    numeroDocumento: ['', Validators.required],
    nomeRazaoSocial: ['', Validators.required],
    nomeFantasia: [''],
    email: ['']
  });

  // Suporta mapa vindo do backend como string ou string[]
  serverErrors: Record<string, string | string[]> = {};

  constructor(){
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      const url = this.router.url;
      if (url.endsWith('/editar')) this.mode = 'edit';
      else if (idParam) this.mode = 'view';
      else this.mode = 'create';

      this.id = idParam ? Number(idParam) : null;
      if (this.id) {
        this.svc.getById(this.id).subscribe((e: EmpresaDto) => {
          const tp = (e.tipoPessoa || 'CNPJ').toUpperCase();
          const digits = this.onlyDigits(e.numeroDocumento || '');
          // Exibe mascarado inicialmente também no modo edição para evitar parecer vazio
          const valueForInput = this.maskDocumento(tp, digits);
          this.form.patchValue({
            tipoPessoa: tp,
            numeroDocumento: valueForInput,
            nomeRazaoSocial: e.nomeRazaoSocial,
            nomeFantasia: e.nomeFantasia || '',
            email: e.email || ''
          }, { emitEvent: false });
          this.form.updateValueAndValidity({ onlySelf: false, emitEvent: false });
          if (this.mode === 'view') {
            this.form.disable();
          } else {
            this.form.enable();
            // Se não for ADMIN_MAIN, desabilita tipo/documento sempre.
            // Além disso, se empresa for bloqueada, mantém bloqueado também.
            if (!this.auth.isMaster || e.bloqueada) {
              this.form.get('tipoPessoa')?.disable({ emitEvent: false });
              this.form.get('numeroDocumento')?.disable({ emitEvent: false });
            }
          }
          this.form.markAsPristine();
        });
      } else {
        this.form.reset({ tipoPessoa: 'CNPJ' }, { emitEvent: false });
        this.form.enable();
      }
    });

    // Reaplica validação/máscara quando tipoPessoa mudar
    this.form.get('tipoPessoa')?.valueChanges.subscribe(tp => {
      const ctrl = this.form.get('numeroDocumento');
      if (!ctrl) return;
      // Se estiver editando e o tipo mudou, limpar o documento para reentrada
      if (this.mode === 'edit') {
        ctrl.setValue('', { emitEvent: false });
      }
      // Limpamos erro de servidor e mensagem ao alterar o tipo
      if (ctrl.errors?.['server']) {
        const { server, ...rest } = ctrl.errors;
        ctrl.setErrors(Object.keys(rest).length ? rest : null);
      }
      if (this.serverErrors['numeroDocumento']) {
        delete this.serverErrors['numeroDocumento'];
      }
      const digits = this.onlyDigits(ctrl.value || '');
      const masked = this.maskDocumento((tp || 'CNPJ').toUpperCase(), digits);
      ctrl.setValue(masked, { emitEvent: false });
      ctrl.updateValueAndValidity({ emitEvent: false });
    });

    // Quando Razão Social muda e Fantasia estiver vazia, copia automaticamente
    this.form.get('nomeRazaoSocial')?.valueChanges.subscribe(val => {
      const fantasiaCtrl = this.form.get('nomeFantasia');
      if (!fantasiaCtrl) return;
      const fantasia = (fantasiaCtrl.value || '').toString();
      if (!fantasia) {
        fantasiaCtrl.setValue(val || '', { emitEvent: false });
      }
    });

    // Ao digitar/alterar documento, limpar erro de servidor e mensagem
    this.form.get('numeroDocumento')?.valueChanges.subscribe(() => {
      const ctrl = this.form.get('numeroDocumento');
      if (!ctrl) return;
      if (ctrl.errors?.['server']) {
        const { server, ...rest } = ctrl.errors;
        ctrl.setErrors(Object.keys(rest).length ? rest : null);
      }
      if (this.serverErrors['numeroDocumento']) {
        delete this.serverErrors['numeroDocumento'];
      }
    });
  }

  onCancel(){
    this.router.navigate(['/empresas']);
  }

  onSubmit(){
    this.serverErrors = {};
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.mode === 'edit' && this.form.pristine) {
      this.snackbar.info('Nada para salvar.');
      return;
    }
    const raw = this.form.getRawValue() as any;
    const payload: EmpresaCreateDto = {
      ...raw,
      numeroDocumento: this.onlyDigits(raw.numeroDocumento || '')
    };
    this.saving = true;
    if (this.mode === 'create') {
      this.svc.create(payload).pipe(finalize(() => this.saving = false)).subscribe({
        next: () => { this.snackbar.success('Empresa criada com sucesso.'); this.router.navigate(['/empresas']); },
        error: (err) => this.handleServerError(err)
      });
    } else if (this.mode === 'edit' && this.id != null) {
      this.svc.update(this.id, payload).pipe(finalize(() => this.saving = false)).subscribe({
        next: () => { this.snackbar.success('Empresa atualizada com sucesso.'); this.router.navigate(['/empresas']); },
        error: (err) => this.handleServerError(err)
      });
    }
  }

  private handleServerError(err: any){
    if (err?.status === 400 && err.error && err.error.errors) {
      this.serverErrors = err.error.errors;
      // propaga erro do servidor para os controles, quando possível
      Object.keys(this.serverErrors).forEach(key => {
        const ctrl = this.form.get(key);
        if (ctrl) {
          const existing = ctrl.errors || {};
          ctrl.setErrors({ ...existing, server: true });
        }
      });
    } else if (err?.status === 409) {
      // Conflito (duplicado). Backend envia ProblemDetail com 'errors' quando for doc duplicado.
      const errors = err?.error?.errors;
      if (errors && errors['numeroDocumento']) {
        this.serverErrors = errors;
        const ctrl = this.form.get('numeroDocumento');
        if (ctrl) {
          const existing = ctrl.errors || {};
          ctrl.setErrors({ ...existing, server: true, duplicado: true });
        }
        this.snackbar.error('Documento já cadastrado.');
      } else {
        const detail = err?.error?.detail || 'Registro já existe com este documento.';
        this.snackbar.error(detail);
      }
    } else {
      this.snackbar.error('Falha ao salvar. Tente novamente.');
    }
  }

  // Pega a primeira mensagem do servidor para um campo (string ou string[])
  getServerError(field: string): string | null {
    const v = this.serverErrors?.[field];
    if (!v) return null;
    return Array.isArray(v) ? (v[0] || null) : v;
  }

  // ===== Helpers de máscara/validação CPF/CNPJ =====
  onDocumentoInput(ev: Event){
    const input = ev.target as HTMLInputElement;
    const ctrl = this.form.get('numeroDocumento');
    const tipo = (this.form.get('tipoPessoa')?.value || 'CNPJ').toUpperCase();
    const digits = this.onlyDigits(input.value);
    const masked = this.maskDocumento(tipo, digits);
    input.value = masked;
    ctrl?.setValue(masked, { emitEvent: false });
    ctrl?.updateValueAndValidity({ emitEvent: false });
  }

  // Foco/blur removidos: manter sempre mascarado como na consulta

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

  // Validação de CPF/CNPJ (com dígitos verificadores)
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

  // Validator dinâmico para numeroDocumento
  private numeroDocumentoValidator = (ctrl: AbstractControl) => {
    const tipo = (this.form.get('tipoPessoa')?.value || 'CNPJ').toUpperCase();
    const val = ctrl.value || '';
    if (!val) return { required: true };
    if (tipo === 'CPF') return this.isValidCPF(val) ? null : { cpfInvalido: true };
    if (tipo === 'CNPJ') return this.isValidCNPJ(val) ? null : { cnpjInvalido: true };
    return null;
  };

  ngOnInit(){
    // aplica validator dinâmico após init
    const docCtrl = this.form.get('numeroDocumento');
    docCtrl?.addValidators(this.numeroDocumentoValidator);
  }

  // Placeholder dinâmico para o documento (evita expressão complexa no template)
  getDocumentoPlaceholder(): string {
    const tp = (this.form.get('tipoPessoa')?.value || 'CNPJ').toUpperCase();
    return tp === 'CPF' ? '000.000.000-00' : '00.000.000/0000-00';
  }

  // Habilita salvar quando válido e houve alteração (no modo edição)
  get canSave(): boolean {
    if (this.mode === 'view') return false;
    if (this.saving) return false;
    // Habilita quando campos obrigatórios estão preenchidos;
    // no modo edição, exige alteração (dirty)
    const requiredOk = this.requiredFieldsFilled();
    if (!requiredOk) return false;
    if (this.mode === 'edit') return this.form.dirty;
    return true; // create
  }

  private requiredFieldsFilled(): boolean {
    const tp = (this.form.get('tipoPessoa')?.value || '').toString().trim();
    const docRaw = (this.form.get('numeroDocumento')?.value || '').toString();
    const doc = this.onlyDigits(docRaw);
    const nome = (this.form.get('nomeRazaoSocial')?.value || '').toString().trim();
    return !!tp && !!doc && !!nome;
  }
}
