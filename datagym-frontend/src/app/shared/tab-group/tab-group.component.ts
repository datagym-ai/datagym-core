import { AfterContentInit, Component, Input } from '@angular/core';
import { TabGroup } from './models/TabGroup';
import { UserService } from '../../client/service/user.service';
import { ActivatedRoute } from '@angular/router';
import { Tab } from './models/Tab';


@Component({
  selector: 'app-tab-group',
  templateUrl: './tab-group.component.html',
  styleUrls: ['./tab-group.component.css']
})
export class TabGroupComponent implements AfterContentInit{
  @Input()
  public tabGroup: TabGroup;

  @Input()
  private readonly vh: number;

  @Input()
  public sub: string;

  public get viewHeight(): string {
    return typeof this.vh === 'number'
      ? `${ this.vh }vh`
      : '';
  }

  constructor(private route: ActivatedRoute, private userService: UserService) {}

  ngAfterContentInit(): void {

    if (!!this.route.snapshot.data['SUPER_ADMIN'] && this.userService.hasScope('account.admin')) {
      return;
    }

    this.tabGroup.tabs = this.tabGroup.tabs.filter((tab: Tab) => {
      return this.userService.hasPermissionFor(this.sub, tab.roles);
    });
  }
}
