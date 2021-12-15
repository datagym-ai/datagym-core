import {InfoSection} from './InfoSection';

export class InfoConfiguration {

  /**
   * One or more sections are supported.
   */
  public sections: InfoSection[] = [];

  constructor(public title: string, public description?: string) {}

  public addSection(section: InfoSection): InfoConfiguration {
    this.sections.push(section);

    return this;
  }

}
