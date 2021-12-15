import {LcEntryChangeType} from '../change/LcEntryChangeType';
import {LcEntryValue} from '../LcEntryValue';
import {LcEntryGeometry} from '../../../label-config/model/geometry/LcEntryGeometry';

/**
 * Describe a single video value line as ~immutable~ object.
 *
 * It's immutable expect the _keys property that can be filtered or extended.
 *
 * Also keep the svg properties to 'draw' the video value line.
 */
export class VideoLineData {
  public readonly id: string;

  /**
   * Stacked the frames into chunks.
   * E.g. each junk starts with an START and ends with an END change object
   * or contains only one START_CHANGE object.
   *
   * Each change object is then mapped to it's frame number.
   */
  public readonly chunks: number[][] = [];

  public get min(): number {
    return Math.min(...this.keys);
  }
  public get max(): number {
    return Math.max(...this.keys);
  }

  public get keys(): number[] {
    return this._keys;
  }
  public readonly sizeInFrames: number;
  public readonly frames: {[key: number]: LcEntryChangeType} = {};
  public readonly color: string;
  public readonly percentagePerFrame: number;

  public get innerFrameOffset(): number {
    return this.min;
  }

  public get frameBoxWidth(): number {
    const hundred = 100;
    return hundred / this.sizeInFrames;
  }

  public get frameBoxWidthPercentage(): string {
    return `${this.frameBoxWidth}%`;
  }

  public get svgWidth(): string {
    return `${this.sizeInFrames * this.percentagePerFrame}%`;
  }

  public get frameActionDefaults(): {} {
    return {
      class: 'frame-action',
      width: '50%',
      height: '50%',
      stroke: this.actionStrokeColor,
      'stroke-width': '15%',
      transform: 'rotate(45)'
    };
  }

  public get keyFrameActionHref(): string {
    return this.idFor('key-frame');
  }

  public get defaultFrameActionHref(): string {
    return this.idFor('default-frame');
  }

  public get frameDefaults() {
    return {
      height: '100%',
      width: this.frameBoxWidthPercentage,
      viewBox: '-50 -15 100 100',
    };
  }

  public get backgroundRectangle() {
    return {
      // class is required for mouse actions.
      class: 'background-rectangle',
      height: '100%',
      width: '100%',
      rx: '3px',
      ry: '3px',
      fill: this.color,
      opacity: '0.8'
    };
  }

  public get background() {
    return {
      id: this.idFor('background'),
      height: '100%',
    };
  }

  public get keyFrameAction() {
    return {
      ...this.frameActionDefaults,
      fill: this.actionFillColor
    };
  }

  public get keyFrame() {
    return {
      id: this.keyFrameActionHref,
      ...this.frameDefaults
    };
  }

  public readonly warningColor: string;

  private readonly actionFillColor: string;
  private readonly actionStrokeColor: string;
  private readonly _keys: number[] = [];

  public constructor(
    public readonly value: LcEntryValue,
    percentagePerFrame: number
  ) {

    const color = (value.lcEntry as LcEntryGeometry).color;

    const darkColor = '#333';
    const lightColor = '#fff';
    const isDark = VideoLineData.isDark(color);

    this.actionFillColor = isDark ? lightColor : darkColor;
    this.actionStrokeColor = isDark ? darkColor : lightColor;

    this.warningColor = VideoLineData.getWarningColor(color)

    this.id = value.id;
    this.color = (value.lcEntry as LcEntryGeometry).color;
    this.percentagePerFrame = percentagePerFrame;

    const frames: {[key: number]: LcEntryChangeType} = {};
    value.change.forEach(change => {
      frames[change.frameNumber] = change.frameType;
    });

    this.frames = frames;
    this._keys = value.frameNumbers;
    this.sizeInFrames = this.max - this.min + 1;

    this.chunks = this.partition(frames);
  }

  /**
   * Helper method to generate document wide unique id's for each svg element.
   * @param element
   */
  public idFor(element: string): string {
    return `${ element }-${ this.id }`;
  }

  /**
   * Do some magic with the change objects:
   *
   * sort them by frame number & split them into chunks starting
   * with an LcEntryChangeType.START object and ending with an
   * VideoKeyFrameType.END object.
   *
   * @private
   */
  private partition(changes: { [key: number]: LcEntryChangeType }): number[][] {

    /**
     * Split the video meta data into chunks starting with an
     * START frame and ending with an END frame.
     *
     * @param metas
     * @private
     */
    const splitter = (metas: { [key: number]: LcEntryChangeType }): number[][] => {

      const ret: number[][] = [];

      let currentStack: number[] = [];
      const keys = Object.keys(metas).map(m => +m);
      keys.forEach(key => {
        const type = metas[key];
        if (type === LcEntryChangeType.START || type === LcEntryChangeType.START_END) {
          currentStack = [];
        }
        currentStack.push(key);
        if (type === LcEntryChangeType.END || type === LcEntryChangeType.START_END) {
          ret.push(currentStack);
          currentStack = [];
        }
      });

      return ret;
    };

    return splitter(changes);
  }

  /**
   * Thanks to https://awik.io/determine-color-bright-dark-using-javascript/
   * @param color in form '#aa', '#abc' or '#aabbcc'.
   * @private
   */
  private static isDark(color: string): boolean {
    const [r, g, b] = VideoLineData.rgb(color);

    const f1 = 0.299;
    const f2 = 0.587;
    const f3 = 0.114;
    const half = 127.5; // 255 / 2

    const hsp = Math.sqrt(
      f1 * (r * r) +
      f2 * (g * g) +
      f3 * (b * b)
    );

    // Using the HSP value, determine whether the color is light or dark
    return hsp > half;
  }

  private static rgb(color: string): [number, number, number] {

    const two = 2;
    const three = 3;
    const four = 4;
    const six = 6;
    // let color = (value.lcEntry as LcEntryGeometry).color;
    if (color.indexOf('#') !== -1) {
      color = color.substr(1);
    }
    color = color.substr(0, six);
    let r: string, g: string, b: string;
    switch (color.length) {
      case two:
        r = color;
        b = color;
        g = color;
        break;
      case three:
        r = color.substr(0, 1);
        g = color.substr(1, 1);
        b = color.substr(two, 1);
        break;
      case six:
        r = color.substr(0, two);
        g = color.substr(two, two);
        b = color.substr(four, two);
        break;
      default:
      // nothing to do, not supported!
    }

    return [
      parseInt(`0x${r}`, 16),
      parseInt(`0x${g}`, 16),
      parseInt(`0x${b}`, 16)
    ];
  }

  protected static getWarningColor(color: string) {
    color = color.toUpperCase();
    /**
     * Colors are generated via material color tool.
     * The dg color is set as primary color and the light version
     * is selected.
     *
     * https://material.io/resources/color/#!/?view.left=0&view.right=1&primary.color=009688
     */
    const colors: [string, string][] = [
      ['#3333FF', '#7e61ff'],
      ['#6300ED', '#9e47ff'],
      ['#9A00CD', '#cf4dff'],
      ['#CD0066', '#ff5293'],
      ['#FB110D', '#ff5d3d'],
      ['#F141FA', '#bb00c6'], // #ff7cff
      ['#FAF45C', '#c4c225'], // #FAF45C
      ['#009A00', '#54cc40'],
      ['#66FF33', '#0ecb00'], // #a1ff6c
      ['#41FAD8', '#00c6a7'], // #84ffff
    ];

    return colors.find(c => c[0] === color)[1].toUpperCase();
  }
}
