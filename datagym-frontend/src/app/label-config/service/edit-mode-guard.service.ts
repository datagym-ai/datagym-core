import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanDeactivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { EditEntryComponent } from '../component/edit-entry/edit-entry.component';
import { Observable, Subscription } from 'rxjs';
import { AppButtonInput } from '../../shared/button/button.component';
import { DialogueService } from '../../shared/service/dialogue.service';
import { LabelConfigService } from './label-config.service';
import { DialogueModel } from '../../shared/dialogue-modal/DialogueModel';

// Just for linting
type guardResponse = Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree;


@Injectable({
  providedIn: 'root'
})
export class EditModeGuardService implements CanActivate, CanDeactivate<EditEntryComponent> {
  private isEditMode: boolean = false;
  private alertSub: Subscription;

  constructor(private dialogueService: DialogueService,
              private lcService: LabelConfigService) {
  }

  get editMode(): boolean {
    return this.isEditMode;
  }

  set editMode(newState: boolean) {
    this.isEditMode = newState;
    if (this.isEditMode === true) {
      this.lcService.dirty = true;
    }
  }

  public toggle() {
    this.editMode = !this.editMode;
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): guardResponse {
    if (this.editMode) {
      return false;
    }
    this.editMode = true;
    return true;
  }

  canDeactivate(component: EditEntryComponent, currentRoute: ActivatedRouteSnapshot,
                currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot): guardResponse{
    if (!this.editMode && currentRoute.routeConfig.path !== 'configure') {
      return true;
    }
    if (!this.editMode && !this.lcService.dirty) {
      return true;
    }
    const title = 'GLOBAL.DIALOGUE.TITLE.CONFIRM_UNSAVED_FORM';
    const content = 'GLOBAL.DIALOGUE.BODY.CONFIRM_UNSAVED_FORM';
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = { label: 'GLOBAL.CONFIRM', styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    this.alertSub = this.dialogueService.closeAction.subscribe((choice: boolean) => {
      this.alertSub.unsubscribe();
      if (choice === true) {
        this.editMode = false;
      }
    });
    return this.dialogueService.closeAction;
  }
}
