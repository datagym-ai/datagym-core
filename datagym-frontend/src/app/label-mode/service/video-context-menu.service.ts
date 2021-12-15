import {EventEmitter, Injectable} from '@angular/core';
import {ContextMenuConfig} from '../model/video/ContextMenuConfig';


@Injectable({
  providedIn: 'root'
})
export class VideoContextMenuService {

  public readonly onOpen: EventEmitter<ContextMenuConfig> = new EventEmitter<ContextMenuConfig>();
  public readonly onDeleteValue: EventEmitter<string> = new EventEmitter<string>();
  public readonly onDeleteKeyFrame: EventEmitter<[string, number]> = new EventEmitter<[string, number]>();

  /**
   * Expand:
   * - the value id to expand,
   * - the `chunkOffset` to find the existing change object.
   * - direction, left = true, right = false;
   */
  public readonly onExpand: EventEmitter<[string, number, boolean]> = new EventEmitter<[string, number, boolean]>();

  constructor() { }

  public open(config: ContextMenuConfig): void {
    this.onOpen.emit(config);
  }

  public close(): void {
    this.onOpen.emit(null);
  }
}
