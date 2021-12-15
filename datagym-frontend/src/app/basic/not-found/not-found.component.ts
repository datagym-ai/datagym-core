import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css'],
  // Use 'ViewEncapsulation.None' to style the app-button element
  encapsulation: ViewEncapsulation.None
})
export class NotFoundComponent implements OnInit {

  constructor(private location: Location) { }

  ngOnInit() {}

  public onGoBack(): void {
    this.location.back();
  }
}
