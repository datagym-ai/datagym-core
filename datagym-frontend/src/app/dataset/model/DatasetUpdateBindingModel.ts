

export class DatasetUpdateBindingModel {
  private name: string;
  private shortDescription?: string;

  constructor(name: string, shortDescription?: string) {
    this.name = name;
    if (shortDescription!==null) {
      this.shortDescription = shortDescription;
    }
  }
}
