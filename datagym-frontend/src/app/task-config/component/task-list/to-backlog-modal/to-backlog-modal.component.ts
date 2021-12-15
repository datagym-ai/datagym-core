import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DatasetList} from '../../../../dataset/model/DatasetList';
import {NgxSmartModalService} from 'ngx-smart-modal';
import {MoveTasksModalHelper} from '../../../service/MoveTasksModalHelper';

@Component({
  selector: 'app-to-backlog-modal',
  templateUrl: './to-backlog-modal.component.html',
  styleUrls: ['./to-backlog-modal.component.css']
})
export class ToBacklogModalComponent implements OnInit {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public readonly modalId: string = 'ToBacklogModal';

  @Input()
  public disabled: boolean = true;

  @Input()
  public readonly datasets: DatasetList[];

  // @Output()
  // public onSelected: EventEmitter<string> = new EventEmitter<string>();
  @Output()
  public get onSelected(): EventEmitter<string> {
    return this.actions.onSelected;
  }

  public readonly actions: MoveTasksModalHelper;

  constructor(private modalService: NgxSmartModalService) {
    this.actions = new MoveTasksModalHelper(this.modalId, modalService);
  }

  ngOnInit(): void {
    this.actions.init(this.datasets);
  }

  ngOnDestroy(): void {
  }
}
