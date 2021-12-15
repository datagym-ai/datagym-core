import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { DatasetService } from '../../service/dataset.service';
import { Subscription } from 'rxjs';
import { DatasetList } from '../../model/DatasetList';
import { ActivatedRoute } from '@angular/router';
import { MediaType } from '../../../project/model/MediaType.enum';

@Component({
  selector: 'app-dataset-list',
  templateUrl: './dataset-list.component.html',
  styleUrls: ['./dataset-list.component.css']
})
export class DatasetListComponent implements OnInit, OnDestroy {
  public datasets: DatasetList[] = [];
  private serviceSub: Subscription;

  @Input()
  public filterText: string;
  public isSuperadminMode: boolean = false;

  public MediaType = MediaType;

  constructor(
    private route: ActivatedRoute,
    private datasetService: DatasetService) {
  }

  ngOnInit() {
    /**
     * In route of account admin fetch *all* datasets
     */
    this.isSuperadminMode = !!this.route.snapshot.data['SUPER_ADMIN'];
    const callback = this.isSuperadminMode
      ? this.datasetService.getAllDatasetsAsAccountAdmin()
      : this.datasetService.getDatasets();

    this.serviceSub = callback.subscribe((datasets: DatasetList[]) => {
      this.datasets = datasets;
    });
  }

  ngOnDestroy(): void {
    this.serviceSub.unsubscribe();
  }
}
