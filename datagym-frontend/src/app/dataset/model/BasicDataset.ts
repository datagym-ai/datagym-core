import { DatasetDetail } from './DatasetDetail';
import { DatasetList } from './DatasetList';
import { OrgDataMin } from '../../client/model/OrgDataMin';
import { MediaType } from '../../project/model/MediaType.enum';

/**
 * Parent of
 * - DatasetDetail
 * - DatasetList
 */
export abstract class BasicDataset {

  public id: string;
  public name: string;
  public timestamp: number;
  public shortDescription?: string;
  public owner: string;
  // optional: remove these properties from api response:
  public deleted: boolean;
  public deleteTime: number | null = null;
  // Only for super-admin view
  public orgData: OrgDataMin;
  public mediaType: MediaType;

  public static isDummy(dataset: string): boolean;
  public static isDummy(dataset: DatasetList): boolean;
  public static isDummy(dataset: DatasetDetail): boolean;
  public static isDummy(dataset: BasicDataset | string): boolean {
    if (!dataset) {
      return false;
    }

    const name: string = typeof dataset === 'string' ? dataset : dataset.name;
    return BasicDataset.isDummyByName(name);
  }

  public static isDummyByName(name: string): boolean {
    const identifier: string[] = ['Dummy_Dataset_One', 'Dummy_Dataset_Two'];
    return identifier.includes(name);
  }

}
