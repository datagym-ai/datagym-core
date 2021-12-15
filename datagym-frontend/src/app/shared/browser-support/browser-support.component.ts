import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BrowserSupportService} from "./browser-support.service";


@Component({
  selector: 'app-browser-support',
  templateUrl: './browser-support.component.html',
  styleUrls: ['./browser-support.component.css']
})
export class BrowserSupportComponent implements OnInit {

  @ViewChild('alert', {static: true})
  alert: ElementRef;

  public browserName: string;
  public isSupported: boolean = true;

  public get displayAlert(): boolean {
    // show the alert warning only once during the livetime of the browser-support service.
    return !this.isSupported && !this.browserSupport.wasClosed;
  }

  constructor(private browserSupport: BrowserSupportService) {}

  ngOnInit() {
    this.browserName = this.browserSupport.getBrowserName();
    this.isSupported = this.browserSupport.checkIfSupported(this.browserName);
  }

  close(): void {
    this.browserSupport.wasClosed = true;
    this.alert.nativeElement.remove();
  }
}
