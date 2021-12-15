import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavItem } from './model/NavItem';
import { NavGroup } from './model/NavGroup';
import { NavBarService } from './service/nav-bar.service';
import { UserService } from '../../client/service/user.service';
import {OidcUserInfo} from '../../client/model/OidcUserInfo';
import {filter, map, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent implements OnInit, OnDestroy {
  public readonly navigation: NavGroup[] = [];
  public readonly navIcons = {
    'fold': 'fas fa-arrow-left',
    'unfold': 'fas fa-arrow-right', // 'fa-bars',
    'documentation': 'fas fa-book',
    'projects': 'fas fa-plus',
    'datasets': 'fas fa-list-ul',
    'task': 'fas fa-tasks',
    'key': 'fas fa-key',
    'logout': 'fas fa-power-off',
    'user': 'fas fa-user',
  };

  public get navIsFolded(): boolean {
    return this.navBarService.folded;
  }

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private navBarService: NavBarService, private userService: UserService) {
  }

  ngOnInit(): void {

    // Home group with tasks entry
    this.navigation.push(
      new NavGroup('NAVBAR.NAV_GROUP.HOME', [
        new NavItem(this.navIcons.task, 'NAVBAR.TASKS', '/tasks', []),
        new NavItem(this.navIcons.documentation, 'NAVBAR.DOCUMENTATION', '/goto/documentation', []),
      ], [])
    );

    // Administration group with tasks entry
    this.navigation.push(
      new NavGroup('NAVBAR.NAV_GROUP.ADMINISTRATION', [
        new NavItem(this.navIcons.projects, 'NAVBAR.PROJECTS', '/projects', []),
        new NavItem(this.navIcons.datasets, 'NAVBAR.DATASETS', '/datasets', []),
        new NavItem(this.navIcons.key, 'NAVBAR.API_TOKENS', '/api-tokens', []),
      ], [])
    );

    // User Tab & Logout
    const navGroupUser = new NavGroup('NAVBAR.NAV_GROUP.USER', [
      new NavItem(this.navIcons.user, 'NAVBAR.USER', '/account-settings', []),
    ], []);
    if (!this.userService.userDetails.isOpenCoreEnvironment) {
      navGroupUser.navItems.push(new NavItem(this.navIcons.logout, 'NAVBAR.LOGOUT', '/logout', []));
    }

    this.navigation.push(navGroupUser);

    // Super admin pages
    if(this.userService.userDetails.scopes.includes('account.admin')){
      this.navigation.push(
        new NavGroup('NAVBAR.NAV_GROUP.SUPER_ADMIN', [
          new NavItem(this.navIcons.projects, 'NAVBAR.PROJECTS', '/admin/projects', []),
          new NavItem(this.navIcons.datasets, 'NAVBAR.DATASETS', '/admin/datasets', []),
        ], [])
      );
    }

    // Set user name
    this.updateUserName(this.userService.userDetails.name);
  }

  /**
   * When the username is available, lookup for the USER group ...
   * .. and replace the entry with the title 'USER' with the actual user name.
   * @param name
   * @private
   */
  private updateUserName(name: string) {
    const indexOfUserSection = this.navigation.findIndex(
      group => group.i18nHeadline === 'NAVBAR.NAV_GROUP.USER'
    );
    const oldEntry = this.navigation[indexOfUserSection];

    const items = oldEntry.navItems.map(item => item.name === 'NAVBAR.USER'
      ? new NavItem(item.classIcon, name, item.link, item.roles)
      : item
    );

    this.navigation[indexOfUserSection] = new NavGroup(oldEntry.i18nHeadline, items, oldEntry.roles);
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onToggleFolded(): void {
    this.navBarService.toggleFoldedMode();
  }

  public getNavbarCollapseString(): string{
    if (this.navIsFolded) {
      return 'NAVBAR.EXPAND';
    } else{
      return 'NAVBAR.COLLAPSE';
    }
  }

}
