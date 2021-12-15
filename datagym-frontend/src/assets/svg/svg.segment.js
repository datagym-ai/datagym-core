'use strict';

SVG.Segment = SVG.invent({
  // Initialize node
  create: 'path',
  // Inherit from,
  inherit: SVG.Path,

  extend: {
    plot: function (points, innerLines) {
      // simple validation of points
      points = !Array.isArray(points) ? [] : points;
      points = points.length > 0 ? points : [[0, 0]];
      // simple validation of innerLines
      innerLines = !Array.isArray(innerLines) ? [] : innerLines;
      innerLines = innerLines.map(inner => !Array.isArray(inner) ? [] : inner);
      // Force points to be clockwise
      points = this.isClockwise(points) ? points.splice(0).reverse() : points;
      const d = [this.points2pathArray(points)];
      // Force inner lines to be counter clockwise
      for (const innerLine of innerLines) {
        const innerPoints = !this.isClockwise(innerLine) ? innerLine.splice(0).reverse() : innerLine;
        d.push(this.points2pathArray(innerPoints));
      }
      const path = d.join(' ');
      return this.clear().attr('d', (this._array = path))
    },
    /**
     * Check if the given points are clockwise ordered.
     *
     * @param points
     * @return {boolean}
     */
    isClockwise(points) {
      return this.area(points) < 0;
    },
    /**
     * Calculate area of a ( 2D ) contour polygon.
     *
     * @param points
     * @return {number}
     */
    area(points) {
      const n = points.length;
      let a = 0.0;
      for ( let p = n - 1, q = 0; q < n; p = q ++ ) {
        a += points[ p ].x * points[ q ].y - points[ q ].x * points[ p ].y;
      }
      return a * 0.5;
    },
    /**
     * Just a wrapper to cast the given points into the expected PathArray.
     * @param points
     * @return {svgjs.PathArray}
     */
    points2pathArray(points) {
      return new SVG.PathArray(`M${ (new SVG.PointArray(points)).toString() }z`)
    }
  },

  // Add parent method
  construct: {
    // Create a wrapped path element
    segment: function (points, innerPoints) {
      // make sure plot is called as a setter
      return this.put(new SVG.Segment).plot(points || [], innerPoints || [])
    }
  }
});
