import {LcEntryChange} from '../../model/change/LcEntryChange';
import {Observable} from 'rxjs';


export interface EntryChangeApiInterface {

  delete(change: string|LcEntryChange): Observable<void>;
  update(change: LcEntryChange): Observable<LcEntryChange>;
  create(grandParentId: string, valueId: string, change: LcEntryChange): Observable<LcEntryChange>;

}
