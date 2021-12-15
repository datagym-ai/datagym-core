import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {LimitPricingPlanViewModel} from "../model/LimitPricingPlanViewModel";

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private http: HttpClient) { }

  public getLimitsByOrganisation(org: string): Observable<LimitPricingPlanViewModel> {
    return this.http.get<LimitPricingPlanViewModel>('/api/limit/' + org);
  }

  public createDummyProject(personalOrg: string): Observable<void> {
    return this.http.get<void>('/api/dummy/' + personalOrg);
  }
}
