import {WorkspacePoint} from './WorkspacePoint';


export class PointsCollection {

  // public name = 'PointsCollection';
  public id: string = '';
  public points: WorkspacePoint[] = [];

  constructor(points: WorkspacePoint[], id?: string) {
    this.points = points;
    this.id = id;
  }
}
