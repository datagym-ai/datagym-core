import {EventEmitter, Injectable} from '@angular/core';
import {BehaviorSubject, Observable, of, Subject} from 'rxjs';
import {map} from 'rxjs/operators';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {LcEntryValue} from '../model/LcEntryValue';
import {LcEntryValueCreateBindingModel} from '../model/LcEntryValueCreateBindingModel';
import {EntryValueApiService} from './entry-value-api/entry-value-api.service';
import {WorkspaceControlService} from '../../svg-workspace/service/workspace-control.service';
import {BaseGeometryData} from '../../svg-workspace/geometries/geometry-data/BaseGeometryData';
import {LcEntryPointValue} from '../model/geometry/LcEntryPointValue';
import {PointGeometryData} from '../../svg-workspace/geometries/geometry-data/PointGeometryData';
import {LcEntryLineValue} from '../model/geometry/LcEntryLineValue';
import {LineGeometryData} from '../../svg-workspace/geometries/geometry-data/LineGeometryData';
import {LcEntryPolyValue} from '../model/geometry/LcEntryPolyValue';
import {PolygonGeometryData} from '../../svg-workspace/geometries/geometry-data/PolygonGeometryData';
import {LcEntryRectangleValue} from '../model/geometry/LcEntryRectangleValue';
import {RectangleGeometryData} from '../../svg-workspace/geometries/geometry-data/RectangleGeometryData';
import {GeometryType} from '../../svg-workspace/geometries/GeometryType';
import {WorkspacePoint} from '../../svg-workspace/model/WorkspacePoint';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {EntryConfigService} from './entry-config.service';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {Project} from '../../project/model/Project';
import {ProjectService} from '../../project/service/project.service';
import {UserService} from '../../client/service/user.service';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../model/classification/LcEntryClassificationValue';
import {ImageSegmentationGeometryData} from '../../svg-workspace/geometries/geometry-data/ImageSegmentationGeometryData';
import {LcEntryImageSegmentationValue} from '../model/geometry/LcEntryImageSegmentationValue';
import {Point} from '../model/geometry/Point';
import {GeometryProperties} from '../../svg-workspace/geometries/GeometryProperties';
import {SingleTaskResponseModel} from '../model/SingleTaskResponseModel';
import {LabelModeType, Media} from '../model/import';
import {EntryChangeService} from './entry-change.service';
import {VideoControlService} from './video-control.service';
import {DeleteGeometryConfig} from './entry-value-api/handler/delete-geometry/DeleteGeometryConfig';
import {VideoValueService} from './video-value.service';
import {
  DummyGeometryDeleteHandler,
  GeometryDeleteHandler,
  ImageGeometryDeleteHandler,
  VideoGeometryDeleteHandler
} from './entry-value-api/handler/delete-geometry/handler';
import {
  ChangeDeleteHandler,
  DummyChangeDeleteHandler,
  ImageChangeDeleteHandler,
  VideoChangeDeleteHandler
} from './entry-value-api/handler/delete-change/handler';
import {
  DummyGeometryUpdateHandler,
  GeometryUpdateHandler,
  ImageGeometryUpdateHandler,
  VideoGeometryUpdateHandler
} from './entry-value-api/handler/update-geometry/handler';
import {
  CreateHandler,
  DummyCreateHandler,
  ImageCreateHandler,
  VideoCreateHandler
} from './entry-value-api/handler/create-geometry/handler';
import {
  ClassificationUpdateHandler,
  DummyClassificationUpdateHandler,
  ImageClassificationUpdateHandler,
  VideoClassificationUpdateHandler
} from './entry-value-api/handler/update-classification/handler';

import {LcEntryChange} from '../model/change/LcEntryChange';
import {ValueStack} from './media-controller/value-stack/ValueStack';
import {ValidityObserver} from './media-controller/validity/ValidityObserver';
import {ValueServiceValidityObserver} from './media-controller/validity/ValueServiceValidityObserver';
import {ValueStackFactory} from './media-controller/value-stack/ValueStackFactory';
import {ValueServiceStackFactory} from './media-controller/value-stack/ValueServiceStackFactory';
import {VideoExpandHandler} from './entry-value-api/handler/expand/VideoExpandHandler';
import {ExpandHandler} from './entry-value-api/handler/expand/ExpandHandler';
import {DummyExpandHandler} from './entry-value-api/handler/expand/DummyExpandHandler';
import {DummyValidityObserver} from './media-controller/validity/DummyValidityObserver';


