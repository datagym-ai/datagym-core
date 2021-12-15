import { Component, OnInit } from '@angular/core';
import {LegendConfiguration} from '../../../../shared/dg-legend/LegendConfiguration';
import {LegendSection} from '../../../../shared/dg-legend/LegendSection';
import {LegendService} from '../../../../shared/dg-legend/legend.service';

@Component({
  selector: 'app-info-modal',
  templateUrl: './info-modal.component.html',
  styleUrls: ['./info-modal.component.css']
})
export class InfoModalComponent implements OnInit {

  public legendConfiguration: LegendConfiguration;

  constructor(private legendService: LegendService) { }

  ngOnInit() {
    this.legendConfiguration = InfoModalComponent.generateLegendConfig();
  }

  public openLegend(): void {
    this.legendService.openDialogue();
  }

  private static generateLegendConfig(): LegendConfiguration {

    const title: string = 'FEATURE.TASK_CONFIG.LEGEND.TITLE';
    const description: string = 'FEATURE.TASK_CONFIG.LEGEND.DESCRIPTION';
    const conf = new LegendConfiguration(title, description);

    const states = LegendSection.ICON_DESCRIPTION('FEATURE.TASK_CONFIG.LEGEND.STATES.TITLE')
        .addRow('fas fa-layer-group', 'FEATURE.TASK_CONFIG.LEGEND.STATES.BACKLOG')
        .addRow('fas fa-stopwatch', 'FEATURE.TASK_CONFIG.LEGEND.STATES.WAITING')
        .addRow('fas fa-stopwatch', 'FEATURE.TASK_CONFIG.LEGEND.STATES.WAITING_CHANGED')
        .addRow('fas fa-circle-notch', 'FEATURE.TASK_CONFIG.LEGEND.STATES.IN_PROGRESS')
        .addRow('fas fa-step-forward', 'FEATURE.TASK_CONFIG.LEGEND.STATES.SKIPPED')
        .addRow('fas fa-check', 'FEATURE.TASK_CONFIG.LEGEND.STATES.COMPLETED')
        .addRow('fas fa-check-double', 'FEATURE.TASK_CONFIG.LEGEND.STATES.REVIEWED')
        .addRow('fas fa-check-double', 'FEATURE.TASK_CONFIG.LEGEND.STATES.REVIEWED_SKIP')
    ;
    const actions = LegendSection.ICON_DESCRIPTION('FEATURE.TASK_CONFIG.LEGEND.ACTIONS.TITLE')
      .addRow('fas fa-arrow-left', 'FEATURE.TASK_CONFIG.LEGEND.ACTIONS.TO_BACKLOG')
      .addRow('fas fa-arrow-left', 'FEATURE.TASK_CONFIG.LEGEND.ACTIONS.TO_WAITING_CHANGED')
      .addRow('fas fa-arrow-right', 'FEATURE.TASK_CONFIG.LEGEND.ACTIONS.TO_WAITING')
      .addRow('fas fa-eye', 'FEATURE.TASK_CONFIG.LEGEND.ACTIONS.OPEN_IMAGE')
      .addRow('fas fa-magic', 'FEATURE.TASK_CONFIG.LEGEND.ACTIONS.OPEN_TASK')
    ;
    const taskBadges = LegendSection.ICON_DESCRIPTION('Task Badges')
      .addRow('fas fa-clipboard-check', 'FEATURE.TASK_CONFIG.LEGEND.TASK_BADGES.PRE_LABEL_SUCCESS')
      .addRow('fas fa-slash', 'FEATURE.TASK_CONFIG.LEGEND.TASK_BADGES.PRE_LABEL_FAILED')
      .addRow('fas fa-file-import', 'FEATURE.TASK_CONFIG.LEGEND.TASK_BADGES.JSON_IMPORT')
    ;

    conf.addSection(states)
      .addSection(actions)
      .addSection(taskBadges)
    ;

    return conf;
  }
}
