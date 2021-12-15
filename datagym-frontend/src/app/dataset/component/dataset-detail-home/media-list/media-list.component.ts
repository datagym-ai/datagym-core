import {Component, ElementRef, Input, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {AppButtonInput} from '../../../../shared/button/button.component';
import {DialogueModel} from '../../../../shared/dialogue-modal/DialogueModel';
import {Subject, Subscription} from 'rxjs';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {Media} from '../../../../basic/media/model/Media';
import {MediaListItemComponent} from './media-list-item/media-list-item.component';
import {DatasetService} from '../../../service/dataset.service';
import {take, takeUntil} from 'rxjs/operators';
import {DatasetFilterAndPageParam} from '../../../model/DatasetFilterAndPageParam';
import {FormGroup} from '@angular/forms';
import {FilterBase} from '../../../../shared/dynamic-filter/FilterBase';
import {TranslateService} from '@ngx-translate/core';
import {FilterTextbox} from '../../../../shared/dynamic-filter/FilterTextbox';
import {FilterDropdown} from '../../../../shared/dynamic-filter/FilterDropdown';
import {MediaSourceType} from '../../../../basic/media/model/MediaSourceType';
import {MediaService} from '../../../service/media.service';


const DEFAULT_ITEMS: number = 50;

@Component({
  selector: 'app-media-list',
  templateUrl: './media-list.component.html',
  styleUrls: ['./media-list.component.css']
})
export class MediaListComponent implements OnInit, OnDestroy {

  @Input()
  public connectedProjects: number = 0;
  @Input()
  public dummy: boolean = false;
  @Input()
  public datasetId: string;
  @Input()
  public isVideo: boolean = false;
  @Input()
  public isAdmin: boolean = true;

  public medias: Media[] = [];

  public selectedMediaIds: string[] = [];

  @ViewChild('checkbox')
  public checkbox: ElementRef;
  @ViewChildren('mediaListItems')
  public mediaListItems: QueryList<MediaListItemComponent>;

  public invalidMediaCount: number = 0;
  public page: number = 0;
  public totalItems: number;
  public loading: boolean = false;
  // Filtering
  public filterConfiguration: FilterBase<string>[] = [];
  // Paging
  public currentLimit = DEFAULT_ITEMS;
  private unsubscribe: Subject<void> = new Subject<void>();

  private deleteDialogueSub: Subscription;


  constructor(private dialogService: DialogueService,
              private datasetService: DatasetService,
              private mediaService: MediaService,
              private translate: TranslateService) {
  }

  ngOnInit(): void {
    this.invalidMediaCount = this.medias.filter(m => !m.valid).length;
    this.buildFilterConfiguration();
    this.getPage(0, null, null);
  }

  ngOnDestroy(): void {
    if (this.deleteDialogueSub) {
      this.deleteDialogueSub.unsubscribe();
    }
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onDeleteConfirmation(mediaId: string): void {
    if (this.dummy) {
      return;
    }
    const del = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE.LABEL';
    const title = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE.TITLE';
    const cancel = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE.CANCEL';
    const content = this.connectedProjects === 0
      ? 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE.CONTENT'
      : 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE.CONTENT_WITH_CONNECTIONS';

    const buttonLeft: AppButtonInput = {label: del, styling: 'warn'};
    const dialogueContent: DialogueModel = {title, content, buttonLeft, buttonRight: cancel};
    this.dialogService.openDialogue(dialogueContent);
    // Todo: take(1), takeUntil
    this.deleteDialogueSub = this.dialogService.closeAction.subscribe((choice: boolean) => {
      if (this.deleteDialogueSub) {
        this.deleteDialogueSub.unsubscribe();
      }
      if (choice === true) {
        // Todo: take(1), takeUntil
        this.mediaService.deleteMediaById(mediaId).subscribe(() => {
          this.getPage(this.page);
        });
      }
    });
  }

  public onDeleteMultipleConfirmation(): void {
    if (this.dummy) {
      return;
    }
    const del = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE_MULTI.LABEL';
    const title = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE_MULTI.TITLE';
    const cancel = 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE_MULTI.CANCEL';
    const content = this.connectedProjects === 0
      ? 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE_MULTI.CONTENT'
      : 'FEATURE.DATASET.MEDIA_LIST.DELETE_DIALOGUE_MULTI.CONTENT_WITH_CONNECTIONS';

    const buttonLeft: AppButtonInput = {label: del, styling: 'warn'};
    const dialogueContent: DialogueModel = {title, content, buttonLeft, buttonRight: cancel};
    this.dialogService.openDialogue(dialogueContent);
    // Todo: take(1), takeUntil
    this.deleteDialogueSub = this.dialogService.closeAction.subscribe((choice: boolean) => {
      if (this.deleteDialogueSub) {
        this.deleteDialogueSub.unsubscribe();
      }
      if (choice === true) {
        // Todo: take(1), takeUntil
        this.mediaService.deleteMediaByIdList(this.selectedMediaIds).subscribe(() => {
          this.getPage(this.page);
        });
      }
        this.selectedMediaIds = [];
        this.checkbox.nativeElement.checked = false;
    });
  }

  public onSelected(mediaId: string): void {
    const index = this.selectedMediaIds.indexOf(mediaId, 0);
    if (index > -1) {
      this.selectedMediaIds.splice(index, 1);
    } else {
      this.selectedMediaIds.push(mediaId);
    }

    this.checkbox.nativeElement.checked = !(this.medias.length > this.selectedMediaIds.length && !!this.checkbox);
  }

  selectAll() {
    this.selectedMediaIds = [];
    this.mediaListItems.forEach(item => {
      item.checkbox.nativeElement.checked = true;
      this.selectedMediaIds.push(item.media.id);
    });
  }

  deselectAll() {
    this.selectedMediaIds = [];
    if (this.mediaListItems !== undefined) {
      this.mediaListItems.forEach(item => {
        item.checkbox.nativeElement.checked = false;
      });
      this.checkbox.nativeElement.checked = false;
    }
  }

  changeSelectAll(event: MouseEvent) {
    if ((event.target as HTMLElement as HTMLInputElement).checked) {
      this.selectAll();
    } else {
      this.deselectAll();
    }
  }

  public getPage(page: number, mediaSourceType?: MediaSourceType, mediaName?: string): void {
    this.deselectAll();

    this.loading = true;
    this.page = page;
    const datasetFilterAndPageParam = new DatasetFilterAndPageParam();
    datasetFilterAndPageParam.numberOfElementsPerPage = this.currentLimit;
    datasetFilterAndPageParam.pageIndex = page;
    datasetFilterAndPageParam.mediaName = mediaName;
    datasetFilterAndPageParam.mediaSourceType = mediaSourceType;

    // Todo: unsubscribe.
    this.datasetService.getDatasetMedia(this.datasetId, datasetFilterAndPageParam).subscribe(data => {
      this.loading = false;
      this.totalItems = data.totalElements;
      this.medias = data.elements;
    });
  }

  public dynamicFilterChanged(event: FormGroup) {
    if (this.currentLimit !== undefined) {
      // Reset page if limit changes
      if (this.currentLimit !== event.value.limit) {
        this.page = 0;
      }
      if (event.value.limit.length === 0) {
        this.currentLimit = DEFAULT_ITEMS;
      } else {
        this.currentLimit = event.value.limit;
      }
    }
    this.getPage(this.page, event.value.mediaSourceType, event.value.mediaName);
  }

  private buildFilterConfiguration(): void {
    const translateStrings: string[] = [
      'FEATURE.DATASET.MEDIA_LIST.TYPE',
      'FEATURE.DATASET.MEDIA_LIST.NAME',
      'FEATURE.TASK_CONFIG.FILTER.LIMIT'
    ];
    this.translate.get(translateStrings).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(translatedText => {
      const LIMIT_LABEL = translatedText['FEATURE.TASK_CONFIG.FILTER.LIMIT'];

      this.filterConfiguration = [
        new FilterTextbox({
          key: 'mediaName',
          placeholder: translatedText['FEATURE.DATASET.MEDIA_LIST.NAME'],
          colSize: 4
        }),
        new FilterDropdown({
          key: 'mediaSourceType',
          colSize: 3,
          placeholder: translatedText['FEATURE.DATASET.MEDIA_LIST.TYPE'],
          options: [
            {key: MediaSourceType.LOCAL, value: MediaSourceType.LOCAL},
            {key: MediaSourceType.SHAREABLE_LINK, value: MediaSourceType.SHAREABLE_LINK},
            {key: MediaSourceType.AWS_S3, value: MediaSourceType.AWS_S3}
          ]
        }),
        new FilterDropdown({
          key: 'limit',
          colSize: 2,
          placeholder: `${LIMIT_LABEL} ${DEFAULT_ITEMS}`,
          options: [
            {key: 100, value: `${LIMIT_LABEL} 100`},
            {key: 200, value: `${LIMIT_LABEL} 200`},
            {key: 500, value: `${LIMIT_LABEL} 500`},
            {key: 1000, value: `${LIMIT_LABEL} 1000`}
          ]
        })
      ];
    });
  }
}
