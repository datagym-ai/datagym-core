

export class LcEntryValueCreateBindingModel {
  public iterationId: string;
  public mediaId: string;
  public lcEntryId?: string;
  public lcEntryValueParentId?: string;
  public labelTaskId?: string;

  constructor(iterationId: string, mediaId: string, lcEntryId?: string, lcEntryValueParentId?: string, labelTaskId?: string) {
    this.iterationId = iterationId;
    this.mediaId = mediaId;
    this.lcEntryId = lcEntryId;
    this.lcEntryValueParentId = lcEntryValueParentId;
    this.labelTaskId = labelTaskId;
  }
}
