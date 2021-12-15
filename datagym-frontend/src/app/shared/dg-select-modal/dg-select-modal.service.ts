import { Injectable } from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {DgSelectOptionModel} from './DgSelectOptionModel';

/**
 * @deprecated try to use a single component without a service.
 */
@Injectable({
  providedIn: 'root'
})
export class DgSelectModalService {
  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public modalId: string = 'DgSelectModal';

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(configuration: DgSelectOptionModel[], reviewerModalId?: string): void {
    reviewerModalId = typeof reviewerModalId === 'string' ? reviewerModalId : this.modalId;
    const modal: NgxSmartModalComponent = this.modalService.get(reviewerModalId);
    modal.setData(configuration);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
