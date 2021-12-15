
export class Point {
  public readonly x: number;
  public readonly y: number;

  public constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public clone(): Point {
    return new Point(this.x, this.y);
  }

  public equals(other: Point) {
    return !!other && this.x === other.x && this.y === other.y;
  }

  public static fromEvent(event: UIEvent): Point {
    /**
     * UIEvent.layerX & UIEvent.layerY are Non-standard but supported by all major browsers.
     * See https://developer.mozilla.org/en-US/docs/Web/API/UIEvent/layerX for reference.
     */
    return new Point(
      (event as unknown as {readonly layerX: number}).layerX,
      (event as unknown as {readonly layerY: number}).layerY
    );
  }
}
