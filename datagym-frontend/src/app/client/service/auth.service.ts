import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OidcUserInfo } from '../model/OidcUserInfo';

/**
 * Service for backend calls regarding the authentication rest-api
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {

  }

  public getUserInfo(): Observable<OidcUserInfo> {
    return this.http.get<OidcUserInfo>(`/api/userinfo/`);
  }

  public login(): void {
    location.assign(`/auth/login/`);
  }

  public logout(): void {
    location.assign(`/auth/login/`);
  }
}
