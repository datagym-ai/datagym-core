import {MediaController} from './MediaController';
import {EntryValueService} from '../entry-value.service';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {ImageMediaProfile} from './profile/ImageMediaProfile';
import {flatNestedGeometries} from '../utils';
import {LcEntryType} from '../../../label-config/model/LcEntryType';
import {LcEntryValue} from '../../model/LcEntryValue';
import {EntryValueApiService} from '../entry-value-api/entry-value-api.service';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';

export class ImageMediaController implements MediaController {

  public readonly profile = new ImageMediaProfile();

  // May move some 'global' shortcuts here?
  public readonly shortcuts: { key: string|string[], callback: () => void, shiftKey?: boolean }[] = [];

  constructor(
    private benchmarkSetMode: boolean,
    private valueService: EntryValueService,
    private readonly entryValueApiService: EntryValueApiService
  ) {}

  public initValues(task: SingleTaskResponseModel, frameNumber: number|undefined): void {
    const entryValues = task.labelIteration.entryValues;
    const geometries = this.cleanupGeometries(entryValues);
    const classifications = entryValues.filter(value => LcEntryType.isClassification(value.lcEntry));

    this.valueService.initValues(task, geometries, classifications, this.benchmarkSetMode);
  }

  public cleanupGeometries(values: LcEntryValue[]): LcEntryGeometryValue[] {

    // filter for geometryValues and update their validity
    const geometryValues = flatNestedGeometries(values);

    // filter 'empty' geometries without coordinates.
    const geometryValuesWithCoordinates = geometryValues.filter(geometry => geometry.hasCoordinates());
    // find & delete the filtered geometries.
    geometryValues
      .filter(geo => !geometryValuesWithCoordinates.includes(geo))
      .forEach(geo => {
        geo.markAsDeleted();
        this.entryValueApiService.deleteValueById(geo.id).subscribe();
      });

    geometryValuesWithCoordinates.forEach(
      geo => geo.children = geo.children.filter(child => LcEntryType.isClassification(child.lcEntry))
    );

    return geometryValuesWithCoordinates;
  }

  public teardown(): void {
  }
}
