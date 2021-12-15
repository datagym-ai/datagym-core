'use strict';

/**
 * @typedef {Object} Position
 *
 * @property {number} x1 - Start point x
 * @property {number} y1 - Start point y
 * @property {number} x2 - End point x
 * @property {number} y2 - End point y
 */

/**
 * @typedef {Object} Point
 *
 * @property {number} x
 * @property {number} y
 */


(function(root, factory){

  if(typeof define === 'function' && define.amd)
    define(['svg.js'], factory);

  else if(typeof module === 'object' && module.exports)
    module.exports = factory(require('svg.js'));

  else
    root.SVGIntersections = factory(root.SVG);

}(this, function(SVG){

  SVG.extend(SVG.Polyline, SVG.Polygon, {
    pop: function () {
      return pop(this);
    },
    count: function () {
      return this.array().value.length;
    },
    filterPoints: function (callback) {
      filterPoints(this, callback);
    }
  });

  SVG.extend(SVG.Polyline, {
    /**
     * Get the list of WorkspacePoints from polygon or polyline
     *
     * @return {WorkspacePoint[]}
     */
    getPoints: function () {
      return getPoints(this);
    },
    asPath: function (additionalAttributes) {
      return replaceWithPath(this, additionalAttributes);
    },
    /**
     * A helper function to create the helping points.
     *
     * Note: This function returns not a pair of the last and first point.
     *
     * returns array like: [ [p1, p2], [p2, p3], ... [p<n -1>, p<n>] ]
     */
    getHelpingLinePoints: function () {
      const drawedPoints = this.array();
      const lineTargets = drawedPoints.value.map((e, i, a) => [e, a[i + 1 < a.length ? i + 1 : 0]]);
      lineTargets.pop();
      return lineTargets;
    }
  });
  SVG.extend(SVG.Polygon, SVG.Polyline, {
    /**
     * A helper function to create the helping points.
     *
     * Note: This function returns also a pair of the last and first point.
     *
     * returns array like: [ [p1, p2], [p2, p3], ... [p<n>, p1] ]
     */
    getHelpingLinePoints: function () {
      const drawedPoints = this.array();
      return drawedPoints.value.map((e, i, a) => [e, a[i + 1 < a.length ? i + 1 : 0]]);
    },
    insertPoint: function (index, point) {
      insertPoint(this, index, point);
    }
  });
  SVG.extend(SVG.Path, {
    /**
     * Get the list of WorkspacePoints from polygon or polyline
     *
     * @return {WorkspacePoint[]}
     */
    getPoints: function () {
      return getPointsFromPath(this);
    },
    cutOut: function (points) {
      cutOutFromPath(this, points);
    }
  });
  SVG.extend(SVG.PointArray, {
    removeDoublePoints: function () {
      return removeDoublePoints(this);
    },
    isClockwise: function () {
      return isClockwise(this.value);
    }
  })

  /**
   * Insert a new point at the given index.
   * @param polyLike
   * @param index: position in the points stack
   * @param point: the new point as [x, y]
   */
  function insertPoint(polyLike, index, point) {
    polyLike.array().value.splice(index, 0, point);
  }

  /**
   * The pop() method removes the last point of the polygon
   * and returns it. This method changes the number of polygon points.
   */
  function pop(polyLike) {
    const values = polyLike.array().value;
    const value = values.pop();
    polyLike.plot(values);
    return value;
  }

  /**
   * Apply the filter callback to all points within the polygon or polyline
   * and plot the filtered points. Returns the new created array of points.
   *
   * @param polygon
   * @param callback
   * @return {number[]}
   */
  function filterPoints(polygon, callback) {
    const points = polygon.array();
    const filtered = points.value.filter(callback);
    polygon.plot(filtered);
    return filtered;
  }

  /**
   * Copy the stored data & use the same  color / opacity
   * for stroke and background may not work as expected.
   *
   * @param polyLine
   * @param attributes
   * @return {Polygon}
   */
  function replaceWithPath(polyLine, attributes) {

    let pointArray = polyLine.array();
    pointArray.removeDoublePoints();
    if (!pointArray.isClockwise()) {
      pointArray.reverse();
    }

    const path = polyLine.doc().path(`M${ pointArray.toString() }z`);

    copyAttributes(path, polyLine, attributes, 'points');

    polyLine.hide();
    polyLine.remove();

    return path;
  }

  /**
   * Convert the polygon points into Array
   * of Points with x & y coordinates.
   *
   * @param polygon
   * @return {[]}
   */
  function getPoints(polygon) {
    const drawedPoints = polygon.array();
    return castPointArrayToPointList(drawedPoints.value);
  }

  /**
   * Convert the path object into
   * a stacked points array.
   */
  function getPointsFromPath(path) {
    const pathArray = path.array();

    let stack = [];
    const final = [];

    pathArray.value.forEach(segment => {
      if (Array.isArray(segment)) {
        if (segment.length === 3) {
          stack.push([segment[1], segment[2]])
        } else {
          // filter following duplicate entries
          stack = stack.filter((element, index) => {
            return index === 0 || element[0] !== stack[index -1][0] && element[1] !== stack[index -1][1];
          });
          // remove the last point if it's the same as the first one.
          if (stack.length > 2 &&
            stack[0][0] === stack[stack.length - 1][0] &&
            stack[0][1] === stack[stack.length - 1][1]) {
            stack.pop();
          }

          final.push(stack);
          stack = [];
        }
      }
    });

    return final;
  }

  /**
   * Cut the polygon defined by the given points array
   * from the path object.
   *
   * @param path
   * @param points
   */
  function cutOutFromPath(path, points) {

    let array = new SVG.PointArray(points);
    array.removeDoublePoints();
    if (array.isClockwise()) {
      array.reverse();
    }

    const newPoints = new SVG.PathArray(`M${ array.toString() }z`);

    const current = path.array().value;
    current.push(...newPoints.value);

    path.plot(current);
  }

  /**
   * Remove all duplicate points if they follow the same one.
   *
   * e.g [[0, 0], [0,0] -> [[0, 0]]
   *
   * @param array
   * @return {svgjs.PointArray|*}
   */
  function removeDoublePoints (array) {
    array.value = array.value.filter((element, index) => {
      return index === 0 || element[0] !== array.value[index -1][0] && element[1] !== array.value[index -1][1];
    });
    return array;
  }

  /**
   * Check if the given points are clockwise ordered.
   *
   * Sum over the edges (x2 − x1)(y2 + y1).
   * https://stackoverflow.com/a/1165943
   *
   * @param points
   * @return {boolean}
   */
  function isClockwise(points) {

    if (points.length === 0) {
      return true;
    }

    let sum = 0;
    for (let i = 0; i < points.length; i++) {
      const point = points[i];
      const next = points[ i+1 === points.length ? 0 : i + 1];

      sum += ((next[0] - point[0]) * (next[1] + next[2]));
    }

    return 0 <= sum;
  }

  /**
   * Helper method to copy all attributes expect the id
   * from the source element to the target. Optional add
   * some additional arguments that should be set on the
   * target element.
   *
   * This function is for internal usage only.
   *
   * @param target the new element
   * @param source copy from this one.
   * @param additional object of key => value properties.
   * @param deletes: list of keys to delete
   */
  function copyAttributes(target, source, additional, deletes) {
    const attr = source.attr();
    delete attr['id'];

    deletes = !/*not*/!!deletes ? [] : deletes;
    deletes = typeof deletes === 'string' ? [deletes] : deletes;
    deletes = Array.isArray(deletes) ? deletes : [];
    deletes.forEach(del => {
      delete attr[del];
    });

    if (additional === undefined || additional === null || typeof additional !== 'object') {
      additional = {};
    }

    target.attr({...attr, ...additional});
  }

  /**
   * Cast the PointArray to points list with x & y
   *
   * @param drawedPoints
   * @return {[]}
   */
  function castPointArrayToPointList(drawedPoints) {
    const points = [];
    for (const point of drawedPoints) {
      const pointX = point[0];
      const pointY = point[1];

      points.push({x: pointX, y: pointY});
    }

    return points;
  }

  return {
    pop: pop,
    insertPoint: insertPoint,
    filterPoints: filterPoints,
    // SVG.Polyline
    getPoints: getPoints,
    // SVG.Path
    cutOut: cutOutFromPath,
    // SVG.PointArray
    removeDoublePoints: removeDoublePoints,
    isClockwise: isClockwise
  };

}));
