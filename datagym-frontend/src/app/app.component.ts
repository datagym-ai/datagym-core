import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { RequestPendingObserverService } from './client/service/request-pending-observer.service';
import { delay, filter, map, take, takeUntil } from 'rxjs/operators';
import { NavBarService } from './basic/navbar/service/nav-bar.service';
import { Subject } from 'rxjs';
import { UserService } from './client/service/user.service';
import {NavigationStart, Router, RouterEvent} from '@angular/router';
import {PreviewModeUri} from './label-mode/model/PreviewModeUri';

type notificationOptions = {[key: string]: boolean|number|string[]};

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  public title = 'datagym-frontend';
  public navIsFolded = false;
  public isLoading: boolean = false;
  public isWorkBench: boolean = false;
  public inLabelMode: boolean = false;

  public notificationOptions: notificationOptions = {
    position: ['top', 'center'],
    timeOut: 6000,
    clickToClose: true,
    showProgressBar: true,
    pauseOnHover: true
  };
  private unsubscribe: Subject<void>;

  constructor(
    translate: TranslateService,
    private requestPendingObserverService: RequestPendingObserverService,
    private navBarService: NavBarService,
    private userService: UserService,
    private router: Router
  ) {
    translate.setDefaultLang('en');
    translate.use('en');
    this.unsubscribe = new Subject<void>();
    this.navBarService.isFolded.pipe(takeUntil(this.unsubscribe)).subscribe((isFolded: boolean) => {
      this.navIsFolded = isFolded;
    });
    this.navBarService.isHidden.pipe(takeUntil(this.unsubscribe)).subscribe((isHidden: boolean) => {
      this.isWorkBench = isHidden;
    });

    // Load user info on first initialisation
    this.router.events.pipe(
      filter((event) => event instanceof NavigationStart),
      map((event: NavigationStart) => event.url),
      take(1)
    ).subscribe((url: string) => {

      if (PreviewModeUri.equals(url)) {
        // disable user service within the preview mode.
        return;
      }
    });
  }

  ngOnInit(): void {
    this.requestPendingObserverService.requestsPending$.pipe(
      delay(0),
      takeUntil(this.unsubscribe)
    ).subscribe(loading => {
      this.isLoading = loading;
    });

    // Subscribe on NavigationStart events to check if in label-mode
    this.router.events.pipe(
      takeUntil(this.unsubscribe),
      filter((event: RouterEvent) => event instanceof NavigationStart)
    ).subscribe((event: NavigationStart) => {
        const url: string = event.url;
        if (!this.inLabelMode) {
          this.inLabelMode = AppComponent.checkInLabelMode(url);
          return;
        }
        if (!AppComponent.checkInLabelMode(url)) {
          // only add css transitions after 1s to avoid content transitioning over navBar
          const transitionTime: number = 1000;
          setTimeout(() => this.inLabelMode = false, transitionTime);
        }
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Check if the url points to the label-mode
   *
   * @param url
   */
  private static checkInLabelMode(url: string): boolean {
    // extract just the first two parts of the url
    const end = 3;
    return url.split('/').slice(1, end).includes('label-mode'); // in label mode as usual
  }
}
