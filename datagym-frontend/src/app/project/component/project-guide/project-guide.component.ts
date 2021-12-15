import {Component, Input, OnInit} from '@angular/core';
import {ProjectDashboard} from "../../model/ProjectDashboard";
import {LabelTaskState} from "../../../task-config/model/LabelTaskState";
import {DataGymPlan} from "../../../basic/account-settings/model/DataGymPlan";

@Component({
  selector: 'app-project-guide',
  templateUrl: './project-guide.html',
  styleUrls: ['./project-guide.component.css']
})
export class ProjectGuideComponent implements OnInit {
  @Input()
  public projectDashboard: ProjectDashboard;
  public tasksReleased: boolean = false;
  private noCollaborationSupport: DataGymPlan[] = [DataGymPlan.FREE_DEVELOPER];
  public collaboratorPermission: boolean;

  constructor() {
  }

  ngOnInit() {
    this.tasksReleased = this.checkIfTasksMoved();
    this.collaboratorPermission = this.checkIfPlanAllowsCollaborators();
  }

  private checkIfTasksMoved(): boolean {
    for (let taskStatusKey in this.projectDashboard.taskStatus) {
      if (taskStatusKey == LabelTaskState.WAITING_CHANGED ||
          taskStatusKey == LabelTaskState.IN_PROGRESS ||
          taskStatusKey == LabelTaskState.WAITING) {
        return true;
      }
    }
    return false;
  }

  private checkIfPlanAllowsCollaborators(): boolean {
    return !this.noCollaborationSupport.includes(this.projectDashboard.currentPlan);
  }
}
