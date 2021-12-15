import {EventEmitter, Injectable} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';

@Injectable({
  providedIn: 'root'
})
export class MoveTasksDialogueService {

  public readonly modalId: string = 'MoveTasksModal';

  public readonly closeAction: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(affectedTasksCounter: number): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(affectedTasksCounter);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
