
export enum LabelTaskState {
  BACKLOG = 'BACKLOG',
  WAITING = 'WAITING',
  WAITING_CHANGED = 'WAITING_CHANGED',
  IN_PROGRESS = 'IN_PROGRESS',
  SKIPPED = 'SKIPPED',
  COMPLETED = 'COMPLETED',
  REVIEWED = 'REVIEWED',
  REVIEWED_SKIP = 'REVIEWED_SKIP',

  /**
   * This state is just internally used and not
   * known within the backend. The preview demo
   * mode sets this state so the task control bar
   * only displays one 'leave demo mode' button
   * instead of 'skip', 'submit & exit' &
   * 'submit & next' buttons.
   */
  PREVIEW = 'PREVIEW',
  /**
   * This state is just internally used and not
   * known within the backend. The admin preview
   * mode sets this state so the task control bar
   * only displays one 'leave demo mode' button and
   * a message instead of 'skip', 'submit & exit' &
   * 'submit & next' buttons.
   */
  ADMIN_VIEW = 'ADMIN_VIEW'
}


export namespace LabelTaskState {

  export function getIcon(state: LabelTaskState): string {
    switch (state) {
      case LabelTaskState.BACKLOG:
        return 'icon fas fa-layer-group';
      case LabelTaskState.COMPLETED:
        return 'icon fas fa-check';
      case LabelTaskState.IN_PROGRESS:
        return 'icon fas fa-circle-notch';
      case LabelTaskState.SKIPPED:
        return 'icon fas fa-step-forward';
      case LabelTaskState.WAITING:
      case LabelTaskState.WAITING_CHANGED:
        return 'icon fas fa-stopwatch';
      case LabelTaskState.REVIEWED:
        return 'icon fas fa-check-double';
      case LabelTaskState.REVIEWED_SKIP:
        return 'icon fas fa-check-double';
      default:
        // just in case, some states were added to the enum.
        return '';
    }
  }

  export function getEndPoint(newState: LabelTaskState): string {
    let endpoint;
    switch (newState) {
      case LabelTaskState.IN_PROGRESS:
        // not supported & not available from 'outside'
        break;
      case LabelTaskState.WAITING_CHANGED:
        endpoint = 'skipToWC';
        break;
      case LabelTaskState.BACKLOG:
        endpoint = 'toBacklog';
        break;
      case LabelTaskState.COMPLETED:
        endpoint = 'completeTask';
        break;
      case LabelTaskState.SKIPPED:
        endpoint = 'skipTask';
        break;
      case LabelTaskState.WAITING:
        endpoint = 'toWaiting';
        break;
    }

    return endpoint;
  }

}
