import {LcEntryValueCreateBindingModel} from '../../model/LcEntryValueCreateBindingModel';
import {Observable, of} from 'rxjs';
import {LcEntryValueUpdateBindingModel} from '../../model/LcEntryValueUpdateBindingModel';
import {EntryValueApiInterface} from './entry-value-api-interface';
import {EntryConfigService} from '../entry-config.service';
import {LcEntryGeometry} from '../../../label-config/model/geometry/LcEntryGeometry';
import {LcEntryValue} from '../../model/LcEntryValue';
import {UUID} from 'angular2-uuid';
import {LcEntryValueFactory} from '../../model/value-response-factory/LcEntryValueFactory';
import {LcEntryClassification} from '../../../label-config/model/classification/LcEntryClassification';
import {EntryValueService} from '../entry-value.service';
import {LcEntryType} from '../../../label-config/model/LcEntryType';
import {LcEntryRectangleValue} from '../../model/geometry/LcEntryRectangleValue';
import {LcEntryLineValue} from '../../model/geometry/LcEntryLineValue';
import {LcEntryPointValue} from '../../model/geometry/LcEntryPointValue';
import {LcEntryPolyValue} from '../../model/geometry/LcEntryPolyValue';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../../model/classification/LcEntryClassificationValue';

/**
 * Within the preview mode, mock/fake the api requests.
 *
 * This is a implementation detail to support the preview mode.
 *
 * Don't use this class directly. Instead use the entryValueApiInterfaceFactory
 * method to receive the right version of LabelTaskApiInterface.
 */
export class EntryValueApiPreview implements EntryValueApiInterface {

  public valueService: EntryValueService = null;

  constructor(private configService: EntryConfigService) {
  }

  private static getMockData(binding: LcEntryValueCreateBindingModel, required: boolean = false): object {
    return {
      'id': UUID.UUID(),
      'mediaId': binding.mediaId,
      'labelIterationId': binding.iterationId,
      'timestamp': +new Date,
      'labeler': 'demoLabeler',
      'parent': null,
      'valid': !required,
      'points': [],
    };
  }

  public createValuesByGeometry(lcEntryId: string, body: LcEntryValueCreateBindingModel): Observable<LcEntryGeometryValue> {

    const geometry: LcEntryGeometry = this.configService.rootGeometries.find(
      (geo: LcEntryGeometry) => geo.id === lcEntryId
    );

    const obj = EntryValueApiPreview.mockNewGeometryValue(geometry, body) as LcEntryGeometryValue;
    obj.updateValidity();
    return of(obj);
  }

  public deleteValueById(id: string): Observable<void> {
    // nothing to do here :D
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public updateSingleValue(id: string, data: LcEntryValueUpdateBindingModel): Observable<LcEntryValue> {
    // nothing to do here :D
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public getMediaClassificationValues(labelConfigurationId: string, lcEntryValueCreateBindingModel: LcEntryValueCreateBindingModel): Observable<LcEntryClassificationValue[]> {

    const binding = {
      mediaId: this.valueService.mediaId,
      iterationId: this.valueService.labelIterationId,
      labelTaskId: this.valueService.labelTaskId
    };

    const mediaClassifications: LcEntryClassificationValue[] = this.configService.globalClassifications.map(
      (config: LcEntryClassification) => EntryValueApiPreview.mockNewClassificationValue(config, binding)
    );

    return of(mediaClassifications);
  }

  private static mockNewGeometryValue(geo: LcEntryGeometry, binding: LcEntryValueCreateBindingModel): LcEntryValue {

    const mockData = EntryValueApiPreview.getMockData(binding, false);
    const obj = LcEntryValueFactory.castFromObject(mockData, geo);

    obj.children = geo.children.map((entry: LcEntryClassification) => {
      return EntryValueApiPreview.mockNewClassificationValue(entry, binding);
    });

    return obj;
  }

  private static mockNewClassificationValue(classification: LcEntryClassification, binding: LcEntryValueCreateBindingModel): LcEntryClassificationValue {

    const mockData = EntryValueApiPreview.getMockData(binding, classification.required);
    const obj = LcEntryValueFactory.castFromObject(mockData, classification);

    obj.children = classification.children.map((entry: LcEntryClassification) => {
      return EntryValueApiPreview.mockNewClassificationValue(entry, binding);
    });

    return obj as LcEntryClassificationValue;
  }

  /**
   * Change the geometry type of an existing geometry.
   *
   * Note: this will delete all classifications without warning!
   *
   * @param id
   * @param newLcEntryId
   */
  public changeGeometryType(id: string, newLcEntryId: string): Observable<LcEntryGeometryValue> {

    const geometry: LcEntryGeometry = this.configService.rootGeometries.find(
      (geo: LcEntryGeometry) => geo.id === newLcEntryId
    );

    const binding = {
      mediaId: this.valueService.mediaId,
      iterationId: this.valueService.labelIterationId,
      labelTaskId: this.valueService.labelTaskId
    };

    const obj = EntryValueApiPreview.mockNewGeometryValue(geometry, binding) as LcEntryGeometryValue;
    obj.updateValidity();

    /**
     * Copy the geometry properties from the existing one.
     */
    const geometryValue: LcEntryValue = this.valueService.geometries.find(
      (geo: LcEntryGeometryValue) => geo.id === id
    );

    switch (geometry.type) {
      case LcEntryType.RECTANGLE:
        (obj as LcEntryRectangleValue).x = (geometryValue as LcEntryRectangleValue).x;
        (obj as LcEntryRectangleValue).y = (geometryValue as LcEntryRectangleValue).y;
        (obj as LcEntryRectangleValue).height = (geometryValue as LcEntryRectangleValue).height;
        (obj as LcEntryRectangleValue).width = (geometryValue as LcEntryRectangleValue).width;
        break;
      case LcEntryType.LINE:
        (obj as LcEntryLineValue).points = (geometryValue as LcEntryLineValue).points;
        break;
      case LcEntryType.POINT:
        (obj as LcEntryPointValue).x = (geometryValue as LcEntryPointValue).x;
        (obj as LcEntryPointValue).y = (geometryValue as LcEntryPointValue).y;
        break;
      case LcEntryType.POLYGON:
        (obj as LcEntryPolyValue).points = (geometryValue as LcEntryPolyValue).points;
        break;
      default:
        // nothing to do here
        break;
    }

    return of(obj);
  }
}
