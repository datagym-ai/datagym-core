import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ProjectService } from '../../service/project.service';
import { Project } from '../../model/Project';
import { Subscription } from 'rxjs';
import { UserService } from '../../../client/service/user.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit, OnDestroy {
  public projects: Project[] = [];
  private serviceSub: Subscription;
  public isSuperadminMode: boolean = false;
  @Input()
  public filterText: string;

  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService,
    public userService: UserService) {
  }

  ngOnInit() {
    /**
     * In route of account admin fetch *all* projects
     */
    this.isSuperadminMode = !!this.route.snapshot.data['SUPER_ADMIN'];
    const callback = this.isSuperadminMode
      ? this.projectService.fetchAllProjectsAsAccountAdmin()
      : this.projectService.fetchProjects(true);

    this.serviceSub = callback.subscribe((projects: Project[]) => {
      this.projects = projects;
    });
  }

  ngOnDestroy(): void {
    this.serviceSub.unsubscribe();
  }
}
