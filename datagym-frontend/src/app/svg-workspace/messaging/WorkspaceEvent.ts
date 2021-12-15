import { WorkspaceEventType } from './WorkspaceEventType';

export class WorkspaceEvent {
  public internalId: string;
  public eventType: WorkspaceEventType;
  public payload: any;

  constructor(internalId: string, eventType: WorkspaceEventType, payload?: any) {
    this.internalId = internalId;
    this.eventType = eventType;
    this.payload = payload;
  }
}
