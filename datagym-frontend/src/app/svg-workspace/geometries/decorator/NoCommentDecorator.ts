import {CommentDecorator} from './CommentDecorator';
import {Point} from '../../../label-mode/model/geometry/Point';


export class NoCommentDecorator extends CommentDecorator {

  public show(): void {
    // Nothing to do.
  }

  public hide(): void {
    // Nothing to do.
  }

  public move(position: Point): void {
    // Nothing to do.
  }

  public remove(): void {
    // Nothing to do.
  }

}
