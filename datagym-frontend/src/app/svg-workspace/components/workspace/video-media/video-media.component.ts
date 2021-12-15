import { Component, ElementRef, EventEmitter, Input, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { PanZoomConfig } from 'ngx-panzoom';
import { WorkspaceInternalService } from '../../../service/workspace-internal.service';
import { DomEventServiceService, EventHandler } from '../../../service/dom-event-service.service';
import { VideoControlService } from '../../../../label-mode/service/video-control.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';


@Component({
  selector: 'app-video-media',
  templateUrl: './video-media.component.html',
  styleUrls: ['./video-media.component.css']
})
export class VideoMediaComponent implements OnInit {

  @Input('config')
  public panZoomConfig: PanZoomConfig;

  @Output()
  public readonly onClick: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  @Output()
  public readonly onVideoLoadError: EventEmitter<Event> = new EventEmitter<Event>();

  @Output()
  public readonly afterVideoLoaded: EventEmitter<Event> = new EventEmitter<Event>();

  @ViewChild('video', { static: false })
  private video: ElementRef;

  @ViewChild('source', { static: false })
  private source: ElementRef;

  private events: EventHandler[] = [];

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public videoService: VideoControlService,
    public workspace: WorkspaceInternalService,
    private renderer: Renderer2,
    private evt: DomEventServiceService,
  ) { }

  ngOnInit(): void {

    this.videoService.onPlayPause.pipe(takeUntil(this.unsubscribe)).subscribe((play: boolean) => {
      if (!!play) {
        if (this.video.nativeElement.paused) {
          this.video.nativeElement.play();
        }
      } else {
        if (!this.video.nativeElement.paused) {
          this.video.nativeElement.pause();

          // Update current time position because the timeupdate may not get triggered
          this.videoService.currentTime = this.video.nativeElement.currentTime;
          this.videoService.onSeek.next(this.video.nativeElement.currentTime);
        }
      }
    });

  }

  ngOnDestroy(): void {
    this.evt.removeEvents(this.events);
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  ngAfterViewInit(): void {

    this.videoService.onSeek.pipe(takeUntil(this.unsubscribe)).subscribe((newTime: number) => {

      this.workspace.onEscapePressedEvent();

      if (!this.video.nativeElement.paused) {
        this.video.nativeElement.pause();
      }

      this.video.nativeElement.currentTime = newTime;
    });

    this.events = [
      {
        element: this.video.nativeElement,
        name: 'loadstart',
        callback: () => this.videoService.loaded = false,
        dispose: null
      },
      // This would reset the video to frame/time 0 after loading it.
      // {
      //   element: this.video.nativeElement,
      //   name: 'loadeddata',
      //   callback: () => this.video.nativeElement.currentTime = 0,
      //   dispose: null
      // },
      {
        element: this.video.nativeElement,
        name: 'loadedmetadata',
        callback: event => {
          this.video.nativeElement.muted = true;
          this.video.nativeElement.disablePictureInPicture = true;
          this.videoService.loaded = true;
          this.videoService.duration = this.video.nativeElement.duration;
          this.afterVideoLoaded.emit(event);
        },
        dispose: null
      },
      // {
      //   /*
      //    * 'canplay' will be always triggered when toggle between frames.
      //    */
      //   element: this.video.nativeElement,
      //   name: 'canplay',
      //   callback: event => console.log('canplay', event),
      //   dispose: null
      // },
      {
        element: this.video.nativeElement,
        name: 'ended',
        callback: () => this.videoService.ended = true,
        dispose: null
      },
      {
        element: this.video.nativeElement,
        name: 'error',
        callback: (event) => {
          this.videoService.loaded = false;
          this.onVideoLoadError.emit(event);
        },
        dispose: null
      },
      {
        // 404 Server errors occur on source tag
        element: this.source.nativeElement,
        name: 'error',
        callback: (event) => {
          this.videoService.loaded = false;
          this.onVideoLoadError.emit(event);
        },
        dispose: null
      },
      {
        element: this.video.nativeElement,
        name: 'timeupdate',
        /**
         * Note: The timeupdate event doesn't fire per frame change. Instead if fires between
         * ~15ms and ~250ms (depending on the browser and maybe it's version). May improve this
         * by using `requestAnimFrame()` instead.
         * @see:
         * - https://developer.mozilla.org/en-US/docs/Web/API/window/requestAnimationFrame
         * - https://bugzilla.mozilla.org/show_bug.cgi?id=571822
         * - https://stackoverflow.com/a/17048103
         */
        callback: () => this.videoService.currentTime = this.video.nativeElement.currentTime,
        dispose: null
      },
      // {
      //   element: this.video.nativeElement,
      //   name: 'seeked',
      //   callback: () => this.videoService.currentTime = this.video.nativeElement.currentTime,
      //   dispose: null
      // },
      // {
      //   element: this.video.nativeElement,
      //   name: 'suspended',
      //   callback: event => {
      //     console.log('suspended');
      //     console.log(event);
      //   },
      //   dispose: null
      // },
      // {
      //   element: this.video.nativeElement,
      //   name: "contextmenu",
      //   callback: event => console.log('contextmenu', event),
      //   dispose: null
      // },
      // {
      //   element: this.player.nativeElement,
      //   name: "mousemove",
      //   callback: event => console.log('loadstart', event),
      //   dispose: null
      // }
    ];

    // .seekFinished(this.video.nativeElement.currentTime)
    // public onSeeked: EventEmitter<number> = new EventEmitter<number>();

    this.evt.addEvents(this.renderer, this.events);
  }
}
