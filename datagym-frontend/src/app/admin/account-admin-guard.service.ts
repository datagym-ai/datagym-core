import {Injectable} from '@angular/core';
import {UserService} from '../client/service/user.service';
import {ActivatedRouteSnapshot, CanActivate, CanActivateChild, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';

/**
 * Just for linting.
 */
type CanActivateResponse = Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree;

/**
 * Use this guard to enable/disable routes
 * that are only for account admins available.
 *
 * Account admins are detected by scope 'account.admin'.
 */
@Injectable({
  providedIn: 'root'
})
export class AccountAdminGuard implements CanActivate, CanActivateChild {

  private readonly requiredScope: string = 'account.admin';

  constructor(private userService: UserService) {}

  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): CanActivateResponse {

    return this.userService.hasScope(this.requiredScope);
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): CanActivateResponse {

    return this.userService.hasScope(this.requiredScope);
  }
}