type WithFlat<T> = { flat: (depth: number) => T[] };

@Injectable({
  providedIn: 'root'
})
export class EntryValueService {
  /**
   * Loaded or created geometry entryValues.
   *
   * This is a flatted list of all created geometries. That means,
   * all nested geometries can be found with .find() because there is no
   * nesting level in this list.
   */
  public geometries: LcEntryGeometryValue[] = [];
  public mediaClassifications: LcEntryClassificationValue[] = [];
  public readonly mediaClassificationsValid$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  /**
   * Emitted to reload value-list.
   *
   * Do not emit this event within this service. Use `onChanged` instead.
   * Inform the EntryValueListComponent to update the list.
   */
  public readonly changed: EventEmitter<void> = new EventEmitter<void>();

  // Emitted after saved geometries have been drawn (last step of initialization)
  public readonly valuesLoaded$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  public valueObserver: ValidityObserver = new DummyValidityObserver();

  // task-state
  public labelTaskId = '';
  public labelIterationId: string = '';
  public labelConfigurationId: string = '';
  public projectId: string = '';
  public benchmarkSetPermission: boolean = false;
  public labelModeType: LabelModeType = undefined;

  /**
   * Flag within the workspace listener to indicate that the svg layer is setup and ready.
   *
   * A guard to not call `initSavedGeometriesFromValues()` before the workspace is up and
   * working to prevent `ERROR TypeError: svgLayer is undefined` within the BaseGeometry when
   * calling the abstract `createSvgObject()` method within it's constructor.
   */
  public workspaceReady: boolean = false;

  public get mediaId(): string {
    return !!this.media ? this.media.id : '';
  }

  public get url(): string {
    return !!this.media ? this.media.url : '';
  }

  private media: Media = undefined;
  // internal state to check if it's necessary to make a backend call when getting globalClassifications
  private mediaClassificationValuesLoaded: boolean = false;
  private valueStackFactory: ValueStackFactory = new ValueServiceStackFactory(this);

  /**
   * When the `reset()` method would be called before the init method,
   * it would raise an type error `*Handler is undefined` for every following
   * handler so instantiate it with the dummy handler.
   *
   * @private
   */
  private createGeometryHandler: CreateHandler = new DummyCreateHandler();
  private updateGeometryHandler: GeometryUpdateHandler = new DummyGeometryUpdateHandler();
  private updateClassificationHandler: ClassificationUpdateHandler = new DummyClassificationUpdateHandler();
  private deleteChangeHandler: ChangeDeleteHandler = new DummyChangeDeleteHandler();
  private deleteGeometryHandler: GeometryDeleteHandler = new DummyGeometryDeleteHandler();
  private expandGeometryHandler: ExpandHandler = new DummyExpandHandler();

