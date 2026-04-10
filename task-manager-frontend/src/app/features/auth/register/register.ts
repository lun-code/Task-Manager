import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/Auth/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { fullName, email, password } = this.form.value;

    this.authService.register({ fullName: fullName!, email: email!, password: password! }).subscribe({
      next: () => this.router.navigate(['/email-sent']),
      error: (err) => {
        if (err.status === 409) {
          this.errorMessage.set('Este email ya está en uso');
        } else {
          this.errorMessage.set('Ha ocurrido un error, inténtalo de nuevo');
        }
        this.loading.set(false);
      },
    });
  }

  protected get fullNameControl() {
    return this.form.get('fullName');
  }

  protected get emailControl() {
    return this.form.get('email');
  }

  protected get passwordControl() {
    return this.form.get('password');
  }
}