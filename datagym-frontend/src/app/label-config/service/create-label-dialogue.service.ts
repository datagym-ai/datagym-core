import { Injectable } from '@angular/core';
import { NgxSmartModalComponent, NgxSmartModalService } from 'ngx-smart-modal';
import {CreateLabelModalConfiguration} from '../model/CreateLabelModalConfiguration';

@Injectable({
  providedIn: 'root'
})
export class CreateLabelDialogueService {
  private modalId: string = 'labelModal';

  constructor(private modalService: NgxSmartModalService) {
  }

  public openCreateDialogue(config: CreateLabelModalConfiguration | null): void {
    if (config === null || !config) {
      config = new CreateLabelModalConfiguration();
    }
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(config);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
