import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {InvalidReason} from '../../basic/media/model/InvalidReason';
import {Observable, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private baseURL: string = '/api/media';

  constructor(private http: HttpClient) {
  }

  public fetchMediaById(id: string) {
    return this.http.get(`${this.baseURL}/${id}`);
  }

  public deleteMediaById(id: string) {
    return this.http.delete(`${this.baseURL}/${id}`);
  }

  public deleteMediaByIdList(id: string[]) {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: id
    };
    return this.http.delete(`${this.baseURL}/list`, options);
  }

  public getMediaInvalidMessage(type: InvalidReason): string {
    return InvalidReason.getErrorMessage(type);
  }

  public restoreMediaById(id: string) {
    return this.http.delete(`${this.baseURL}/${id}/restore`);
  }

  public deleteMediaByIdPermanently(id: string) {
    return this.http.delete(`${this.baseURL}/${id}/deleteFromDb`);
  }

  /*
   * For createImage see shared/file-upload
   */
  public streamMediaFile(url: string): Observable<string> {
    return this.http.get(url, {responseType: 'text'});
  }

  public openMediaInNewTab(mediaId: string): Observable<boolean> {
    const mediaUrl = `${this.baseURL}/${mediaId}`;
    const subject = new Subject<boolean>();

    this.streamMediaFile(mediaUrl).subscribe(
      (response: string) => {
        /**
         * The response can either contain a url or an image stream. If the url parsing fails, it is the
         * image stream.
         */
        try {
          const url = new URL(response);
          window.open(url.href, '_blank');
        } catch (e) {
          window.open(mediaUrl, '_blank');
        }

        subject.next(true);
        subject.complete();
      },
      () => {
        subject.error(true);
        subject.complete();
      });
    return subject.asObservable();
  }
}
