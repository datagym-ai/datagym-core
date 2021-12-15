import {Component, OnDestroy, OnInit} from '@angular/core';
import {EntryValueService} from '../../service/entry-value.service';
import {Subject} from 'rxjs';
import {filter, take, takeUntil} from 'rxjs/operators';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {GeometryItemHolder} from '../../model/GeometryItemHolder';


@Component({
  selector: 'app-entry-value-list',
  templateUrl: './entry-value-list.component.html',
  styleUrls: ['./entry-value-list.component.css']
})
export class EntryValueListComponent implements OnInit, OnDestroy {
  public values: GeometryItemHolder[] = [];
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private entryValueService: EntryValueService) {
  }

  ngOnInit() {
    this.entryValueService.valuesLoaded$.pipe(filter(value => value === true), take(1)).subscribe(() => {
      this.values = this.createItemStack(this.entryValueService.geometries);
    });
    this.entryValueService.changed.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.values = this.createItemStack(this.entryValueService.geometries);
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  private createItemStack(values: LcEntryGeometryValue[]): GeometryItemHolder[] {

    const rootGeometries = values.filter(geo => geo.lcEntryValueParentId === null);

    return rootGeometries.map(geo => new GeometryItemHolder(
      geo, values.filter(child => child.lcEntryValueParentId === geo.id))
    );
  }
}
