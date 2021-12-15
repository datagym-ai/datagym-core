import {ContentType} from "../../basic/media/model/ContentType";

export interface UploadMediaOptionsInterface {
  acceptedTypes?: ContentType[],
  center?: boolean;
  closeable?: boolean,
  errorI18n?: string
}
