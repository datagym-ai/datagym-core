import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-action-icon',
  templateUrl: './dg-action-icon.component.html',
  styleUrls: ['./dg-action-icon.component.css']
})
export class DgActionIconComponent implements OnInit {

  @Input()
  public icon: string = undefined;

  @Input()
  public active: boolean = false;

  @Output()
  public readonly onClick: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  constructor() { }

  ngOnInit(): void {
    if (typeof this.icon === 'string' && !!this.icon && this.icon.startsWith('fa-')) {
      this.icon = `fas ${ this.icon }`;
    }
  }

}
