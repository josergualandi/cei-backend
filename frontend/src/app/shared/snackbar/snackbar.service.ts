import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type SnackType = 'success' | 'error' | 'info';

export interface Snack {
  id: number;
  message: string;
  type: SnackType;
  timeout?: number;
}

@Injectable({ providedIn: 'root' })
export class SnackbarService {
  private seq = 1;
  private _snacks$ = new BehaviorSubject<Snack[]>([]);
  snacks$ = this._snacks$.asObservable();

  private push(snack: Omit<Snack, 'id'>){
    const s: Snack = { id: this.seq++, ...snack };
    const list = [...this._snacks$.value, s];
    this._snacks$.next(list);
    const ttl = snack.timeout ?? 3500;
    if (ttl > 0){
      setTimeout(() => this.dismiss(s.id), ttl);
    }
  }

  dismiss(id: number){
    this._snacks$.next(this._snacks$.value.filter(s => s.id !== id));
  }

  success(message: string, timeout = 3000){ this.push({ message, type: 'success', timeout }); }
  error(message: string, timeout = 4000){ this.push({ message, type: 'error', timeout }); }
  info(message: string, timeout = 3000){ this.push({ message, type: 'info', timeout }); }
}
