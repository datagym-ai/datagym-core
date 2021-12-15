import {EventEmitter, Injectable, OnDestroy} from '@angular/core';
import {LabelTaskApiService} from './label-task-api.service';
import {MoveAllProjectsBindingModel} from '../model/MoveAllProjectsBindingModel';
import {Subscription} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class LabelTaskService implements OnDestroy{

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public modalId: string = 'MoveTasksModal';

  /**
   * On submitting the bulk move modal, this event is used to transport the selected values.
   */
  public onMoveLabelTasks: EventEmitter<void> = new EventEmitter<void>();

  private moveAllSubscription: Subscription;

  constructor(private api: LabelTaskApiService) { }

  public ngOnDestroy(): void {
    if (this.moveAllSubscription) {
      this.moveAllSubscription.unsubscribe();
    }
  }

  public moveAllTasks(moveAll: MoveAllProjectsBindingModel): void {
    if (this.moveAllSubscription) {
      this.moveAllSubscription.unsubscribe();
    }
    this.moveAllSubscription = this.api.moveAllTo(moveAll).subscribe(_ => {
      if (this.moveAllSubscription) {
        this.moveAllSubscription.unsubscribe();
      }
      this.onMoveLabelTasks.emit();
    });
  }
}
