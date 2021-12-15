import {Injectable} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {InfoConfiguration} from './InfoConfiguration';

@Injectable({
  providedIn: 'root'
})
export class InfoService {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public modalId: string = 'InfoModal';

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(configuration ?: InfoConfiguration): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(configuration || true);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
