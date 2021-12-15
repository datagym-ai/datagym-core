import {EventEmitter, Injectable} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from "ngx-smart-modal";
import {LegendConfiguration} from "./LegendConfiguration";

@Injectable({
  providedIn: 'root'
})
export class LegendService {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public modalId: string = "LegendModal";

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(configuration ?: LegendConfiguration): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(configuration || true);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
