import {MediaSourceType} from '../../basic/media/model/MediaSourceType';
import {UploadMediaOptionsInterface} from "./UploadMediaOptionsInterface";

export class UploadModalConfiguration {

  public options: UploadMediaOptionsInterface = {};

  private constructor(
    public uploadType: MediaSourceType,
    public datasetId: string,
    defaultOptions: UploadMediaOptionsInterface,
    options ?: UploadMediaOptionsInterface,
  ) {
    // make sure options is defined
    options = !!options ? options : {};

    // merge default values with options, options may overrides default values.
    this.options = {...defaultOptions, ...options};
  }

  public static LOCAL_IMAGES(datasetId: string, options ?: UploadMediaOptionsInterface): UploadModalConfiguration {

    // set some default values
    const defaultOptions = {
      center: true
    };

    return new UploadModalConfiguration(
      MediaSourceType.LOCAL, datasetId, defaultOptions, options
    );
  }

  public static SHAREABLE_LINK(datasetId: string, options ?: UploadMediaOptionsInterface): UploadModalConfiguration {

    const defaultOptions = {};

    return new UploadModalConfiguration(
      MediaSourceType.SHAREABLE_LINK, datasetId, defaultOptions, options
    );
  }

  public static AWS_S3(datasetId: string, options ?: UploadMediaOptionsInterface): UploadModalConfiguration {

    const defaultOptions = {
      closeable: false
    };

    return new UploadModalConfiguration(
      MediaSourceType.AWS_S3, datasetId, defaultOptions, options
    );
  }

}
