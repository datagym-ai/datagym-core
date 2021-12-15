import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Project } from '../model/Project';
import { ProjectService } from './project.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectResolverService implements Resolve<Project> {

  constructor(private projectService: ProjectService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Project> | Promise<Project> {
    const id = route.paramMap.get('id');
    return this.projectService.getProjectById(id);
  }
}
