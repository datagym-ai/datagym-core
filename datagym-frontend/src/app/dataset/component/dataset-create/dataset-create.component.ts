import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {DatasetService} from '../../service/dataset.service';
import {DatasetCreateBindingModel} from '../../model/DatasetCreateBindingModel';
import {DatasetFormBuilder} from '../../model/DatasetFormBuilder';
import {DatasetDetail} from '../../model/DatasetDetail';
import {UserService} from '../../../client/service/user.service';
import {TextfieldComponent} from '../../../shared/textfield/textfield.component';
import {MediaType} from '../../../project/model/MediaType.enum';


@Component({
  selector: 'app-dataset-create',
  templateUrl: './dataset-create.component.html',
  styleUrls: ['./dataset-create.component.css']
})
export class DatasetCreateComponent implements OnInit, AfterViewInit {
  public createDatasetForm: FormGroup;

  @ViewChild('datasetNameField')
  public datasetNameField: TextfieldComponent;

  public MediaType = MediaType;

  constructor(private router: Router,
              private datasetService: DatasetService,
              public userService: UserService) {
  }

  ngOnInit() {
    this.createDatasetForm = DatasetFormBuilder.create();
  }

  ngAfterViewInit(): void {
    this.datasetNameField.inputElementRef.nativeElement.focus();
  }

  public onSubmit() {
    const name = this.createDatasetForm.value.datasetName;
    const desc = this.createDatasetForm.value.datasetShortDescription;
    const mediaType = this.createDatasetForm.value.mediaType;
    const owner: string = this.createDatasetForm.value.datasetOwner;

    const newDataset: DatasetCreateBindingModel = new DatasetCreateBindingModel(owner, name, mediaType, desc || null);
    this.datasetService.createDataset(newDataset).subscribe((dataset: DatasetDetail) => {
      this.createDatasetForm.reset();
      this.router.navigate(['datasets', 'details', dataset.id]).then();
    });
  }

  public onCancel() {
    this.createDatasetForm.reset();
    this.router.navigate(['datasets']).then();
  }
}
