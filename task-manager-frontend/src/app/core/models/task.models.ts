export interface TaskResponse {
  id: string;
  title: string;
  description: string;
  completed: boolean;
  createdAt: string;
  categoryName: string;
}

export interface TaskPageResponse {
  tasks: TaskResponse[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
}

export interface TaskCreateRequest {
  title: string;
  description?: string;
  categoryId: number;
}

export interface TaskPatchRequest {
  title?: string;
  description?: string;
  completed?: boolean;
  categoryId?: number;
}