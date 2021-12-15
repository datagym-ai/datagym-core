import {Component, OnInit} from '@angular/core';
import {UploadModalConfiguration} from '../../../../dataset/model/UploadModalConfiguration';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-import-prediction',
  templateUrl: './import-prediction.component.html',
  styleUrls: ['./import-prediction.component.css']
})
export class ImportPredictionComponent implements OnInit {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public readonly modalId: string = 'UploadPredictionModal';

  public get uploadUrl(): string {

    const projectId: string = Object.keys(this.configuration).includes('id')
      ? this.configuration['id'] as string
      : '';

    return `/api/project/${ projectId }/prediction`;
  }

  get hasConfiguration(): boolean {
    return !!this?.configuration || false;
  }

  public isCloseable: boolean = true;
  public isOpen: boolean = false;

  private dataSub: Subscription;
  private modal: NgxSmartModalComponent;
  /**
   * A fully generic configuration.
   */
  private configuration: object = {};


  constructor(
    private modalService: NgxSmartModalService,
  ) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);
    this.modal.onOpen.subscribe(()=>{
      this.isOpen = true;
    });
    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    this.dataSub = this.modal.onDataAdded.subscribe((configuration: UploadModalConfiguration) => {
      this.configuration = configuration;
    });
  }


  ngOnDestroy(): void {
    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    if (this.modal) {
      this.modal.close();
    }
  }

  public onClose(): void {
    this.modal.removeData();
    this.isOpen = false;
  }

}
