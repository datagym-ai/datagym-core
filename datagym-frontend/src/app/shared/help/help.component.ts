import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit {

  private baseUrl: string = 'https://docs.datagym.ai/documentation/';

  @Input('link')
  public fragmentArgument: string = '';

  @Input('help')
  public description: string = 'GLOBAL.READ_THE_DOCS';

  get fragment(): string {
    if (!this.fragmentArgument) {
      return '';
    }
    const forbiddenStart = ['#', '/'];
    let fragment = this.fragmentArgument;
    while (fragment.length > 0 && forbiddenStart.includes(fragment.charAt(0))) {
      fragment = fragment.substring(1);
    }
    return fragment;
  }

  get url(): string {
    return this.baseUrl + this.fragment;
  }

  get help(): string {
    return !!this.description ? this.description : this.url;
  }

  constructor() { }

  ngOnInit() {
  }

}
