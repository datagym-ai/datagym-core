import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DatasetList} from '../../../../../dataset/model/DatasetList';

@Component({
  selector: 'app-list-item',
  templateUrl: './list-item.component.html',
  styleUrls: ['./list-item.component.css']
})
export class ListItemComponent implements OnInit {

  @Input()
  public dataset: DatasetList;
  @Input()
  public isDummy: boolean = false;

  @Output()
  public onDeleteEvent: EventEmitter<string> = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {}
}
