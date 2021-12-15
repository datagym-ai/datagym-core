import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { DatasetDetail } from '../model/DatasetDetail';
import { DatasetService } from './dataset.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DatasetResolverService implements Resolve<DatasetDetail> {

  constructor(private datasetService: DatasetService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<DatasetDetail> | Promise<DatasetDetail> | DatasetDetail {
    const id = route.paramMap.get('id');
    return this.datasetService.getDatasetById(id);
  }
}
