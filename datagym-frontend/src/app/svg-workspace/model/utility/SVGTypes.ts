import {WorkspacePoint} from '../WorkspacePoint';
import {Element, PointArray} from 'svg.js';


/*
 * The svg-plugins sadly doesn't have type definitions. To use some
 * of the methods of the svg objects we must hide the data definition
 * otherwise the frontend would not compile.
 *
 * To avoid linting errors/warning 'The "any" type should not be used'
 * this type declarations can be used instead.
 *
 * e.g:
 * `this.svgObject.array();`
 * would not compile because TypeScript / the compiler means that the
 * "Property 'array' does not exist on type 'Element'." Of course, we
 * know that this property exists, so we can cast to any to avoid this.
 *
 * `(this.svgObject as any).array();`
 * would cause "Unexpected any. Specify a different type." warnings.
 *
 * Use the following line instead:
 * `(this.svgObject as unknown as WithArray).array();`
 *
 * Note: the first cast to unknown may be necessary to tell TypeScript
 * that the cast to `svgWithArray` is intended here even if the compiler
 * dont know about the existing properties of the svg object.
 *
 * Include it like
 * `import * as SVGType from '../../model/utility/SVGTypes';`
 *
 * And use it like
 * `(this.svgObject as unknown as SVGType.WithArray).array();`
 */

// Attributes
export type WithPoints = {points?: SVGPointList};
export type WithNode = {node : WithPoints};

export type count = {count : () => number};
export type filterPoints<T = any> = {filterPoints: (callback) => T[]};
export type insertPoint = {insertPoint : (index: number, point: [number, number]) => void};

// Methods
export type WithOff = {off : (name?: string) => Element};
export type WithRadius = {radius: (radius: number) => Element};
export type WithDraw = {draw : (ev?: string|Event) => Element};
export type WithArray = {array : (name?: string) => PointArray};
export type WithAsPath = {asPath : (attr?: {[key:string]:unknown}) => Element};
export type WithGetHelpingLinePoints = {getHelpingLinePoints : () => number[][]};
export type WithGetPoints = {getPoints : () => WorkspacePoint[]}; // Returns just an object with x & y properties, does not use the ctr.
// export type WithGetPointsStack = {getPoints : () => WorkspacePoint[][]}; // Returns just an object with x & y properties, does not use the ctr.
export type WithDraggable = {draggable: (config?: boolean|{[key:string]:unknown}) => Element};
export type WithGetScreenCTM = {getScreenCTM: () => DOMMatrix};
export type WithResize = {resize: (config: {[key:string]:unknown}) => WithDraggable};
export type WithPlot = {plot : (points: WorkspacePoint[]|number[]|number, y?:number, w?:number, d?:number) => Element};
export type WithSegment = {segment : (points: WorkspacePoint[], inner:WorkspacePoint[][]) => Element};
export type WithSegmentPlot = {plot : (points: WorkspacePoint[], inner:WorkspacePoint[][]) => Element};
export type WithSelectize = {selectize: (val?: boolean|{[key:string]:unknown}, val2?: boolean|{[key:string]:unknown}) => Element};
