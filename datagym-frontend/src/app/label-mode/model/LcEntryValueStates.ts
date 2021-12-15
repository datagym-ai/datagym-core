

export enum LcEntryValueStates {
  DEFAULT = 'DEFAULT',
  ACTIVE = 'ACTIVE',
  HIDDEN = 'HIDDEN',
  ERROR = 'ERROR'
}

export namespace LcEntryValueStates {

  export function getColorClass(state: LcEntryValueStates): string {
    let color = '';
    switch (state) {
      case LcEntryValueStates.ACTIVE:
        color = 'dg-primary-color';
        break;
      case LcEntryValueStates.ERROR:
        color = 'dg-warn-color';
        break;
      case LcEntryValueStates.HIDDEN:
        color = 'value-hidden';
        break;
      case LcEntryValueStates.DEFAULT:
        // Fall through
      default:
        // Fall through
        break;
    }
    return color;
  }
}
