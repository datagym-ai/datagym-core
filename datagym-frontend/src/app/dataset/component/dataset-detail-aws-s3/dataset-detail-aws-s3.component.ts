import {Component, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {AWS_S3_FORM_STATES, AwsS3FormBuilder} from '../../model/AwsS3FormBuilder';
import {DatasetDetail} from '../../model/DatasetDetail';
import {AwsS3CredentialView} from '../../model/AwsS3CredentialView';
import {ActivatedRoute} from '@angular/router';
import {AwsS3CredentialUpdate} from '../../model/AwsS3CredentialUpdate';
import {AwsS3SyncStatusView} from '../../model/AwsS3SyncStatusView';
import {AwsS3Service} from '../../service/aws-s3.service';
import {AwsS3BucketUpdate} from '../../model/AwsS3BucketUpdate';
import {AwsS3Create} from '../../model/AwsS3Create';
import {finalize} from 'rxjs/operators';


@Component({
  selector: 'app-dataset-detail-aws-s3',
  templateUrl: './dataset-detail-aws-s3.component.html',
  styleUrls: ['./dataset-detail-aws-s3.component.css']
})
export class DatasetDetailAwsS3Component implements OnInit {
  public isDummy: boolean = false;
  public isLoading: boolean = false;
  public dataset: DatasetDetail;
  public editAwsS3Form: FormGroup;

  public states = AWS_S3_FORM_STATES;
  public formState: AWS_S3_FORM_STATES = AWS_S3_FORM_STATES.CREATE;

  public credentials: AwsS3CredentialView = undefined;
  public synchronizeState: AwsS3SyncStatusView = undefined;

  public get regions(): string[] {
    return AwsS3FormBuilder.regions();
  }

  constructor(
    private s3: AwsS3Service,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.dataset = this.route.parent.snapshot.data.dataset as DatasetDetail;
    this.credentials = this.route.snapshot.data.credentials as AwsS3CredentialView;
    this.isDummy = DatasetDetail.isDummy(this.dataset);

    if (!!this.credentials) {
      this.formState = this.credentials['id'] === null ? AWS_S3_FORM_STATES.CREATE : AWS_S3_FORM_STATES.BUCKET;
      this.editAwsS3Form = AwsS3FormBuilder.create(this.isDummy, this.credentials, this.formState);
    }
  }

  ngOnDestroy(): void {
  }

  public onSubmit(): void {
    if (this.formState === AWS_S3_FORM_STATES.CREATE) {
      this.onCreate();
    } else if (this.formState === AWS_S3_FORM_STATES.BUCKET) {
      this.onUpdateBucket();
    } else if (this.formState === AWS_S3_FORM_STATES.CREDENTIALS) {
      this.onUpdateCredentials();
    }
  }

  public onReset(): void {
    this.editAwsS3Form = AwsS3FormBuilder.create(this.isDummy, this.credentials, this.formState);
  }

  public switchForm(newState: AWS_S3_FORM_STATES): void {
    this.formState = newState;
    this.editAwsS3Form = AwsS3FormBuilder.create(this.isDummy, this.credentials, this.formState);
  }

  private onCreate(): void {
    const payload = new AwsS3Create();
    payload.name = this.editAwsS3Form.value.name;
    payload.locationPath = this.editAwsS3Form.value.locationPath;
    payload.bucketName = this.editAwsS3Form.value.bucketName;
    payload.bucketRegion = this.editAwsS3Form.value.bucketRegion;
    payload.accessKey = this.editAwsS3Form.value.accessKey;
    payload.secretKey = this.editAwsS3Form.value.secretKey;

    this.isLoading = true;
    this.synchronizeState = undefined;
    this.s3.create(this.dataset.id, payload).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe((response: AwsS3CredentialView) => {
      this.credentials = response;
      this.switchForm(AWS_S3_FORM_STATES.BUCKET);
      this.synchronize();
    });
  }

  private onUpdateBucket(): void {

    const payload = new AwsS3BucketUpdate();
    payload.name = this.editAwsS3Form.value.name;
    payload.locationPath = this.editAwsS3Form.value.locationPath;
    payload.bucketName = this.editAwsS3Form.value.bucketName;
    payload.bucketRegion = this.editAwsS3Form.value.bucketRegion;

    this.isLoading = true;
    this.synchronizeState = undefined;
    this.s3.updateBucket(this.dataset.id, payload).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe((response: AwsS3CredentialView) => {
      this.credentials = response;
      this.switchForm(AWS_S3_FORM_STATES.BUCKET);
      this.synchronize();
    });
  }

  private onUpdateCredentials(): void {

    const payload = new AwsS3CredentialUpdate();
    payload.accessKey = this.editAwsS3Form.value.accessKey;
    payload.secretKey = this.editAwsS3Form.value.secretKey;

    this.isLoading = true;
    this.synchronizeState = undefined;
    this.s3.updateCredentials(this.dataset.id, payload).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe((response: AwsS3CredentialView) => {
      this.credentials = response;
      this.switchForm(AWS_S3_FORM_STATES.CREDENTIALS);
      this.synchronize();
    });
  }

  private synchronize(): void {
    this.isLoading = true;
    this.s3.synchronize(this.dataset.id).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe((response: AwsS3SyncStatusView | undefined) => {
      this.synchronizeState = response;
    });
  }
}
