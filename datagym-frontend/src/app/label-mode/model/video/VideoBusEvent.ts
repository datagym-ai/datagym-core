import {LcEntryChange} from '../change/LcEntryChange';
import {LineObject} from './LineObject';
import {VideoLineSVG} from './VideoLineSVG';


export type VideoBusEventCommand =
  'select' |
  'contextmenu';


/**
 * The video bus is used to let the VideoControlComponent
 * communicate with the svg object of the video value line.
 */
export class VideoBusEvent {

  constructor(
    public readonly command: VideoBusEventCommand,
    public readonly valueId: string,
    public readonly target: LcEntryChange|LineObject|VideoLineSVG = null,
    public readonly payload = null
  ) {}

}
