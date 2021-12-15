import {WorkspacePoint} from '../WorkspacePoint';
import {PointStack} from '../WorkspacePoints';


/**
 * Utility to detect bounding boxes touching each other.
 */
export class BoundingBoxRectangle {
  // Math.floor to round down pixel coordinates. Required because bbox adds tiny float numbers due to stroke-width

  public get tl(): WorkspacePoint {
    return new WorkspacePoint(Math.floor(this.x), Math.floor(this.y));
  }

  public get tr(): WorkspacePoint {
    return new WorkspacePoint(Math.floor(this.x + this.width), Math.floor(this.y));
  }

  public get bl(): WorkspacePoint {
    return new WorkspacePoint(Math.floor(this.x), Math.floor(this.y + this.height));
  }

  public get br(): WorkspacePoint {
    return new WorkspacePoint(Math.floor(this.x + this.width), Math.floor(this.y + this.height));
  }

  public get height(): number {
    return this.h;
  }

  public get width(): number {
    return this.w;
  }

  public get size(): number {
    return this.h * this.w;
  }

  constructor(
    public readonly x: number,
    public readonly y: number,
    public readonly h: number,
    public readonly w: number
  ) { }

  /**
   * Additional named ctr to use the bounding box directly to construct this object.
   *
   * @param bbox
   * @constructor
   */
  public static FROM_BBOX(bbox: { x: number, y: number, height: number, width: number }) {
    return new BoundingBoxRectangle(bbox.x, bbox.y, bbox.height, bbox.width);
  }

  /**
   * Additional named ctr to create the bounding box directly from the points list.
   *
   * @param points
   * @constructor
   */
  public static FROM_POINTS(points: PointStack): BoundingBoxRectangle {
    points = Array.isArray(points) ? points : points.points;

    const xx = points.map(point => point.x);
    const yy = points.map(point => point.y);

    const xMax = Math.max(...xx);
    const xMin = Math.min(...xx);
    const yMax = Math.max(...yy);
    const yMin = Math.min(...yy);

    return new BoundingBoxRectangle(xMin, yMin, (yMax - yMin), (xMax - xMin));
  }

  public toDataString(): string {
    return `Rectangle (
    tl: (${this.tl.x}, ${this.tl.y})
    tr: (${this.tr.x}, ${this.tr.y})
    bl: (${this.bl.x}, ${this.bl.y})
    x: ${this.x},
    y: ${this.y},
    width: ${this.width},
    height: ${this.height}
)`;
  }

  /**
   * Return true if the other intersects this rectangle.
   *
   * @param other
   */
  public intersect(other: BoundingBoxRectangle): boolean {
    return (this.x <= (other.x + other.width) &&
      other.x <= (this.x + this.width) &&
      this.y <= (other.y + other.height) &&
      other.y <= (other.y + other.height));
  }
}
