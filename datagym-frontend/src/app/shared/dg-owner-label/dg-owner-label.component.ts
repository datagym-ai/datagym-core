import { Component, Input, OnInit } from '@angular/core';
import { UserService } from '../../client/service/user.service';
import { OidcOrgInfo } from '../../client/model/OidcOrgInfo';

@Component({
  selector: 'app-dg-owner-label',
  templateUrl: './dg-owner-label.component.html',
  styleUrls: ['./dg-owner-label.component.css']
})
export class DgOwnerLabelComponent implements OnInit {

  @Input()
  public organisation: OidcOrgInfo | string;

  @Input()
  public hideIcon: boolean = false;

  public internalOwner: OidcOrgInfo | undefined;

  /**
   * If the organisation is from type string
   * and the no internalOwner was found, display
   * the organisation id.
   */
  public get displayOrganisationID(): boolean {
    return this.internalOwner === undefined && typeof this.organisation === 'string';
  }

  public get name(): string {

    if (this.internalOwner === undefined && typeof this.organisation === 'string') {
      return this.organisation;
    }

    if (!!this.internalOwner.personal) {
      return this.userService.name;
    }

    if (!/* not */!!this.internalOwner.personal) {
      return this.internalOwner.name;
    }

    return '';
  }

  constructor(public userService: UserService) {

  }

  ngOnInit() {
    if(typeof this.organisation === 'string'){
      this.internalOwner = this.userService.getOrgBySub(this.organisation as string);
    } else{
      this.internalOwner = this.organisation as OidcOrgInfo;
    }
  }

}
