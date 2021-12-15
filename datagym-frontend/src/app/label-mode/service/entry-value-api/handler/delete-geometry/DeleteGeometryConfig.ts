/**
 * Deleting in the video label mode is complex.
 * The workflow depends u.a. on
 * - the number of LcEntryChanges,
 * - the way it is deleted (from the workspace or the value line)
 * - canceled while drawing?
 *
 * Let's configure the required behaviour here.
 */
export class DeleteGeometryConfig {
  /**
   * Delete the geometry for that frameNumber.
   */
  public readonly frameNumber?: number;
  /**
   * Delete the full geometry ignoring the state of it's keyFrames.
   * @default: false
   */
  public readonly deleteGeometry?: boolean = false;

  public constructor({frameNumber = 0, deleteGeometry = false}) {
    this.frameNumber = typeof frameNumber === 'number' ? frameNumber : undefined;
    this.deleteGeometry = !!deleteGeometry;
  }
}
