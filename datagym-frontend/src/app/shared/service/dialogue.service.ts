import { EventEmitter, Injectable } from '@angular/core';
import { DialogueModel } from '../dialogue-modal/DialogueModel';
import { NgxSmartModalComponent, NgxSmartModalService } from 'ngx-smart-modal';

@Injectable({
  providedIn: 'root'
})
export class DialogueService {
  public closeAction: EventEmitter<boolean> = new EventEmitter<boolean>();
  private dialogModalId: string = 'dialogueModal';

  constructor(private modalService: NgxSmartModalService) {
  }

  public openDialogue(dialogueContent: DialogueModel): void {
    const dialog: NgxSmartModalComponent = this.modalService.get(this.dialogModalId);
    dialog.setData(dialogueContent);
    if (dialog.hasData()) {
      dialog.open();
    }
  }

  public onCloseAction(choice: boolean) {
    this.closeAction.emit(choice);
  }
}
