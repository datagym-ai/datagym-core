import { Component, Input, OnInit } from '@angular/core';
import { LegendConfiguration } from '../../../shared/dg-legend/LegendConfiguration';
import { LegendService } from '../../../shared/dg-legend/legend.service';
import { AccountService } from '../service/account.service';
import { LegendSection } from '../../../shared/dg-legend/LegendSection';
import { LimitPricingPlanViewModel } from '../model/LimitPricingPlanViewModel';

@Component({
  selector: 'app-limit-modal',
  templateUrl: './limit-modal.component.html',
  styleUrls: ['./limit-modal.component.css']
})
export class LimitModalComponent implements OnInit {

  @Input()
  public org: string;

  constructor(
    private legendService: LegendService,
    private accountService: AccountService
  ) { }

  ngOnInit() {}

  public openLegend(): void {

    this.accountService.getLimitsByOrganisation(this.org).subscribe((limits: LimitPricingPlanViewModel) => {
      // create the legend configuration based on the limits response
      const config = LimitModalComponent.generateLegendConfig(limits);
      this.legendService.openDialogue(config);
    });
  }

  private static generateLegendConfig(limits: LimitPricingPlanViewModel): LegendConfiguration {

    const title: string = 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.TITLE';
    const description: string = 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.DESCRIPTION';
    const conf = new LegendConfiguration(title, description);

    const lt: string = 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.LIMIT';

    const colWidth = 6;
    const project = {used: limits.projectUsed, limit: limits.projectLimit};
    const labels = {used: limits.labelLimit - limits.labelRemaining, limit: limits.labelLimit};
    const storage = {used: limits.storageUsed, limit: limits.storageLimit};
    const aiseg = {used: limits.aiSegLimit - limits.aiSegRemaining, limit: limits.aiSegLimit};

    const limitSection = LegendSection.EXPLANATION('FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.TITLE', colWidth)
      .addTextRow(lt, 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.PROJECT' , project)
      .addTextRow(lt, 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.IMAGES' , labels)
      .addTextRow('FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.STORAGE_LIMIT', 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.STORAGE' , storage)
      .addTextRow(lt, 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.LIMIT.AISEG' , aiseg)
    ;

    const api: string = limits.apiAccess
      ? 'fas fa-check dg-primary'
      : 'fas fa-times dg-warn-color';

    const externalStorage: string = limits.apiAccess
      ? 'fas fa-check dg-primary'
      : 'fas fa-times dg-warn-color';

    const restrictionsSection = LegendSection.ICON_DESCRIPTION('FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.RESTRICTION.TITLE')
      .addRow(api, 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.RESTRICTION.API')
      .addRow(externalStorage, 'FEATURE.ACCOUNT_SETTINGS.LIMIT_LEGEND.RESTRICTION.EXTERNAL_STORAGE')
    ;

    return conf
      .addSection(limitSection)
      .addSection(restrictionsSection)
      ;
  }
}
