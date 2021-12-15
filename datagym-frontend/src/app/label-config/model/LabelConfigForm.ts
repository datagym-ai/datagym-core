import {RestrictionUtility} from '../../shared/validator/RestrictionUtility';

export class LabelConfigForm {

  public static readonly textFieldMaxLength = RestrictionUtility.labelConfig.textFieldMaxLength;
  public static readonly exportKey = RestrictionUtility.labelConfig.exportKey;
  public static readonly exportName = RestrictionUtility.labelConfig.exportName;


  // [1 .. 9, 0]
  public static getPossibleShortcuts(): string[] {
    return Array.from({ length: 10 })
      .map((value, index, self) => (index + 1) % self.length)
      .map(shortcut => `${ shortcut }`);
  }

}
