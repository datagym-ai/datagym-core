import {WorkspaceEventType} from '../messaging/WorkspaceEventType';

/**
 * Generic interface to filter the workspace events.
 */
export interface WorkspaceEventFilter {

  /**
   * This method is used like an array filter.
   * Takes the workspace event type that should be filtered.
   *
   * @return true if the event type matches the criteria.
   *
   * @param type
   */
  match(type: WorkspaceEventType): boolean;
}
