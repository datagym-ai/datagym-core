import { Component, Input } from '@angular/core';
import { UserService } from '../../client/service/user.service';
import { FormGroupDirective } from '@angular/forms';
import { OidcOrgInfo } from '../../client/model/OidcOrgInfo';

/**
 * This component can only be used within a wrapped form group
 * - it will take the formControlName as input parameter
 */
@Component({
  selector: 'app-dg-select-owner',
  templateUrl: './dg-select-owner.component.html',
  styleUrls: ['./dg-select-owner.component.css']
})
export class DgSelectOwnerComponent {

  public isPrivateSelected = false;
  public possibleOrgs: OidcOrgInfo[] = [];
  public noOrganisationFound = false;

  ngOnInit() {
    let controlValue = this.parentF.form.get(this.controlName).value;
    if (this.readOnly && (controlValue !== undefined || controlValue !== null)) {
      this.possibleOrgs = [this.userService.getOrgBySub(controlValue)];
    } else {
      this.possibleOrgs = this.userService.getAdminOrgsWithoutPrivate();
      if (this.possibleOrgs.length > 0) {
        this.parentF.form.get(this.controlName).setValue(this.possibleOrgs[0].sub);
      } else if (this.possibleOrgs.length == 0) {
        this.onSelectorChange();
      }
    }
  }

  public redirectToAccountSettings() {
    const url = `/api/accountSettings`;
    window.open(url, '_blank');
  }

  @Input('label')
  public labelText: string;

  @Input('hint')
  public hintText: string;

  @Input('readOnly')
  public readOnly: boolean = false;

  @Input('controlName')
  public controlName: string;

  constructor(public userService: UserService,
              public parentF: FormGroupDirective) {
  }

  public onSelectorChange() {
    this.isPrivateSelected = !this.isPrivateSelected;

    if (this.isPrivateSelected) {
      this.possibleOrgs = this.userService.getPrivateOrgs();
    } else {
      this.possibleOrgs = this.userService.getAdminOrgsWithoutPrivate();
    }

    // Check if there are any orgs available
    this.noOrganisationFound = this.possibleOrgs.length == 0;

    // Set the select box "selected" value
    if (this.possibleOrgs.length > 0) {
      this.parentF.form.get(this.controlName).setValue(this.possibleOrgs[0].sub);
    } else {
      this.parentF.form.get(this.controlName).setValue(undefined);
    }
  }
}
