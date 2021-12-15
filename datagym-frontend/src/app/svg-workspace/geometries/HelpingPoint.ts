import {Circle} from "svg.js";

export class HelpingPoint {
  public helpingPoint: Circle;
  public distance: number;

  constructor(
    helpingPoint: Circle,
    distance: number) {

    this.helpingPoint = helpingPoint;
    this.distance = distance;
  }
}
