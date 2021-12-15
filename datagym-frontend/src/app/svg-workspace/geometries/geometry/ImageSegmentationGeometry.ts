import {Element} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {ImageSegmentationGeometryData} from '../geometry-data/ImageSegmentationGeometryData';
import {PolygonGeometry} from './PolygonGeometry';
import {BoundingBoxRectangle as Rectangle} from '../../model/utility/BoundingBoxRectangle';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import RTree from 'rtree/lib';
import * as greinerHormann from 'greiner-hormann';
import {WorkspaceEventType} from '../../messaging/WorkspaceEventType';
import {WorkspacePoints} from '../../model/WorkspacePoints';
import {PointsCollection} from '../../model/PointsCollection';

/**
 * This geometry is used for media segmentation.
 *
 * It should not hold any comment!
 */
export class ImageSegmentationGeometry extends PolygonGeometry {
  protected readonly className: string = 'ImageSegmentationGeometry';

  public set isEraser(isEraser: boolean) {
    this.isEraserType = isEraser;
    this.svgObject.attr('eraser', 'true');
  }

  /**
   * The rTree for lookup search for possible matches
   * of another media segmentation geometries.
   *
   * @see: https://github.com/leaflet-extras/RTree
   */
  private static rTree = RTree();
  /**
   * Internal flag just do 'ignore' this media segmentation
   * until the delete request is completed. This is set if the
   * segment is 'eaten' by another one.
   */
  private preparedForDeletion = false;

  /**
   * The inner edges as stack.
   */
  private inner: WorkspacePoint[][] = [];

  /**
   * @override
   */
  protected get comment(): string {
    const errorMessage = `comment is not supported on ${this.className}`;
    throw new Error(errorMessage);
  }

  /**
   * Flag to indicate that this media segmentation is registered
   * within the rTree for lookup searches via bounding boxes.
   */
  private inTree: boolean = false;

  /**
   * Hidden property to mark eraser that should not be printed.
   *
   * @private
   */
  private isEraserType: boolean = false;

  /**
   * At least three unique points within the geometry.
   *
   * @private
   */
  private isInvalid: boolean = true;

  /**
   * Get the scaling vector for synchronizing data & svg.
   */
  protected get scaleVector(): WorkspacePoint {
    return new WorkspacePoint(
      this.workspace.resizeOffsetHeight,
      this.workspace.resizeOffsetWidth
    );
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    this.svgObject = (svgLayer.polyline([]) as unknown as Element);
    const factor = 2;
    this.geometryProperties.fillOpacity = this.geometryProperties.fillOpacity * factor;
  }

  /**
   * Override the default setSvgProperties method
   * to set only the border property but don't fill
   * out the body.
   */
  protected setSvgProperties(): void {
    this.setSvgBorderProperties();
    this.svgObject.fill('none');
  }

  /**
   * Helper method to register one or more media segmentation geometries within
   * the rTree for lookup.
   *
   * Respects the 'preparedForDeletion' flag and ignores that geometries.
   *
   * @param segmentations one or multiple media segmentation geometries.
   */
  private static registerGeometryInTree(segmentations: ImageSegmentationGeometry | ImageSegmentationGeometry[]): void {
    segmentations = Array.isArray(segmentations) ? segmentations : [segmentations];
    segmentations.forEach(segmentation => {
      if (segmentation.preparedForDeletion) {
        return;
      }
      if (segmentation.inTree) {
        return;
      }
      const boundingRectangle = Rectangle.FROM_BBOX(segmentation.svgObject.bbox());

      ImageSegmentationGeometry.rTree.insert(boundingRectangle, segmentation);
      segmentation.inTree = true;
    });
  }

  public getGeometryData(): ImageSegmentationGeometryData {
    // let the following line commented out because the getPointsFromPath(path) method from
    // svg.poly_extend.js is still buggy.
    // this.syncSvgToData(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
    const pointsCollection = [this.points, ...this.inner].map(points => new PointsCollection(points));
    return new ImageSegmentationGeometryData(pointsCollection);
  }

