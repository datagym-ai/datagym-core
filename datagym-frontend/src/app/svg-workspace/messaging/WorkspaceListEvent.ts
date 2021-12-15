import { WorkspaceEventType } from './WorkspaceEventType';
import { WorkspaceEvent } from './WorkspaceEvent';

export class WorkspaceListEvent extends WorkspaceEvent {
  public internalIdList: string [];

  constructor(internalIdList: string[], eventType: WorkspaceEventType, payload?: any) {
    super(undefined, eventType, payload);
    this.internalIdList = internalIdList;
  }
}
