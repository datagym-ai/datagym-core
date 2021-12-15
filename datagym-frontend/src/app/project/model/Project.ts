import {DatasetList} from '../../dataset/model/DatasetList';
import {OrgDataMin} from '../../client/model/OrgDataMin';
import {MediaType} from './MediaType.enum';


export class Project {
  public id: string;
  public name: string;
  public shortDescription: string;
  public description ?: string;
  public pinned: boolean;
  public datasets ?: DatasetList[];
  public labelConfigurationId ?: string;
  public labelIterationId ?: string;
  public owner: string;
  public exportable: boolean = false;
  public reviewActivated: boolean = false;
  public timestamp: number;
  public mediaType: MediaType;

  // Only for super-admin view
  public orgData: OrgDataMin;

  public static countMedia(project: Project): number {
    const datasets: DatasetList[] = !!project && !!project.datasets && project.datasets.length > 0
      ? project.datasets
      : [];

    return datasets
      .map(d => d.mediaCount)
      .reduce((sum, current) => sum + current, 0);
  }

  public static isDummy(project: string): boolean;
  public static isDummy(project: Project): boolean;
  public static isDummy(project: Project | string): boolean {
    // use the passed project as name if it's of type string,
    // else try to read the project.name or use an empty string.
    const name = typeof project === 'string' ? project : !project ? '' : project.name || '';
    if (!name) {
      return false;
    }

    const identifier: string[] = ['Dummy_Project', 'Intersections'];
    return identifier.includes(name);
  }
}
