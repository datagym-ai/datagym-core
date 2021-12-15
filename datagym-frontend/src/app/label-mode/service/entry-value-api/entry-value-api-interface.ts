import {LcEntryValueCreateBindingModel} from '../../model/LcEntryValueCreateBindingModel';
import {Observable} from 'rxjs';
import {LcEntryValueUpdateBindingModel} from '../../model/LcEntryValueUpdateBindingModel';
import {EntryValueService} from '../entry-value.service';
import {LcEntryValue} from '../../model/LcEntryValue';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../../model/classification/LcEntryClassificationValue';

/**
 * All LabelTaskApiService implementations returned from the 'entryValueApiInterfaceFactory'
 * must implement this interface. It's only here to force all implementations to implement
 * all methods.
 */
export interface EntryValueApiInterface {

  /**
   * Force to set an back reference
   */
  valueService: EntryValueService;

  /**
   * @param labelConfigurationId
   * @param lcEntryValueCreateBindingModel
   */
  getMediaClassificationValues(labelConfigurationId: string, lcEntryValueCreateBindingModel: LcEntryValueCreateBindingModel): Observable<LcEntryClassificationValue[]>;

  createValuesByGeometry(lcEntryId: string, body: LcEntryValueCreateBindingModel): Observable<LcEntryGeometryValue>

  deleteValueById(id: string): Observable<void>;

  updateSingleValue(id: string, data: LcEntryValueUpdateBindingModel): Observable<LcEntryValue>;

  /**
   * Change the geometry type of an existing geometry.
   *
   * Note: this will delete all classifications without warning!
   *
   * @param id
   * @param newLcEntryId
   */
  changeGeometryType(id: string, newLcEntryId: string): Observable<LcEntryGeometryValue>;
}
