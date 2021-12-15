import {SVGObject} from './SVGObject';
import {VideoLineData} from './VideoLineData';
import {VideoBusEvent} from './VideoBusEvent';
import {Point} from '../geometry/Point';
import {Subject} from 'rxjs';
import {ContextMenuConfig} from "./ContextMenuConfig";



/**
 * Create an svg block for each value line like the following for  each bar.
 *
 *
 * `<svg height="1em" width="1257.8512396694216%" x="49.586776859504134%" y="0.5em">
 *   <defs>
 *     <pattern id="fill-{{ id }}" opacity="1" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
 *       <rect width="100" height="100" opacity="1" fill="#FB110D"></rect>
 *       <path stroke="{{ color | warnColor }}" opacity="1" stroke-width="4" d="{{ path }}"></path>
 *     </pattern>
 *     <svg id="background-{{ id }}" height="100%">
 *       <rect fill="url(#fill-{{ id }})" class="background-rectangle" opacity="0.8" height="100%" width="100%" rx="3px" ry="3px"></rect>
 *     </svg>
 *     <svg id="key-frame-{{ id }}" height="100%" width="0.0657030223390276%" viewBox="-50 -15 100 100">
 *       <rect class="frame-action" width="50%" height="50%" stroke-width="15%" transform="rotate(45)"
 *          stroke="{{ actionStrokeColor }}" fill="{{ actionFillColor }}"></rect>
 *     </svg>
 *   </defs>
 *
 *   <!-- the background rectangles -->
 *   <use x="0%" width="60%" href="#background-{{ id }}"></use>
 *   <use x="68%" width="32%" href="#background-{{ id }}"></use>
 *
 *   <!-- the frames with their actions lay above the background rectangles -->
 *   <use x="0%" href="#key-frame-{{ id }}"></use>
 *   <use x="99.93429697766098%" href="#key-frame-{{ id }}"></use>
 * </svg>`
 */
export class LineObject {
  private readonly line: VideoLineData;
  private readonly defs: SVGElement;
  private readonly backgroundTemplate: SVGElement;

  constructor(
    line: VideoLineData,
    index: number,
    node: Element,
    private readonly bus: Subject<VideoBusEvent>
  ) {
    this.line = line;

    node.setAttributeNS(null, 'width', line.svgWidth);

    /*
     * Let's add the defs section with templates for keyFrame, defaultFrame ans spaceFrame.
     */
    const defs = SVGObject.create('defs');

    const patternId = line.idFor('fill');
    const fill = `url(#${ patternId })`;

    /*
     * <pattern id="fill-XXX" opacity="0.8" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
     *   <rect fill="{{ color }}" width="100" height="100" opacity="1"></rect>
     *   <path stroke="{{ color | warningColor }}" opacity="1" stroke-width="4" d="{{ path }}"></path>
     * </pattern>
     */
    const patternRectangle = SVGObject.create('rect', {width: 100, height: 100, opacity: 1, fill: line.color});
    const patternPath = SVGObject.create('path', {stroke: line.color, opacity: 1, 'stroke-width': 4, d: 'M 0 0 L 20 20 M 10 -10 L 30 10 M -10 10 L 10 30'});
    const patternProps = {id: patternId, opacity: 1, x: 0, y: 0, width: 20, height: 20, patternUnits: 'userSpaceOnUse'};
    const pattern = SVGObject.create('pattern', patternProps, [patternRectangle, patternPath]);
    const backgroundRectangle = SVGObject.create('rect', {...line.backgroundRectangle, fill});
    const background = SVGObject.create('svg', line.background, backgroundRectangle);
    const keyFrameAction = SVGObject.create('rect', line.keyFrameAction);
    const keyFrame = SVGObject.create('svg', line.keyFrame, keyFrameAction);

    defs.append(pattern, background, keyFrame);
    node.appendChild(defs);

    this.backgroundTemplate = background;
    this.defs = defs;

    /*
     * Let's add as second chapter the background rectangles where necessary.
     */
    line.chunks.map((chunk) =>
      LineObject.chunkIntoRectangleData(chunk, line.chunks.length, line.sizeInFrames, line.innerFrameOffset)
    ).forEach((attributes, rectangleIndex) => {
      const rectangle = SVGObject.use(background, attributes);
      this.addContextMenuListener(rectangle, rectangleIndex);
      this.addSelectClickListener(rectangle);
      node.appendChild(rectangle);
    });

    /*
     * Let's add as third chapter the frames.
     */
    Array.from({length: line.sizeInFrames}).forEach((_, i: number) => {
      const indexOffset = i + line.innerFrameOffset;
      if (!this.line.keys.includes(indexOffset)) {
        // don't draw anything.
        return;
      }
      const href = line.keyFrameActionHref;
      const use = SVGObject.use(href, {x: `${i * line.frameBoxWidth}%`});
      const chunkOffset = line.chunks.findIndex(chunk => chunk.includes(indexOffset));
      this.addContextMenuListener(use, chunkOffset, indexOffset);
      node.appendChild(use);
    });

    // Deprecated until the valid flag is supported.
    // if (!/*not*/!!line.value.valid) {
    //   this.setInvalid();
    // }
  }

