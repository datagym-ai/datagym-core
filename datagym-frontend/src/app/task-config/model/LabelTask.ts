import {LabelTaskState} from './LabelTaskState';
import {PreLabelState} from './PreLabelState';

export class LabelTask {
  public taskId: string;
  public projectId: string;
  public projectName: string;
  public labelTaskState: LabelTaskState;
  public mediaId: string;
  public mediaName: string;
  public labeler: string | null;
  public iterationId: string;
  public iterationRun: number;
  public preLabelState: PreLabelState;
  public benchmark?: boolean;
  public hasJsonUpload?: boolean;
}
