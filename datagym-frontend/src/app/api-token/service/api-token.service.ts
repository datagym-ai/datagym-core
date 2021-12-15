import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {ApiTokenCreateBindingModel} from '../model/ApiTokenCreateBindingModel';
import {ApiToken} from '../model/ApiToken';


@Injectable({
  providedIn: 'root'
})
export class ApiTokenService {

  private baseURL: string = '/api/token';

  constructor(private http: HttpClient) {}

  public createApiToken(newApiToken: ApiTokenCreateBindingModel): Observable<ApiToken> {
    return this.http.post<ApiToken>(`${ this.baseURL }`, newApiToken);
  }

  public listApiTokens(): Observable<ApiToken[]> {
    return this.http.get<ApiToken[]>(`${ this.baseURL }`);
  }

  public deleteTokenById(id: string): Observable<ApiToken> {
    return this.http.delete<ApiToken>(`${ this.baseURL }/${ id }`);
  }

  public deleteToken(token: ApiToken): Observable<ApiToken> {
    return this.deleteTokenById(token.id);
  }
}
