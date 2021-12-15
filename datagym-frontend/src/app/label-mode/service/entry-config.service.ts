import {Injectable} from '@angular/core';
import {LabelConfiguration} from '../../label-config/model/LabelConfiguration';
import {LcEntry} from '../../label-config/model/LcEntry';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {LcEntryClassification} from '../../label-config/model/classification/LcEntryClassification';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {BehaviorSubject, Observable} from 'rxjs';
import {TaskControlService} from './task-control.service';
import {EntryConfigApiService} from './entry-config-api/entry-config-api.service';
import {LcEntryImageSegmentationEraser} from '../../label-config/model/geometry/LcEntryImageSegmentationEraser';
import {WorkspaceControlService} from '../../svg-workspace/service/workspace-control.service';
import {SingleTaskResponseModel} from '../model/SingleTaskResponseModel';
import {LabelModeType, Media} from '../model/import';

type WithFlat<T> = { flat: (depth: number) => T[] };

@Injectable({
  providedIn: 'root'
})
export class EntryConfigService {
  public configInitDone: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public rootGeometries: LcEntryGeometry[] = [];
  public globalClassifications: LcEntryClassification[] = [];
  public hasRequiredGlobalClassifications: boolean = false;
  public datasetId: string;
  public datasetName: string;

  private config: LabelConfiguration;

  private _media: Media;
  private type: LabelModeType = undefined;

  public get mediaType(): LabelModeType {
    return this.type;
  }

  public get media(): Media {
    return this._media;
  }

  constructor(
    private api: EntryConfigApiService,
    private taskControl: TaskControlService,
    private workspace: WorkspaceControlService
  ) {}

  /**
   * Detect if the label configuration was changed from the project admin
   * while the labeler made a great job and detected a new geometry or
   * classified the whole media.
   */
  public hasConfigChanged() : Observable<boolean> {
    return this.api.hasConfigChanged(
      this.taskControl.labelIterationId,
      this.taskControl.lastChangedConfig
    );
  }

  get id(): string {
    return this.config.id;
  }

  get projectId(): string {
    return this.config.projectId;
  }

  /**
   * Find an LcEntry within the stacks this.rootGeometries of this.globalClassifications by it's id.
   *
   * @param id
   */
  public findById(id: string): LcEntry | undefined;

  /**
   * Find an LcEntry within the stacks this.rootGeometries of this.globalClassifications by it's id.
   *
   * To speed up the lookup the optional `isGeometry` flag is used to decide if only within this.rootGeometries or
   * this.globalClassifications should be searched.
   *
   * @param id
   * @param isGeometry
   */
  public findById(id: string, isGeometry: boolean): LcEntry | undefined;

  /**
   * Find an LcEntry within the stacks this.rootGeometries of this.globalClassifications by it's id.
   *
   * Optional the `isGeometry` flag can be passed as second argument. If it is set it's used to
   * decide if only within this.rootGeometries or this.globalClassifications should be searched.
   * On default both stacks would be searched at.
   *
   * @param id
   * @param isGeometry
   */
  public findById(id: string, isGeometry: boolean = undefined): LcEntry | undefined {
    return this.find(entry => entry.id === id, isGeometry);
  }

  /**
   * Find an LcEntry within the stacks this.rootGeometries and this.globalClassifications
   *
   * @param predicate
   */
  public find(predicate: (value: LcEntry, index: number, obj: LcEntry[]) => boolean) : LcEntry | undefined;

  /**
   * Find an LcEntry within the stacks this.rootGeometries and this.globalClassifications
   *
   * If `isGeometry` is true the lookup is only within the stack this.rootGeometries. Otherwise only
   * within the stack this.globalClassifications
   *
   * @param predicate
   * @param isGeometry
   */
  public find(predicate: (value: LcEntry, index: number, obj: LcEntry[]) => boolean, isGeometry: boolean) : LcEntry | undefined;

  /**
   * Find an LcEntry within the stacks this.rootGeometries and this.globalClassifications
   *
   * Optional the `isGeometry` flag can be used to look only within the this.rootGeometries stack
   * or only within the this.globalClassifications stack. On default both stacks are searched.
   *
   * @param predicate
   * @param isGeometry
   */
  public find(predicate: (value: LcEntry, index: number, obj: LcEntry[]) => boolean, isGeometry: boolean = undefined) : LcEntry | undefined {

    if (isGeometry === true) {
      // check only geometries
      return this.rootGeometries.find(predicate);
    }
    if (isGeometry === false) {
      // check only media classifications
      return this.globalClassifications.find(predicate);
    }

    const match: LcEntry = this.rootGeometries.find(predicate);
    if (!!match) {
      return match;
    }
    return this.globalClassifications.find(predicate);
  }

