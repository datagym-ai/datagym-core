import { WorkspacePoint } from '../../model/WorkspacePoint';


export class PolygonGeometryData{
  points: WorkspacePoint[];
  comment?: string;

  constructor(points: WorkspacePoint[], comment?: string) {
    this.points = points;
    this.comment = comment;
  }
}
