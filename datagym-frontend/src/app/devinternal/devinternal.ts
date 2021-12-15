import { Component, OnInit } from '@angular/core';
import { DialogueService } from '../shared/service/dialogue.service';
import { HttpClient } from '@angular/common/http';
import { DialogueModel } from '../shared/dialogue-modal/DialogueModel';

@Component({
  selector: 'app-devinternal',
  templateUrl: './devinternal.component.html',
  styleUrls: ['./devinternal.component.css']
})
export class Devinternal implements OnInit{

  constructor(private dialogueService: DialogueService,
              private http: HttpClient) {
  }

  public ngOnInit(): void {
  }

  public onClick(): void {
    const dialogueContent: DialogueModel = {title: 'a dialogue', content: 'content goes here', buttonLeft: 'accept', buttonRight: 'cancel'};
    this.dialogueService.openDialogue(dialogueContent);
  }

  public getNextTask(): void {
    const projectId: string = 'e178f781-1252-47e2-adcd-dfabc5c5ec73';
    this.http.get('/api/user/nextTask/' + projectId).subscribe(response => {
      console.log('response: ', response);
    });
  }

  public createDummyProject(): void {
    const orgId: string = null;
    this.http.get('/api/dummy/' + orgId).subscribe();
  }

  public redirectToAccMngmt() {
    const url = `/api/accountSettings`;
    location.assign(url);
  }
}
