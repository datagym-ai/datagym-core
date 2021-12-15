

export class WorkspacePoint {
  x: number;
  y: number;

  constructor(x?: number, y?: number) {
    if (x !== undefined) {
      this.x = x;
    } else{
      this.x = 0;
    }
    if (y !== undefined) {
      this.y = y;
    } else{
      this.y = 0;
    }
  }

  public updateCoordinates(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public update(mouseEvent: MouseEvent, ctm: any) {
    this.x = (mouseEvent.clientX - ctm.e) / ctm.a;
    this.y = (mouseEvent.clientY - ctm.f) / ctm.d;
  }

  /**
   * Compare this point with the given.
   * @param other
   */
  public equals(other: {x:number, y:number}): boolean {
    return WorkspacePoint.equals(this, other);
  }

  /**
   * Static implementation to allow the equals comparison
   * also with objects of type {x: number, y:number} without
   * using the ctr. of this class.
   *
   * @param a
   * @param b
   */
  public static equals(a: {x:number, y:number}, b: {x:number, y:number}): boolean {
    return a.x === b.x && a.y === b.y;
  }

  /**
   * Compare two stacks of Workspace points.
   *
   * The order matters!
   *
   * @param stack1
   * @param stack2
   */
  public static compareStack(stack1: WorkspacePoint[], stack2: WorkspacePoint[]): boolean {

    if (stack1.length !== stack2.length) {
      return false;
    }

    for(let i = 0; i < stack1.length; i++) {
      if (!WorkspacePoint.equals(stack1[i], stack2[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * Scale this workspace point up with the given conversion width & height.
   *
   * @param vector the scaling factor
   * @return new WorkspacePoint
   */
  public scaleUp(vector: WorkspacePoint): WorkspacePoint {
    return WorkspacePoint.scaleUp(this, vector);
  }

  /**
   * Scale this workspace point up with the given conversion width & height.
   *
   * @param vector the scaling factor
   * @return new WorkspacePoint
   */
  public scaleDown(vector: WorkspacePoint): WorkspacePoint {
    return WorkspacePoint.scaleDown(this, vector);
  }

  /**
   * Scale the given workspace point up with the given conversion width & height.
   *
   * @param source point
   * @param vector the scaling factor
   * @return new WorkspacePoint
   */
  public static scaleUp(source: WorkspacePoint, vector: WorkspacePoint): WorkspacePoint {
    return new WorkspacePoint(
      source.x * vector.x,
      source.y * vector.y
    );
  }

  /**
   * Scale the given workspace point down with the given conversion width & height.
   *
   * @param source point
   * @param vector the scaling factor
   * @return new WorkspacePoint
   */
  public static scaleDown(source: WorkspacePoint, vector: WorkspacePoint): WorkspacePoint {
    return new WorkspacePoint(
      source.x / vector.x,
      source.y / vector.y
    );
  }

  public static fromEvent(event: UIEvent): WorkspacePoint {
    /**
     * UIEvent.layerX & UIEvent.layerY are Non-standard but supported by all major browsers.
     * See https://developer.mozilla.org/en-US/docs/Web/API/UIEvent/layerX for reference.
     */
    return new WorkspacePoint(
      (event as unknown as {readonly layerX: number}).layerX,
      (event as unknown as {readonly layerY: number}).layerY
    );
  }
}
