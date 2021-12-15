import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AwsS3CredentialView} from './AwsS3CredentialView';


export enum AWS_S3_FORM_STATES {
  CREATE, // show all
  CREDENTIALS, // show only the keys
  BUCKET // show only the bucket data
}

/**
 * Create the form group to connect a dataset from aws s3
 * to avoid duplicate code and make sure the validator rules
 * take affect in all used places.
 */
export class AwsS3FormBuilder {

  // Todo: validators?

  public static create(disabled: boolean, credentials: AwsS3CredentialView, formState: AWS_S3_FORM_STATES) : FormGroup {

    const group = {};

    if (formState === AWS_S3_FORM_STATES.CREATE || formState === AWS_S3_FORM_STATES.BUCKET) {
      const name = !!credentials && !!credentials.name ? credentials.name : '';
      const locationPath = !!credentials && !!credentials.locationPath ? credentials.locationPath : '';
      const bucketName = !!credentials && !!credentials.bucketName ? credentials.bucketName : '';
      const bucketRegion = !!credentials && !!credentials.bucketRegion ? credentials.bucketRegion : '';

      group['name'] = new FormControl({value: name, disabled}, [Validators.required]);
      group['bucketName'] = new FormControl({value: bucketName, disabled}, [Validators.required]);
      group['locationPath'] = new FormControl({value: locationPath, disabled}, []);
      group['bucketRegion'] = new FormControl({value: bucketRegion, disabled}, [Validators.required]);
    }
    if (formState === AWS_S3_FORM_STATES.CREATE || formState === AWS_S3_FORM_STATES.CREDENTIALS) {
      const accessKey = !!credentials && !!credentials.accessKey ? credentials.accessKey : '';
      group['accessKey'] = new FormControl({value: accessKey, disabled}, [Validators.required]);
      // The key is never sent back to the frontend. They are always empty.
      group['secretKey'] = new FormControl({value: '', disabled}, [Validators.required]);
    }

    return new FormGroup(group);
  }

  /**
   * Note: the uppercase region keys are used within
   * 'FEATURE.DATASET.DETAILS.AWS_S3.LABEL.REGIONS' as
   * translation key to be translatable.
   *
   * The regions can be found here:
   * https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region
   */
  public static regions(): string[] {
    return [
      'US_EAST_1',
      'US_EAST_2',
      'US_WEST_1',
      'US_WEST_2',
      'AF_SOUTH_1',
      'AP_EAST_1',
      'AP_SOUTH_1',
      'AP_NORTHEAST_1',
      'AP_NORTHEAST_2',
      'AP_NORTHEAST_3',
      'AP_SOUTHEAST_1',
      'AP_SOUTHEAST_2',
      'CA_CENTRAL_1',
      'CN_NORTH_1',
      'CN_NORTHWEST_1',
      'EU_CENTRAL_1',
      'EU_WEST_1',
      'EU_WEST_2',
      'EU_WEST_3',
      'EU_SOUTH_1',
      'EU_NORTH_1',
      'ME_SOUTH_1',
      'SA_EAST_1',
    ];
  }
}
