import {Point} from '../../../label-mode/model/geometry/Point';


export abstract class CommentDecorator {

  public abstract show(): void;

  public abstract hide(): void;

  public abstract move(position: Point): void;

  public abstract remove(): void;
}
