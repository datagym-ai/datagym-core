import {ContextMenuEventType} from './ContextMenuEventType';


export class ContextMenuEvent {

  public ids: string[];
  public type: ContextMenuEventType = null;
  public payload ?: any = null;

  constructor(
    ids: string | string[],
    type: ContextMenuEventType,
    payload?: any
  ) {
    // make sure the ids is an array
    this.ids = typeof ids === 'string' ? [ids] : ids;
    this.type = type;
    this.payload = payload;
  }

}
