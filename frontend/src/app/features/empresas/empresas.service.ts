import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface EmpresaDto {
  id: number;
  tipoPessoa: string;
  numeroDocumento: string;
  nomeRazaoSocial: string;
  nomeFantasia?: string;
  tipoAtividade?: string;
  cnae?: string;
  dataAbertura?: string; // LocalDate ISO string
  situacao?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  telefone?: string;
  email?: string;
  criadoEm?: string; // Instant ISO
  atualizadoEm?: string; // Instant ISO
}

export interface EmpresaCreateDto {
  tipoPessoa: string;
  numeroDocumento: string;
  nomeRazaoSocial: string;
  nome?: string;
  cnpj?: string;
  nomeFantasia?: string;
  tipoAtividade?: string;
  cnae?: string;
  dataAbertura?: string; // yyyy-MM-dd
  situacao?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  telefone?: string;
  email?: string;
}

@Injectable({ providedIn: 'root' })
export class EmpresasService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/api/empresas`;

  list(): Observable<EmpresaDto[]> {
    return this.http.get<EmpresaDto[]>(this.base);
  }

  create(payload: EmpresaCreateDto): Observable<EmpresaDto> {
    return this.http.post<EmpresaDto>(this.base, payload);
  }

  getById(id: number): Observable<EmpresaDto> {
    return this.http.get<EmpresaDto>(`${this.base}/${id}`);
  }

  update(id: number, payload: EmpresaCreateDto): Observable<EmpresaDto> {
    return this.http.put<EmpresaDto>(`${this.base}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
