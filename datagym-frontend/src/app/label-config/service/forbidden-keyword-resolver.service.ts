
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { LabelConfigApiService } from './label-config-api.service';


@Injectable({
  providedIn: 'root'
})
export class ForbiddenKeywordResolverService implements Resolve<string[]> {

  constructor(private labelConfigAPIService: LabelConfigApiService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<string[]> | Promise<string[]> {
    return this.labelConfigAPIService.getForbiddenKeywords();
  }
}
