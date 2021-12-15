import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-dg-expand-collapse',
  templateUrl: './dg-expand-collapse.component.html',
  styleUrls: ['./dg-expand-collapse.component.css']
})
export class DgExpandCollapseComponent implements OnInit {

  @Input()
  public show: boolean = false;
  @Input()
  public set open(open: boolean) {
    this.isOpen = open;
    this.openChange.emit(open);
  }
  public get open(): boolean {
    return this.isOpen;
  }
  @Output()
  public openChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  private isOpen: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

}
