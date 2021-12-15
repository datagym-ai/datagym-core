import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AwsS3MediaService {

  private baseURL: string = '/api/dataset';

  constructor(private http: HttpClient) {
  }

  public createAwsPreSignedUploadURI(datasetId: string, btoaFilename: string): Observable<string> {
    let httpParams: HttpParams = new HttpParams();
    httpParams = httpParams.append('filename', btoaFilename);
    return this.http.get<string>(`${this.baseURL}/${datasetId}/signedUri`, {
      responseType: 'text' as any,
      params: httpParams
    });
  }

  public confirmPreSignedUrlUpload(datasetId: string, preSignedUrl: string): Observable<any> {
    return this.http.post(`/api/dataset/${datasetId}/confirmUri`, preSignedUrl);
  }
}
