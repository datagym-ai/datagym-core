import {BasicDataset} from './BasicDataset';

/**
 * The used api response to display the dataset list view and is also used within
 * the projects package to list the connected datasets.
 */
export class DatasetList extends BasicDataset {

  public mediaCount: number = 0;
  public projectCount: number = 0;

}
