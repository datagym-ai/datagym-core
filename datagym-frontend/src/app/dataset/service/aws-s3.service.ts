import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AwsS3CredentialView } from '../model/AwsS3CredentialView';
import { AwsS3SyncStatusView } from '../model/AwsS3SyncStatusView';
import { HttpClient } from '@angular/common/http';
import { AwsS3Create } from '../model/AwsS3Create';
import { AwsS3BucketUpdate } from '../model/AwsS3BucketUpdate';
import { AwsS3CredentialUpdate } from '../model/AwsS3CredentialUpdate';


@Injectable({
  providedIn: 'root'
})
export class AwsS3Service {

  private baseURL: string = '/api/dataset';

  constructor(private http: HttpClient) {
  }

  public getData(datasetId: string): Observable<AwsS3CredentialView> {
    return this.http.get<AwsS3CredentialView>(`${this.baseURL}/${datasetId}/aws`);
  }

  public create(datasetId: string, payload: AwsS3Create): Observable<AwsS3CredentialView> {
    return this.http.post<AwsS3CredentialView>(`${this.baseURL}/${datasetId}/aws`, payload);
  }

  public updateBucket(datasetId: string, payload: AwsS3BucketUpdate): Observable<AwsS3CredentialView> {
    const url = `${this.baseURL}/${datasetId}/aws/bucket`;
    return this.http.put<AwsS3CredentialView>(url, payload);
  }

  public updateCredentials(datasetId: string, payload: AwsS3CredentialUpdate): Observable<AwsS3CredentialView> {
    const url = `${this.baseURL}/${datasetId}/aws/keys`;
    return this.http.put<AwsS3CredentialView>(url, payload);
  }

  public synchronize(datasetId: string): Observable<AwsS3SyncStatusView|undefined> {
    return this.http.post<AwsS3SyncStatusView>(`${this.baseURL}/${datasetId}/aws/sync`, {});
  }
}
