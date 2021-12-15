import {AfterViewInit, Component} from '@angular/core';
import {Subscription} from 'rxjs';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {MoveTasksDialogueService} from '../../service/move-tasks-dialogue.service';

@Component({
  selector: 'app-move-tasks-modal',
  templateUrl: './move-tasks-modal.component.html',
  styleUrls: ['./move-tasks-modal.component.css']
})
export class MoveTasksModalComponent implements AfterViewInit {

  public get modalId(): string {
    return this.moveTasksDialogue.modalId;
  }

  public affectedTasksCounter: number;
  public modal: NgxSmartModalComponent;
  private dataSub: Subscription;

  constructor(
    private modalService: NgxSmartModalService,
    private moveTasksDialogue: MoveTasksDialogueService
    ) { }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);
    this.dataSub = this.modal.onDataAdded.subscribe((affectedTasksCounter: number) => {
      this.affectedTasksCounter = affectedTasksCounter;
    });
  }

  public onClose(): void {
    this.modal.removeData();
  }
  public onMove() {
    this.moveTasksDialogue.closeAction.emit(true);
    this.modal.close();
  }

  public onMoveNot() {
    this.moveTasksDialogue.closeAction.emit(false);
    this.modal.close();
  }

}
