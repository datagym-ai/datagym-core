import { Component, Input } from '@angular/core';
import { Tab } from '../models/Tab';

@Component({
  selector: 'app-tab',
  templateUrl: './tab.component.html',
  styleUrls: ['./tab.component.css']
})
export class TabComponent {
  @Input()
  public tab: Tab
}