  /**
   * Sets all geometry specific parameter with the svg-object (like x,y and positions)
   *
   * Note: this implementation overrides this.svgObject property!
   */
  public syncDataToSvg(): void;
  public syncDataToSvg(conversionHeight: number, conversionWidth: number): void;
  public syncDataToSvg(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {

    const points = this.points.map(
      pos => new WorkspacePoint(pos.x / conversionHeight, pos.y / conversionWidth)
    );
    const inner = this.inner.map(stack => stack.map(
      pos => new WorkspacePoint(pos.x / conversionHeight, pos.y / conversionWidth))
    );

    /**
     * We start drawing with an polyline object without filled area.
     * To display it as an segmentation we must convert it into a path object.
     * Only that object support the inner borders.
     */
    if (this.svgObject !== undefined && this.svgObject.type !== 'path') {
      this.svgObject.remove();
      this.svgObject = undefined;
      this.svgObject = (this.workspace.svgLayer as unknown as SVGType.WithSegment).segment(points, inner);
    } else {
      (this.svgObject as unknown as SVGType.WithSegmentPlot).plot(points, inner);
    }

    if (this.isEraserType) {
      this.svgObject.hide();
      return;
    }
    this.setSvgFillProperties();
    this.svgObject.stroke('none');
  }

  /**
   * Synchronizes the svg object with the data object
   */
  public syncSvgToData(): void;
  public syncSvgToData(conversionHeight: number, conversionWidth: number): void;
  public syncSvgToData(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    /**
     * This method is only used internally (expect by some AISeg geometries but
     * they don't use instances from ImageSegmentationGeometry) and the path object
     * does not support any getPoints method.
     *
     * Todo: implement getPoints within the svg.segment.js plugin.
     */
    const errorMessage = `syncSvgToData is not supported on ${this.className}`;
    throw new Error(errorMessage);
  }

  public setGeometryData(data: ImageSegmentationGeometryData): void {

    const points = data.pointsCollection.shift().points;
    if (data.pointsCollection.length === 0) {
      this.inner = [];
    }

    this.points = new WorkspacePoints(points).removeFollowingDuplicates().popLikeFirst().points;
    this.inner = data.pointsCollection.map((stack: PointsCollection) => {
      return new WorkspacePoints(stack.points).removeFollowingDuplicates().popLikeFirst().points;
    });
    this.syncDataToSvg();
    ImageSegmentationGeometry.registerGeometryInTree(this);
  }

  public beforeSelecting() {
  }

  public afterSelecting() {
  }

  public beforeUnselecting() {
  }

  protected onDragStartEvent(event): void {
  }

  protected onDragEndEvent(event): void {
  }

  /**
   * Handle all the segmentations of other types.
   * They should be removed if they share some points.
   *
   * Possible cases:
   * - cut corners
   * - cut segment out
   * - create a second/third... element
   *
   * @param segmentations
   */
  private handleOtherSegments(segmentations: ImageSegmentationGeometry[]): void {
    for (const segmentation of segmentations) {
      this.handleOtherSegment(segmentation);
    }
  }

  /**
   *
   * @param other
   * @private
   * @return should the other segment (re)added to the rTree with
   * the new bounding boxes?
   */
  private handleOtherSegment(other: ImageSegmentationGeometry): void {

    if (other.preparedForDeletion) {
      // Do not delete the segment twice.
      return;
    }

    const intersection = greinerHormann.intersection(this.points, other.points);
    if (!/*not*/!!intersection) {
      console.warn('no intersection');
      // No intersections.
      return;
    }
    const diffs = greinerHormann.diff(other.points, this.points);
    const union = greinerHormann.union(other.points, this.points);

    /**
     * If the union stack is equal to the other points
     * and the intersection stack is equal to this points
     * then
     * the new segment cuts out a inner border on the other
     * segment.
     *
     * Both segments should be registered as they are in the rTree.
     */
    const requiredDiffLength = 2;
    if (diffs.length === requiredDiffLength
      && union.length === 1
      && intersection.length === 1
      && WorkspacePoints.equals(intersection[0], this.points)
      && WorkspacePoints.equals(union[0], other.points)
    ) {
      // cut out the inner border
      other.inner = [...other.inner, this.points];
      other.syncDataToSvg();
      other.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
      return;
    }

    /**
     * If the union stack is equal to the this points
     * and the intersection stack is equal to other points
     * then
     * the new segment is larger than the old one and replaces
     * that segment completely. All inner borders are also removed.
     *
     * Only the new segments should be registered in the rTree.
     */
    if (diffs.length === requiredDiffLength
      && union.length === 1
      && intersection.length === 1
      && WorkspacePoints.equals(intersection[0], other.points)
      && WorkspacePoints.equals(union[0], this.points)
    ) {
      other.preparedForDeletion = true;
      return;
    }

    /**
     * Otherwise the new segments touches the old one and the
     * intersection should be removed from the old section as
     * it can be only attached to one segment.
     *
     * Note: by touching the outer border and inner borders,
     * the inner border must be removed. They appear as new outer
     * border. To reach this goal, merge them temporarily with the
     * new segment before cutting it out.
     */

    /**
     * The none touching inners belong to only one of the parts and
     * remain untouched.
     */
    const nonTouchingInners = other.inner.filter(inner =>
      null === greinerHormann.intersection(this.points, inner)
    );
    /**
     * The touching inners appear as part of the outer border.
     */
    const touchingInners = other.inner.filter(inner =>
      null !== greinerHormann.intersection(this.points, inner)
    );

    for (let i = 0; i < diffs.length; i++) {
      const diff = diffs[i];
      /**
       * All inner paths from the nonTouchingInners stack may remain
       * as inner borders within the diff segment.
       */
      const fullInner = nonTouchingInners.filter(inner =>
        null !== greinerHormann.intersection(diff, inner)
      );
      /**
       * Calculate the new outer border by using the diff stack as
       * starting polygon and cutting out all previously inner border
       * that appear as new outer border.
       */
      const newOuter = touchingInners.filter(inner =>
        null !== greinerHormann.intersection(diff, inner)
      ).reduce((previous, current) => {
        return greinerHormann.diff(previous, current)[0];
      }, diff);

      if (i === 0) {
        /**
         * On index 0 update the other segment.
         */
        other.points = newOuter;
        other.inner = fullInner;
        other.syncDataToSvg();
        other.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
      } else {
        /**
         * On all other indexes let the WorkspaceListenerService create a new
         * geometry with the given outer and inner borders.
         */
        const pointCollection = [newOuter, ...fullInner].map(points => new PointsCollection(points));
        const payload = new ImageSegmentationGeometryData(pointCollection);
        other.nextWorkspaceEvent(WorkspaceEventType.CREATE_SEGMENT_GEOMETRY, payload);
      }
    }
  }

  /**
   * Remove this svg object from svg layer.
   * Override if some other svg objects may be
   * created during the runtime.
   */
  public deleteFromSVGLayer(): void {
    if (this.inTree && this.svgObject) {
      const boundingBox = Rectangle.FROM_BBOX(this.svgObject.bbox());
      ImageSegmentationGeometry.rTree.remove(boundingBox, this);
    }
    if (this.svgObject !== undefined) {
      this.svgObject.remove();
      this.svgObject = undefined;
    }
  }

  public toDataString(): string {
    const space = 2;
    return `${this.className} (
    id: ${this.svgObject.id()} / ${this.geometryProperties.identifier},
    points: ${JSON.stringify(this.points, null, space)},
    inner: ${JSON.stringify(this.inner, null, space)},
    )`;
  }

  protected onDrawStopEvent(event): void {

    this.points = new WorkspacePoints((this.svgObject as unknown as SVGType.WithGetPoints).getPoints())
      .removeFollowingDuplicates().popLikeFirst().scaleUp(this.scaleVector).points;

    const minRequiredPoints = 3;
    if (this.points.length < minRequiredPoints) {
      this.svgObject.hide();
      // Just hide, removing from svg cause some unexpected behaviour.
      // this.svgObject.remove();
      return;
    }
    this.isInvalid = false;

    /*
     * Remove all possible intersecting media segmentations geometries from
     * rTree and return them. Process that batch of segmentations and re-add only
     * the remaining geometries.
     */
    const imageSegmentations = ImageSegmentationGeometry.searchInTree(this);

    if (imageSegmentations.length === 0) {
      ImageSegmentationGeometry.registerGeometryInTree(this);
      this.syncDataToSvg();
      return;
    }

    /*
     * Print out the new path object and update it within the backend.
     * All other typed media segmentation geometries should not change this new one.
     */
    this.syncDataToSvg();
    this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);

    /**
     * This segmentation geometry should be cut out from all other geometries.
     */
    this.handleOtherSegments(imageSegmentations);

    const ids2delete = [this, ...imageSegmentations]
      .filter(segment => !!segment.preparedForDeletion)
      .map(segment => segment.geometryProperties.identifier);

    if (ids2delete.length > 0) {
      this.nextWorkspaceListEvent(WorkspaceEventType.DELETE_WITHOUT_REQUEST, ids2delete);
    }

    /*
     * Re register all media segmentation geometries from the same type.
     *
     * The 'preparedForDeletion' flag is respected.
     */
    ImageSegmentationGeometry.registerGeometryInTree(this);
    ImageSegmentationGeometry.registerGeometryInTree(imageSegmentations);
  }

  /**
   * Helper method to read possible matches from rTree.
   *
   * @param segment
   */
  private static searchInTree(segment: ImageSegmentationGeometry): ImageSegmentationGeometry[] {

    const boundingRectangle = Rectangle.FROM_BBOX(segment.svgObject.bbox());
    const segmentations = ImageSegmentationGeometry.rTree.remove(boundingRectangle).map(leaf => leaf.leaf) as ImageSegmentationGeometry[];
    if (segmentations === undefined) {
      return [];
    }
    segmentations.forEach(segmentation => segmentation.inTree = false);
    return segmentations;
  }
}
