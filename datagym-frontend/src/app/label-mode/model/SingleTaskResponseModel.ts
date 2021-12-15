import {LabelIteration} from './LabelIteration';
import {LabelConfiguration, LabelModeType, LabelTaskState, Media, PreLabelState} from './import';

/**
 * Response of GET '/api/task/<id>'
 *
 * LabelTaskService.loadTaskById(<id>)
 */
export class SingleTaskResponseModel {

  /**
   * Optional comment from reviewer
   * NOT REQUIRED MAX CHAR 128
   */
  public reviewComment?: string;
  public reviewActivated?: boolean;

  constructor(
    public taskId: string,
    public labelTaskState: LabelTaskState,
    public preLabelState: PreLabelState,
    public projectName: string,
    public labelConfig: LabelConfiguration,
    public labelIteration: LabelIteration,
    /**
     * @deprecated rename to media.
     */
    public media: Media,
    public aiSegLimitReached: boolean,
    public lastChangedConfig: number,
    public projectType: LabelModeType,
    public datasetId: string,
    public datasetName: string
  ) {}

}
