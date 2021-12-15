import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {AwsS3CredentialView} from '../model/AwsS3CredentialView';
import {Observable, of} from 'rxjs';
import {AwsS3Service} from './aws-s3.service';
import {catchError, map} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class S3ResolverService implements Resolve<AwsS3CredentialView>{

  constructor(private s3: AwsS3Service) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<AwsS3CredentialView> | Promise<AwsS3CredentialView> | AwsS3CredentialView {

    const datasetId = route.parent.paramMap.get('id');
    return this.s3.getData(datasetId).pipe(
      map(res => res),
      catchError((p1, p2: Observable<AwsS3CredentialView>) => {
        return of(null);
      })
    );
  }
}
