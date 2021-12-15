import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { LabelConfiguration } from '../model/LabelConfiguration';
import { LabelConfigApiService } from './label-config-api.service';
import { Project } from '../../project/model/Project';

@Injectable({
  providedIn: 'root'
})
export class LabelConfigResolverService implements Resolve<LabelConfiguration> {

  constructor(private labelConfigAPIService: LabelConfigApiService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<LabelConfiguration> | Promise<LabelConfiguration> {
    const project: Project = route.parent.parent.data.project;
    const id = project.labelConfigurationId;
    return this.labelConfigAPIService.getLabelConfigById(id);
  }
}
