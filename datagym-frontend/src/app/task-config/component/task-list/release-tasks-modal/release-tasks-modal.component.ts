import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgxSmartModalService} from 'ngx-smart-modal';
import {DatasetList} from '../../../../dataset/model/DatasetList';
import {MoveTasksModalHelper} from '../../../service/MoveTasksModalHelper';

@Component({
  selector: 'app-release-tasks-modal',
  templateUrl: './release-tasks-modal.component.html',
  styleUrls: ['./release-tasks-modal.component.css']
})
export class ReleaseTasksModalComponent implements OnInit {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public readonly modalId: string = 'ReleaseTasksModal';

  @Input()
  public disabled: boolean = true;

  @Input()
  public readonly datasets: DatasetList[];

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
