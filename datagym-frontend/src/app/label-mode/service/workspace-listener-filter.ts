import {WorkspaceEventType} from '../../svg-workspace/messaging/WorkspaceEventType';
import {WorkspaceEventFilter} from '../../svg-workspace/service/WorkspaceEventFilter';

/**
 * This callback is used to filter the workspace events.
 */
export type FilterCallback = (WorkspaceEventType) => boolean;

/**
 * Generic way of filtering workspace events.
 */
export class WorkspaceListenerFilter implements WorkspaceEventFilter {

  /**
   * May create your own WorkspaceListenerFilter by passing a method of
   * type FilterCallback to the constructor.
   *
   * @param filterCallback
   */
  public constructor(private filterCallback: FilterCallback) {}

  public match(type: WorkspaceEventType): boolean {
    return this.filterCallback(type);
  }

  /**
   * The default implementation of 'eventFilter' listening to only
   * one WorkspaceEventType.
   *
   * Uses a FilterCallback method returning only true if the event type matches the given one.
   *
   * @param filterType WorkspaceEvent or WorkspaceEventType to filter for.
   */
  public static TYPE(filterType: WorkspaceEventType): WorkspaceListenerFilter {

    return new WorkspaceListenerFilter((eventType: WorkspaceEventType) => eventType === filterType);
  }

  /**
   * Creates an EventFilter with 'no filter'.
   *
   * Uses a FilterCallback method returning always true.
   *
   * For development purposes.
   */
  public static NO_FILTER(): WorkspaceListenerFilter {
    return new WorkspaceListenerFilter(() => true);
  }

  /**
   * Creates an EventFilter with filter about many types.
   *
   * Uses a FilterCallback method returning true if the event type is listed in the array.
   *
   * For development purposes.
   */
  public static TYPES(filterTypes: WorkspaceEventType[]) : WorkspaceListenerFilter {
    return new WorkspaceListenerFilter((eventType: WorkspaceEventType) => filterTypes.includes(eventType));
  }
}
