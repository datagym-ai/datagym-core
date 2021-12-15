import { Element } from "svg.js";

interface options {
  zoomFactor?: number,
  zoomMin?: number,
  zoomMax?: number
}

declare module "svg.js" {
  export interface Container {
    panZoom(options?: options): this
  }

  export interface Element{
    draw(): void
  }
}
