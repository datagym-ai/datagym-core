import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, CanDeactivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {LabelModeComponent} from '../../label-mode/component/label-mode/label-mode.component';
import {NavBarService} from '../navbar/service/nav-bar.service';
import {Observable} from 'rxjs';

/**
 * Note:
 * This is a copy of '../label-mode/toggle-nav-bar.guard'
 * It may be removed or both should be combined and moved
 * into shared component. It stays for development purposes
 * here.
 */
@Injectable({
  providedIn: 'root'
})
export class ToggleNavBarGuard implements CanActivate, CanDeactivate<LabelModeComponent> {

  constructor(private navBarService: NavBarService) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    this.navBarService.hide();
    return true;
  }

  canDeactivate(
    component: LabelModeComponent,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    this.navBarService.show();
    return true;
  }
}
