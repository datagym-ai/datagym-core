import {Injectable} from '@angular/core';
import {ScaledImageDimensions} from '../model/utility/ScaledImageDimensions';


/**
 * Workspace related utility service to outsource various top-level functions
 */
@Injectable({
  providedIn: 'root'
})
export class WorkspaceUtilityService {

  constructor() {
  }

  /**
   * Returns the optimal media width and height to fit in the given container
   * @param contains
   * @param containerWidth
   * @param containerHeight
   * @param width
   * @param height
   */
  public getObjectFitSize(contains /* true = contain, false = cover */, containerWidth, containerHeight, width, height) {
    const doRatio = width / height;
    const cRatio = containerWidth / containerHeight;
    let targetWidth;
    let targetHeight;
    const test = contains ? (doRatio > cRatio) : (doRatio < cRatio);

    if (test) {
      targetWidth = containerWidth;
      targetHeight = targetWidth / doRatio;
    } else {
      targetHeight = containerHeight;
      targetWidth = targetHeight * doRatio;
    }

    return new ScaledImageDimensions(targetWidth, targetHeight, (containerWidth - targetWidth) / 2, (containerHeight - targetHeight) / 2);
  }
}
