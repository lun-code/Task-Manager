import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskService } from '../../../core/services/task/task-service';
import { CategoryService } from '../../../core/services/category/category-service';
import { TaskResponse, TaskCreateRequest, TaskPatchRequest } from '../../../core/models/task.models';
import { CategoryResponse } from '../../../core/models/category.models';

@Component({
  selector: 'app-task-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './task-form-modal.html',
  styleUrl: './task-form-modal.css',
})
export class TaskFormModal implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly taskService = inject(TaskService);
  private readonly categoryService = inject(CategoryService);

  // Input: si llega una tarea es modo edición, si no es modo creación
  readonly task = input<TaskResponse | null>(null);

  // Outputs: eventos que emite al padre
  readonly saved = output<void>();
  readonly cancelled = output<void>();

  protected readonly categories = signal<CategoryResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly showCategoryForm = signal(false);
  protected readonly newCategoryName = signal('');

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(255)]],
    categoryId: [null as number | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.loadCategories();

    // Si hay tarea, rellenamos el formulario para edición
    const task = this.task();
    if (task) {
      this.form.patchValue({
        title: task.title,
        description: task.description,
      });
    }
  }

  private loadCategories(): void {
    this.categoryService.findAll().subscribe({
      next: (categories) => {
        this.categories.set(categories);

        // Si estamos editando, seleccionamos la categoría actual
        const task = this.task();
        if (task) {
          const match = categories.find(c => c.name === task.categoryName);
          if (match) {
            this.form.patchValue({ categoryId: match.id });
          }
        }
      },
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { title, description, categoryId } = this.form.value;
    const task = this.task();

    if (task) {
      // Modo edición
      const dto: TaskPatchRequest = {
        title: title!,
        description: description ?? undefined,
        categoryId: categoryId!,
      };

      this.taskService.patch(task.id, dto).subscribe({
        next: () => this.saved.emit(),
        error: () => {
          this.errorMessage.set('Error al actualizar la tarea');
          this.loading.set(false);
        },
      });
    } else {
      // Modo creación
      const dto: TaskCreateRequest = {
        title: title!,
        description: description ?? undefined,
        categoryId: categoryId!,
      };

      this.taskService.save(dto).subscribe({
        next: () => this.saved.emit(),
        error: () => {
          this.errorMessage.set('Error al crear la tarea');
          this.loading.set(false);
        },
      });
    }
  }

  protected onCancel(): void {
    this.cancelled.emit();
  }

  protected toggleCategoryForm(): void {
    this.showCategoryForm.set(!this.showCategoryForm());
    this.newCategoryName.set('');
  }

  protected onNewCategoryNameChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.newCategoryName.set(value);
  }

  protected saveNewCategory(): void {
    const name = this.newCategoryName().trim();
    if (!name || name.length < 3) return;

    this.categoryService.save({ name }).subscribe({
      next: (category) => {
        this.categories.update(cats => [...cats, category]);
        this.form.patchValue({ categoryId: category.id });
        this.showCategoryForm.set(false);
        this.newCategoryName.set('');
      },
      error: () => {
        this.errorMessage.set('Error al crear la categoría');
      },
    });
  }

  protected get titleControl() {
    return this.form.get('title');
  }

  protected get descriptionControl() {
    return this.form.get('description');
  }

  protected get categoryControl() {
    return this.form.get('categoryId');
  }

  protected get isEditing(): boolean {
    return this.task() !== null;
  }
}