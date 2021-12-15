import { Component, OnDestroy, OnInit } from '@angular/core';
import { Project } from '../../model/Project';
import { FormGroup } from '@angular/forms';
import { ProjectService } from '../../service/project.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AppButtonInput } from '../../../shared/button/button.component';
import { DialogueService } from '../../../shared/service/dialogue.service';
import { Subject, Subscription } from 'rxjs';
import { ProjectUpdateBindingModel } from '../../model/ProjectUpdateBindingModel';
import { DialogueModel } from '../../../shared/dialogue-modal/DialogueModel';
import { ProjectFormBuilder } from '../../model/ProjectFormBuilder';
import { takeUntil } from 'rxjs/operators';
import { MediaType } from '../../model/MediaType.enum';


@Component({
  selector: 'app-project-detail-settings',
  templateUrl: './project-detail-settings.component.html',
  styleUrls: ['./project-detail-settings.component.css']
})
export class ProjectDetailSettingsComponent implements OnInit, OnDestroy {
  public editProjectForm: FormGroup;
  public project: Project;
  private updateProjectSub: Subscription;
  private dialogueSub: Subscription;
  private deleteProjectSub: Subscription;
  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  public MediaType = MediaType;

  get isDummy(): boolean {
    return Project.isDummy(this.project);
  }

  constructor(private router: Router,
              private route: ActivatedRoute,
              private projectService: ProjectService,
              private dialogueService: DialogueService) {
  }

  ngOnInit() {
    this.project = this.route.parent.snapshot.data.project as Project;
    this.editProjectForm = ProjectFormBuilder.create(this.project);
  }

  ngOnDestroy(): void {
    if (this.updateProjectSub) {
      this.updateProjectSub.unsubscribe();
    }
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.deleteProjectSub) {
      this.deleteProjectSub.unsubscribe();
    }
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onDelete(): void {
    const title = 'FEATURE.PROJECT.DETAIL.SETTINGS.DANGER_ZONE.MODAL.TITLE';
    const content = 'FEATURE.PROJECT.DETAIL.SETTINGS.DANGER_ZONE.MODAL.CONTENT';
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = { label: 'GLOBAL.DELETE', styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.deleteProjectSub) {
      this.deleteProjectSub.unsubscribe();
    }
    this.dialogueSub = this.dialogueService.closeAction.pipe(takeUntil(this.unsubscribe)).subscribe((choice: boolean) => {
      if (choice === false) {
        return;
      }
      this.deleteProjectSub = this.projectService.deleteProject(this.project.id).pipe(takeUntil(this.unsubscribe)).subscribe(() => {
        this.router.navigate(['projects']).then();
      });
    });
  }

  public onSubmit(): void {

    if (this.isDummy) {
      return;
    }

    const id = this.project.id;
    const name = this.editProjectForm.value.projectName;
    const shortDescription = this.editProjectForm.value.projectShortDescription;
    const longDescription = this.editProjectForm.value.projectDescription;
    const updatedProject = new ProjectUpdateBindingModel(name, shortDescription, longDescription);
    if (this.updateProjectSub) {
      this.updateProjectSub.unsubscribe();
    }
    this.updateProjectSub = this.projectService.updateProject(id, updatedProject).pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.router.onSameUrlNavigation = 'reload';
      this.router.navigate([], { relativeTo: this.route }).then();
    });
  }

  public onReset(): void {

    if (this.isDummy) {
      return;
    }

    this.editProjectForm = ProjectFormBuilder.create(this.project);
  }
}
