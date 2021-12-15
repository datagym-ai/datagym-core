import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {MediaProfile} from './profile/MediaProfile';
import {LcEntryValue} from '../../model/LcEntryValue';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';

/**
 * The `MediaController` define the layout of the label-mode and handle the `initValue` method
 * of the `EntryValueService`.
 *
 * All controller used within the `MediaControlServiceService` must implement this interface.
 */
export interface MediaController {

  /**
   * Todo: implement a class for the shortcuts.
   */
  readonly shortcuts: { key: string|string[], callback: () => void, shiftKey?: boolean }[];

  /**
   * Control the template with some constant settings depending on the type.
   *
   * Prepared to use the media profile without the media controller.
   */
  readonly profile: MediaProfile;

  initValues(task: SingleTaskResponseModel, frameNumber: number|undefined): void;

  /**
   * Remove (and delete in BE) all geometries that are *empty*
   * where the definition of *empty* depends on the label mode.
   *
   * @param values
   * @return a flatted list of all 'not empty'-geometries.
   */
  cleanupGeometries(values: LcEntryValue[]): LcEntryGeometryValue[];

  teardown(): void;
}
