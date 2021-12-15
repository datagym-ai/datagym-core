
export enum LabelChoice {
  // Let the user choose between geometries and classifications
  NONE,
  // Let the user select a geometry type.
  GEOMETRY,
  // Let the user select a classification type.
  CLASSIFICATION,
  // Let the user select a geometry type expect the segmentation.
  GEOMETRY_WITHOUT_SEGMENTATION
}

/**
 * The 'create label modal' needs a proper configuration
 * to display either
 * - a selection screen between 'geometries' or 'classifications'
 * - a list of all 'classifications' (possible on all nesting levels)
 * - a list of all 'geometries' (for the root and first nesting level) only possible on the first nesting level.
 * - all geometries expect the media segmentation for nesting level 1.
 */
export class CreateLabelModalConfiguration {

  /**
   * Segmentations are only on the first level supported
   * and appear as geometry.
   */
  public get choice(): LabelChoice {
    if (this.labelChoice === LabelChoice.GEOMETRY && this.nestingLevel > 0) {
      return LabelChoice.GEOMETRY_WITHOUT_SEGMENTATION;
    }
    if (this.nestingLevel > 1) {
      return LabelChoice.CLASSIFICATION;
    }
    return this.labelChoice;
  }

  public set choice(choice: LabelChoice) {
    this.labelChoice = choice;
  }

  constructor(
    public id: string | null = null,
    private nestingLevel: number = 0,
    private labelChoice: LabelChoice = LabelChoice.NONE
  ) {}

}
