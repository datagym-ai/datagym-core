import { WorkspacePoint } from '../../model/WorkspacePoint';


export class LineGeometryData{
  points: WorkspacePoint[];
  comment?: string;


  constructor(points: WorkspacePoint[], comment?: string) {
    this.points = points;
    this.comment = comment;
  }
}
