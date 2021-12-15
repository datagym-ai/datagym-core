import { NavItem } from './NavItem';

export class NavGroup {
  constructor(
    public i18nHeadline: string,
    public navItems: NavItem[],
    public roles: string[]
  ) {
  }
}
