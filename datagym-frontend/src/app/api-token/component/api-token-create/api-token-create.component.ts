import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiToken } from '../../model/ApiToken';
import { ApiTokenCreateBindingModel } from '../../model/ApiTokenCreateBindingModel';
import { ApiTokenService } from '../../service/api-token.service';
import { TextfieldComponent } from '../../../shared/textfield/textfield.component';
import { UserService } from '../../../client/service/user.service';


@Component({
  selector: 'app-api-token-create',
  templateUrl: './api-token-create.component.html',
  styleUrls: ['./api-token-create.component.css']
})
export class ApiTokenCreateComponent implements OnInit {
  public createAPITokenForm: FormGroup;
  public showPrivateCreate = false;
  public showOrgCreate = true;

  @ViewChild('apiTokenNameField')
  public apiTokenNameField: TextfieldComponent;

  @ViewChild('radioPriv')
  public isPrivChecked;

  @ViewChild('radioOrg')
  public isOrgChecked;

  constructor(private router: Router,
              private tokenService: ApiTokenService,
              public userService: UserService) {
  }

  private tokenPattern: string = '^[0-9a-z_\.\-]{1,30}$';

  public createForm(apiToken?: ApiToken) {

    const owner = !!apiToken && !!apiToken.owner ? apiToken.owner : null;
    const name = !!apiToken && !!apiToken.name ? apiToken.name : null;

    return new FormGroup({
      'apiTokenOwner': new FormControl({value: owner}, [Validators.required]),
      'apiTokenName': new FormControl(name, [
        Validators.required,
        Validators.pattern(this.tokenPattern)
      ])
    });
  }

  ngOnInit() {
    this.createAPITokenForm = this.createForm();
  }

  ngAfterViewInit(): void {
    this.apiTokenNameField.inputElementRef.nativeElement.focus();
  }


  public onSubmit() {
    const owner = this.createAPITokenForm.value.apiTokenOwner;
    const name = this.createAPITokenForm.value.apiTokenName;
    const model = new ApiTokenCreateBindingModel(owner, name);
    this.tokenService.createApiToken(model).subscribe(() => {
      this.createAPITokenForm.reset();
      this.router.navigate(['api-tokens']).then();
    });
  }

  public onCancel() {
    this.createAPITokenForm.reset();
    this.router.navigate(['api-tokens']).then();
  }
}
