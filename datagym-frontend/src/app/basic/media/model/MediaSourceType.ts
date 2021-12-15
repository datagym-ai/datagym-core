
export enum MediaSourceType {
  LOCAL = 'LOCAL',
  SHAREABLE_LINK = 'SHAREABLE_LINK',
  AWS_S3 = 'AWS_S3',

  // unknown state is for internal usage in frontend only.
  UNKNOWN = 'UNKNOWN'
}

export namespace MediaSourceType {

  export function toIcon(type: MediaSourceType): string;
  export function toIcon(type: MediaSourceType = undefined): string {

    type = !!type ? type : MediaSourceType.UNKNOWN;

    switch (type) {
      case MediaSourceType.AWS_S3:
        return 'fab fa-aws';
      case MediaSourceType.LOCAL:
        return 'fas fa-image';
      case MediaSourceType.SHAREABLE_LINK:
        return 'fas fa-external-link-alt';
      case MediaSourceType.UNKNOWN:
      // fall through
      default:
        return 'fas fa-question';
    }
  }

  export function toName(type: MediaSourceType): string;
  export function toName(type: MediaSourceType = undefined): string {

    type = !!type ? type : MediaSourceType.UNKNOWN;

    return type.toString();
  }

}
