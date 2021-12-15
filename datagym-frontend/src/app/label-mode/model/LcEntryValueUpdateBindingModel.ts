
/**
 * Todo: Are these values really required?
 *
 * Is the id as identifier not enough? And then just
 * the 'change' object or the value depending properties?
 *
 * Also check if the value.children property can be removed
 * (within the FE) because we don't need them here to update
 * the values.
 */
export class LcEntryValueUpdateBindingModel {
  public id: string;
  /**
   * @deprecated: Todo: Check if we can remove that property.
   */
  public lcEntryId: string;
  /**
   * @deprecated: Todo: Check if we can remove that property.
   */
  public lcEntryValueParentId: string;
  public valid: boolean;
  public labelTaskId: string;
  /**
   * @deprecated: Todo: Check if we can remove that property.
   */
  public comment?: string;

  constructor(id: string, lcEntryId: string, lcEntryValueParentId: string, valid: boolean, labelTaskId:string, comment?: string) {
    this.id = id;
    this.lcEntryId = lcEntryId;
    this.lcEntryValueParentId = lcEntryValueParentId;
    this.valid = valid;
    this.labelTaskId = labelTaskId;
    this.comment = comment;
  }
}
