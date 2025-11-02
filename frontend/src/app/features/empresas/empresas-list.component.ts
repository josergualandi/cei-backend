import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmpresasService, EmpresaDto } from './empresas.service';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-empresas-list',
  imports: [CommonModule, RouterLink],
  template: `
  <div class="page">
    <div class="toolbar">
      <h2>Empresas</h2>
      <a routerLink="/login">Sair</a>
    </div>
    <div class="content" *ngIf="empresas; else loadingTpl">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Documento</th>
            <th>Razão Social</th>
            <th>Fantasia</th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let e of empresas">
            <td>{{e.id}}</td>
            <td>{{e.numeroDocumento}}</td>
            <td>{{e.nomeRazaoSocial}}</td>
            <td>{{e.nomeFantasia || '-'}}</td>
            <td>{{e.email || '-'}}</td>
          </tr>
          <tr *ngIf="empresas.length === 0"><td colspan="5">Nenhum registro</td></tr>
        </tbody>
      </table>
    </div>
    <ng-template #loadingTpl>Carregando...</ng-template>
  </div>
  `,
  styles: [`
    .page { padding: 24px; }
    .toolbar { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
    table { width:100%; border-collapse: collapse; }
    th, td { text-align:left; padding: 8px 12px; border-bottom: 1px solid #eee; }
    thead th { border-bottom: 2px solid #ddd; }
  `]
})
export class EmpresasListComponent {
  private svc = inject(EmpresasService);
  empresas: EmpresaDto[] | null = null;

  constructor(){
    this.svc.list().subscribe(res => this.empresas = res);
  }
}
