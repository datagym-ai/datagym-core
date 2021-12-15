/**
 * To reduce module dependencies the `LcEntryGeometryValue` is not used here.
 */
export class ValueConfiguration {
  /**
   * Optional color argument. Use ${GeometryProperties.defaultColor} as default.
   */
  readonly color: string;

  /**
   * LcEntryGeometryId
   */
  readonly id: string;

  /**
   * lcEntryParentId to identify nested geometries.
   */
  readonly lcEntryParentId: string;

  /**
   * For nested geometries we need the parent's id to reduce unnecessary lookups.
   */
  readonly lcEntryValueParentId: string;

  /**
   * To display the geometry within the context menu we need the icon.
   */
  readonly icon: string;

  /**
   * To display the geometry within the context menu we need the entryValue as name.
   */
  readonly entryValue: string;

  /**
   * Create a GeometryConfiguration object with only undefined properties.
   *
   * @constructor Note: this does not call the constructor.
   */
  public static UNDEFINED(): ValueConfiguration {
    return {
      id: undefined,
      color: undefined,
      icon: undefined,
      entryValue: undefined,
      lcEntryParentId: undefined,
      lcEntryValueParentId: undefined
    };
  }

  /**
   * Create a GeometryConfiguration object with only undefined properties and some from the properties object.
   * @param properties Set these properties.
   * @constructor Note: this does not call the constructor.
   */
  public static WITH(properties: {[key: string]: string}): ValueConfiguration {
    const undef = ValueConfiguration.UNDEFINED();
    const keys = Object.keys(undef);

    const merged = {...undef, ...properties};

    // Remove all properties that are not included within the GeometryConfiguration class.
    Object.keys(merged).filter(key => !keys.includes(key)).forEach(key => delete merged[key]);

    return merged;
  }
}
