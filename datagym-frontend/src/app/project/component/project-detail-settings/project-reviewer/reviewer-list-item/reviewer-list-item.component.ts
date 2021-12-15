import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ProjectReviewer} from '../../../../model/ProjectReviewer';

@Component({
  selector: 'app-reviewer-list-item',
  templateUrl: './reviewer-list-item.component.html',
  styleUrls: ['./reviewer-list-item.component.css']
})
export class ReviewerListItemComponent implements OnInit {

  @Input()
  public reviewer: ProjectReviewer;

  @Output()
  public onDeleteEvent: EventEmitter<string> = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {}
}
