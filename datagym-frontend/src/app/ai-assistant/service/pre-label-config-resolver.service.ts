import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import {Observable, of} from 'rxjs';
import { Project } from '../../project/model/Project';
import {PreLabelApiService} from './pre-label-api.service';
import {PreLabelInfoViewModel} from '../model/PreLabelInfoViewModel';
import {catchError, map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PreLabelConfigResolverService implements Resolve<PreLabelInfoViewModel> {

  constructor(private preLabelApiService: PreLabelApiService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<PreLabelInfoViewModel> | Promise<PreLabelInfoViewModel> {
    const project: Project = route.parent.parent.data.project;
    const id = project.id;
    return this.preLabelApiService.getPreLabelInfoByProject(id).pipe(
      map(res => res),
      catchError(function(p1: any,p2: Observable<PreLabelInfoViewModel>){
        return of(null);})
    );
  }
}
