
/**
 * This is the only place, where the preview mode url is defined.
 *
 * This url is defined in two parts:
 * - root is used within AppRoutingModule without the AuthGuard,
 * - path is used within the LabelModeRoutingModule just to not need a third url segment for the id.
 */
export enum PreviewModeUri {
  ROOT = 'preview',
  PATH = 'label-mode',
  /*
   * https://angular.io/api/core/Injectable#providedIn
   * Check also 'root' | 'platform' | 'any' | null
   */
  PROVIDED_IN = 'any',
  /*
   * On leaving the preview mode, go to this url.
   */
  URL2GO = 'https://www.datagym.ai/'
}

export namespace PreviewModeUri {
  /**
   * Also define here a function to access the fully uri.
   */
  export function getUrl() : string {
    return `/${ PreviewModeUri.ROOT }/${ PreviewModeUri.PATH }`;
  }

  export function equals(url2test?: unknown): boolean {

    if (!/* not */!!url2test || typeof url2test !== 'string') {
      return false;
    }

    return url2test === getUrl();
  }

  /**
   * Call this method on leaving the preview mode.
   */
  export function leavePreviewMode() {
    window.location.href = PreviewModeUri.URL2GO;
  }
}
