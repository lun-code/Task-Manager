import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth-guard';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { TaskList } from './features/tasks/task-list/task-list';
import { CategoryList } from './features/categories/category-list/category-list';

export const routes: Routes = [
  { path: '', redirectTo: 'tasks', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'tasks', component: TaskList, canActivate: [authGuard] },
  { path: 'categories', component: CategoryList, canActivate: [authGuard] },
  { path: '**', redirectTo: 'tasks' },
];