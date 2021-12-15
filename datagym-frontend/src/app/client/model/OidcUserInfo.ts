import {OidcOrgInfo} from './OidcOrgInfo';

export class OidcUserInfo {
  sub: string;
  email: string;
  name: string;
  orgs: OidcOrgInfo[];
  scopes: string[];
  isOpenCoreEnvironment?: boolean = false;
}
