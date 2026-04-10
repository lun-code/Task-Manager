import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-verify-email',
  imports: [RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css',
})
export class VerifyEmail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  protected readonly loading = signal(true);
  protected readonly success = signal(false);
  protected readonly errorMessage = signal('');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token'); // Lee el token de la URL, snapshot es una foto de la URL en el momento de la carga del componente

    if (!token) {
      this.loading.set(false);
      this.errorMessage.set('Token de verificación no válido.');
      return;
    }

    this.http.get(`http://localhost:8080/api/auth/verify?token=${token}`, { responseType: 'text' }).subscribe({ // ResponseType es text porque el backend devuelve un string simple, no un JSON, aunque se ignora en este caso
      next: () => {
        this.loading.set(false);
        this.success.set(true);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('El enlace de verificación no es válido o ha expirado.');
      },
    });
  }
}