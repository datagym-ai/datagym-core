

export enum ContextMenuEventType {
  // Transform a set of geometries into another geometry type.
  TRANSFORM = 'TRANSFORM',
  // Hide all selected geometries.
  HIDE = 'HIDE',

  // DELETE is supported via message bus from workspace.

  REFINE_PREDICTION = 'REFINE_PREDICTION',

  DUPLICATE_GEOMETRY = 'DUPLICATE_GEOMETRY'
}
