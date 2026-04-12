import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  TaskPageResponse,
  TaskResponse,
  TaskCreateRequest,
  TaskPatchRequest,
} from '../../models/task.models';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/tasks`;

  // Obtener tareas con filtros y paginación
  findAll(page: number = 0, size: number = 10, completed?: boolean, categoryId?: number): Observable<TaskPageResponse> {

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (completed !== undefined && completed !== null) {
      params = params.set('completed', completed.toString()); // HttpParams es inmutable, por eso hacemos params = params.set
    }

    if (categoryId !== undefined && categoryId !== null) {
      params = params.set('categoryId', categoryId.toString());
    }

    return this.http.get<TaskPageResponse>(this.apiUrl, { params });
  }

  findById(id: string): Observable<TaskResponse> {
    return this.http.get<TaskResponse>(`${this.apiUrl}/${id}`);
  }

  save(dto: TaskCreateRequest): Observable<TaskResponse> {
    return this.http.post<TaskResponse>(this.apiUrl, dto);
  }

  patch(id: string, dto: TaskPatchRequest): Observable<TaskResponse> {
    return this.http.patch<TaskResponse>(`${this.apiUrl}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}