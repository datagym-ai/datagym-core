import {ValidityObserver} from './ValidityObserver';
import {VideoMediaController} from "../VideoMediaController";


/**
 * @deprecated Remove when we support valid flag in video labeling mode.
 */
export class VideoValidityObserver implements ValidityObserver {
  /**
   * Video label mode doesn't support global classifications at all.
   */
  readonly globalClassificationsValid = true;

  hasGeometries(): boolean {
    return this.mediaController.hasGeometries();
  }

  /**
   * We don't support the valid flag in the video labeling mode.
   * Remind: Maybe not all geometries are visible within the
   * EntryValueService so check the valueLines stack from
   * the VideoMediaController when implementing this feature.
   */
  hasInvalidGeometries(): boolean {
    return false;
  }

  constructor(private mediaController: VideoMediaController) {}

  teardown(): void {
    // Have some break. Here is nothing to do.
  }
}
