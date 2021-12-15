import {LcEntry} from './LcEntry';
import {LcEntryType} from './LcEntryType';
import {LcEntryRectangle} from './geometry/LcEntryRectangle';
import {LcEntryGeometry} from './geometry/LcEntryGeometry';
import {LcEntryLine} from './geometry/LcEntryLine';
import {LcEntryPoint} from './geometry/LcEntryPoint';
import {LcEntryPolygon} from './geometry/LcEntryPolygon';
import {LcEntryText} from './classification/LcEntryText';
import {LcEntryClassification} from './classification/LcEntryClassification';
import {LcEntryChecklist} from './classification/LcEntryChecklist';
import {HasOptionsMap} from './classification/HasOptionsMap';
import {LcEntrySelect} from './classification/LcEntrySelect';
import {PreLabelGeometry} from '../../ai-assistant/model/PreLabelGeometry';
import {LcEntryImageSegmentation} from './geometry/LcEntryImageSegmentation';


export class LcEntryFactory {

  /**
   * Cast the entry list properly
   */
  public static castEntriesListProperly(entries: LcEntry[]): LcEntry[] {
    return entries.map((e : LcEntry) => LcEntryFactory.castEntriesProperly(e));
  }

  /**
   * Need to cast all of this so it has the actual LcEntry properties (typescript casting only works with constructor)
   * @param entry
   */
  public static castEntriesProperly(entry: LcEntry): LcEntry {
    if (entry === undefined) {
      return undefined;
    }
    let castEntry: LcEntry;
    switch (entry.type) {
      case LcEntryType.RECTANGLE:
        castEntry = new LcEntryRectangle(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.LINE:
        castEntry = new LcEntryLine(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.POINT:
        castEntry = new LcEntryPoint(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.POLYGON:
        castEntry = new LcEntryPolygon(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        castEntry = new LcEntryImageSegmentation(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.FREE_TEXT:
        castEntry = new LcEntryText(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryText).maxLength);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.CHECKLIST:
        castEntry = new LcEntryChecklist(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryClassification & HasOptionsMap).options);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
      case LcEntryType.SELECT:
        castEntry = new LcEntrySelect(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryClassification & HasOptionsMap).options);
        castEntry.id = entry.id;
        castEntry.children = entry.children.map(c => LcEntryFactory.castEntriesProperly(c));
        break;
    }
    return castEntry;
  }

  public static copyEntry(entry: LcEntry): LcEntry {
    if (entry === undefined) {
      return undefined;
    }
    let castEntry: LcEntry;
    switch (entry.type) {
      case LcEntryType.RECTANGLE:
        castEntry = new LcEntryRectangle(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.LINE:
        castEntry = new LcEntryLine(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.POINT:
        castEntry = new LcEntryPoint(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.POLYGON:
        castEntry = new LcEntryPolygon(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        castEntry = new LcEntryImageSegmentation(
          entry.lcEntryParentId,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryGeometry).color,
          (entry as LcEntryGeometry).shortcut);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.FREE_TEXT:
        castEntry = new LcEntryText(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryText).maxLength);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.CHECKLIST:
        castEntry = new LcEntryChecklist(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryClassification & HasOptionsMap).options);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
      case LcEntryType.SELECT:
        castEntry = new LcEntrySelect(
          entry.lcEntryParentId,
          (entry as LcEntryClassification).required,
          entry.entryKey,
          entry.entryValue,
          (entry as LcEntryClassification & HasOptionsMap).options);
        castEntry.children = entry.children.map(c => LcEntryFactory.copyEntry(c));
        break;
    }
    return castEntry;
  }

  public static createEmptyLcEntryFromPreLabelGeometry(preLabelGeometry: PreLabelGeometry): LcEntry {
    let castEntry: LcEntry;
    switch (preLabelGeometry.type) {
      case LcEntryType.RECTANGLE:
        castEntry = new LcEntryRectangle(
          null,
          preLabelGeometry.entryKey,
          preLabelGeometry.entryValue,
           null, null);
        delete castEntry.id;
        break;
      case LcEntryType.POLYGON:
        castEntry = new LcEntryPolygon(
          null,
          preLabelGeometry.entryKey,
          preLabelGeometry.entryValue,
          null,null);
        delete castEntry.id;
        break;
    }
    return castEntry;
  }

}
