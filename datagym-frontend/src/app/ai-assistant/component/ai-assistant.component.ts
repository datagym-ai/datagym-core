import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {PreLabelInfoViewModel} from '../model/PreLabelInfoViewModel';
import {PreLabelApiService} from '../service/pre-label-api.service';
import {PreLabelConfigUpdateBindingModel} from '../model/PreLabelConfigUpdateBindingModel';
import {Project} from '../../project/model/Project';
import {DialogueModel} from '../../shared/dialogue-modal/DialogueModel';
import {DialogueService} from '../../shared/service/dialogue.service';
import {Subscription} from 'rxjs';
import {AppButtonInput} from '../../shared/button/button.component';


@Component({
  selector: 'app-project-ai',
  templateUrl: './ai-assistant.component.html',
  styleUrls: ['./ai-assistant.component.css']
})
export class AiAssistantComponent implements OnInit, OnDestroy{
  progressPercentage: number;
  preLabelConfig: PreLabelInfoViewModel;
  project: Project;
  refreshInterval;

  constructor(private route: ActivatedRoute,
              private preLabelApiService: PreLabelApiService,
              private dialogueService: DialogueService) {
  }

  ngOnInit(): void {
    this.preLabelConfig = this.route.snapshot.data.preLabelConfig as PreLabelInfoViewModel;
    this.project = this.route.parent.parent.snapshot.data.project as Project;
    if (!!this.preLabelConfig) {
      this.updatePercentage();
      if (this.preLabelConfig.activePreLabeling && this.progressPercentage < 100) this.startRefreshInterval();
      if (this.preLabelConfig.activePreLabeling && this.preLabelConfig.countWaitingTasks == 0) this.updatePreLabelingState(false);
    }
  }

  ngOnDestroy() {
    this.clearRefreshInterval();
  }

  private clearRefreshInterval() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  startPreLabeling() {
    if(this.preLabelConfig.aiSegRemaining !== -1 && this.preLabelConfig.countReadyTasks > this.preLabelConfig.aiSegRemaining){
      const buttonLeft: AppButtonInput = { label: 'Accept', styling: 'success' };
      const dialogueContent: DialogueModel = {
        title: 'This operation exceeds your AI-assistance limit',
        content: `Only ${ this.preLabelConfig.aiSegRemaining } of the ${ this.preLabelConfig.countReadyTasks } images you selected will be labeled`,
        buttonLeft,
        buttonRight: 'Cancel'};
      this.dialogueService.openDialogue(dialogueContent);
      const dialogSub: Subscription = this.dialogueService.closeAction.subscribe((choice: boolean) => {
        if(choice){
          this.preLabelConfig.activePreLabeling = true;
          this.startRefreshInterval();
          this.updatePreLabelingState(true);
          dialogSub.unsubscribe();
        }else{
          this.stopPreLabeling();
          dialogSub.unsubscribe();
        }
      });
      return;
    }
    this.preLabelConfig.activePreLabeling = true;
    this.startRefreshInterval();
    this.updatePreLabelingState(true);
  }

  stopPreLabeling() {
    this.clearRefreshInterval();
    setTimeout(() => {    //<<<---    using ()=> syntax
      this.updatePreLabelingState(false);
    }, 500);
  }

  private startRefreshInterval() {
    this.refreshInterval = setInterval(() => {
      this.updatePreLabelConfig();
    }, 2000);
  }

  private updatePreLabelingState(state: boolean) {
    let conf: PreLabelConfigUpdateBindingModel;
    if (state) {
      conf = PreLabelConfigUpdateBindingModel.convertPreLabelMappingsToUpdateModel(true, this.preLabelConfig.preLabelMappings);
    } else {
      conf = new PreLabelConfigUpdateBindingModel(state, null);
    }
    this.preLabelApiService.updatePreLabelConfigByProject(this.project.id,conf).subscribe((preLabelConf:PreLabelInfoViewModel) => {
        this.preLabelConfig = preLabelConf;
        this.updatePercentage();
      }
    );
  }

  private updatePreLabelConfig() {
    this.preLabelApiService.getPreLabelInfoByProject(this.project.id).subscribe((preLabelConfig:PreLabelInfoViewModel) => {
      this.preLabelConfig = preLabelConfig;
      this.updatePercentage();
      if (this.progressPercentage === 100 || this.preLabelConfig.countWaitingTasks === 0) {
        this.updatePreLabelingState(false);
        this.clearRefreshInterval();
      }
    });
  }

  private updatePercentage() {
    if (this.preLabelConfig.activePreLabeling) {
      if ((this.preLabelConfig.countWaitingTasks === 0) && (this.preLabelConfig.countFinishedTasks === 0)) {
        this.progressPercentage = 100;
      } else {
        const calc = this.preLabelConfig.countFinishedTasks / (this.preLabelConfig.countWaitingTasks + this.preLabelConfig.countFinishedTasks) * 100;
        this.progressPercentage = Math.trunc(calc);
      }
    } else {
      if (this.preLabelConfig.countReadyTasks === 0) {
        this.progressPercentage = 100;
      } else {
        const calc = this.preLabelConfig.countFinishedTasks / (this.preLabelConfig.countReadyTasks + this.preLabelConfig.countFinishedTasks) * 100;
        this.progressPercentage = Math.trunc(calc);
      }
    }
  }

  setPreLabelConfig(preLabelConfig: PreLabelInfoViewModel) {
    this.preLabelConfig = preLabelConfig;
    if (this.preLabelConfig.activePreLabeling) this.startPreLabeling();
  }
}
