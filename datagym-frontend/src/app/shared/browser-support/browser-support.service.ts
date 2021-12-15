import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BrowserSupportService {

  public wasClosed: boolean = false;
  public readonly unSupportedBrowsers: string[] = ['Firefox'];

  constructor() { }

  public checkIfSupported(browserName: string): boolean {
    return !this.unSupportedBrowsers.includes(browserName);
  }

  public isBrowser(browserName: string) {
    return browserName === this.getBrowserName();
  }

  public getBrowserName(): string {
    const agent = window.navigator.userAgent.toLowerCase();
    switch (true) {
      case agent.indexOf('edge') > -1:
        return 'Edge';
      case agent.indexOf('opr') > -1 && !!(<any>window).opr:
        return 'Opera';
      case agent.indexOf('chrome') > -1 && !!(<any>window).chrome:
        return 'Chrome';
      case agent.indexOf('trident') > -1:
        return 'Internet Explorer';
      case agent.indexOf('firefox') > -1:
        return 'Firefox';
      case agent.indexOf('safari') > -1:
        return 'Safari';
      default:
        return 'an unknown application';
    }
  }
}
