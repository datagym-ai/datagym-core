import {Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {WorkspaceInternalService} from '../../service/workspace-internal.service';
import {WorkspaceUtilityService} from '../../service/workspace-utility.service';
import {PanZoomAPI, PanZoomConfig, PanZoomModel} from 'ngx-panzoom';
import {Subject, Subscription} from 'rxjs';
import {DynamicScriptLoaderService} from '../../service/dynamic-script-loader.service';
import {WorkspaceEvent} from '../../messaging/WorkspaceEvent';
import {WorkspaceEventType} from '../../messaging/WorkspaceEventType';
import {take, takeUntil} from 'rxjs/operators';
import {ContextMenuService} from '../../service/context-menu.service';
import {BrowserSupportService} from '../../../shared/browser-support/browser-support.service';
import {Router} from '@angular/router';
import {MediaService} from '../../../dataset/service/media.service';
import {GeometryType} from '../../geometries/GeometryType';
import {LabelModeType} from '../../../label-mode/model/import';


@Component({
  selector: 'app-workspace',
  templateUrl: './workspace.component.html',
  styleUrls: ['./workspace.component.css']
})

export class WorkspaceComponent implements OnInit, OnDestroy {

  @Input()
  public disableDemoMode: boolean = false;

  @ViewChild('drawBox')
  public drawBox: ElementRef;

  public get edgeUserAgent(): boolean {
    return / Edge\//.test(navigator.userAgent);
  }

  public readonly LabelModeType = LabelModeType;

  public panZoomConfig: PanZoomConfig = new PanZoomConfig;
  public panZoomAPI: PanZoomAPI;


  private apiSubscription: Subscription;
  /**
   * The size in pixel how much the arrow-keys should pan
   */
  private arrowPanningStep: number = 20;

  /**
   * Some features of pan-zoom are buggy on
   * Firefox so DataGym does not support this features.
   */
  private readonly isFirefox: boolean;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private router: Router,
    public contextMenu: ContextMenuService,
    public workspace: WorkspaceInternalService,
    private workUtility: WorkspaceUtilityService,
    private browserService: BrowserSupportService,
    private scriptLoader: DynamicScriptLoaderService,
    public mediaService: MediaService,
  ) {

    this.isFirefox = browserService.isBrowser('Firefox');

    this.panZoomConfig.zoomLevels = 6;
    this.panZoomConfig.scalePerZoomLevel = 2.0;
    this.panZoomConfig.zoomStepDuration = 0.2;
    this.panZoomConfig.freeMouseWheelFactor = 0.01;
    this.panZoomConfig.neutralZoomLevel = 0;
    this.panZoomConfig.initialZoomLevel = 0;
    this.panZoomConfig.zoomOnDoubleClick = false;
    this.panZoomConfig.zoomOnMouseWheel = !this.isFirefox;
    this.panZoomConfig.zoomButtonIncrement = 0;
    this.togglePanning(true);
  }

  ngOnInit() {
    // First load main svg.js
    this.scriptLoader.loadScript('svg.js').then(() => {
      // Then load plugins

      const scripts: string[] = [
        'svg.draw.js',
        'svg.draggable.js',
        'svg.select.js',
        'svg.resize.js',
        // todo: check if this is really required.
        'svg.intersections.js',
        'svg.poly_extend.js',
        'svg.segment.js',
      ];

      this.scriptLoader.load(...scripts).then(() => {
        this.workspace.internalWorkspaceEventBus.next(new WorkspaceEvent(undefined, WorkspaceEventType.WORKSPACE_INITIALIZED));
      });
    });

    this.apiSubscription = this.panZoomConfig.api.pipe(takeUntil(this.unsubscribe)).subscribe((api: PanZoomAPI) => this.panZoomAPI = api);

    let previousMouseWheelState: boolean = this.panZoomConfig.zoomOnMouseWheel;
    this.workspace.isUserDrawingSub.pipe(takeUntil(this.unsubscribe)).subscribe((isDrawing: boolean) => {
      if (isDrawing) {
        if (!!this.workspace.currentAISegGeometry && this.workspace.drawingGeometry.geometryProperties.geometryType === GeometryType.POLYLINE) {
          previousMouseWheelState = this.panZoomConfig.zoomOnMouseWheel;
          this.panZoomConfig.zoomOnMouseWheel = false;
        }
      } else {
        this.panZoomConfig.zoomOnMouseWheel = previousMouseWheelState;
      }
    });

    // On zoom
    this.panZoomConfig.modelChanged.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((change: PanZoomModel) => {
      if (this.contextMenu.visible) {
        this.contextMenu.close();
      }
      this.workspace.closeVideoContextMenu();
      this.workspace.updateZoomState(change.zoomLevel, this.panZoomConfig.zoomLevels, change.isPanning);
    });

    // Disable panning if user is drawing
    this.workspace.isUserDrawingSub.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(drawing => {
      console.log('drawing', drawing);
      this.togglePanning(drawing);
    });
  }

  ngOnDestroy(): void {
    this.workspace.disableDrawingMode();
    if (this.apiSubscription) {
      this.apiSubscription.unsubscribe();
    }
    this.unsubscribe.next();
    this.unsubscribe.complete();
    this.workspace.resetMedia();
  }

  @HostListener('contextmenu', ['$event'])
  onRightClick(event) {
    // Context menu is only available while clicking on a geometry.
    // See workspace.onGeometryContextMenu
    this.contextMenu.close();
    this.workspace.closeVideoContextMenu();
    event.preventDefault();
  }

  /**
   * Close the context menu if it's visible or intercept click
   * on panZoom and set focus to drawBox to enable keyListener.
   */
  public handleClick(click: MouseEvent) {
    this.workspace.closeVideoContextMenu();
    if (this.contextMenu.visible) {
      this.contextMenu.close();
    } else {
      this.drawBox.nativeElement.focus();
    }
  }

  /**
   * Listen on keyEvents when drawBox has focus (see this.handleClick())
   * @param event
   */
  public handleKeyDownEvent(event: KeyboardEvent) {
    // check for geometry shortcuts and re-emit event
    // Note: shift + geometry shortcut result in special chars depending on the keyboard layout like '@'
    if (event.key.match(/[0-9]/) || (event.shiftKey && event.code.match(/Digit([0-9])/))) {
      // construct copy of event because events can only be dispatched once
      const newEvent: KeyboardEvent = new KeyboardEvent(event.type, event);
      document.body.dispatchEvent(newEvent);
    }
    if (event.ctrlKey && event.key === 'c') {
      this.workspace.onCtrlCopyEvent();
    }
    if (event.ctrlKey && event.key === 'v') {
      this.workspace.onCtrlPasteEvent();
    }
    if (event.key === 'Escape') {
      if (this.workspace.aiSegActive === true) {
        this.workspace.cancelAISeg();
      }
      this.workspace.onEscapePressedEvent();
    }

    // Arrow keys left & right *with* ctrlKey are used to iterate through the entry value list and select the next one
    // so panning is only available if the ctrl key is not pressed. Also the combination of shift key and arrow key is
    // used to toggle frames in video labeling mode.
    if (!this.isFirefox && !event.ctrlKey && !event.shiftKey) {
      if (event.key === 'ArrowRight') {
        this.panZoomAPI.panDelta({x: -this.arrowPanningStep, y: 0});
      }
      if (event.key === 'ArrowLeft') {
        this.panZoomAPI.panDelta({x: this.arrowPanningStep, y: 0});
      }
      if (event.key === 'ArrowUp') {
        this.panZoomAPI.panDelta({x: 0, y: this.arrowPanningStep});
      }
      if (event.key === 'ArrowDown') {
        this.panZoomAPI.panDelta({x: 0, y: -this.arrowPanningStep});
      }
    }
    if (event.key === 'Delete') {
      this.workspace.onDeleteRequest();
    }
  }

  public onMediaLoadError(event): void {
    event.target.hidden = true;
    // Method sends the media url as a http.get request to generate the error message
    // triggered by the HttpInterceptor.
    // The response produced by the <img> tag can't be intercepted!
    this.mediaService.streamMediaFile(event.target.src).pipe(take(1))
      .subscribe(
        () => {
        }, () => {
          this.router.navigate(['/tasks']).then();
        });

  }

  private togglePanning(state: boolean) {
    this.panZoomConfig.panOnClickDrag = !state && !this.isFirefox;
  }
}
