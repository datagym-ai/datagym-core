export class Tab {
  title: string;
  link: string;
  roles: string[];

  constructor(title: string, link: string, roles: string[]) {
    this.title = title;
    this.link = link;
    this.roles = roles;
  }
}
