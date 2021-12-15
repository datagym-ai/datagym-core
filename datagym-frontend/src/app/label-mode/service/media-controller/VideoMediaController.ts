import {MediaController} from './MediaController';
import {LcEntryType, VideoMedia} from '../../model/import';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {Subject} from 'rxjs';
import {EntryValueService} from '../entry-value.service';
import {VideoControlService} from '../video-control.service';
import {filter, takeUntil} from 'rxjs/operators';
import {ValueLineFrameFilter} from '../../model/video/ValueLineFrameFilter';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {InterpolationService} from '../interpolation.service';
import {VideoValueService} from '../video-value.service';
import {VideoMediaProfile} from './profile/VideoMediaProfile';
import {flatNestedGeometries} from '../utils';
import {EntryValueApiService} from '../entry-value-api/entry-value-api.service';
import {LcEntryValue} from '../../model/LcEntryValue';
import {ValueStack} from './value-stack/ValueStack';
import {ValueStackFactory} from './value-stack/ValueStackFactory';
import {VideoValidityObserver} from './validity/VideoValidityObserver';


export class VideoMediaController implements MediaController, ValueStackFactory {

  public readonly profile = new VideoMediaProfile();

  public readonly shortcuts: { key: string|string[], callback: () => void, shiftKey?: boolean }[] = [
    {
      // On space, toggle video player.
      key: [' ', 'k'],
      callback: () => this.videoService.isPlaying = !this.videoService.isPlaying
    },
    {
      key: ',', // Jump to previous frame
      callback: () => this.videoService.seekFrame(-1)
    },
    {
      key: 'ArrowLeft', // Jump to previous frame
      shiftKey: true,
      callback: () => this.videoService.seekFrame(-1)
    },
    {
      key: '.', // Jump to next frame
      callback: () => this.videoService.seekFrame(1)
    },
    {
      key: 'ArrowRight', // Jump to next frame
      shiftKey: true,
      callback: () => this.videoService.seekFrame(1)
    },
    {
      key: 'j', // Jump to previous frame
      callback: () => this.videoService.seekFrame(-this.videoService.skipFrameCounter)
    },
    {
      key: 'ArrowDown', // Jump to previous frame
      shiftKey: true,
      callback: () => this.videoService.seekFrame(-this.videoService.skipFrameCounter)
    },
    {
      key: 'l', // Jump to next frame
      callback: () => this.videoService.seekFrame(this.videoService.skipFrameCounter)
    },
    {
      key: 'ArrowUp', // Jump to next frame
      shiftKey: true,
      callback: () => this.videoService.seekFrame(this.videoService.skipFrameCounter)
    },
    {
      key: '>', // speed up the video speed
      callback: () => this.videoService.speedUp()
    },
    {
      key: '<', // slow down the video speed
      callback: () => this.videoService.slowDown()
    }
  ];

  /**
   * A guard to not initialise the same frame twice in a row.
   *
   * @private
   */
  private currentInitiatedFrame: number = undefined;

  private valueLines: LcEntryGeometryValue[] = [];

