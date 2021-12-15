import { Component, OnDestroy, OnInit } from '@angular/core';
import { UserService } from '../../client/service/user.service';
import { FormControl, FormGroup } from '@angular/forms';
import { OidcUserInfo } from '../../client/model/OidcUserInfo';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { LabNotificationService } from '../../client/service/lab-notification.service';
import { AccountService } from './service/account.service';

@Component({
  selector: 'app-account-settings',
  templateUrl: './account-settings.component.html',
  styleUrls: ['./account-settings.component.css']
})
export class AccountSettingsComponent implements OnInit, OnDestroy {

  public userForm: FormGroup;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public userService: UserService,
    private accountService: AccountService,
    private labNotificationService: LabNotificationService
  ) {}

  ngOnInit() {
    this.buildUserForm();
  }

  private buildUserForm(): void{
    this.userForm = new FormGroup({
      name: new FormControl(this.userService.userDetails.name),
      email: new FormControl(this.userService.userDetails.email)
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public redirectToAccountSettings() {
    const url = `/api/accountSettings`;
    window.open(url, "_blank");
  }

  public redirectToAccountSettingsOrganisation(organisationId: string) {
    const url = `/api/accountSettings/` + organisationId;
    window.open(url, "_blank");
  }

  public createDummy(): void {
    const personalOrg = this.userService.userDetails.orgs.find(o => o.personal === true && o.role === 'ADMIN');
    if (personalOrg === undefined || !personalOrg.sub) {
      const translateKey = 'FEATURE.ACCOUNT_SETTINGS.DUMMY_FAILED';
      this.labNotificationService.error_i18(translateKey);
      return;
    }
    this.accountService.createDummyProject(personalOrg.sub).subscribe(() => {
      const translateKey = 'FEATURE.ACCOUNT_SETTINGS.DUMMY_CREATED';
      this.labNotificationService.success_i18(translateKey);
    });
  }
}
