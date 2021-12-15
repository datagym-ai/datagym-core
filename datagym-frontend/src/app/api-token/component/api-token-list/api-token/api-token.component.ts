import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ApiToken} from '../../../model/ApiToken';
import {LabNotificationService} from '../../../../client/service/lab-notification.service';


@Component({
  selector: 'app-api-token',
  templateUrl: './api-token.component.html',
  styleUrls: ['./api-token.component.css']
})
export class ApiTokenComponent implements OnInit {

  @Input()
  public token: ApiToken;

  // only used to display the token as 'stars' or 'clear text'
  public state: string = 'stars';

  get key(): string {
    return this.state !== 'clear'
      ? '*'.repeat(this.token.id.length)
      : this.token.id;
  }

  @Output()
  public onDelete: EventEmitter<void> = new EventEmitter<void>();

  constructor(private labNotificationService: LabNotificationService) { }

  ngOnInit() {}

  public copy2clipboard(): void {

    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.token.id;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);

    const translateKey = 'FEATURE.API_TOKEN.LIST.COPY_SUCCESS';
    this.labNotificationService.success_i18(translateKey);
  }

}
