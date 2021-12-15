
/**
 * Let the user only complete it's task when the state is valid.
 */
export interface ValidityObserver {

  globalClassificationsValid: boolean;

  hasGeometries(): boolean;

  hasInvalidGeometries(): boolean;

  /**
   * May some teardown logic should be implemented?
   */
  teardown(): void
}
