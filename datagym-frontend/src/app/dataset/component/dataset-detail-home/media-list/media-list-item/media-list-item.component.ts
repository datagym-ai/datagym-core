import {Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {Media} from '../../../../../basic/media/model/Media';
import {MediaSourceType} from '../../../../../basic/media/model/MediaSourceType';
import {VideoMedia} from '../../../../../basic/media/model/VideoMedia';
import {MediaService} from '../../../../service/media.service';
import {take, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';


@Component({
  selector: 'app-media-list-item',
  templateUrl: './media-list-item.component.html',
  styleUrls: ['./media-list-item.component.css']
})
export class MediaListItemComponent implements OnInit, OnDestroy {
  @Input()
  public media: Media;
  @Input()
  public index: number = 0;
  @Input()
  public dummy: boolean = false;
  @Input()
  public invalidMediaCount: number;
  @Input()
  public isVideo: boolean = false;
  @Input()
  public connectedProjects: number = 0;
  @Input()
  public isAdmin: boolean = true;
  @Output()
  public onDelete: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  public onSelected: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  public onShowMediaError: EventEmitter<void> = new EventEmitter<void>();

  public readonly MediaType = MediaSourceType;

  public get video(): VideoMedia {
    return this.media as VideoMedia;
  }

  @ViewChild('checkbox')
  checkbox: ElementRef;

  @Input()
  public checked: boolean;
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(public mediaService: MediaService) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onShowMedia(): void {
    this.mediaService.openMediaInNewTab(this.media.id)
      .pipe(
        take(1), takeUntil(this.unsubscribe))
      .subscribe(() => {
      }, () => {
        this.onShowMediaError.emit();
      });
  }
}
