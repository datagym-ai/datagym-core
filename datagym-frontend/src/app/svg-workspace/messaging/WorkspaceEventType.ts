import {WorkspaceEvent} from './WorkspaceEvent';

export enum WorkspaceEventType {
  // Payload: boolean to trigger autodraw-next
  DRAW_FINISHED,
  DATA_UPDATED,
  // Sends a WorkspaceListEvent
  DELETE_REQUEST,
  // Gets fired when user presses escape while drawing
  CANCELED,
  SELECTED,
  UNSELECTED,
  // Gets fired if the workspace and ALL PLUGINS are loaded
  WORKSPACE_INITIALIZED,
  // Gets fired after the svg-layer is created an the media is loaded
  WORKSPACE_READY,
  // Gets fired within the media segmentation geometry when some
  // media segments are merged and on of the value objects is obsolete.
  // Use this event with caution, the user will not be notified.
  DELETE_WITHOUT_REQUEST,
  // Gets fired within the media segmentation geometry when some
  // media segments are split up. The existing entry value gets one of the
  // new points but for each new segment a new entry value must be created.
  CREATE_SEGMENT_GEOMETRY,
  // Any event within the workspace that should close the video value line context menu.
  // Use this event to remove module dependencies. Don't include the VideoContextMenuService.
  CLOSE_VIDEO_CONTEXT_MENU,
  // Gets fired if the aiseg-process gets canceled
  AISEG_CANCELED
}

export namespace WorkspaceEventType {

  /**
   * For development purposes to display the event with type as name and the used id if available.
   *
   * @param event WorkspaceEvent | WorkspaceEventType
   */
  export function toString(event: WorkspaceEvent): string;
  export function toString(event: WorkspaceEventType): string;
  export function toString(event: WorkspaceEvent | WorkspaceEventType): string {

    const type: WorkspaceEventType = event instanceof WorkspaceEvent ? event.eventType : event;

    const name: string = WorkspaceEventType.getName(type);
    const id: string = event instanceof WorkspaceEvent ? `, ${ event.internalId }` : '';

    return name + id;
  }

  /**
   * For development purposes to display the event type as name.
   *
   * @param event WorkspaceEvent
   */
  export function getName(event: WorkspaceEvent): string;
  /**
   * For development purposes to display the event type as name.
   *
   * @param event WorkspaceEventType
   */
  export function getName(event: WorkspaceEventType): string;
  /**
   * For development purposes to display the event type as name.
   *
   * @param event WorkspaceEvent | WorkspaceEventType
   */
  export function getName(event: WorkspaceEvent | WorkspaceEventType): string {

    const type = event instanceof WorkspaceEvent ? event.eventType : event;

    switch (type) {
      case WorkspaceEventType.CANCELED:
        return 'CANCELED';
      case WorkspaceEventType.DATA_UPDATED:
        return 'DATA_UPDATED';
      case WorkspaceEventType.DELETE_REQUEST:
        return 'DELETE_REQUEST';
      case WorkspaceEventType.DRAW_FINISHED:
        return 'DRAW_FINISHED';
      case WorkspaceEventType.SELECTED:
        return 'SELECTED';
      case WorkspaceEventType.UNSELECTED:
        return 'UNSELECTED';
      case WorkspaceEventType.WORKSPACE_INITIALIZED:
        return 'WORKSPACE_INITIALIZED';
      case WorkspaceEventType.WORKSPACE_READY:
        return 'WORKSPACE_READY';
      case WorkspaceEventType.DELETE_WITHOUT_REQUEST:
        return 'DELETE_WITHOUT_REQUEST';
      case WorkspaceEventType.CREATE_SEGMENT_GEOMETRY:
        return 'CREATE_SEGMENT_GEOMETRY';
      case WorkspaceEventType.CLOSE_VIDEO_CONTEXT_MENU:
        return 'CLOSE_VIDEO_CONTEXT_MENU';
    }
    return '';
  }
}
