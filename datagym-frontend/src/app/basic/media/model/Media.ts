import {InvalidReason} from './InvalidReason';
import {MediaSourceType} from './MediaSourceType';


export abstract class Media {
  public id: string;

  /**
   * Todo: add the `url` property to the response for *all* media types.
   */
  public url: string;
  public timestamp: number;
  public mediaSourceType: MediaSourceType;
  public mediaName: string;
  public valid: boolean;
  public reason: InvalidReason | null;

}
