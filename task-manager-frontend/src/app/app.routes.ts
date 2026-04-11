import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth-guard';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { TaskList } from './features/tasks/task-list/task-list';
import { EmailSent } from './features/auth/email-sent/email-sent';
import { VerifyEmail } from './features/auth/verify-email/verify-email';

export const routes: Routes = [
  { path: '', redirectTo: 'tasks', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'tasks', component: TaskList, canActivate: [authGuard] },
  { path: 'email-sent', component: EmailSent, canActivate: [guestGuard] },
  { path: 'verify', component: VerifyEmail, canActivate: [guestGuard] },
  { path: '**', redirectTo: 'tasks' },
];