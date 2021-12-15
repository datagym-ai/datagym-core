import { Injectable } from '@angular/core';
import {LcEntryChange} from '../model/change/LcEntryChange';
import {Observable} from 'rxjs';
import {EntryChangeApiService} from './entry-change-api/entry-change-api.service';
import {VideoControlService} from './video-control.service';
import {EntryValueService} from './entry-value.service';
import {LcEntryValue} from '../model/LcEntryValue';
import {LcEntryChangeFactory} from '../model/change/LcEntryChangeFactory';
import {map} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class EntryChangeService {

  public valueService: EntryValueService;

  /**
   * @param api
   * @param video
   */
  constructor(private api: EntryChangeApiService, private video: VideoControlService) { }

  public delete(change: string|LcEntryChange): Observable<void> {
    return this.api.delete(change);
  }

  public update(change: LcEntryChange): Observable<LcEntryChange> {
    return this.api.update(change).pipe(map(
      response =>(new LcEntryChangeFactory(response)).fromChangeObject(response)
    ));
  }

  /**
   * Create a new LcEntryChange object.
   *
   * Note: this Method doesn't call the ctr of the change object.
   * Make sure to use the factory or update the id of an already
   * existing change object that the 'kind' identifier exists on
   * that object.
   *
   * @param value
   * @param change
   * @throws Error
   */
  public create(value: LcEntryValue, change: LcEntryChange): Observable<LcEntryChange>;

  /**
   * Create a new LcEntryChange object.
   *
   * Note: this Method doesn't call the ctr of the change object.
   * Make sure to use the factory or update the id of an already
   * existing change object that the 'kind' identifier exists on
   * that object.
   *
   * @param valueId
   * @param rootParentId
   * @param change
   * @throws Error
   */
  public create(valueId: string, rootParentId: string, change: LcEntryChange): Observable<LcEntryChange>;

  /**
   * Generic version of `create()`.
   *
   * @param a LcEntryGeometryValue or valueId as string.
   * @param b rootParentId as string or the change object.
   * @param c the change object if it's not passed via parameter b.
   * @throws Error
   */
  public create(a: string|LcEntryValue, b:string|LcEntryChange, c: LcEntryChange = undefined): Observable<LcEntryChange> {

    if (typeof a === 'string' && typeof b === 'string' && !/*not*/!!c) {
      throw new Error('Invalid arguments. Cannot create a new change object.');
    }

    const valueId = typeof a === 'string' ? a : a.id;
    const rootParentId = typeof b === 'string' ? b : this.valueService.findRootValue(a).id;
    const change = typeof b === 'object' ? b : c;
    // Set here the default handcrafted value if the change object is not passed as object.
    const handCrafted = !!change ? change.handCrafted : true;

    /**
     * Just some precautions if a change object should be created that is not possible.
     */
    if (change.frameNumber < this.video.startFrame) {
      throw new Error(`Invalid arguments: frameNumber '${ change.frameNumber }' is lower than '${ this.video.startFrame }'.`);
    }
    if (change.frameNumber > this.video.totalFrames) {
      throw new Error(`Invalid arguments: frameNumber '${ change.frameNumber }' is higher than '${ this.video.totalFrames }'.`);
    }

    return this.api.create(rootParentId, valueId, change).pipe(map(
      response => (new LcEntryChangeFactory({...response, handCrafted})).fromChangeObject(response)
    ));
  }
}
