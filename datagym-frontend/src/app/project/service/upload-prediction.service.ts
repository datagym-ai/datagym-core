import { Injectable } from '@angular/core';
import { NgxSmartModalComponent, NgxSmartModalService } from 'ngx-smart-modal';

@Injectable({
  providedIn: 'root'
})
export class UploadPredictionService {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public modalId: string = 'UploadPredictionModal';

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(configuration: object): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(configuration);
    if (modal.hasData()) {
      modal.open();
    }
  }

}
