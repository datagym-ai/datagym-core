import {LabelTaskApiInterface} from './label-task-api-interface';
import {LabelTaskApiPreview} from './label-task-api-preview';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelTaskState} from '../../../task-config/model/LabelTaskState';
import {tap} from 'rxjs/operators';


/**
 * This class is a preview class but loads a 'real' task from the backend.
 * All other api methods are mocked within the LabelTaskApiPreview class.
 * This class is also injected via a service factory method.
 * @see LabelTaskApiService
 */
export class LabelTaskApiAdmin extends LabelTaskApiPreview implements LabelTaskApiInterface {

  private baseUrl = '/api/task';

  public readonly adminMode: boolean = true;

  constructor(private http: HttpClient) {
    super();
  }

  public getTaskDataById(taskId: string): Observable<SingleTaskResponseModel> {
    const url = `${this.baseUrl}/${taskId}`;
    return this.http.get<SingleTaskResponseModel>(url).pipe(
      tap((task: SingleTaskResponseModel) => task.labelTaskState = LabelTaskState.ADMIN_VIEW)
    );
  }
}
