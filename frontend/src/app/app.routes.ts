import { Routes } from '@angular/router';

export const routes: Routes = [
	{ path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
	{ path: 'empresas', loadComponent: () => import('./features/empresas/empresas-list.component').then(m => m.EmpresasListComponent) },
	{ path: '', pathMatch: 'full', redirectTo: 'empresas' },
	{ path: '**', redirectTo: 'empresas' }
];
