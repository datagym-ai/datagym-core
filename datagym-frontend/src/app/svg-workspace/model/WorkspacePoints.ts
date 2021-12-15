import {WorkspacePoint} from './WorkspacePoint';
import {BoundingBoxRectangle} from './utility/BoundingBoxRectangle';

export type PointStack = WorkspacePoints | WorkspacePoint[] | {x: number, y:number}[];

/**
 * This is just a wrapper class to handle stacks of workspace points.
 *
 * It's designed as immutable class returning after every calculation
 * a new instance of WorkspacePoints.
 */
export class WorkspacePoints {

  public get points(): WorkspacePoint[] {
    return this.workspacePoints;
  }

  private readonly workspacePoints: WorkspacePoint[] = [];

  constructor(points: {x:number, y:number}[]) {
    this.workspacePoints = points.map(point => new WorkspacePoint(point.x, point.y));
  }

  public removeFollowingDuplicates(): WorkspacePoints {
    const minLength = 2;
    if (this.workspacePoints.length < minLength) {
      return new WorkspacePoints(this.workspacePoints);
    }

    const notEquals = (a: WorkspacePoint, b: WorkspacePoint) => !WorkspacePoint.equals(a, b);
    return new WorkspacePoints(this.workspacePoints.filter((element, index) => {
      return index === 0 || notEquals(element, this.workspacePoints[index - 1]);
    }));
  }

  public popLikeFirst(): WorkspacePoints {
    const minLength = 2;
    if (this.workspacePoints.length < minLength) {
      return new WorkspacePoints(this.workspacePoints);
    }

    const equals = (a: WorkspacePoint, b: WorkspacePoint) => WorkspacePoint.equals(a, b);
    const clone = this.workspacePoints.slice(0); // copy
    if (equals(clone[0], clone[clone.length -1])) {
      clone.pop();
    }
    return new WorkspacePoints(clone);
  }

  public scaleUp(vector: WorkspacePoint): WorkspacePoints {
    return new WorkspacePoints(this.workspacePoints.map(point => point.scaleUp(vector)));
  }

  public scaleDown(vector: WorkspacePoint): WorkspacePoints {
    return new WorkspacePoints(this.workspacePoints.map(point => point.scaleDown(vector)));
  }

  public reverse(): WorkspacePoints {
    return new WorkspacePoints(this.workspacePoints.reverse());
  }

  public isClockwise(): boolean {
    return this.area() >= 0;
  }

  public area(): number {
    let area = 0;
    const half = 2;
    for (let i = 0; i < this.workspacePoints.length; i++) {
      const j = (i + 1) % this.workspacePoints.length;
      area += this.workspacePoints[i].x * this.workspacePoints[j].y;
      area -= this.workspacePoints[j].x * this.workspacePoints[i].y;
    }
    return area / half;
  }

  public getBoundingBoxRectangle(): BoundingBoxRectangle {
    return BoundingBoxRectangle.FROM_POINTS(this);
  }

  /**
   * Compare this with another WorkspacePoints stack.
   * @param points
   */
  public equals(points: WorkspacePoints|WorkspacePoint[]) : boolean {
    return WorkspacePoints.equals(this, points);
  }

  /**
   * Compare two stacks of WorkspacePoints.
   *
   * They are equals if they have both the same length
   * and the same points with the same order.
   *
   * @param a
   * @param b
   */
  public static equals(a: WorkspacePoints|WorkspacePoint[], b: WorkspacePoints|WorkspacePoint[]): boolean {

    a = Array.isArray(a) ? a : a.workspacePoints;
    b = Array.isArray(b) ? b : b.workspacePoints;

    if (a.length !== b.length) {
      return false;
    }
    if (a.length === 0) {
      // b has also the length of 0
      return true;
    }

    for (let i = 0; i < a.length; i++) {
      if (!WorkspacePoint.equals(a[i], b[i])) {
        return false;
      }
    }
    return true;
  }
}
