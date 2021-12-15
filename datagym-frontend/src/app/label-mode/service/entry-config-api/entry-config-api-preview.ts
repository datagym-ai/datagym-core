import {EntryConfigApiInterface} from './entry-config-api-interface';
import {Observable, of} from 'rxjs';


export class EntryConfigApiPreview implements EntryConfigApiInterface{

  hasConfigChanged(labelIterationId: string, lastChangedConfig: number): Observable<boolean> {
    return of(false);
  }

}
