import { Injectable } from '@angular/core';
import {LabelModeType} from '../model/import';
import {SingleTaskResponseModel} from '../model/SingleTaskResponseModel';
import {EntryValueService} from './entry-value.service';
import {BehaviorSubject} from 'rxjs';
import {EntryConfigService} from './entry-config.service';
import {MediaController} from './media-controller/MediaController';
import {MediaControllerFactoryService} from './media-controller/media-controller-factory.service';
import {ValidityObserver} from './media-controller/validity/ValidityObserver';

/**
 * This is just a 'proxy' service using a private controller instance.
 * The used controller instances depends on the current media type.
 */
@Injectable({
  providedIn: 'root'
})
export class MediaControlService {

  public get labelModeHeight(): string {
    const percentage = 100;
    return `${ percentage - this.controller.profile.videoPanelHeight }%`;
  }

  public get videoPanelHeight(): string {
    return `${ this.controller.profile.videoPanelHeight }%`;
  }

  public get type(): LabelModeType {
    return this.controller.profile.mediaType;
  }

  public get showValueList(): boolean {
    return this.controller.profile.showValueList;
  }

  public get showVideoPanel(): boolean {
    return this.controller.profile.showVideoPanel;
  }

  public get allowClassificationBar(): boolean {
    return this.controller.profile.allowClassificationBar;
  }

  get projectId(): string {
    return this.configService.projectId;
  }

  get hasGlobalClassifications(): boolean {
    return this.configService.globalClassifications.length > 0;
  }

  get hasRequiredGlobalClassifications(): boolean {
    return this.configService.hasRequiredGlobalClassifications;
  }

  public get valuesLoaded$(): BehaviorSubject<boolean> {
    return this.valueService.valuesLoaded$;
  }

  private task: SingleTaskResponseModel = undefined;

  // Just a precaution that the controller is always set.
  private controller: MediaController = this.factory.creatDummyController();

  constructor(
    private valueService: EntryValueService,
    private configService: EntryConfigService,
    private factory: MediaControllerFactoryService,
  ) {}

  /**
   * Initialise `EntryConfigService` & `EntryValueService` depending on the project type.
   * @param task
   * @param frameNumber: optional load this frame number in the video labeling mode.
   */
  public init(task: SingleTaskResponseModel, frameNumber: number|undefined): void {
    this.task = task;
    // Do not use the type getter here because it uses the currently set controller.
    this.controller = this.factory.createMediaController(this.task.projectType);
    this.configService.initConfiguration(task);
    this.controller.initValues(this.task, frameNumber);
  }

  public teardown(): void {
    this.configService.reset();
    this.valueService.reset();
    this.controller.teardown();
  }

  public getValidityObserver(): ValidityObserver {
    return this.valueService.valueObserver;
  }

  public getShortCuts(): { key: string|string[], callback: () => void, shiftKey?: boolean }[] {
    return this.controller.shortcuts;
  }
}
