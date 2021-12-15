import {EventEmitter, Injectable} from '@angular/core';
import {ContextMenuEventType} from '../messaging/ContextMenuEventType';
import {Observable, ReplaySubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {ContextMenuEvent} from '../messaging/ContextMenuEvent';
import {WorkspacePoint} from '../model/WorkspacePoint';


@Injectable({
  providedIn: 'root'
})
export class ContextMenuService {

  // Pass x & y positions in px.
  public onShow: EventEmitter<WorkspacePoint> = new EventEmitter<WorkspacePoint>();
  public onClose: EventEmitter<void> = new EventEmitter<void>();
  public contextMenuEventBus = new ReplaySubject<ContextMenuEvent>(1);

  public onAction(type: ContextMenuEventType): Observable<ContextMenuEvent> {
    return this.contextMenuEventBus.pipe(
      filter((event: ContextMenuEvent) => event.type === type)
    );
  }

  public get visible(): boolean {
    return this._showContextMenu;
  }

  public changeContextMenuToOpen(): void {
    this._showContextMenu = true;
  }

  public get originalMenuVisible(): boolean{
    return this._showOriginalMenu;
  }

  private _showContextMenu: boolean = false;
  private _showOriginalMenu: boolean = true;

  constructor() { }

  public openCommentMenu(){
    this._showOriginalMenu = false;
  }

  public open(position: WorkspacePoint): void {
    this.close(); // close the open context menu to change the position.
    this.onShow.emit(position);
  }

  public close(): void {
    if (this._showContextMenu) {
      this._showContextMenu = false;
      this._showOriginalMenu = true;
      this.onClose.emit();
    }
  }
}
