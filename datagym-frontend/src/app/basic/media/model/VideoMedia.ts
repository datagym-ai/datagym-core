import { Media } from './Media';

export class VideoMedia extends Media {

  public fps: number;
  public totalFrames: number;

  public width: number;
  public height: number;
  public awsKey: string;
  public lastError: string|null;
  public lastErrorTimeStamp: number|null;

  public size: number;
  public codecName: string;
  public formatName: string;
  public duration: number; // seconds.milliseconds
}