  /**
   * Find an LcEntry within the stacks this.rootGeometries of this.globalClassifications by it's id.
   *
   * Optional any LcEntryType can be passed as second argument. If one is passed that type is used to
   * decide if only within this.rootGeometries or this.globalClassifications should be searched.
   * On default both stacks would be searched at.
   *
   * @param id
   * @param isGeometry
   */
  public findRecursiveById(id: string, isGeometry: boolean = undefined): LcEntry | undefined {
    return this.findRecursive(entry => entry.id === id, isGeometry);
  }

  public findRecursive(predicate: (value: LcEntry, index: number, obj: LcEntry[]) => boolean, isGeometry: boolean = undefined) : LcEntry | undefined {
    /**
     * Find within the given stack.
     *
     * @param stack
     */
    const finder = (stack: LcEntry[]) : LcEntry | undefined => {
      let found: LcEntry = stack.find(predicate);
      if (!!found) {
        return found;
      }

      for (const entry of stack) {
        const children = entry.children || [];
        found = finder(children);
        if (!!found) {
          return found;
        }
      }

      return undefined;
    };

    if (isGeometry === true) {
      // check only geometries
      return finder(this.rootGeometries);
    }

    if (isGeometry === false) {
      // check only classifications
      return finder(this.globalClassifications);
    }

    let matching: LcEntry = finder(this.rootGeometries);
    if (!!matching) {
      return matching;
    }

    matching = finder(this.globalClassifications);
    if (!!matching) {
      return matching;
    }

    return undefined;
  }

  public initConfiguration(task: SingleTaskResponseModel): void {
    this.config = task.labelConfig;
    this._media = task.media;
    this.type = task.projectType;
    this.datasetId = task.datasetId;
    this.datasetName = task.datasetName;

    this.initWorkspaceConfig(task.labelConfig.entries);
    this.initLists();
  }

  /**
   * Helper-method to reset state when navigating away from label-mode (called in label-mode onDestroy)
   */
  public reset(): void {
    this.rootGeometries = [];
    this.datasetId = undefined;
    this.datasetName = undefined;
    this.globalClassifications = [];
    this.configInitDone.next(false);
    this.hasRequiredGlobalClassifications = false;
    this.config = null;
  }

  /**
   * Loops through given array and nested elements
   * returns true if an element is required
   * @param lcEntryClassification
   */
  private checkForRequiredGlobalClassifications(lcEntryClassification: LcEntryClassification[]): boolean {
    for (const classification of lcEntryClassification) {
      if (classification.required === true) {
        return true;
      }
      // Loop recursively through children
      if (classification.children && classification.children.length > 0) {
        const childRequired = this.checkForRequiredGlobalClassifications(classification.children as LcEntryClassification[]);
        if (childRequired === true) {
          return true;
        }
      }
    }
    // No required global classifications found
    return false;
  }

  /**
   * Callback function for array.sort to order LcEntries alphabetically by their entryValue
   * This gets called on rootGeometries in initLists()
   * @param entryA
   * @param entryB
   */
  private orderByRootNameAlphabetically = (entryA: LcEntry, entryB: LcEntry): number => {
    if (entryA.entryValue === null || entryB.entryValue === null) {
      return 0;
    } else {
      return entryA.entryValue.localeCompare(entryB.entryValue);
    }
  };

  /**
   * This will sort the configuration entries into rootGeometries and globalClassifications
   * emits configInitDone event when finished
   */
  private initLists(): void {
    const rootGeometries: LcEntryGeometry[] = [];
    const globalClassifications: LcEntryClassification[] = [];
    this.config.entries.forEach((entry: LcEntry) => {
      if (LcEntryType.isGeometry(entry.type)) {
        rootGeometries.push(entry as LcEntryGeometry);
      } else {
        globalClassifications.push(entry as LcEntryClassification);
      }
    });
    rootGeometries.sort(this.orderByRootNameAlphabetically);

    const hasSegmentations = !!rootGeometries.find(geo => geo.type === LcEntryType.IMAGE_SEGMENTATION);
    if (hasSegmentations) {
      rootGeometries.push(new LcEntryImageSegmentationEraser());
    }

    this.rootGeometries = rootGeometries;
    this.globalClassifications = globalClassifications;
    this.hasRequiredGlobalClassifications = this.checkForRequiredGlobalClassifications(this.globalClassifications);
    this.configInitDone.next(true);
  }

  /**
   * Register a lightweight version of the label configuration within the
   * workspace to simplify geometry creation and the context menu.
   *
   * @param configuration
   * @private
   */
  private initWorkspaceConfig(configuration: LcEntry[]): void {
    const maxConfigDepth = 3;
    const flat = [...configuration, ...configuration.map(config => config.children)];
    const flatted = (flat as unknown as WithFlat<LcEntry>).flat(maxConfigDepth);

    const geometryConfiguration = flatted.filter(config => LcEntryType.isGeometry(config));
    this.workspace.initConfiguration(geometryConfiguration as LcEntryGeometry[]);
  }
}
