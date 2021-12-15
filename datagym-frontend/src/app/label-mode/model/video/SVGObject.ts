
/**
 * These are the supported svg types
 */
type svgTypes = 'svg' | 'defs' | 'rect' | 'use' | 'path' | 'pattern';


/**
 * Just a factory to create dom objects via 'document.createElementNS'.
 */
export class SVGObject {

  private static readonly namespace = 'http://www.w3.org/2000/svg';

  public static create(type: svgTypes, attributes: { [key: string]: string|number } = {}, children: SVGElement|SVGElement[] = []): SVGElement {

    const classes = this.extractClassesFromAttributes(attributes);
    const id = this.extractIdFromAttributes(attributes);

    delete attributes['id'];
    delete attributes['class'];

    const svgObject = document.createElementNS(this.namespace, type);
    if (!!id) {
      svgObject.id = id;
    }
    if (classes.length > 0) {
      svgObject.classList.add(...classes.filter(klass => !!klass));
    }

    Object.keys(attributes).forEach(key => {
      svgObject.setAttributeNS(null, key, `${attributes[key]}`);
    });

    // Append all given optional children.
    const childs = Array.isArray(children) ? children : [children];
    childs.forEach(child => svgObject.appendChild(child));

    return svgObject;
  }

  /**
   * Create a use element.
   *
   * Most attributes on use do not override those already on the element referenced by use.
   * Only the attributes x, y, width, height and href on the use element will override those
   * set on the referenced element. However, any other attributes not set on the referenced
   * element will be applied to the use element.
   *
   * Note:  width, and height have no effect on use elements, unless the element referenced has a viewbox
   * - i.e. they only have an effect when use refers to a svg or symbol element.
   *
   * @see: https://developer.mozilla.org/en-US/docs/Web/SVG/Element/use
   *
   * @param reference the id of the target or the target itself.
   * @param attributes
   */
  public static use(reference: string|SVGElement, attributes: { [key: string]: string } = {}) {
    const href = this.extractRef(reference);

    return this.create('use', {...attributes, href});
  }

  /**
   * Get the id from the reference.
   *
   * @param reference
   */
  public static extractId(reference: string|SVGElement): string {
    return typeof reference === 'string' ? reference : reference.id;
  }

  /**
   * Extract the id and prefix with '#' to use as href attribute.
   *
   * @param reference
   */
  public static extractRef(reference: string|SVGElement): string {
    const id = this.extractId(reference);
    return id.length === 0 ? '' : id.charAt(0) === '#' ? id : `#${ id }`;
  }

  private static extractClassesFromAttributes(attributes: { [key: string]: string|number }): string[] {

    const classes = {class: [], ...attributes}['class'];

    if (!/*not*/!!classes) {
      return [];
    }

    const classesArray = Array.isArray(classes) ? classes : [classes];
    return classesArray.filter(klass => !!klass);
  }

  private static extractIdFromAttributes(attributes: { [key: string]: string|number }): string {

    const id = {id: '', ...attributes}['id'];

    return typeof id === 'string' ? id : '';
  }
}
