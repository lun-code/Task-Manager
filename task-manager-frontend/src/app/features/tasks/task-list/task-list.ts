import { Component, inject, OnInit, signal } from '@angular/core';
import { TaskService } from '../../../core/services/task/task-service';
import { CategoryService } from '../../../core/services/category/category-service';
import { TaskResponse } from '../../../core/models/task.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { TaskFormModal } from '../task-form-modal/task-form-modal';

@Component({
  selector: 'app-task-list',
  imports: [TaskFormModal],
  templateUrl: './task-list.html',
  styleUrl: './task-list.css',
})
export class TaskList implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly categoryService = inject(CategoryService);

  protected readonly tasks = signal<TaskResponse[]>([]);
  protected readonly categories = signal<CategoryResponse[]>([]);
  protected readonly loading = signal(false);

  // Paginación
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly pageSize = 10;

  // Filtros
  protected readonly filterCompleted = signal<boolean | undefined>(undefined);
  protected readonly filterCategoryId = signal<number | undefined>(undefined);

  // Modal
  protected readonly showModal = signal(false);
  protected readonly selectedTask = signal<TaskResponse | null>(null);

  ngOnInit(): void {
    this.loadTasks();
    this.loadCategories();
  }

  private loadTasks(): void {
    this.loading.set(true);
    this.taskService.findAll(
      this.currentPage(),
      this.pageSize,
      this.filterCompleted(),
      this.filterCategoryId(),
    ).subscribe({
      next: (page) => {
        this.tasks.set(page.tasks);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadCategories(): void {
    this.categoryService.findAll().subscribe({
      next: (categories) => this.categories.set(categories),
    });
  }

  protected onFilterCompleted(value: string): void {
    if (value === 'all') {
      this.filterCompleted.set(undefined);
    } else {
      this.filterCompleted.set(value === 'true');
    }
    this.currentPage.set(0);
    this.loadTasks();
  }

  protected onFilterCategory(value: string): void {
    this.filterCategoryId.set(value === 'all' ? undefined : Number(value));
    this.currentPage.set(0);
    this.loadTasks();
  }

  protected goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadTasks();
  }

  protected openCreateModal(): void {
    this.selectedTask.set(null);
    this.showModal.set(true);
  }

  protected openEditModal(task: TaskResponse): void {
    this.selectedTask.set(task);
    this.showModal.set(true);
  }

  protected closeModal(): void {
    this.showModal.set(false);
    this.selectedTask.set(null);
  }

  protected onModalSaved(): void {
    this.closeModal();
    this.loadTasks();
  }

  protected toggleComplete(task: TaskResponse): void {
    this.taskService.patch(task.id, { completed: !task.completed }).subscribe({
      next: () => this.loadTasks(),
    });
  }

  protected deleteTask(task: TaskResponse): void {
    if (!confirm(`¿Eliminar la tarea "${task.title}"?`)) return;
    this.taskService.delete(task.id).subscribe({
      next: () => this.loadTasks(),
    });
  }

  protected formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }
}