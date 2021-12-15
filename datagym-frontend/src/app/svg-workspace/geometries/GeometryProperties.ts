import {GeometryType} from './GeometryType';
import {UUID} from 'angular2-uuid';
import {ValueConfiguration} from './ValueConfiguration';


const DEFAULT_FILL_OPACITY = 0.2;

export class GeometryProperties {
  public readonly defaultColor: string = '#8000FF';

  /**
   * A unique identifier to identify event-bus updates and send commands to (like deletion, selection, etc.)
   */
  public readonly identifier: string;

  /**
   * For nested geometries store the parent id.
   */
  public readonly lcEntryValueParentId: string;

  /**
   * geometryType: The specific type of the geometry
   */
  public readonly geometryType: GeometryType;

  public readonly lcEntryId: string;
  public readonly lcEntryParentId: string;

  public readonly icon: string;
  public readonly name: string;

  public visible: boolean = true;
  public draggable: boolean = true;

  /**
   * Optional comment for geometry.
   */
  public get comment(): string {
    return this._comment;
  }

  /**
   * Just a 'workaround' to ensure that the comment property
   * is only set within `BaseGeometry.addCommentDecorator()`.
   */
  public _comment: string = undefined;

  public fill: boolean = true;
  public fillColor: string;
  public fillOpacity: number = DEFAULT_FILL_OPACITY;

  public border: boolean = true;
  public borderColor: string;
  public borderWidth: number = 1;

  /**
   * The specific color of the geometry, <undefined> will choose the default color
   *
   * @param color
   */
  public set color(color: string) {
    this.fillColor = color ? color : this.defaultColor;
    this.borderColor = color ? color : this.defaultColor;
  }

  public get color(): string {
    return this.fillColor;
  }

  /**
   * @param identifier required id
   * @param geometryType
   * @param configuration
   */
  constructor(identifier: string, geometryType: GeometryType, configuration: ValueConfiguration) {
    this.identifier = identifier;
    this.geometryType = geometryType;
    this.color = configuration.color;
    this.lcEntryId = configuration.id;
    this.lcEntryParentId = configuration.lcEntryParentId;
    this.name = configuration.entryValue;
    this.icon = configuration.icon;
    this.lcEntryValueParentId = configuration.lcEntryValueParentId;
  }

  /**
   * Create a new GeometryProperties with random identifier.
   *
   * @param geometryType
   * @param configuration
   * @constructor
   */
  public static RANDOM(geometryType: GeometryType, configuration: ValueConfiguration): GeometryProperties {
    return new GeometryProperties(UUID.UUID(), geometryType, configuration);
  }
}
