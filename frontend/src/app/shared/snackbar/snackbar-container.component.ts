import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SnackbarService } from './snackbar.service';

@Component({
  standalone: true,
  selector: 'app-snackbar-container',
  imports: [CommonModule],
  template: `
  <div class="snack-wrap" aria-live="polite" aria-atomic="true">
    <div *ngFor="let s of svc.snacks$ | async" class="snackbar" [class.ok]="s.type==='success'" [class.err]="s.type==='error'" [class.info]="s.type==='info'">
      <span class="msg">{{ s.message }}</span>
      <button class="close" (click)="svc.dismiss(s.id)" aria-label="Fechar">×</button>
    </div>
  </div>
  `,
  styles: [`
  .snack-wrap{ position: fixed; right: 24px; bottom: 24px; z-index: 1000; display:flex; flex-direction:column; gap:8px; }
  .snackbar{ display:flex; align-items:center; gap:12px; background:#333; color:#fff; padding:10px 14px; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,.18); min-width: 240px; max-width: 420px; }
  .snackbar.ok{ background:#2e7d32; }
  .snackbar.err{ background:#c62828; }
  .snackbar.info{ background:#1976d2; }
  .snackbar .close{ background:transparent; border:none; color:#fff; font-size:18px; cursor:pointer; line-height:1; }
  .snackbar .msg{ flex:1; }
  @media (max-width: 600px){ .snack-wrap{ left: 12px; right: 12px; bottom: 12px; } .snackbar{ width:100%; min-width:unset; } }
  `]
})
export class SnackbarContainerComponent{
  svc = inject(SnackbarService);
}
