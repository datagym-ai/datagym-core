import { MediaType } from '../../project/model/MediaType.enum';


export class DatasetCreateBindingModel {
  private owner: string;
  private name: string;
  private shortDescription?: string;
  private mediaType: MediaType;


  constructor(owner: string, name: string, mediaType: MediaType, shortDescription?: string) {
    this.name = name;
    this.owner = owner;
    this.mediaType = mediaType;
    if (shortDescription !== null) {
      this.shortDescription = shortDescription;
    }
  }
}