  private task: SingleTaskResponseModel = undefined;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private readonly benchmarkSetMode: boolean,
    private readonly valueService: EntryValueService,
    private readonly videoService: VideoControlService,
    private readonly videoValueService: VideoValueService,
    private readonly interpolationService: InterpolationService,
    private readonly entryValueApiService: EntryValueApiService
  ) {
    this.videoValueService.init();
  }

  /**
   * Maybe not all geometries are visible within the EntryValueService
   * so check the valueLines stack from here.
   */
  public hasGeometries(): boolean {
    return this.valueLines.length > 0;
  }

  /**
   * Let the entryValueService access all of the geometries not only
   * the ones visible within the current frame.
   */
  public createValueStack(): ValueStack {
    // We don't support media classifications in video labeling mode.
    return new ValueStack(this.valueLines);
  }

  /**
   * Initialise the controller depending on the media mode type
   * and let that controller initialise the value service.
   *
   * @param task
   * @param frameNumber: optional load this frame number in the video labeling mode.
   */
  public initValues(task: SingleTaskResponseModel, frameNumber: number|undefined): void {
    this.task = task;

    // Override the ValidityObserver to use the internal stack from here.
    this.valueService.valueObserver = new VideoValidityObserver(this);

    // Initialise the video control service.
    const videoMedia = task.media as VideoMedia;
    const startFrameNumber = VideoMediaController.extractStartFrame(frameNumber, videoMedia.totalFrames);
    this.videoService.init(videoMedia, startFrameNumber);

    const entryValues = task.labelIteration.entryValues;
    this.valueLines = this.cleanupGeometries(entryValues);

    this.initFrame(this.videoService.currentFrameNumber);

    this.videoService.onFrameChanged.pipe(takeUntil(this.unsubscribe)).subscribe((frameNumber) => {
      this.initFrame(frameNumber);
    });

    this.videoValueService.onUpdateClassification.pipe(takeUntil(this.unsubscribe)).subscribe(classification => {

      const handler = (value: LcEntryValue): void => {
        value.children = value.children.map(child => {
          if (child.id !== classification.id) {
            return child;
          }
          // Don't override the children!
          classification.children = child.children;
          return classification;
        });
        value.children.forEach(handler);
      }

      this.valueLines.forEach(handler);
    });

    this.interpolationService.onInterpolationTypeChanged.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      /*
       * When the interpolation type was changed, the frame number doesn't change.
       * Reset the frame guard to reinitialise the value service and the
       * video value lines.
       */
      const currentFrameNumber = this.currentInitiatedFrame;
      this.currentInitiatedFrame = undefined;
      this.initFrame(currentFrameNumber);
    });

    this.videoValueService.newValue.pipe(
      filter(value => value.lcEntryValueParentId === null),
      filter(value => LcEntryType.isGeometry(value.lcEntry)),
      takeUntil(this.unsubscribe)
    ).subscribe((value: LcEntryGeometryValue) => {
      /*
       * When a new geometry is created the frame number doesn't change.
       * Reset the frame guard to reinitialise the value service and the
       * video value lines.
       */
      this.valueLines.push(value);
      const currentFrameNumber = this.currentInitiatedFrame;
      this.currentInitiatedFrame = undefined;
      this.initFrame(currentFrameNumber);
    });

    /**
     * When deleting a geometry clear also the stack here so the geometry
     * would not appear when entering the next frame. For the current frame
     * the deletion is handled via VideoControlComponent where the event
     * is emitted.
     */
    this.videoValueService.onDeleteValue.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(id => {
      this.valueLines = this.valueLines.filter(line => line.id !== id);
    });

    /**
     * When deleting a keyframe the current frame should be reinitialized to update the geometry position
     * (e.g. interpolation)
     */
    this.videoValueService.onDeleteKeyframe.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.initFrame(this.currentInitiatedFrame, true);
    });

    /**
     * When a geometry gets moved or resized, this callback
     * is fired via:
     * - VideoUpdateHandler::updateGeometry
     * - VideoValueService.onUpdateValue.emit(value); ~90
     * this subscription would
     * - reload the same frame
     * - unselect all of them
     * - delete all of them in workspace
     * - redraw the same values
     *
     * Todo: check what would be broken when this subscription is removed.
     */
    // this.videoValueService.onUpdateValue.pipe(
    //   filter(value => value.lcEntryValueParentId === null),
    //   filter(value => LcEntryType.isGeometry(value.lcEntry)),
    //   takeUntil(this.unsubscribe)
    // ).subscribe(updatedGeometry => {
    //   this.valueLines = this.valueLines.map(geo => geo.id === updatedGeometry.id ? updatedGeometry : geo);
    //   /*
    //    * When a geometry is updated the frame number doesn't change.
    //    * Reset the frame guard to reinitialise the value service and the
    //    * video value lines.
    //    */
    //   const currentFrameNumber = this.currentInitiatedFrame;
    //   this.currentInitiatedFrame = undefined;
    //   this.initFrame(currentFrameNumber);
    // });
  }

  public cleanupGeometries(values: LcEntryValue[]): LcEntryGeometryValue[] {

    // filter for geometryValues and update their validity
    const geometries = flatNestedGeometries(values);

    const empty = geometries
      // Comment the following filter to just delete *all* geometries.
      .filter(geo => !/*not*/!!geo.change || !Array.isArray(geo.change) || geo.change.length === 0);

    empty.forEach(geo2deleted => {
      geo2deleted.markAsDeleted();
      this.entryValueApiService.deleteValueById(geo2deleted.id).subscribe(() => {});
    });

    const ids2delete = empty.map(geo => geo.id);
    return geometries.filter(geo => !ids2delete.includes(geo.id));
  }

  /**
   * The teardown method is defined in both interfaces:
   * - MediaController
   * - ValidityObserver
   *
   * So it would be called twice:
   * - MediaController: MediaControlService.teardown()
   * - ValidityObserver: EntryValueService.reset()
   *
   * (The `MediaControlService.teardown()` method also calls `EntryValueService.reset()`.
   */
  public teardown(): void {
    // Reset the internal state
    this.task = undefined;
    this.valueLines = [];
    this.currentInitiatedFrame = undefined;
    // ... unsubscribe internal ...
    this.unsubscribe.next();
    this.unsubscribe.complete();
    // ... and cleanup
    this.videoService.reset();
    this.videoValueService.teardown();
  }

  /**
   * Lookup in the valueLines stack for *all* values that
   * are visible within the visible range of the video line bars.
   *
   * Initiates also the value service with *all* values that are
   * visible in the current frame. Note: Some of the values may be
   * visible within the value lines but not within the current frame.
   *
   * The visible range is like (frameNumber+- totalFrames / 2)
   *
   * @param frameNumber
   * @private
   */
  private initFrame(frameNumber: number, force: boolean = false): void {

    /**
     * A guard that the same frame is not initialised more than once.
     */
    if (force === false && this.currentInitiatedFrame === frameNumber) {
      return;
    }
    this.currentInitiatedFrame = frameNumber;
    if (this.currentInitiatedFrame === undefined) {
      return;
    }

    const range = this.videoService.feDescription.frameBarRange;
    const totalFrames = this.videoService.totalFrames;
    const lineFilter = new ValueLineFrameFilter(frameNumber, range, totalFrames);
    const currentValueLines = this.valueLines.filter(line => lineFilter.filter(line));
    this.videoValueService.currentValueLines = currentValueLines;
    const geometries = currentValueLines
      .map(l => this.interpolationService.getGeometryForIndex(l, frameNumber))
      .filter(geo => !!geo);

    this.valueService.initValues(
      this.task,
      geometries,
      [],
      this.benchmarkSetMode,
      this
    );
    this.valueService.initSavedGeometriesFromValues();
  }

  /**
   * May the start frame is part of the url path. Extract or use 0 as default.
   * @param target
   * @param max
   * @private
   */
  private static extractStartFrame(target: number|undefined, max: number): number {

    const frameNumber = target !== undefined && typeof target == 'number'
      ? target
      : 0;

    return 0 <= frameNumber && frameNumber <= max
      ? frameNumber
      : 0;
  }
}
