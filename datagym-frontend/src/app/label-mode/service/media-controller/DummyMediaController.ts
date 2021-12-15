import {MediaController} from './MediaController';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {DummyMediaProfile} from './profile/DummyMediaProfile';
import {LcEntryValue} from '../../model/LcEntryValue';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';

/**
 * This is just a dummy used while startup or in some error cases.
 */
export class DummyMediaController implements MediaController {

  public readonly profile = new DummyMediaProfile();

  public readonly shortcuts: { key: string|string[], callback: () => void, shiftKey?: boolean }[] = [];

  public initValues(task: SingleTaskResponseModel, frameNumber: number|undefined): void {
    /* dummy does nothing */
  }

  public teardown(): void {
    /* dummy does nothing */
  }

  public cleanupGeometries(values: LcEntryValue[]): LcEntryGeometryValue[] {
    /* dummy does nothing */
    return [];
  }
}
