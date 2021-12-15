import { Injectable } from '@angular/core';
import {ApiToken} from '../model/ApiToken';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {ApiTokenService} from './api-token.service';


@Injectable({
  providedIn: 'root'
})
export class ApiTokenListResolverService implements Resolve<ApiToken[]>{

  constructor(private tokenService: ApiTokenService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ApiToken[]> | Promise<ApiToken[]> | ApiToken[] {
    return this.tokenService.listApiTokens();
  }
}
