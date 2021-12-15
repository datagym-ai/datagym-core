import {MediaUploadStatus} from './MediaUploadStatus';

export class UrlImageUploadViewModel {
  internal_media_ID: string | null;
  external_media_ID: string | null;
  imageUrl: string;
  mediaUploadStatus: MediaUploadStatus;
}
