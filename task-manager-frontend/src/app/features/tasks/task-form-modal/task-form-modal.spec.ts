import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskFormModal } from './task-form-modal';

describe('TaskFormModal', () => {
  let component: TaskFormModal;
  let fixture: ComponentFixture<TaskFormModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFormModal],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskFormModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
