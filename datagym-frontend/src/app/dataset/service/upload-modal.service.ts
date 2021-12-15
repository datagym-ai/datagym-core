import {EventEmitter, Injectable} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Media} from '../../basic/media/model/Media';
import {UploadModalConfiguration} from '../model/UploadModalConfiguration';


@Injectable({
  providedIn: 'root'
})
export class UploadModalService {

  /**
   * This event transports the new uploaded media.
   */
  public onImageUploaded: EventEmitter<Media> = new EventEmitter<Media>();

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public modalId: string = 'UploadModal';

  constructor(private modalService: NgxSmartModalService) { }

  public openDialogue(configuration: UploadModalConfiguration): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(configuration);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
