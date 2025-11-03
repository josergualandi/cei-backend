import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellComponent } from './layout/shell.component';

export const routes: Routes = [
	{ path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
	{
		path: '',
		component: ShellComponent,
		canActivate: [authGuard],
		children: [
			{ path: 'home', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
			{ path: 'empresas', loadComponent: () => import('./features/empresas/list/empresas-list.component').then(m => m.EmpresasListComponent) },
			{ path: 'empresas/novo', loadComponent: () => import('./features/empresas/cadastro/empresa-form.component').then(m => m.EmpresaFormComponent) },
			{ path: 'empresas/:id', loadComponent: () => import('./features/empresas/cadastro/empresa-form.component').then(m => m.EmpresaFormComponent) },
			{ path: 'empresas/:id/editar', loadComponent: () => import('./features/empresas/cadastro/empresa-form.component').then(m => m.EmpresaFormComponent) },
			{ path: 'perfis', loadComponent: () => import('./features/perfis/perfis.component').then(m => m.PerfisComponent) },
			{ path: 'usuarios', loadComponent: () => import('./features/usuarios/usuarios.component').then(m => m.UsuariosComponent) },
			{ path: '', pathMatch: 'full', redirectTo: 'home' }
		]
	},
	{ path: '**', redirectTo: 'login' }
];
