import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/Auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { email, password } = this.form.value;

    this.authService.login({ email: email!, password: password! }).subscribe({
      next: () => this.router.navigate(['/tasks']),
      error: () => {
        this.errorMessage.set('Email o contraseña incorrectos');
        this.loading.set(false);
      },
    });
  }


  // Getters, son tajos para acceder a los campos del formulario desde el HTML sin tener que escribir form.get('email') cada vez. Se usan para mostrar los errores de validación.
  protected get emailControl() {
    return this.form.get('email');
  }

  protected get passwordControl() {
    return this.form.get('password');
  }
}