  public show(): void {
    this.toggleVisibility(true);
  }

  public hide(): void {
    this.toggleVisibility(false);
  }

  /**
   * Todo: call this method when the valid flag is supported in video labeling mode.
   */
  public setValid(): void {
    this.markAsInvalid(true);
  }

  /**
   * Todo: call this method when the valid flag is supported in video labeling mode.
   */
  public setInvalid() {
    this.markAsInvalid(false);
  }

  /**
   * Add the default context menu listener.
   *
   * @param element
   * @param index
   * @private
   */
  private addContextMenuListener(element: SVGElement, index: number): void;

  /**
   * Add the context menu listener with optional entry 'delete keyframe'.
   *
   * @param element the svg element to listen on.
   * @param index the index of the chunk group is required for some actions.
   * @param frameNumber delete keyframe for this frameNumber
   * @private
   */
  private addContextMenuListener(element: SVGElement, index: number, frameNumber: number): void;

  /**
   * Add the context menu listener.
   *
   * @param element the svg element to listen on.
   * @param index the index of the chunk group is required for some actions.
   * @param frameNumber display the 'delete keyframe' for this frameNumber action in context menu
   * @private
   */
  private addContextMenuListener(element: SVGElement, index: number, frameNumber: number = undefined): void{
    element.addEventListener('contextmenu', (event: MouseEvent) => {

      const point = Point.fromEvent(event);

      event.preventDefault();
      event.stopPropagation();

      const chunk = this.line.chunks[index];

      const config = new ContextMenuConfig(
        point,
        this.line.id,
        index,
        frameNumber,
        {
          min: Math.min(...chunk),
          max: Math.max(...chunk)
        }
      );

      this.bus.next(new VideoBusEvent('contextmenu', this.line.id, this, config));
    });
  }

  private addSelectClickListener(element: SVGElement): void {
    element.addEventListener('click', () => {
      this.bus.next(new VideoBusEvent('select', this.line.id));
    });
  }

  /**
   * 'hide' doesn't *hide* all of the nested elements nor the video line
   * value itself. Instead it hides all (key)frames and sets the opacity to 0.4;
   */
  private toggleVisibility(visible: boolean): void {
    const definitions = Array.from(this.defs.childNodes);
    const keyFrameTemplate = this.defs.lastChild as unknown as SVGElement;

    if (!!visible) {
      // Show all
      definitions.forEach(definition => (definition as SVGElement).style.display = 'inline');
      this.backgroundTemplate.style.opacity = '1';
      keyFrameTemplate.style.display = 'inline';
    } else {
      // Hide all
      keyFrameTemplate.style.display = 'none';
      this.backgroundTemplate.style.display = 'inline';
      this.backgroundTemplate.style.opacity = '0.4';
    }
  }

  /**
   * Flip on
   * - pattern.opacity: 0.8 <--> 1
   * - path.stroke: color <--> warnColor.
   *
   * @param invalid
   * @private
   */
  private markAsInvalid(invalid: boolean): void {
    const definitions = Array.from(this.defs.childNodes);
    const pattern = definitions.find(definition => definition.nodeName === 'pattern') as unknown as SVGElement;
    const path = Array.from(pattern.childNodes).find(el => el.nodeName === 'path') as unknown as SVGElement;

    if (!!invalid) {
      pattern.setAttributeNS(null, 'opacity', '0.8');
      path.setAttributeNS(null, 'stroke', this.line.color);
    } else {
      pattern.setAttributeNS(null, 'opacity', '1');
      path.setAttributeNS(null, 'stroke', this.line.warningColor);
    }
  }

  /**
   * Convert the video meta data chunks into position and width attributes.
   *
   * @param frameNumbers
   * @param length
   * @param sizeInFrames
   * @param innerFrameOffset
   * @private
   */
  private static chunkIntoRectangleData(frameNumbers: number[], length: number, sizeInFrames: number, innerFrameOffset: number): { x: string, width: string } {

    if (length === 1) {
      return {x: '0%', width: '100%'};
    }

    const centi = 100;
    const start = Math.min(...frameNumbers) - innerFrameOffset;
    const end = Math.max(...frameNumbers) - innerFrameOffset;
    const rectangleSizeInFrames = end + 1 - start;

    const x = `${start * centi / sizeInFrames}%`;
    const width = `${rectangleSizeInFrames * centi / sizeInFrames}%`;

    return {width, x};
  }
}
