import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {InfoTO} from "../model/InfoTO";

@Injectable({
  providedIn: 'root'
})
export class SystemService {

  private baseURL: string = '/api/system';

  constructor(private http: HttpClient) { }

  public systemInfo(): Observable<InfoTO> {
    const url = `${this.baseURL}/info`;

    return this.http.get<InfoTO>(url);
  }
}
