import {Observable} from 'rxjs';


export interface EntryConfigApiInterface {

  hasConfigChanged(labelIterationId: string, lastChangedConfig: number) : Observable<boolean>;

}
