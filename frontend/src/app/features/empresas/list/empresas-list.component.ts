import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { EmpresasService, EmpresaDto } from '../empresas.service';
import { AuthService } from '../../../core/auth.service';
import { SnackbarService } from '../../../shared/snackbar/snackbar.service';

@Component({
  standalone: true,
  selector: 'app-empresas-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './empresas-list.component.html',
  styleUrls: ['./empresas-list.component.scss']
})
export class EmpresasListComponent {
  private svc = inject(EmpresasService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private snackbar = inject(SnackbarService);
  empresas: EmpresaDto[] | null = null;

  constructor(){
    this.load();
  }

  load(){
    this.svc.list().subscribe(res => this.empresas = res);
  }

  onInsert(){
    this.router.navigate(['/empresas/novo']);
  }

  onView(e: EmpresaDto){
    this.router.navigate(['/empresas', e.id]);
  }

  onEdit(e: EmpresaDto){
    this.router.navigate(['/empresas', e.id, 'editar']);
  }

  onDelete(e: EmpresaDto){
    const ok = confirm(`Deseja excluir a empresa "${e.nomeRazaoSocial}"?`);
    if (!ok) return;
    this.svc.delete(e.id).subscribe({
      next: () => { this.snackbar.success('Empresa excluída com sucesso.'); this.load(); },
      error: () => this.snackbar.error('Falha ao excluir. Tente novamente.')
    });
  }

  formatDocumento(e: EmpresaDto): string {
    const doc = e?.numeroDocumento || '';
    if (!doc) return '-';
    const tipo = (e?.tipoPessoa || '').toUpperCase();
    if (tipo === 'CNPJ' && doc.length === 14) {
      // 00.000.000/0000-00
      return doc.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    }
    if (tipo === 'CPF' && doc.length === 11) {
      // 000.000.000-00
      return doc.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    return doc;
  }

  get isAdmin(): boolean { return this.auth.isMaster; }
}
