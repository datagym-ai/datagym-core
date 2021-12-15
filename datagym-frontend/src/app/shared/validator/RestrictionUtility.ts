/**
 * Central
 */
export class RestrictionUtility {

  private static nameLength = 128;
  private static descriptionLength = 128;
  private static fullDescriptionLength = 1024;
  private static namePattern = '^[a-zA-Z0-9_ ]*$';

  /**
   * The '-' character seems to break the regex pattern '^[a-zA-Z0-9_ -,.\r\n]*$'
   * and evaluates to 'Matches a character in the range SPACE to "," (char code 32 to 44). Case sensitive.'.
   * Even a escaped '\-' does not work. So here is a full rewritten pattern. Let's explain:
   * - [a-zA-Z0-9]: Any alphanumeric sign,
   * - [_ ,.]: underscore, whitespace, comma or dot
   * - [-]: the minus sign
   * - [àâçéèêëîïôûùüÿñæœ]: french special chars
   * - [äöüß]: german umlauts and ß
   * - [\r\n]: for the newline
   * These pattern are concatenated with 'or' and can be repeated many times. E.g.: (PATTERN1 | PATTERN2)*
   *
   * @private
   */
  private static descriptionPattern = '^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüß])*$';
  /**
   * The same as above with additional newline chars '\r' & '\n'.
   * @private
   */
  private static multiLineDescriptionPattern = '^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüß]|[\r\n])*$';

  public static readonly project = {
    name: {
      maxLength: RestrictionUtility.nameLength,
      pattern: RestrictionUtility.namePattern
    },
    description: {
      maxLength: RestrictionUtility.descriptionLength,
      pattern: RestrictionUtility.descriptionPattern
    },
    fullDescription: {
      maxLength: RestrictionUtility.fullDescriptionLength,
      pattern: RestrictionUtility.multiLineDescriptionPattern
    }
  }

  public static readonly dataset = {
    name: {
      maxLength: RestrictionUtility.nameLength,
      pattern: RestrictionUtility.namePattern
    },
    description: {
      maxLength: RestrictionUtility.descriptionLength,
      pattern: RestrictionUtility.descriptionPattern
    }
  }

  public static readonly labelConfig = {
    exportKey: {
      maxLength: 30,
      pattern: '^[0-9a-z_\.\-]*$'
    },
    exportName: {
      maxLength: 56,
      pattern: '^[a-zA-Z0-9_ !@#$%^&*()?\\-àâçéèêëîïôûùüÿñæœäöüß]*$'
    },
    textFieldMaxLength: 255
  }

}
