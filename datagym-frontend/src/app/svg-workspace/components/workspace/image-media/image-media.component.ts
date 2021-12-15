import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PanZoomConfig} from 'ngx-panzoom';
import {WorkspaceInternalService} from '../../../service/workspace-internal.service';


@Component({
  selector: 'app-image-media',
  templateUrl: './image-media.component.html',
  styleUrls: ['./image-media.component.css']
})
export class ImageMediaComponent implements OnInit {

  @Input('config')
  public panZoomConfig: PanZoomConfig;

  @Output()
  public readonly onClick: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  @Output()
  public readonly onImageLoadError: EventEmitter<Event> = new EventEmitter<Event>();

  @Output()
  public readonly afterImageLoaded: EventEmitter<Event> = new EventEmitter<Event>();

  constructor(public workspace: WorkspaceInternalService) { }

  ngOnInit(): void {
  }

}
