import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {Subject} from 'rxjs';
import {filter, takeUntil} from 'rxjs/operators';
import {VideoLineSVG} from '../../model/video/VideoLineSVG';
import {VideoBusEvent} from '../../model/video/VideoBusEvent';
import {EntryValueService} from '../../service/entry-value.service';
import {VideoControlService} from '../../service/video-control.service';
import {LabelModeUtilityService} from '../../service/label-mode-utility.service';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../model/change/LcEntryChange';
import {Point} from '../../model/geometry/Point';
import {VideoContextMenuService} from '../../service/video-context-menu.service';
import {InterpolationService} from '../../service/interpolation.service';
import {VideoValueService} from '../../service/video-value.service';
import {FrameNumberDescription} from '../../model/video/FrameNumberDescription';
import {FrameBarDescription} from '../../model/video/FrameBarDescription';
import {VideoControlButton} from '../../model/video/VideoControlButton';
import {VideoControlDescription} from '../../model/video/VideoControlDescription';
import {ContextMenuConfig} from '../../model/video/ContextMenuConfig';


@Component({
  selector: 'app-video-control',
  templateUrl: './video-control.component.html',
  styleUrls: ['./video-control.component.css'],
  /**
   * Note: The frameLabels will not be updated with
   * changeDetection: ChangeDetectionStrategy.OnPush.
   * I don't understand why, the array reference will be changed
   * every time the frame changes. :(
   */
  // changeDetection: ChangeDetectionStrategy.OnPush
})
export class VideoControlComponent implements OnInit {

  /**
   * For the layout.
   *
   * On mouse over a number, mark the frame bar.
   */
  public hoveredNumberOffset: number = undefined;

  public frameBars: FrameBarDescription[] = [];
  public frameLabels: FrameNumberDescription[] = [];
  public fastBackward: VideoControlButton = VideoControlButton.FastBackward();
  public backward: VideoControlButton = VideoControlButton.Backward();
  public forward: VideoControlButton = VideoControlButton.Forward();
  public fastForward: VideoControlButton = VideoControlButton.FastForward();

  /**
   * This reference is only used to set the elements height
   * depending on the number of video value lines.
   *
   * @private
   */
  @ViewChild('frameBox')
  private readonly frameBox: ElementRef;

  @ViewChild('svgLineLayer')
  private readonly svgLineLayer: ElementRef;

  private videoValueLines: VideoLineSVG[] = [];

  /**
   * The updateAllPositions could be called multiple times for the
   * same offset. This is a small guard to update the ui only once
   * per frame.
   * @private
   */
  private updateAllPositionsLastOffset: number = undefined;

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  private readonly feDescription: VideoControlDescription = undefined;
  private readonly videoCommunicationBus: Subject<VideoBusEvent> = new Subject<VideoBusEvent>();

  constructor(
    private valueService: EntryValueService,
    private labelMode: LabelModeUtilityService,
    public videoController: VideoControlService,
    private contextMenu: VideoContextMenuService,
    private videoValueService: VideoValueService,
    private interpolationService: InterpolationService,
    private labelModeUtilityService: LabelModeUtilityService
  ) {
    this.feDescription = videoController.feDescription;

    this.updateButtons(0);
    this.updateFrameBars(0);
    this.updateFrameLabels(0);

    const totalFrames = this.videoController.totalFrames;
    this.fastBackward = VideoControlButton.FastBackward(totalFrames, 0);
    this.backward = VideoControlButton.Backward(totalFrames, 0);
    this.forward = VideoControlButton.Forward(totalFrames, 0);
    this.fastForward = VideoControlButton.FastForward(totalFrames, 0);
  }

  ngOnInit(): void {
    this.onHiddenStateChanged();

    const currentFrameNumber = this.videoController.currentFrameNumber;
    this.updateButtons(currentFrameNumber);
    this.updateFrameBars(currentFrameNumber);
    this.updateFrameLabels(currentFrameNumber);

    this.onDeleteValue();
    this.onDeletedValue();
    this.onDeleteKeyFrame();
    this.onExpand();
  }

