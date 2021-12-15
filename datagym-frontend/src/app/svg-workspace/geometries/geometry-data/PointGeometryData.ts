import { WorkspacePoint } from '../../model/WorkspacePoint';


export class PointGeometryData{
  point: WorkspacePoint;
  comment?: string;


  constructor(point: WorkspacePoint, comment?: string) {
    this.point = point;
    this.comment = comment;
  }
}
