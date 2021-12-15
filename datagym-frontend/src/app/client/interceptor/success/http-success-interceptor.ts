import {Injectable} from '@angular/core';
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LabNotificationService} from '../../service/lab-notification.service';
import {tap} from 'rxjs/operators';
import {LabError} from '../error/model/LabError';

const STATUS_CODE_ACCEPTED: number = 202;

@Injectable({
  providedIn: 'root'
})
export class HttpSuccessInterceptor implements HttpInterceptor {

  constructor(private notify: LabNotificationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(tap((response: HttpResponse<any>) => {

      if (response.type !== HttpEventType.Response) {
        return;
      }
      if (response.status !== STATUS_CODE_ACCEPTED) {
        return;
      }

      let bodyTo = response.body;
      if(bodyTo) {
        if (bodyTo.key) {
          // everything's fine with known and typed exception
        } else {
          // convert unknown errors
          bodyTo = new LabError();
          bodyTo.key = 'ex_connection';
          bodyTo.params = [`${ status }`, response.body];
        }
        this.notify.labSuccess(bodyTo);
      }
    }));
  }
}
