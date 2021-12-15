import {WorkspacePoint} from './WorkspacePoint';


export const DEFAULT_NUM_POINTS = 40;


export class AiSegCalculate {
  imageId: string;
  frameNumber: number;
  numPoints: number;
  environment: string;
  // None or at least three points are required.
  // The negative points must surround the positive points.
  negativePoints: WorkspacePoint[];
  // At least one positive point is required
  positivePoints: WorkspacePoint[];
  // User session uuid
  userSessionUUID: string;
}