  ngOnDestroy(): void {
    this.videoCommunicationBus.next();
    this.videoCommunicationBus.complete();

    /**
     * teardown all rectangles.
     */
    this.videoValueLines.forEach(rectangle => rectangle.teardown());
    this.videoValueLines = [];

    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  ngAfterViewInit(): void {

    // this.videoController.onFrameChanged.pipe(
    //   takeUntil(this.unsubscribe)
    // ).subscribe((currentFrameNumber: number) => {
    //   // Do not relay on the valuesLoaded$ event it seems to be sometimes not emitted.
    //     //   this.updateButtons(currentFrameNumber);
    //     //   this.updateFrameBars(currentFrameNumber);
    //     //   this.updateFrameLabels(currentFrameNumber);
    //     //   this.updateAllPositions(currentFrameNumber);
    // });

    this.onUpdateValue();
    this.onContextmenu();
    this.onSelectValue();
    this.onValuesLoaded();
  }

  @HostListener('contextmenu', ['$event'])
  private onRightClick(event): void {
    // Context menu is only available while clicking on a video value line.
    event.preventDefault();
  }


  private onUpdateValue(): void {
    this.videoValueService.onUpdateValue.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(geo => {
      const frameByOffset = this.frameByOffset();
      this.videoValueLines
        .filter(line => line.id === geo.id)
        .forEach((rectangle => {
          rectangle.redraw(geo, frameByOffset);
        }));
    });
  }

  private onContextmenu(): void {
    this.videoCommunicationBus.pipe(
      filter(event => !!event),
      filter(event => event.command === 'contextmenu'),
      takeUntil(this.unsubscribe)
    ).subscribe((event: VideoBusEvent) => {

      const config = event.payload as ContextMenuConfig;

      const contextMenuWidth = 250; // px
      const topOffset = this.frameBox.nativeElement.offsetTop;
      const offsetLeft = this.frameBox.nativeElement.offsetLeft;
      // max position at right.
      const maxX = this.frameBox.nativeElement.clientWidth + 2 * offsetLeft - contextMenuWidth;

      const targetPosition = new Point(
        Math.min(maxX, config.position.x + offsetLeft),
        config.position.y + topOffset
      );
      this.contextMenu.open(config.withOptions({endFrame: this.videoController.totalFrames}).moveTo(targetPosition));
    });
  }

  private onSelectValue(): void {
    this.videoCommunicationBus.pipe(
      filter(event => !!event),
      filter(event => event.command === 'select'),
      takeUntil(this.unsubscribe)
    ).subscribe((select: VideoBusEvent) => {

      this.handleSelection(select.valueId);
    });
  }

  private onValuesLoaded(): void {
    this.valueService.valuesLoaded$.pipe(
      filter(loaded => !!loaded),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.updateUI();

      const currentIds = this.videoValueService.currentValueLines.map(l => l.id);
      // Tear down rectangles to be removed
      this.videoValueLines
        .filter(videoValueLine => !currentIds.includes(videoValueLine.id))
        .forEach(videoValueLine => videoValueLine.teardown());

      // Filter out rectangles moved out of the view window.
      this.videoValueLines = this.videoValueLines
        .filter(line => currentIds.includes(line.id));

      this.updateAllPositions();

      const appliedIds = this.videoValueLines.map(rectangle => rectangle.id);
      const linesToCreate = this.videoValueService.currentValueLines
        .filter(line => !appliedIds.includes(line.id));

      const offset = this.frameByOffset();
      const indexOffset = this.videoValueLines.length;
      linesToCreate.forEach((geometry, index) => {
        index += indexOffset;

        const rectangle = new VideoLineSVG(geometry, index, this.feDescription.frameWidthPercentage, this.videoCommunicationBus);
        rectangle.moveTo(new Point(offset, index));
        this.videoValueLines.push(rectangle);
        this.svgLineLayer.nativeElement.appendChild(rectangle.node);
      });

      this.frameBox.nativeElement.style.height = VideoLineSVG.calculateLineYOffset(this.videoValueLines.length);
    });
  }

