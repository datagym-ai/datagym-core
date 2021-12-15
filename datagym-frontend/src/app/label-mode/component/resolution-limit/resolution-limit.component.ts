import {Component, HostListener, OnInit} from '@angular/core';


@Component({
  selector: 'app-resolution-limit',
  templateUrl: './resolution-limit.component.html',
  styleUrls: ['./resolution-limit.component.css']
})
export class ResolutionLimitComponent implements OnInit {

  public limitWidth: number = 1200;
  public limitHeight: number = 580;
  public width: number;
  public height: number;
  public withinLimits: boolean = true;

  constructor() {
  }

  ngOnInit() {
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.updateWithinLimits();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.width = event.target.innerWidth;
    this.height = event.target.innerHeight;
    this.updateWithinLimits();
  }

  private updateWithinLimits() {
    this.withinLimits = !(this.height < this.limitHeight || this.width < this.limitWidth);
  }
}
