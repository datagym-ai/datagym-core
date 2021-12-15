import {Injectable} from '@angular/core';
import {LabelTaskService} from './label-task.service';

@Injectable({
  providedIn: 'root'
})
export class PreviewGuideService {

  /**
   * For each step another media is set.
   * The base url stays the same, just
   * the media names change.
   */
  private imageNames: string[] = [
    'Dialogue_1.gif',
    'Dialogue_2.gif',
    'Dialogue_3.gif'
  ];

  /**
   * Arrays starts with 0.
   */
  public get step(): number {
    return this.stage + 1;
  }

  public get imageUrl(): string {
    const image = this.imageNames[this.stage];
    return `https://media.datagym.ai/prod/preview-guide/${ image }`;
  }

  public get title(): string {
    return `FEATURE.LABEL_MODE.GUIDE.${ this.step }.TITLE`;
  }

  public get description(): string {
    return `FEATURE.LABEL_MODE.GUIDE.${ this.step }.DESCRIPTION`;
  }

  private show: boolean = true;
  private stage: number = 0;

  /**
   * Display the preview guide only
   * - in the preview mode
   * - it it wasn't closed
   * - and another step/media is available.
   */
  public get display(): boolean {
    return !!this.show
      && this.labelTaskService.previewMode
      && this.stage < this.imageNames.length;
  }

  constructor(private labelTaskService: LabelTaskService) { }

  onClose() {
    this.show = false;
  }

  onNext() {
    this.stage++;
  }
}
