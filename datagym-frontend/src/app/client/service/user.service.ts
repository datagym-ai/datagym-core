import { Injectable } from '@angular/core';
import { OidcUserInfo } from '../model/OidcUserInfo';
import { AuthService } from './auth.service';
import { OidcOrgInfo } from '../model/OidcOrgInfo';
import { timeout } from 'rxjs/operators';

/**
 * Service to:
 * - hold the current authenticated user data
 * - handle login/logout mechanism
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  public userDetails: OidcUserInfo;

  constructor(private authService: AuthService) {
  }

  get name(): string {
    return !!this.userDetails && !!this.userDetails.name ? this.userDetails.name : '';
  }

  public setUser(userDetails: OidcUserInfo) {
    this.userDetails = userDetails;
  }

  /**
   * Check if the logged in user has the given scope or all of the given scopes.
   *
   * @param scope
   */
  public hasScope(scope: string | string[]): boolean {

    if (this.userDetails === undefined || this.userDetails.scopes === undefined) {
      return false;
    }

    const scope2test = typeof scope === 'string' ? [scope] : scope;
    return scope2test.filter(s => !this.userDetails.scopes.includes(s)).length === 0;
  }

  /**
   * Sets the user to undefined and navigates to the login-page
   */
  public logout() {
    if (this.userDetails?.isOpenCoreEnvironment) {
      return;
    }
    this.setUser(undefined);
    this.navigateToLogin();
  }

  /**
   * Load the user info data for the first time, if the user is not authenticated,
   * the interceptor triggers the logout-functionality
   * timeout after 30s, this only happens when login-server is down/not responding -> throws error which is caught
   * in error-interceptor which will notify user
   */
  public async initialize() {
    const maxWaitingTime: number = 30000;
    const userInfo = await this.authService.getUserInfo().pipe(timeout(maxWaitingTime)).toPromise();
    this.setUser(userInfo);
  }


  public navigateToLogin(): void {
    this.authService.login();
  }

  /**
   * Return the first personal organisation info.
   */
  public getPersonalOrg(): OidcOrgInfo | undefined {
    return this.getAdminOrgs().find(org => !!org.personal);
  }

  /**
   * Returns all organisations where the current user has admin-permissions
   */
  public getAdminOrgs(): OidcOrgInfo[] {
    if (this.userDetails !== undefined && this.userDetails.orgs !== undefined) {
      return this.userDetails.orgs.filter(org => org.role === 'ADMIN');
    }
    return [];
  }

  /**
   * Returns all organisations where the current user has admin-permissions
   */
  public getPrivateOrgs(): OidcOrgInfo[] {
    if (this.userDetails !== undefined && this.userDetails.orgs !== undefined) {
      return this.userDetails.orgs.filter(org => org.role === 'ADMIN').filter(org => org.personal===true);
    }
    return [];
  }

  /**
   * Returns all organisations where the current user has admin-permissions
   */
  public getAdminOrgsWithoutPrivate(): OidcOrgInfo[] {
    if (this.userDetails !== undefined && this.userDetails.orgs !== undefined) {
      return this.userDetails.orgs.filter(org => org.role === 'ADMIN').filter(org => org.personal===false);
    }
    return [];
  }

  /**
   * Check if the current user has admin-permissions defined by owner property.
   */
  public isAdminFor(ownerId: string): boolean {
    const adminOrgs = this.getAdminOrgs();
    return !!adminOrgs.find(orgs => orgs.sub === ownerId);
  }

  /**
   * Returns a organisation by its sub
   * @param sub
   */
  public getOrgBySub(sub: string): OidcOrgInfo | undefined {
    return this.userDetails.orgs.find(org => org.sub === sub);
  }

  /**
   * Check if the current logged in user has the required role.
   *
   * Note: the ADMIN role is not automatic a USER.
   * If a tab should only be visible for ADMINs, pass 'ADMIN' as roles.
   * But if the tab should be visible for ADMINs and USERs, pass 'ADMIN' and 'USER' as roles.
   * Otherwise, the restriction 'USER' would exclude ADMINs.
   *
   * @param sub select the organisation by it's sub.
   * @param allowedRoles Array of allowed roles.
   * @param options override default configuration.
   */
  public hasPermissionFor(sub: string | undefined, allowedRoles: string[], options ?: {}): boolean {

    // the default configuration
    const def = {
      /*
       * If allowedRoles is empty,
       * this value is returned.
       */
      onNoRolesRequired: true,
      /*
       * If the user is not in the organisation,
       * this value is returned.
       */
      onNotInOrganisation: false,
      /*
       * If no subscription is given, no permission
       * check can be made. So this value is returned.
       */
      onNoSubscription: true
    };

    // setup the configuration.
    const config = { ...def, ...options };

    // check if a valid
    if (!sub) {
      return !!config.onNoSubscription;
    }

    // no special permissions required.
    if (allowedRoles.length === 0) {
      return !!config.onNoRolesRequired;
    }

    // search the owner organisation via sub.
    const org = this.getOrgBySub(sub);
    if (org === undefined) {
      return !!config.onNotInOrganisation;
    }

    // Check if the user's role in the organisation is listed in the allowed Rules
    const role = org.role;
    return allowedRoles.indexOf(role) !== -1;
  }

}
