import { WorkspacePoint } from '../../model/WorkspacePoint';

export class RectangleGeometryData {
  startPoint: WorkspacePoint;
  height: number;
  width: number;
  comment? : string;


  constructor(startPoint: WorkspacePoint, height: number, width: number, comment?: string) {
    this.startPoint = startPoint;
    this.height = height;
    this.width = width;
    this.comment = comment;
  }
}
