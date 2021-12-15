import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { ApiToken } from '../../model/ApiToken';
import {ApiTokenService} from '../../service/api-token.service';
import {Subscription} from 'rxjs';
import {DialogueService} from '../../../shared/service/dialogue.service';
import {AppButtonInput} from '../../../shared/button/button.component';
import {DialogueModel} from '../../../shared/dialogue-modal/DialogueModel';


@Component({
  selector: 'app-api-token-list',
  templateUrl: './api-token-list.component.html',
  styleUrls: ['./api-token-list.component.css']
})
export class ApiTokenListComponent implements OnInit, OnDestroy {

  @Input()
  public filterText: string;

  public tokens: ApiToken[] = [];

  private listTokensSub: Subscription;
  private deleteDialogueSub: Subscription;

  constructor(private tokenService: ApiTokenService, private dialogService: DialogueService) { }

  ngOnInit() {
    this.listTokensSub = this.tokenService.listApiTokens().subscribe((tokens: ApiToken[]) => {
      this.tokens = tokens;
    });
  }

  ngOnDestroy(): void {
    if (this.listTokensSub) {
      this.listTokensSub.unsubscribe();
    }
    if (this.deleteDialogueSub) {
      this.deleteDialogueSub.unsubscribe();
    }
  }

  public onDelete(token: ApiToken): void {
    // Open confirmation dialogue.

    const del = 'FEATURE.API_TOKEN.LIST.DELETE_DIALOGUE.LABEL';
    const title = 'FEATURE.API_TOKEN.LIST.DELETE_DIALOGUE.TITLE';
    const cancel = 'FEATURE.API_TOKEN.LIST.DELETE_DIALOGUE.CANCEL';
    const content = 'FEATURE.API_TOKEN.LIST.DELETE_DIALOGUE.CONTENT';
    const buttonLeft: AppButtonInput = { label: del, styling: 'warn' };
    const dialogueContent: DialogueModel = { title, content, buttonLeft, buttonRight: cancel };

    this.dialogService.openDialogue(dialogueContent);
    this.deleteDialogueSub = this.dialogService.closeAction.subscribe((choice: boolean) => {
      if (this.deleteDialogueSub) {
        this.deleteDialogueSub.unsubscribe();
      }
      if (choice === true) {
        this.tokenService.deleteToken(token).subscribe(() => {
          this.tokens = this.tokens.filter(t => t.id !== token.id);
        });
      }
    });
  }
}
