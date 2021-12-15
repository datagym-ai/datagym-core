import { Component, Input } from '@angular/core';
import { NavItem } from '../model/NavItem';

@Component({
  selector: 'app-navbar-item',
  templateUrl: './navbar-item.component.html',
  styleUrls: ['./navbar-item.component.css']
})
export class NavbarItemComponent {
  @Input()
  public navItem: NavItem;

  @Input()
  public isFolded: boolean;
}
