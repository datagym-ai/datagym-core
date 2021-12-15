import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GotoGuard implements CanActivate {

  /**
   * This is the map of external urls.
   *
   * The first entry is defined as default if no target is defined.
   *
   * If no target is found, the guard returns true, that results in loading the NotFoundComponent.
   */
  public readonly targets = {
    'documentation': 'https://docs.datagym.ai/documentation/'
  };

  constructor() {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const supportedTargets = Object.keys(this.targets);

    const target = !!route.paramMap.get('target')
      ? route.paramMap.get('target')
      : supportedTargets[0];

    if (supportedTargets.includes(target)) {
      const url = this.targets[target];
      window.open(url, '_blank');
      // The url was found -> don't open the page in the app.
      return false;
    }

    // The url was not found -> load the 404 page.
    return true;
  }

}
