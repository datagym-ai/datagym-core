import {PreLabelMapping} from './PreLabelMapping';
import {PreLabelGeometry} from './PreLabelGeometry';

export class PreLabelInfoViewModel {
  activePreLabeling: boolean;
  countReadyTasks: number;
  countWaitingTasks: number;
  countFinishedTasks: number;
  countFailedTasks: number;
  aiSegLimit: number;
  aiSegRemaining: number;
  availableGeometries: PreLabelGeometry[];
  availableNetworkClasses: Object;
  preLabelMappings: PreLabelMapping[];
}
