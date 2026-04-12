import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CategoryResponse,
  CategoryCreateRequest,
} from '../../models/category.models';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/categories`;

  findAll(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.apiUrl);
  }

  findById(id: number): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${this.apiUrl}/${id}`);
  }

  save(dto: CategoryCreateRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.apiUrl, dto);
  }

  patch(id: number, dto: Partial<CategoryCreateRequest>): Observable<CategoryResponse> {
    return this.http.patch<CategoryResponse>(`${this.apiUrl}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}