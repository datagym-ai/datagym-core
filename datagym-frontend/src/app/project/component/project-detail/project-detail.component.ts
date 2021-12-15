import {Component, OnDestroy, OnInit} from '@angular/core';
import { Project } from '../../model/Project';
import { ActivatedRoute } from '@angular/router';
import { TabGroup } from '../../../shared/tab-group/models/TabGroup';
import { Tab } from '../../../shared/tab-group/models/Tab';
import {Subscription} from 'rxjs';
import {MediaType} from '../../model/MediaType.enum';

@Component({
  selector: 'app-project-detail',
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.css']
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
  public tabs: TabGroup;
  public project: Project;

  private routeSub: Subscription;

  constructor(private route: ActivatedRoute) {
  }

  private static initTabGroup(excludeAI: boolean = false): TabGroup {
    const tabs = [
      new Tab('FEATURE.PROJECT.DETAIL.TABS.HOME', 'home', []),
      new Tab('FEATURE.PROJECT.DETAIL.TABS.TASKS', 'tasks', ['ADMIN']),
      new Tab('FEATURE.PROJECT.DETAIL.TABS.LABEL_CONFIG', 'label-config', ['ADMIN']),
    ];

    if (!excludeAI) {
      tabs.push(new Tab('FEATURE.PROJECT.DETAIL.TABS.AI', 'ai-assistant', ['ADMIN']));
    }

    tabs.push(...[
      new Tab('FEATURE.PROJECT.DETAIL.TABS.SETTINGS', 'settings', ['ADMIN'])
    ]);

    return new TabGroup(tabs);
  }

  ngOnInit() {
    // subscribe here to route.data instead of using snapshot.data to react on changes
    // to the dataset within the child/tab components.
    this.routeSub = this.route.data.subscribe((data: { project: Project }) => {
      this.project = data.project;
      const excludeAI = this.project.mediaType === MediaType.VIDEO;
      this.tabs = ProjectDetailComponent.initTabGroup(excludeAI);
    });
  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
  }
}
