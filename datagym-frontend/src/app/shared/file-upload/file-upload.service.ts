import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FileUploadService {

  constructor(private http: HttpClient) {
  }

  /**
   *
   * @param url
   * @param mediaToCreate
   * @param fileName
   */
  public uploadFile(url: string, mediaToCreate: File, fileName: string): Observable<any> {
    const httpOptions: { headers: HttpHeaders } = {headers: new HttpHeaders({'X-filename': fileName})};
    return this.http.post(url, mediaToCreate, httpOptions);
  }

  public uploadMp4ToAwsS3(presignedUrl: string, body: File): Observable<any> {
    return this.http.put(presignedUrl, body, {
      headers: {
        'Content-Type': 'video/mp4'
      }
    })
  }


}
