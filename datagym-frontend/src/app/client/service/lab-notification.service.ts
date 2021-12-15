import { Injectable } from '@angular/core';
import { NotificationsService } from 'angular2-notifications';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, from, Observable } from 'rxjs';
import { LabError } from '../interceptor/error/model/LabError';
import { I18nParams } from '../interceptor/error/model/i18n-params';
import { LabErrorNotification } from '../interceptor/error/model/LabErrorNotification';
import { map, mergeAll, toArray } from 'rxjs/operators';
import { LabErrorDetail } from '../interceptor/error/model/LabErrorDetail';

/**
 * Global notification service
 * note: Wrapper class for the notification-system to easily adapt toast/notification service in the future
 */
@Injectable({
  providedIn: 'root'
})
export class LabNotificationService {

  constructor(private notificationsService: NotificationsService,
              private translate: TranslateService) {
  }

  /**
   * Show a datagym common error object.
   * @param {LabError} error
   */
  public labError(error: LabError) {
    this.translationResolver(error).subscribe((errorNotification: LabErrorNotification) => {
      this.notificationsService.error(errorNotification.headline, this.mapDetailsToHtml(errorNotification.details));
    });
  }

  /**
   * Show a datagym common success object.
   * @param {LabError} error
   */
  public labSuccess(error: LabError) {
    this.translationResolver(error).subscribe((errorNotification: LabErrorNotification) => {
      this.notificationsService.success(errorNotification.headline, this.mapDetailsToHtml(errorNotification.details));
    });
  }

  /**
   * Convenience method show a simple error message string.
   * @param {string} msg
   */
  public error(msg: string) {
    this.notificationsService.error(msg);
  }

  /**
   * Convenience method show a simple error message string using a ngx-translate key
   * @param translateKey
   */
  public error_i18(translateKey: string) {
    this.translate.get(translateKey).subscribe(translation => {
      this.notificationsService.error(translation);
    });
  }

  /**
   * Convenience method show a simple info message string.
   * @param {string} msg
   */
  public info(msg: string) {
    this.notificationsService.info(msg);
  }

  /**
   * Convenience method show a simple simple message string using a ngx-translate key
   * @param translateKey
   */
  public info_i18(translateKey: string) {
    this.translate.get(translateKey).subscribe(translation => {
      this.notificationsService.info(translation);
    });
  }

  /**
   * Convenience method show a simple warn message string.
   * @param {string} msg
   */
  public warn(msg: string) {
    this.notificationsService.warn(msg);
  }

  /**
   * Convenience method show a simple simple message string using a ngx-translate key
   * @param translateKey
   */
  public warn_i18(translateKey: string) {
    this.translate.get(translateKey).subscribe(translation => {
      this.notificationsService.warn(translation);
    });
  }

  /**
   * Convenience method show a simple info message string.
   * @param {string} msg
   */
  public success(msg: string) {
    this.notificationsService.success(msg);
  }

  /**
   * Convenience method show a simple simple message string using a ngx-translate key
   * @param translateKey
   */
  public success_i18(translateKey: string) {
    this.translate.get(translateKey).subscribe(translation => {
      this.notificationsService.success(translation);
    });
  }

  /**
   * Get translation with params
   * @param error the specific error object to translate
   */
  private translationResolver(error: LabError): Observable<LabErrorNotification> {
    let translatedDetails: Observable<any[] | string>;
    if (error.details) {
      translatedDetails= from(error.details)
        .pipe(
          map((detail: LabErrorDetail) => this.translate.get(detail.key, this.transformParams(detail.params))),
          mergeAll(),
          toArray()
        );
    } else{
      translatedDetails = this.translate.get(error.key, this.transformParams(error.params));
    }

    const translatedHeadline = this.translate.get(error.key, this.transformParams(error.params));

    return combineLatest(translatedHeadline, translatedDetails).pipe<LabErrorNotification>(
      map(([headline, details]) => {
        const errorNotification = new LabErrorNotification();
        errorNotification.headline = headline;
        if (Array.isArray(details)) {
          errorNotification.details = details;
        } else{
          errorNotification.details = [];
        }
        return errorNotification;
      }));
  }

  /**
   * Wrap params since ngx-translate only supports "named" params
   * @param params array based params from the ef21-exception library
   */
  private transformParams(params: string[]): I18nParams {
    const result = new I18nParams();
    result.p0 = params[0];
    result.p1 = params[1];
    result.p2 = params[2];
    result.p3 = params[3];
    result.p4 = params[4];
    result.p5 = params[5];
    return result;
  }

  /**
   * Converts a string array to a html-formatted list
   * @param details
   */
  private mapDetailsToHtml(details: string[]): string {
    let htmlBuilder = '<ul>';
    details.forEach(detail => htmlBuilder += `<li>${detail}</li>`);
    htmlBuilder += '</ul>';
    return htmlBuilder;
  }
}
