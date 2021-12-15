import {CommentDecorator} from './CommentDecorator';
import {Container, Element} from 'svg.js';
import {Point} from '../../../label-mode/model/geometry/Point';


export class WithCommentDecorator extends CommentDecorator {

  private readonly svgObject;

  constructor(svgLayer: Container, position: Point, setSvgProperties: (Element) => void) {
    super();

    /*
     * Thanks to FontAwesome for this awesome work: comment-alt
     * @url: https://fontawesome.com/icons/comment-alt
     */
    const coordinates = 'M14 0H2C0.896875 0 0 0.896875 0 2V11C0 12.103125 0.896875 13 2 13H5V15.625' +
      'C5 15.93125 5.35 16.109375 5.596875 15.928125L9.5 13H14C15.103125 13 16 12.103125 16 11V2' +
      'C16 0.8968750000000001 15.103125 0 14 0Z';

    const commentBubble = svgLayer.path(coordinates).move(position.x, position.y);
    this.svgObject = (commentBubble as unknown as Element);
    setSvgProperties(this.svgObject);
  }

  public move(position: Point) {
    this.svgObject.move(position.x, position.y);
  }

  public remove(): void {
    this.svgObject.remove();
  }

  public show(): void {
    this.svgObject.show();
  }

  public hide(): void {
    this.svgObject.hide();
  }
}
