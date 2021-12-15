import { Pipe, PipeTransform } from '@angular/core';
import { Project } from '../model/Project';

@Pipe({
  name: 'projectNameFilter',
  pure: false
})
export class ProjectNameFilterPipe implements PipeTransform {

  transform(projectArray: Project[], name?: string): Project[] {
    if (!projectArray || !name) {
      return projectArray;
    }
    return projectArray.filter((project: Project) => project.name.toLocaleLowerCase().includes(name.toLocaleLowerCase()));
  }

}
