import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';
import {EntryChangeService} from '../../../../entry-change.service';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {Observable} from 'rxjs';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {map} from 'rxjs/operators';

export abstract class DeleteHelper {
  readonly abstract kind: LcEntryChangeType;

  public constructor(
    protected readonly changeApi: EntryChangeService,
  ) {}

  public abstract handlePreviousChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange>;

  public abstract handleNextChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange>;

  public deleteCurrentChange(change2delete: LcEntryChange): Observable<void> {
    return this.changeApi.delete(change2delete).pipe(map(() => null));
  }
}
