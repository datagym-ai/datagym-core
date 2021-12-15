
export enum GeometryType {
  LINE,
  RECTANGLE,
  POLYGON,
  POINT,
  /**
   * The following geometry types are not known
   * within the backend. They are only used with
   * AISeg to select the area of interest with
   * some different tools.
   */
  POLYLINE,
  POINTS,
  APPROXIMATE_EDGES,
  /**
   * A 'special' geometry for media segmentation.
   */
  IMAGE_SEGMENTATION,
  /**
   * A 'special' geometry to remove / erase media segmentation parts.
   * This kind of geometry type is not known within the BE. It's only
   * a tool for the Fe.
   */
  IMAGE_SEGMENTATION_ERASER
}

export namespace GeometryType {

  export function valueOf(key: string): GeometryType|undefined {
    return Object.keys(GeometryType)
      // filter keys
      .filter(type => typeof type === 'string')
      .filter(type => type === key)
      // map to enum types
      .map(type => GeometryType[type])
      // find returns the first occurred element.
      .find(_ => true);
  }
}
