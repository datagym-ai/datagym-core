

export enum AiSegType {
  POINTS = 'POINTS',
  RECTANGLE = 'RECTANGLE',
  POINT = 'POINT',
  BRUSH = 'BRUSH',
  EDGE_LINE = 'EDGE_LINE',
}

export namespace AiSegType {

  export function toIcon(type: AiSegType): string {
    switch (type) {
      case AiSegType.POINTS:
        return 'fas fa-map-marker-alt';
      case AiSegType.RECTANGLE:
        return 'icon-dg-rectangle';
      case AiSegType.BRUSH:
        return 'fas fa-paint-brush';
      case AiSegType.POINT:
        return 'fas fa-crosshairs';
      case AiSegType.EDGE_LINE:
        return 'fas fa-highlighter';
      default:
        return '';
    }
  }

}