  private onHiddenStateChanged(): void {
    this.labelMode.onHiddenStateChanged.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.videoValueLines.forEach(videoValueLine => {
        this.labelMode.isValueHidden(videoValueLine.id)
          ? videoValueLine.hide()
          : videoValueLine.show();
      });
    });
  }


  /**
   * On delete a keyframe:
   */
  private onDeleteKeyFrame(): void {
    this.contextMenu.onDeleteKeyFrame.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(([valueId, frameNumber]) => {
      const line = this.videoValueLines.find(line => line.id === valueId);
      if (!/*not*/!!line) {
        // should not be possible.
        return;
      }
      const value = line.value;
      const change = value.change.find(c => c.frameNumber === frameNumber);
      if (!/*not*/!!change) {
        // should not be possible.
        return;
      }

      this.valueService.deleteChange(
        value as LcEntryGeometryValue,
        change as LcEntryChange
      );
    });
  }

  /**
   * Delete the full value line via context menu.
   * Jobs to be done:
   * - delete the geometry via api.
   * - remove the value line from the stack.
   * - update the remaining stack to 'fill the gap'.
   */
  private onDeleteValue(): void {
    this.contextMenu.onDeleteValue.pipe(takeUntil(this.unsubscribe)).subscribe(id => {
      const deleteConfig = {deleteGeometry: true};
      // Don't look within the videoValueService. The value may be not registered there.
      this.videoValueLines
        .filter(videoValueLine => videoValueLine.id === id)
        .forEach(videoValueLine => {
          this.valueService.deleteGeometryValue(videoValueLine.value, deleteConfig);
        });
    });
  }

  /**
   * When a value is actually deleted.
   * @private
   */
  private onDeletedValue(): void {
    this.videoValueService.onDeleteValue.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((id: string) => {
      this.videoValueLines
        .filter(videoValueLine => videoValueLine.id === id)
        .forEach(videoValueLine => videoValueLine.teardown());
      this.videoValueLines = this.videoValueLines
        .filter(videoValueLine => videoValueLine.id !== id);
      this.videoValueLines.forEach((line, index) => line.y = index);
    });
  }

  private onExpand(): void {
    this.contextMenu.onExpand.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(([id, chunkOffset, left]) => {
      const valueLine = this.videoValueLines.find(l => l.id === id);
      if (!/*not*/!!valueLine) {
        // should not be possible.
        return;
      }

      const chunk = valueLine.chunks[chunkOffset];

      const changeFrameNumber = left
        ? Math.min(...chunk)
        : Math.max(...chunk);

      this.valueService.expandVideoValueLine(valueLine.value, changeFrameNumber, left);
    });
  }

  /**
   * Update the buttons, frame bars, frame bar labels and reposition all drawn value lines.
   * Use the current frame number given from the VideoControlService.
   * @private
   */
  private updateUI(): void {
    const currentFrameNumber = this.videoController.currentFrameNumber;

    this.updateButtons(currentFrameNumber);
    this.updateFrameBars(currentFrameNumber);
    this.updateFrameLabels(currentFrameNumber);
    this.updateAllPositions(currentFrameNumber);
  }

  /**
   * Unselect all geometries within the workspace and select
   * the current value if it wasn't selected before.
   *
   * @param valueId
   * @private
   */
  private handleSelection(valueId: string): void {
    const isSelected = this.labelModeUtilityService.selectedEntryValueIds.includes(valueId);
    this.labelModeUtilityService.unselectAllGeometries();
    if (!isSelected) {
      this.labelModeUtilityService.selectGeometryToEditById(valueId);
    }
  }

  /**
   * Update all positions depending on the current currentFrameNumber
   * calculated within the VideoControlService.
   * @private
   */
  private updateAllPositions(): void;

  /**
   * Update all positions depending on the given current frameNumber.
   * @private
   * @param currentFrameNumber
   * @private
   */
  private updateAllPositions(currentFrameNumber: number): void;

  /**
   * Implementation of the above definitions.
   * @private
   */
  private updateAllPositions(currentFrameNumber: number = undefined): void {
    const offset = this.frameByOffset(currentFrameNumber);
    if (offset === this.updateAllPositionsLastOffset) {
      // Just a guard to not waste some cpu cycles.
      return;
    }
    this.updateAllPositionsLastOffset = offset;
    this.videoValueLines.forEach((videoValueLine, index) => {
      videoValueLine.moveTo(new Point(offset, index));
    });
  }

  private updateFrameLabels(currentFrameNumber: number): void {
    this.frameLabels = this.feDescription.framesNumbers.map(number =>
      new FrameNumberDescription(number, this.feDescription.framesBarsOffset, currentFrameNumber, this.videoController.totalFrames)
    );
  }

  private updateFrameBars(currentFrameNumber): void {
    this.frameBars = this.feDescription.framesBars.map(bar =>
      new FrameBarDescription(bar, currentFrameNumber, this.feDescription.framesBarsOffset, this.videoController.totalFrames)
    );
  }

  private updateButtons(currentFrameNumber: number): void {
    this.fastBackward = this.fastBackward.forFrame(currentFrameNumber);
    this.backward = this.backward.forFrame(currentFrameNumber);
    this.forward = this.forward.forFrame(currentFrameNumber);
    this.fastForward = this.fastForward.forFrame(currentFrameNumber);
  }

  /**
   * Utility to calculate the offset for the current frame
   * depending on the `framesBarsOffset`.
   * @private
   */
  private frameByOffset(): number;

  /**
   * Utility to calculate the offset for the given frame
   * depending on the `framesBarsOffset`.
   * @private
   */
  private frameByOffset(currentFrameNumber: number): number;

  /**
   * Implementation of the above definitions.
   * @param currentFrameNumber
   * @private
   */
  private frameByOffset(currentFrameNumber: number = undefined): number {
    const frameNumber: number = currentFrameNumber === undefined
      ? this.videoController.currentFrameNumber
      : currentFrameNumber;

    const offset: number = this.feDescription.framesBarsOffset;
    return frameNumber + offset;
  }
}
