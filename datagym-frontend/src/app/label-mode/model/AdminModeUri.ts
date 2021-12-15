

/**
 * This is the only place, where the preview mode url is defined.
 *
 * This url is defined in two parts:
 * - root is used within AppRoutingModule without the AuthGuard,
 * - path is used within the LabelModeRoutingModule just to not need a third url segment for the id.
 */
export enum AdminModeUri {
  ROOT = 'admin',
}

export namespace AdminModeUri {

  /**
   * Also define here a function to access the fully uri.
   */
  export function getUrl() : string {
    return `/${ AdminModeUri.ROOT }/`;
  }

  export function isAdminUrl(url2test?: unknown): boolean {

    if (!/* not */!!url2test || typeof url2test !== 'string') {
      return false;
    }

    return url2test.startsWith(getUrl());
  }
}
