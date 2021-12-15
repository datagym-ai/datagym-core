import {LegendSection} from "./LegendSection";

export class LegendConfiguration {

  /**
   * One or more sections are supported.
   */
  public sections: LegendSection[] = [];

  constructor(public title: string, public description?: string) {}

  public addSection(section: LegendSection): LegendConfiguration {
    this.sections.push(section);

    return this;
  }
}
