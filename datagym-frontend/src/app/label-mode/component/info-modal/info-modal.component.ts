import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {EntryConfigService} from '../../service/entry-config.service';
import {filter, take, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {ModalTabConfiguration} from '../../model/ModalTabConfiguration';
import {LabelModeType, LocalImage, MediaSourceType, VideoMedia} from '../../model/import';


@Component({
  selector: 'app-info-modal',
  templateUrl: './info-modal.component.html',
  styleUrls: ['./info-modal.component.css']
})
export class InfoModalComponent implements OnInit, OnDestroy {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public readonly modalId: string = 'LabelModeInfoModal';

  public get hasConfiguration(): boolean {
    return this.configurations.length > 0;
  }

  public get config(): ModalTabConfiguration[] {
    return this.selectedTabIndex >= 0 && this.selectedTabIndex < this.configurations.length
      ? this.configurations[this.selectedTabIndex]
      : [];
  }

  public readonly tabs = [
    'FEATURE.LABEL_MODE.INFO_BOX.TAB_TITLE.SYMBOL',
    'FEATURE.LABEL_MODE.INFO_BOX.TAB_TITLE.SHORTCUT',
    'FEATURE.LABEL_MODE.INFO_BOX.TAB_TITLE.MEDIA'
  ];

  public readonly configurations: ModalTabConfiguration[][] = [];

  public selectedTabIndex: number = 1;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private modalService: NgxSmartModalService, private configService: EntryConfigService) {
  }

  ngOnInit() {

    this.configService.configInitDone.pipe(
      // This event is fired twice, the first time b is false
      filter((b: boolean) => !!b),
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.configurations.push(InfoModalComponent.symbolConfig());
      const isVideo = this.configService.mediaType === LabelModeType.VIDEO;
      const shortcuts = InfoModalComponent.shortcutConfig(isVideo);
      this.configurations.push(shortcuts);
      const mediaInformation = InfoModalComponent.mediaConfig(isVideo, this.configService);
      this.configurations.push(mediaInformation);
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  onClose() {
  }

  public openLegend(): void {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(true);
    if (modal.hasData()) {
      modal.open();
    }
  }

  /**
   * If a action is defined for the entry, call that.
   *
   * @param entry
   */
  public callAction(entry: ModalTabConfiguration): void {
    if (!!entry.action) {
      entry.action();
    }
  }

  private static symbolConfig(): ModalTabConfiguration[] {
    return [
      {description: 'FEATURE.LABEL_MODE.LEGEND.LABEL.TITLE'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.LABEL.SHOW', icon: 'fas fa-eye'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.LABEL.HIDE', icon: 'fas fa-eye-slash'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.LABEL.INVALID', icon: 'error-text fas fa-exclamation-triangle'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.LABEL.DELETE_VALUE', icon: 'fas fa-trash'},

      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.TITLE'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.MAGIC', icon: 'fas fa-brain'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.RECTANGLE', icon: 'icon-dg-rectangle'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.BRUSH', icon: 'fas fa-paint-brush'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.POINT', icon: 'fas fa-crosshairs'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.AISEG.POINTS', icon: 'fas fa-map-marker-alt'},

      {description: 'FEATURE.LABEL_MODE.LEGEND.TASK_CONTROL.TITLE'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.TASK_CONTROL.SUBMIT_EXIT', icon: 'fas fa-times'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.TASK_CONTROL.SKIP', icon: 'fas fa-step-forward'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.TASK_CONTROL.SUBMIT_NEXT', icon: 'fas fa-forward'},

      {description: 'FEATURE.LABEL_MODE.LEGEND.REVIEW_CONTROL.TITLE'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.REVIEW_CONTROL.REVIEW_ACCEPT', icon: 'fas fa-thumbs-up'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.REVIEW_CONTROL.REVIEW_DENY', icon: 'fas fa-thumbs-down'},
      {description: 'FEATURE.LABEL_MODE.LEGEND.REVIEW_CONTROL.REVIEW_STOP', icon: 'fas fa-hand-paper'},
    ];
  }

  private static shortcutConfig(videoLabeling: boolean): ModalTabConfiguration[] {
    const workspaceShortCuts = [
      {key: 'ARROW-Keys', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.ARROWS'},
      {key: 'CLICK', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.SELECT_GEOMETRY'},
      {key: 'CTRL+CLICK', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.SELECT_GEOMETRIES'},
      {key: 'ESC', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.UNSELECT_GEOMETRY'},
      {key: 'ESC', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.CANCEL_DRAWING'},

      {key: 'DEL', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.DELETE'},
      {key: 'SHIFT+DRAG', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.MOVE'},
      {key: 'ENTER', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.FINISH'},
      {key: 'DOUBLE-CLICK', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.DELETE_POINTS'},
      {key: 'CTRL ARROW-Left / ARROW-Right', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.VALUE_NAVIGATION'},
    ];

    if (!videoLabeling) {
      return workspaceShortCuts;
    }

    const videoShortCuts = [
      {kbd: 'k', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.K'},
      {kbd: ',', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.COMMA'},
      {kbd: '.', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.DOT'},
      {kbd: 'j', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.J'},
      {kbd: 'l', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.L'},
      {kbd: '>', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.GT'},
      {kbd: '<', description: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.LT'},
    ];

    return [
      {title: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.WORKSPACE.TITLE'},
      ...workspaceShortCuts,
      {title: 'FEATURE.LABEL_MODE.LEGEND.SHORTCUTS.VIDEO.TITLE'},
      ...videoShortCuts,
    ];
  }

  private static mediaConfig(isVideo: boolean, configService: EntryConfigService): ModalTabConfiguration[] {

    const media = configService.media;

    const datasetId = configService.datasetId;
    const datasetName = configService.datasetName;

    const type: MediaSourceType = !!media.mediaSourceType ? media.mediaSourceType : MediaSourceType.UNKNOWN;
    const icon = MediaSourceType.toIcon(type);
    const mediaUploadDate = new Date(media.timestamp).toLocaleDateString();

    const ret = [];

    if (!!datasetId && datasetName) {

      const openDataset = (): void => {
        const datasetUrl = `${window.location.origin}/#/datasets/details/${datasetId}/home`;
        window.open(datasetUrl, '_blank');
      };

      ret.push({key: datasetName, description: 'FEATURE.LABEL_MODE.INFO_BOX.DATASET', action: openDataset})
    }

    ret.push(...[
      {key: media.mediaName, description: 'FEATURE.LABEL_MODE.INFO_BOX.NAME'},
      {key: type, icon, description: 'FEATURE.LABEL_MODE.INFO_BOX.TYPE'},
      {key: mediaUploadDate, description: 'FEATURE.LABEL_MODE.INFO_BOX.DATE'},
    ]);

    switch (media.mediaSourceType) {
      case MediaSourceType.AWS_S3:
        if (isVideo) {
          const video = media as VideoMedia;
          ret.push(...[
            {key: `${video.width} x ${video.height} px`, description: 'FEATURE.LABEL_MODE.INFO_BOX.RESOLUTION'},
            {key: `${video.fps}`, description: 'FEATURE.LABEL_MODE.INFO_BOX.FPS'},
            {key: `${video.totalFrames}`, description: 'FEATURE.LABEL_MODE.INFO_BOX.TOTAL_FRAMES'},
            {key: `${video.duration} s`, description: 'FEATURE.LABEL_MODE.INFO_BOX.DURATION'},
            {key: `${video.codecName}`, description: 'FEATURE.LABEL_MODE.INFO_BOX.CODEC_NAME'},
          ]);
        } else {
          // We don't have access to other relevant info about the media expect 'awsKey' and some error flags.
        }
        break;
      case MediaSourceType.LOCAL:
        const localImage = media as LocalImage;
        ret.push({
          key: `${localImage.width} x ${localImage.height} px`,
          description: 'FEATURE.LABEL_MODE.INFO_BOX.RESOLUTION'
        });
        break;
      case MediaSourceType.SHAREABLE_LINK:
      // No additional information.
      case MediaSourceType.UNKNOWN:
      // No information.
    }
    return ret;
  }
}
