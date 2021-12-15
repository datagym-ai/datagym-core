import { MediaType } from './MediaType.enum';


export class ProjectCreateBindingModel {
  private owner: string;
  private name: string;
  private shortDescription: string;
  private description: string;
  private mediaType: MediaType;

  constructor(owner: string, name: string, shortDescription: string, description: string, mediaType: MediaType) {
    this.owner = owner;
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
    this.mediaType = mediaType;
  }
}