  /**
   * Calls `updateGeometryStackValidity` before triggering `changed`.
   * @private
   */
  private readonly onChange: EventEmitter<void> = new EventEmitter<void>();

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private userService: UserService,
    private changeApi: EntryChangeService,
    private projectService: ProjectService,
    private configService: EntryConfigService,
    private videoControl: VideoControlService,
    private videoValueService: VideoValueService,
    private entryValueApiService: EntryValueApiService,
    private workspaceController: WorkspaceControlService,
    private labNotificationService: LabNotificationService,
  ) {
    /*
     * To remove warnings about circular dependencies, initiate
     * the following services that depend also on this service.
     */
    this.entryValueApiService.valueService = this;
    this.changeApi.valueService = this;
  }

  /**
   * Helper-method to reset state when navigating away from label-mode (called in label-mode onDestroy)
   */
  public reset(): void {
    this.mediaClassificationValuesLoaded = false;
    this.valuesLoaded$.next(false);
    this.geometries = [];
    this.mediaClassifications = [];
    this.mediaClassificationsValid$.next(false);
    this.labelIterationId = '';
    this.valueObserver.teardown();
    this.deleteChangeHandler.tearDown();
    this.deleteGeometryHandler.tearDown();
    this.updateGeometryHandler.tearDown();
    this.updateClassificationHandler.tearDown();

    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Initialise this service for the current task.
   *
   * @param response
   * @param geometries the values to load. Do not use the task.labelIteration.entryValues attribute.
   * @param globalClassifications
   * @param benchmarkSetMode
   * @param valueStackFactory
   */
  public initValues(
    response: SingleTaskResponseModel,
    geometries: LcEntryGeometryValue[],
    globalClassifications: LcEntryClassificationValue[],
    benchmarkSetMode: boolean,
    valueStackFactory ?: ValueStackFactory
  ) {
    const task = response;
    this.media = task.media;
    this.labelTaskId = task.taskId;
    this.labelModeType = task.projectType;
    this.projectId = task.labelConfig.projectId;
    this.labelIterationId = task.labelIteration.id;
    this.labelConfigurationId = task.labelConfig.id;

    if (!!valueStackFactory) {
      this.valueStackFactory = valueStackFactory;
    }

    /**
     * Complete to recreate new one.
     */
    this.unsubscribe.next();
    this.unsubscribe.complete();

    this.createGeometryHandler.tearDown();
    this.createGeometryHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoCreateHandler(task.taskId, this.changeApi, this, this.videoControl, this.videoValueService.newValue)
      : new ImageCreateHandler(task.taskId, this.onChange, this.entryValueApiService);

    this.updateGeometryHandler.tearDown();
    this.updateGeometryHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoGeometryUpdateHandler(task.taskId, this.entryValueApiService, this.changeApi, this.videoControl, this.videoValueService.onUpdateValue)
      : new ImageGeometryUpdateHandler(task.taskId, this.entryValueApiService, this.onChange);

    this.updateClassificationHandler.tearDown();
    this.updateClassificationHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoClassificationUpdateHandler(task.taskId, this.entryValueApiService, this.videoControl, this.changeApi, this.videoValueService.onUpdateClassification, this.onChange)
      : new ImageClassificationUpdateHandler(task.taskId, this.entryValueApiService, this.onChange);

    this.deleteGeometryHandler.tearDown();
    this.deleteGeometryHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoGeometryDeleteHandler(this, this.entryValueApiService, this.workspaceController, this.changeApi, this.videoControl, this.videoValueService)
      : new ImageGeometryDeleteHandler(this, this.entryValueApiService, this.workspaceController, this.videoValueService.onDeleteValue);

    this.deleteChangeHandler.tearDown();
    this.deleteChangeHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoChangeDeleteHandler(this, this.entryValueApiService, this.workspaceController, this.changeApi, this.videoValueService)
      : new ImageChangeDeleteHandler();

    this.expandGeometryHandler.tearDown();
    this.expandGeometryHandler = this.labelModeType === LabelModeType.VIDEO
      ? new VideoExpandHandler(this.changeApi, this.videoControl, this.videoValueService.onUpdateValue)
      : new DummyExpandHandler();

    this.workspaceController.deleteAllGeometries();

    if (benchmarkSetMode) {
      this.initBenchmarkPermissions();
    }

    if (globalClassifications !== undefined && globalClassifications.length > 0) {
      this.mediaClassifications = globalClassifications;
      this.mediaClassificationValuesLoaded = true;
      this.updateGlobalClassificationListValidity(this.mediaClassifications);
    }

    // filter for geometryValues and update their validity
    this.geometries = geometries;
    this.updateGeometryStackValidity();

    this.onChange.subscribe(() => {
      this.updateGeometryStackValidity();
      this.changed.emit();
    });

    this.valueObserver.teardown();
    this.valueObserver = new ValueServiceValidityObserver(this);
  }

  /**
   * Get observable of global classifications if they are already loaded, or load them from the backend.
   *
   * If a task is the first time loaded, the global classifications entries are not set because that
   * values are never created. In this case, call the backend, otherwise return the pre loaded classifications.
   *
   * Todo: Check if it's possible to replace this with an behaviour subject.
   */
  public getMediaClassificationValues(): Observable<LcEntryClassificationValue[]> {
    if (this.mediaClassificationValuesLoaded) {
      return of(this.mediaClassifications);
    }
    const lcEntryValueCreateBindingModel: LcEntryValueCreateBindingModel =
      new LcEntryValueCreateBindingModel(this.labelIterationId, this.mediaId, 'null', null, this.labelTaskId);
    return this.entryValueApiService.getMediaClassificationValues(this.labelConfigurationId, lcEntryValueCreateBindingModel)
      .pipe(map((mediaClassifications: LcEntryClassificationValue[]) => {
        this.mediaClassifications = mediaClassifications;
        this.mediaClassificationValuesLoaded = true;
        this.updateGlobalClassificationListValidity(this.mediaClassifications);
        return mediaClassifications;
      }));
  }

  /**
   * Create already saved geometries in the workspace
   * Emit true on valuesLoaded$
   */
  public initSavedGeometriesFromValues(): void {

    if (!this.workspaceReady) {
      // Do nothing until the workspace is ready!
      return;
    }

    this.geometries.forEach((value: LcEntryGeometryValue) => {
      this.createGeometryInWorkSpace(value);
    });
    this.valuesLoaded$.next(true);
  }

  public createGeometryInWorkSpace(value: LcEntryGeometryValue): void {
    let geometryType: GeometryType;
    let geometryData: BaseGeometryData;
    const lcEntry = value.lcEntry as LcEntryGeometry;

    switch (value.lcEntry.type) {
      case LcEntryType.RECTANGLE:
        geometryType = GeometryType.RECTANGLE;
        const rectStartPoint = new WorkspacePoint((value as LcEntryRectangleValue).x, (value as LcEntryRectangleValue).y);
        geometryData = new RectangleGeometryData(rectStartPoint, (value as LcEntryRectangleValue).height, (value as LcEntryRectangleValue).width);
        break;
      case LcEntryType.POLYGON:
        geometryType = GeometryType.POLYGON;
        geometryData = new PolygonGeometryData((value as LcEntryPolyValue).points.map(p => new WorkspacePoint(p.x, p.y)));
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        geometryType = GeometryType.IMAGE_SEGMENTATION;
        geometryData = new ImageSegmentationGeometryData((value as LcEntryImageSegmentationValue).pointsCollection);
        break;
      case LcEntryType.LINE:
        geometryType = GeometryType.LINE;
        geometryData = new LineGeometryData((value as LcEntryLineValue).points.map(p => new WorkspacePoint(p.x, p.y)));
        break;
      case LcEntryType.POINT:
        geometryType = GeometryType.POINT;
        const workspacePoint = new WorkspacePoint((value as LcEntryPointValue).x, (value as LcEntryPointValue).y);
        geometryData = new PointGeometryData(workspacePoint);
        break;
      default:
        // May throw an error?
        // Just a precaution this should not be possible
        break;
    }

    // Segmentations cant have comments!
    if (value.lcEntry.type !== LcEntryType.IMAGE_SEGMENTATION) {
      geometryData.comment = value.comment || '';
    }

    const extended = {
      ...lcEntry,
      icon: LcEntryType.getIcon(lcEntry.type),
      lcEntryValueParentId: value.lcEntryValueParentId
    };
    const geometryProperties = new GeometryProperties(value.id, geometryType, extended);
    this.workspaceController.createGeometry(geometryProperties, geometryData);
  }

  /**
   * This Method created duplicated geometries. It contains all of the methods and logic, to draw
   * duplicated geometries and save them to the backend.
   */
  public createClonedGeometryInWorkSpace(target: LcEntryGeometryValue, source: LcEntryGeometryValue, mousePosition: Point): void {

    target.eat(source);

    if (!this.moveCopiedGeometry(target, mousePosition)) {
      const boundaryError = 'FEATURE.LABEL_MODE.ERROR.OUT_OF_BOUNDS';
      this.labNotificationService.info_i18(boundaryError);
      this.deleteGeometryValue(target);
      return;
    }

    this.addGeometryToStack(target);
    this.createGeometryInWorkSpace(target);
    this.updateGeometryById(target.id);

    // Only supports three nesting levels.
    const depth = 3;
    const flatChildren = ([target.children, ...target.children.map(c => c.children)] as unknown as WithFlat<LcEntryValue>).flat(depth);
    flatChildren.filter(c => LcEntryType.isClassification(c.lcEntry)).forEach(child => {
      this.updateClassificationValue(child as LcEntryClassificationValue, target.id);
    });
  }

  /**
   * Create a valueTree in the backend from the given LcEntryGeometry and return the observable
   * @param geometry: lcEntryGeometry to create valueTree from
   * @param lcEntryValueParentId: optional for nested geometries.
   */
  public createValuesByGeometry(geometry: LcEntryGeometry | string, lcEntryValueParentId: string = null): Observable<LcEntryGeometryValue> {
    const geometryId: string = typeof geometry === 'string' ? geometry : geometry.id;
    const body = new LcEntryValueCreateBindingModel(
      this.labelIterationId,
      this.mediaId,
      geometryId,
      lcEntryValueParentId,
      this.labelTaskId
    );

    return this.entryValueApiService.createValuesByGeometry(geometryId, body);
  }

  /**
   * Change the geometry type of an existing geometry.
   *
   * Note: this will delete all classifications without warning!
   *
   * @param id
   * @param newLcEntryId
   */
  public changeGeometryType(id: string, newLcEntryId: string): Observable<LcEntryGeometryValue> {
    return this.entryValueApiService.changeGeometryType(id, newLcEntryId);
  }

  /**
   * WorkspaceListener:
   * - WorkspaceEventType.DRAW_FINISHED
   * - WorkspaceEventType.DATA_UPDATED
   * - WorkspaceEventType.CREATE_SEGMENT_GEOMETRY
   *
   * Get the geometryValue by id and map the workspaceData to it.
   * Then update validity and update the value in the backend.
   *
   * @param geometryValueId: id of geometryValue to update
   */
  public updateGeometryById(geometryValueId: string): void

  /**
   * @param geometryValueId: id of geometryValue to update
   * @param onDrawFinished: Only true when triggered by 'WorkspaceEventType.DRAW_FINISHED'.
   */
  public updateGeometryById(geometryValueId: string, onDrawFinished: boolean): void;

  /**
   * Implementation of the above definitions.
   *
   * @param geometryValueId: id of geometryValue to update
   * @param onDrawFinished: Indicator to create a new one instead of updating a existing geometry.
   */
  public updateGeometryById(geometryValueId: string, onDrawFinished: boolean = false): void {
    const geometryValue: LcEntryGeometryValue = this.geometries.find(value => value.id === geometryValueId);
    this.updateGeometry(geometryValue, onDrawFinished);
  }

  /**
   * Synchronise the workspace to the geometry and update within the BE.
   *
   * @param geometryValue: The geometry to sync and update.
   * @param onDrawFinished: Boolean flag only true on drawFinish to create a new geometry instead of updating one. Required for video labeling.
   */
  public updateGeometry(geometryValue: LcEntryGeometryValue, onDrawFinished: boolean): void {

    if (geometryValue === undefined || geometryValue.isDeleted) {
      // undefined once caused by very fast clicking. Keep that guard.
      // May the delete request is running.
      return;
    }

    this.syncWorkspaceToGeometry(geometryValue);
    this.updateGeometryValueInBackend(geometryValue, onDrawFinished);
  }

  /**
   * Remove the geometry from stack. Removes also nested geometries.
   *
   * Note: this does not call the api.
   *
   * @param value
   */
  public removeGeometryFromStack(value: string | LcEntryGeometryValue): void {
    const id: string = typeof value === 'string' ? value : value.id;

    this.geometries = this.geometries
      .filter(valueInStack => valueInStack.id !== id)
      .filter(valueInStack => valueInStack.lcEntryValueParentId !== id);
    this.onChange.emit();
  }

  public addGeometryToStack(value: LcEntryGeometryValue): void {
    this.geometries.push(value);
    this.onChange.emit();
  }

  /**
   * Delete a geometryValue and its geometry in the workspace.
   * Doesn't delete in workspace if called by workspace CANCELED eventListener.
   *
   * @param value: value to delete
   */
  public deleteGeometryValue(value: LcEntryGeometryValue);

  /**
   * Delete a geometryValue and its geometry in the workspace.
   * Doesn't delete in workspace if called by workspace CANCELED eventListener.
   *
   * @param value: value to delete
   * @param config
   */
  public deleteGeometryValue(value: LcEntryGeometryValue, config: DeleteGeometryConfig);

  /**
   * Implementation of the above definitions.
   * @param value
   * @param arg
   */
  public deleteGeometryValue(value: LcEntryGeometryValue, arg?: DeleteGeometryConfig): void {
    if (!/*not*/!!value || value.isDeleted) {
      return;
    }
    if (this.deleteGeometryHandler.unsupportedKinds.includes(value.kind)) {
      return;
    }

    const frameNumber = !!arg && typeof arg === 'object' ? arg.frameNumber : undefined;
    const deleteGeometry = !!arg && typeof arg === 'object' ? arg.deleteGeometry : false;
    const deleteConfig = new DeleteGeometryConfig({frameNumber, deleteGeometry});

    this.deleteGeometryHandler.deleteGeometry(value, deleteConfig);
  }

  /**
   * Delete all change objects from the value with the frame number.
   *
   * @param value
   * @param frameNumber
   */
  public deleteChange(value: LcEntryGeometryValue, frameNumber: number): void;

  /**
   * Delete the given change object from its value *and* all other change objects
   * with the same frameNumber. (There should only be one, but who knows?)
   *
   * @param value
   * @param change
   */
  public deleteChange(value: LcEntryGeometryValue, change: LcEntryChange): void;

  /**
   * Implementation of the above definitions.
   *
   * @param value
   * @param arg
   */
  public deleteChange(value: LcEntryGeometryValue, arg: number | LcEntryChange): void {
    if (!/*not*/!!value || value.isDeleted) {
      return;
    }
    if (this.deleteChangeHandler.unsupportedKinds.includes(value.kind)) {
      return;
    }
    this.deleteChangeHandler.deleteChange(value, arg);
  }

  public expandVideoValueLine(value: LcEntryGeometryValue, currentStartChange: number, left: boolean): void {

    if (!/*not*/!!value) {
      return;
    }

    const change: LcEntryChange = value.change.find(c => c.frameNumber === currentStartChange);

    if (!/*not*/!!change) {
      return;
    }

    this.expandGeometryHandler.expandVideoValueLine(value, change, left);
  }

  /**
   * Update a classificationValue with children. Update rootGeometry if rootValueId is not null.
   * If handling globalClassifications (rootValueId === null) it also updates globalClassificationListValidity.
   * Emit onChange event to trigger updating of value-list
   * @param value: classificationValue to update
   * @param rootValueId: id of rootGeometry or null if called for globalClassification
   */
  public updateClassificationValue(value: LcEntryClassificationValue, rootValueId: string | null): void {
    // May the delete request is running.
    if (value.isDeleted) {
      return;
    }
    if (rootValueId === null) {
      this.updateGlobalClassificationListValidity(this.mediaClassifications);
    } else {
      // Store here the ids from all valid geometries.
      const previousValid = this.geometries
        .filter(geo => geo.valid)
        .map(geo => geo.id);

      /**
       * Todo: check what's going on here.
       */
        // Update the validity state locally
        // this.updateGeometryStackValidity();

        // List all geometries where the valid state was changed
      const changedGeometries = this.geometries
          .filter(geo => geo.valid && !previousValid.includes(geo.id) || !geo.valid && previousValid.includes(geo.id));

      // And update them within the backend.
      changedGeometries.forEach(geo => {
        this.updateGeometryValueInBackend(geo);
      });
    }

    this.updateClassificationHandler.updateClassification(value);
  }

  /**
   * Find the root value by the given value or it's id / it's parent id.
   *
   * Searches recursively through all geometries and classifications until no more
   * lcEntryValueParentId is set.
   *
   * @param entry
   */
  public findRootValue(entry: string | LcEntryValue): LcEntryValue {
    // Hotfix, sometimes a value is only within the videoValueService stack.
    const currentStack = new ValueStack(this.geometries, this.mediaClassifications);
    const rootValue = currentStack.findRootValue(entry);
    if (!!rootValue) {
      return rootValue;
    }
    return this.valueStackFactory.createValueStack().findRootValue(entry);
  }

  /**
   * Update the valid flags within the whole geometry stack.
   *
   * @private
   */
  private updateGeometryStackValidity() {
    const childGeometries = this.geometries.filter(geo => !!geo.lcEntryValueParentId);
    const rootGeometries = this.geometries.filter(geo => !/*not*/!!geo.lcEntryValueParentId);

    childGeometries.forEach(geo => geo.updateValidity());
    rootGeometries.forEach(geo => geo.updateValidity());

    const invalidChildren = childGeometries.filter(child => !child.valid);
    // Were only interested on the parent ids. The Set makes the array unique.
    const parentOfInvalidChildren = [...new Set(invalidChildren.map(child => child.lcEntryValueParentId))];

    // Every root geometry with a invalid child geometry is also invalid.
    rootGeometries
      .filter(geo => parentOfInvalidChildren.includes(geo.id))
      .forEach(geo => geo.valid = false);
  }

  /**
   * Synchronise the Value with the geometry properties from the workspace.
   *
   * @param geometryValue The geometry value to sync.
   * @private
   */
  private syncWorkspaceToGeometry(geometryValue: LcEntryGeometryValue): void {
    const data: BaseGeometryData = this.workspaceController.getGeometryData(geometryValue.id);
    if (data !== null && data !== undefined) {
      this.updateCommentOnGeometry(geometryValue.id, data.comment);
      /*
       * On video labeling it is possible to update a geometry that
       * is not loaded within this service.
       */
      switch (geometryValue.lcEntry.type) {
        case LcEntryType.POINT:
          (geometryValue as LcEntryPointValue).x = (data as PointGeometryData).point.x;
          (geometryValue as LcEntryPointValue).y = (data as PointGeometryData).point.y;
          break;
        case LcEntryType.LINE:
          (geometryValue as LcEntryLineValue).points = (data as LineGeometryData).points.map(p => new Point(p.x, p.y));
          break;
        case LcEntryType.POLYGON:
          (geometryValue as LcEntryPolyValue).points = (data as PolygonGeometryData).points.map(p => new Point(p.x, p.y));
          break;
        case LcEntryType.RECTANGLE:
          (geometryValue as LcEntryRectangleValue).height = (data as RectangleGeometryData).height;
          (geometryValue as LcEntryRectangleValue).width = (data as RectangleGeometryData).width;
          (geometryValue as LcEntryRectangleValue).x = (data as RectangleGeometryData).startPoint.x;
          (geometryValue as LcEntryRectangleValue).y = (data as RectangleGeometryData).startPoint.y;
          break;
        case LcEntryType.IMAGE_SEGMENTATION:
          (geometryValue as LcEntryImageSegmentationValue).pointsCollection =
            (data as ImageSegmentationGeometryData).pointsCollection;
          break;
        default:
          // Just a precaution, this should not be possible
          break;
      }
    }
  }

  /**
   * Update the geometry value in the BE.
   * @param value geometryValue to update
   * @private
   */
  private updateGeometryValueInBackend(value: LcEntryGeometryValue): void;

  /**
   * Update geometryValue in the backend.
   * @param value: geometryValue to update
   * @param onDrawFinished: Only true when triggered by 'WorkspaceEventType.DRAW_FINISHED'.
   */
  private updateGeometryValueInBackend(value: LcEntryGeometryValue, onDrawFinished: boolean): void;

  /**
   * Implementation of the above definitions.
   *
   * @param value
   * @param onDrawFinished
   * @private
   */
  private updateGeometryValueInBackend(value: LcEntryGeometryValue, onDrawFinished: boolean = false): void {
    if (value.isDeleted) {
      return;
    }
    this.configService.hasConfigChanged().subscribe((hasChanged: boolean) => {
      if (hasChanged) {
        const translateKey = 'FEATURE.LABEL_MODE.PLEASE_RELOAD';
        this.labNotificationService.warn_i18(translateKey);
        return;
      }

      if (onDrawFinished) {
        this.createGeometryHandler.createGeometry(value);
      } else {
        /*
         * Note: When (re)implementing the ability to create keyframes within
         * the video lines, pass the 'change object to create' to the following
         * call.
         *
         * >> `const currentFrameNumber = this.videoControl.currentFrameNumber;`
         * This doesn't work when creating a new change object via valueLine for
         * another frame as `this.videoControl.currentFrameNumber`.
         * Therefore the value line would 'visually remove' the change object
         * at `this.videoControl.currentFrameNumber` and render the valueLine
         * only after reloading as expected.
         *
         * >> `const currentFrameNumber = change2create.frameNumber;`
         * This would not work when moving a geometry within the label mode.
         * In that case 'change2create' is undefined and accessing the frameNumber
         * would raise an TypeError.
         */
        this.updateGeometryHandler.updateGeometry(value);
      }
    });
  }

  /**
   *  If user tries to enter the label-mode with mode=set-benchmark, validate if he has the
   *  permissions by checking if user is admin of project Org
   */
  private initBenchmarkPermissions(): void {
    this.projectService.getProjectById(this.projectId).subscribe((project: Project) => {
        this.benchmarkSetPermission = !!project && this.userService.isAdminFor(project.owner);
      },
      () => this.benchmarkSetPermission = false);
  }

  /**
   * Updates the Comment on a geometry for the backend
   * @param id: id of the geometry the comment is updated on
   * @param comment: the comment to be updated, if the comment is empty or undefined it gets deleted
   */
  private updateCommentOnGeometry(id: string, comment: string) {
    const match = this.geometries.find(geo => id === geo.id);
    if (!/*not*/!!match) {
      return;
    }

    /**
     * Todo: Check why the onChange event is here emitted.
     */

    if (comment !== undefined && comment !== null && comment.length > 0) {
      match.comment = comment;
      this.onChange.emit();
    } else {
      match.comment = '';
      this.onChange.emit();
    }
  }

  /**
   * Update the valid property of required globalClassifications and check children if applicable/necessary.
   */
  private updateGlobalClassificationListValidity(classificationsToCheck: LcEntryClassificationValue[]): void {
    for (const globalClassification of classificationsToCheck) {
      globalClassification.updateValidity();
    }
    const classificationsValid = classificationsToCheck.filter(c => !c.valid).length === 0;
    this.mediaClassificationsValid$.next(classificationsValid);
  }

  /**
   * Try to move the copied geometry:
   *
   * If no mouse position is given (copied via context menu) try to move the geometry with an offset.
   * Otherwise, try to 'flip' the geometry until it is within the workspace.
   *
   * @param geometryValue
   * @param mousePosition
   * @private
   */
  private moveCopiedGeometry(geometryValue: LcEntryGeometryValue, mousePosition: Point): boolean {

    const coordinate = 30;
    const moveOffset = new Point(coordinate, coordinate);
    let offsetPossibilities = [
      new Point(moveOffset.x, moveOffset.y),
      new Point(-moveOffset.x, moveOffset.y),
      new Point(moveOffset.x, -moveOffset.y),
      new Point(-moveOffset.x, -moveOffset.y),
    ];

    const workspaceSize = this.workspaceController.getWorkspaceSize();

    if (!!mousePosition) {
      geometryValue.moveTo(mousePosition);
      if (!geometryValue.outOfBounds(workspaceSize)) {
        return true;
      }

      const bb = geometryValue.getBoundingBox();

      offsetPossibilities = [
        new Point(-bb.x, 0),
        new Point(0, -bb.y),
        new Point(-bb.x, -bb.y),
      ];
    }

    for (const offset of offsetPossibilities) {
      geometryValue.move(offset);
      if (!geometryValue.outOfBounds(workspaceSize)) {
        return true;
      }
      // move back.
      geometryValue.move(new Point(
        offset.x * -1,
        offset.y * -1
      ));
    }
    return false;
  }
}
