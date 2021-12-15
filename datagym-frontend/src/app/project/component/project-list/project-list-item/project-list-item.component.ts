import {Component, Input, OnInit} from '@angular/core';
import {Project} from '../../../model/Project';
import {MediaType} from '../../../model/MediaType.enum';

@Component({
  selector: 'app-project-list-item',
  templateUrl: './project-list-item.component.html',
  styleUrls: ['./project-list-item.component.css']
})
export class ProjectListItemComponent implements OnInit {

  @Input()
  public project: Project;

  @Input()
  public isSuperadminMode: boolean = false;

  public mediaCounter: number = 0;

  public MediaType = MediaType;

  constructor() {
  }

  ngOnInit() {
    this.mediaCounter = Project.countMedia(this.project);
  }
}
