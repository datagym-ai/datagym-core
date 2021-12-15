import {Component, OnInit} from '@angular/core';
import {LabelModeUtilityService} from '../../service/label-mode-utility.service';
import {WorkspaceControlService} from '../../../svg-workspace/service/workspace-control.service';



@Component({
  selector: 'app-active-control',
  templateUrl: './active-control.component.html',
  styleUrls: ['./active-control.component.css']
})
export class ActiveControlComponent implements OnInit{


  constructor(public labelModeUtilityService: LabelModeUtilityService,
              private workspaceControlService: WorkspaceControlService) { }

  ngOnInit(): void {
  }

  cancelAction() {
    this.labelModeUtilityService.resetSelectionState();
    this.labelModeUtilityService.unselectAllGeometries();

    if (this.labelModeUtilityService.aiSegActive) {
      this.labelModeUtilityService.cancelAISeg();
    }
    if (this.labelModeUtilityService.userIsDrawing) {
      this.workspaceControlService.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
    }
  }

  actionSelected(): boolean {
    return this.labelModeUtilityService.userIsDrawing || this.labelModeUtilityService.aiSegActive
  }
}
