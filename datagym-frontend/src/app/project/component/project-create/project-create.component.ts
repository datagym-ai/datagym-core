import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ProjectService } from '../../service/project.service';
import { Router } from '@angular/router';
import { ProjectCreateBindingModel } from '../../model/ProjectCreateBindingModel';
import { ProjectFormBuilder } from '../../model/ProjectFormBuilder';
import { Project } from '../../model/Project';
import { UserService } from '../../../client/service/user.service';
import { TextfieldComponent } from '../../../shared/textfield/textfield.component';
import { MediaType } from '../../model/MediaType.enum';


@Component({
  selector: 'app-project-create',
  templateUrl: './project-create.component.html',
  styleUrls: ['./project-create.component.css']
})
export class ProjectCreateComponent implements OnInit{
  public createProjectForm: FormGroup;

  @ViewChild('projectNameField')
  public projectNameField: TextfieldComponent;

  public MediaType = MediaType;

  constructor(private projectService: ProjectService,
              private router: Router,
              public userService: UserService) {
  }

  ngOnInit() {
    this.createProjectForm = ProjectFormBuilder.create();
  }

  ngAfterViewInit(): void {
    this.projectNameField.inputElementRef.nativeElement.focus();
  }

  public onSubmit(): void {
    const name: string = this.createProjectForm.value.projectName;
    const shortDescription: string = this.createProjectForm.value.projectShortDescription;
    const longDescription: string = this.createProjectForm.value.projectDescription;
    const mediaType: MediaType = this.createProjectForm.value.mediaType;
    const owner: string = this.createProjectForm.value.projectOwner;

    const newProject: ProjectCreateBindingModel = new ProjectCreateBindingModel(owner, name, shortDescription, longDescription, mediaType);
    this.projectService.createProject(newProject).subscribe((project: Project) => {
      this.createProjectForm.reset();
      this.router.navigate(['projects', 'details', project.id]).then();
    });
  }

  public onCancel() {
    this.createProjectForm.reset();
    this.router.navigate(['/projects']).then();
  }
}
