export enum InvalidReason {
  INVALID_MIME_TYPE = 'INVALID_MIME_TYPE',
  INVALID_URL = 'INVALID_URL',
  AWS_VIDEO_PARSE_FAIL = 'AWS_VIDEO_PARSE_FAIL'
}


export namespace InvalidReason {

  /**
   * Returns the specific error message translation for a given InvalidReason
   * @param type The specific type
   */
  export function getErrorMessage(type?: InvalidReason): string {

    switch (type) {
      case InvalidReason.INVALID_MIME_TYPE:
        return 'GLOBAL.INVALID_MEDIA.INVALID_MIME_TYPE';
      case InvalidReason.INVALID_URL:
        return 'GLOBAL.INVALID_MEDIA.INVALID_URL';
      case InvalidReason.AWS_VIDEO_PARSE_FAIL:
        return 'GLOBAL.INVALID_MEDIA.AWS_VIDEO_PARSE_FAIL';
      default:
        return '';
    }
  }
}
