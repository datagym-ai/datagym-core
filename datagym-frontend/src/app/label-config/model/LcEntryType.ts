import {LcEntry} from './LcEntry';


export enum LcEntryType {
  POINT = 'POINT',
  LINE = 'LINE',
  POLYGON = 'POLYGON',
  RECTANGLE = 'RECTANGLE',
  SELECT = 'SELECT',
  CHECKLIST = 'CHECKLIST',
  FREE_TEXT = 'FREETEXT',
  /**
   * A 'special' geometry
   */
  IMAGE_SEGMENTATION = 'IMAGE_SEGMENTATION',
  IMAGE_SEGMENTATION_ERASER = 'IMAGE_SEGMENTATION_ERASER',
}


export namespace LcEntryType {

  /**
   * Get the values of the enum.
   */
  export function values(): LcEntryType[] {
    return Object.keys(LcEntryType)
      // filter keys
      .filter(type => typeof type === 'string')
      // map to enum types
      .map(key => LcEntryType[key]);
  }

  /**
   * Returns the specific translationString for a given LcEntryType
   * @param type The specific type
   */
  export function toString(type?: LcEntryType): string {

    if (!/* not */!!type) {
      return 'GLOBAL.UNDEFINED';
    }

    switch (type) {
      case LcEntryType.SELECT:
        return 'GLOBAL.CLASSIFICATION_TYPES.OPTION';
      case LcEntryType.CHECKLIST:
        return 'GLOBAL.CLASSIFICATION_TYPES.CHECKLIST';
      case LcEntryType.FREE_TEXT:
        return 'GLOBAL.CLASSIFICATION_TYPES.FREE_TEXT';
      case LcEntryType.POINT:
        return 'GLOBAL.GEOMETRY_TYPES.POINT';
      case LcEntryType.POLYGON:
        return 'GLOBAL.GEOMETRY_TYPES.POLYGON';
      case LcEntryType.LINE:
        return 'GLOBAL.GEOMETRY_TYPES.LINE';
      case LcEntryType.RECTANGLE:
        return 'GLOBAL.GEOMETRY_TYPES.RECTANGLE';
      case LcEntryType.IMAGE_SEGMENTATION:
        return 'GLOBAL.GEOMETRY_TYPES.IMAGE_SEGMENTATION';
      case LcEntryType.IMAGE_SEGMENTATION_ERASER:
        return 'GLOBAL.GEOMETRY_TYPES.IMAGE_SEGMENTATION_ERASER';
    }
    return 'GLOBAL.UNDEFINED';
  }

  /**
   * Returns the specific translationString for a given LcEntryType
   * @param type The specific type
   */
  export function getName(type: LcEntryType): string {
    return toString(type);
  }

  /**
   * Returns the specific icon for each lcEntryType
   * @param type The specific type
   */
  export function getIcon(type: LcEntryType|LcEntry): string {
    type = LcEntryType.getLcEntryType(type);
    switch (type) {
      case LcEntryType.POINT:
        return 'icon-dg-point';
      case LcEntryType.POLYGON:
        return 'icon-dg-polygon';
      case LcEntryType.LINE:
        return 'icon-dg-line';
      case LcEntryType.RECTANGLE:
        return 'icon-dg-rectangle';
      case LcEntryType.IMAGE_SEGMENTATION:
        return 'fas fa-brush';
      case LcEntryType.IMAGE_SEGMENTATION_ERASER:
        return 'fas fa-eraser';
      case LcEntryType.CHECKLIST:
        return 'fas fa-check-square';
      case LcEntryType.SELECT:
        return 'fas fa-caret-square-down';
      case LcEntryType.FREE_TEXT:
        return 'fas fa-font';
    }
  }

  export function isGeometry(type: LcEntryType|LcEntry): boolean {
    if (!/*not*/!!type) {
      return false;
    }
    type = LcEntryType.getLcEntryType(type);
    switch (type) {
      case LcEntryType.RECTANGLE:
      case LcEntryType.POLYGON:
      case LcEntryType.LINE:
      case LcEntryType.POINT:
      case LcEntryType.IMAGE_SEGMENTATION:
      case LcEntryType.IMAGE_SEGMENTATION_ERASER:
        return true;
      default:
        return false;
    }
  }

  export function isClassification(type: LcEntryType|LcEntry): boolean {
    return !isGeometry(type);
  }

  export function getLcEntryType(type: LcEntryType|LcEntry): LcEntryType {
    type = typeof type === 'string' ? type : type.type;

    // May check if the type value is a valid LcEntryType.
    // const lcEntryTypeIdentifiers = Object.keys(LcEntryType)
    //   .map(key => LcEntryType[key])
    //   .filter(identifier => typeof identifier === 'string');
    // // Should not be possible if LcEntryType.KEY annotation
    // // is used but the enum values are treated as strings.
    // if (!lcEntryTypeIdentifiers.includes(type)) {
    //   throw new Error(`${ type } is not a valid LcEntryType.`);
    // }

    return type;
  }
}
