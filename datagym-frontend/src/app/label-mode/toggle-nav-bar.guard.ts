import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, CanDeactivate } from '@angular/router';
import { Observable } from 'rxjs';
import {NavBarService} from '../basic/navbar/service/nav-bar.service';
import {LabelModeComponent} from './component/label-mode/label-mode.component';

type stateType = Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree;

@Injectable({
  providedIn: 'root'
})
export class ToggleNavBarGuard implements CanActivate, CanDeactivate<LabelModeComponent> {

  constructor(private navBarService: NavBarService) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): stateType {
    this.navBarService.hide();
    return true;
  }

  canDeactivate(
    component: LabelModeComponent,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot): stateType {
    this.navBarService.show();
    return true;
  }
}
