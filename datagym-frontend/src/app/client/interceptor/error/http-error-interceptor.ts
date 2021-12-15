import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LabError } from './model/LabError';
import { LabNotificationService } from '../../service/lab-notification.service';
import { UserService } from '../../service/user.service';

/**
 * HttpInterceptor catching known errors and error types thrown by the server-side.
 * By default errors are shown as toasts via LabNotificationService. Error requests doesnt get canceled so you may choose
 * to do additional handling in your subscriber.
 */
@Injectable({providedIn: 'root'})
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(private notify: LabNotificationService,
              private userService: UserService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(tap(
      () => {
      },
      (err: any) => {
        let errorTo = err.error;

        if(errorTo) {
          if (errorTo.key) {
            // everything's fine with known and typed exception
          }
          else {
            // convert unknown errors
            try {
              // check if error isn't parsed as readable Object
              errorTo = JSON.parse(errorTo);
              if (errorTo.key) {
                // everything's fine with known and typed exception
              } else {
                errorTo = this.generateConnectionError(err);
              }
            } catch (e) {
              errorTo = this.generateConnectionError(err);
            }

          }
        }
        else {
          // not even HttpErrors:
          errorTo = new LabError();
          errorTo.key = "ex_unknown";
          errorTo.params = ['' + err];
        }


        if("ex_unauthorized" === errorTo.key) {
          this.userService.logout();
        }
        else {
          // Only show error if not HTTP Status 200
          // ex.: GET Image URL returns HTTP 200 but generates error due to same-origin-policy
          if (err.status !== 200) {
            this.notify.labError(errorTo);
          }
        }
      }
    ));
  }

  private generateConnectionError(err: any): LabError {
    let errorTo = new LabError();
    errorTo.key = "ex_connection";
    errorTo.params = ['' + err.status, err.error];
    return errorTo;
  }

}
