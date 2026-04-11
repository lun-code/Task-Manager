import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/Auth/auth.service';
import { UserResponse } from '../../../core/models/auth.models';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {
  private readonly authService = inject(AuthService);

  protected readonly user = signal<UserResponse | null>(null);

  ngOnInit(): void {
    this.authService.getMe().subscribe({
      next: (user) => this.user.set(user),
      error: () => this.logout(),
    });
  }

  protected logout(): void {
    this.authService.logout();
  }
}