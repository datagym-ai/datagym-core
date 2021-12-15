import { Component, OnDestroy, OnInit } from '@angular/core';
import { LabelConfigService } from '../../service/label-config.service';
import { ActivatedRoute } from '@angular/router';
import { LcEntry } from '../../model/LcEntry';
import { Subscription } from 'rxjs';
import { LabelConfiguration } from '../../model/LabelConfiguration';

@Component({
  selector: 'app-label-editor',
  templateUrl: './label-editor.component.html',
  styleUrls: ['./label-editor.component.css']
})
export class LabelEditorComponent implements OnInit, OnDestroy {
  public entries: LcEntry[];
  public startEntries: LcEntry[];
  public labelConfigurationID: string;
  private serviceSub: Subscription;
  private routeSubscription: Subscription;

  constructor(private lcService: LabelConfigService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.routeSubscription = this.route.data.subscribe((data: {labelConfig: LabelConfiguration}) => {
      this.lcService.init(data.labelConfig);
      this.labelConfigurationID = this.lcService.id;
      this.entries = this.lcService.entries;
      this.startEntries = JSON.parse(JSON.stringify(this.lcService.entries));
    });
    this.serviceSub = this.lcService.doneEditing.subscribe(() => {
      this.entries = this.lcService.entries;

      //Check if new labels were added or the required field was changed from false to true
      this.lcService.wereNewLabelsAdded(this.entries);
      this.lcService.wasRequiredChangedFromFalseToTrue(this.startEntries, this.entries);
      this.startEntries = JSON.parse(JSON.stringify(this.lcService.entries));

      this.labelConfigurationID = this.lcService.id;
    });
  }

  ngOnDestroy(): void {
    this.routeSubscription.unsubscribe();
    this.serviceSub.unsubscribe();
  }
}
