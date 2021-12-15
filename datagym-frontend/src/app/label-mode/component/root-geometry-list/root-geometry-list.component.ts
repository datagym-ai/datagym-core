import { Component, OnDestroy, OnInit } from '@angular/core';
import { EntryConfigService } from '../../service/entry-config.service';
import { LcEntryGeometry } from '../../../label-config/model/geometry/LcEntryGeometry';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-root-geometry-list',
  templateUrl: './root-geometry-list.component.html',
  styleUrls: ['./root-geometry-list.component.css']
})
export class RootGeometryList implements OnInit, OnDestroy {
  public rootGeometries: LcEntryGeometry[];
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(public configService: EntryConfigService) {
  }

  ngOnInit() {
    this.configService.configInitDone.pipe(filter(value => value === true), takeUntil(this.unsubscribe)).subscribe(() => {
      this.rootGeometries = this.configService.rootGeometries;
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
