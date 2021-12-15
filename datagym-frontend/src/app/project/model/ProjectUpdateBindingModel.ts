

export class ProjectUpdateBindingModel {
  private name: string;
  private shortDescription: string;
  private description: string;

  constructor(name: string, shortDescription: string, description: string) {
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
  }
}
