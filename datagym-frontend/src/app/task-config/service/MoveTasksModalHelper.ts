import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {DatasetList} from '../../dataset/model/DatasetList';
import {EventEmitter} from '@angular/core';


export class MoveTasksModalHelper {

  public onSelected: EventEmitter<string> = new EventEmitter<string>();

  public options: {id: string, label: string}[] = [];

  public selectedDatasetId: string = '';

  constructor(private readonly modalId, private modalService: NgxSmartModalService) {}

  public init(datasets: DatasetList[]): void {
    const all = {id: 'ALL', label: 'FEATURE.TASK_CONFIG.MOVE_TASKS.ALL'};
    // may simplify this double casting.
    const datasetOptions = datasets
      .filter(dataset => dataset.mediaCount > 0)
      .map(dataset => {return {id: dataset.id, label: dataset.name};});

    this.options = [all].concat(datasetOptions);
  }

  public submit(): void {
      const selectedDatasetId = this.selectedDatasetId;
      this.close();
      if (!!selectedDatasetId) {
      this.onSelected.emit(selectedDatasetId);
    }
  }

  public close(): void {
    const modal = this.modalService.get(this.modalId);
    if (!!modal) {
      modal.removeData();
      modal.close();
    }
  }

  public open(): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(true);
    if (modal.hasData()) {
      modal.open();
    }
  }
}
