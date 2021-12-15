import { Injectable } from '@angular/core';
import {LabelModeType} from '../../model/import';
import {EntryValueService} from '../entry-value.service';
import {VideoControlService} from '../video-control.service';
import {MediaController} from './MediaController';
import {ImageMediaController} from './ImageMediaController';
import {VideoMediaController} from './VideoMediaController';
import {DummyMediaController} from './DummyMediaController';
import {LabelTaskService} from '../label-task.service';
import {InterpolationService} from '../interpolation.service';
import {VideoValueService} from '../video-value.service';
import {EntryValueApiService} from '../entry-value-api/entry-value-api.service';


@Injectable({
  providedIn: 'root'
})
export class MediaControllerFactoryService {

  constructor(
    private readonly valueService: EntryValueService,
    private readonly videoService: VideoControlService,
    private readonly labelTaskService: LabelTaskService,
    private readonly interpolationService: InterpolationService,
    private readonly videoValueService: VideoValueService,
    private readonly entryValueApiService: EntryValueApiService
  ) { }

  public createMediaController(type: LabelModeType): MediaController {

    // Do not use the type getter here because it uses the currently set controller.

    let controller: MediaController;
    switch (type) {
      case LabelModeType.IMAGE:
        controller = this.createImageMediaController();
        break;
      case LabelModeType.VIDEO:
        controller = this.createVideoMediaController();
        break;
      default:
        // Just a precaution that the controller is always set.
        controller = this.creatDummyController();
    }

    return controller;
  }

  public creatDummyController(): MediaController {
    return new DummyMediaController();
  }

  public createVideoMediaController(): MediaController {
    return new VideoMediaController(
      this.labelTaskService.benchmarkSetMode,
      this.valueService,
      this.videoService,
      this.videoValueService,
      this.interpolationService,
      this.entryValueApiService
    );
  }

  public createImageMediaController(): MediaController {
    return new ImageMediaController(
      this.labelTaskService.benchmarkSetMode,
      this.valueService,
      this.entryValueApiService
    );
  }
}
