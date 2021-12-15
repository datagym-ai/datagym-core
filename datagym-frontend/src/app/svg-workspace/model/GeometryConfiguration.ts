import {GeometryType} from '../geometries/GeometryType';

/**
 * A lightweight flatted version of the `LcEntryGeometry`
 * holding just the information necessary for the workspace.
 */

export class GeometryConfiguration {

  constructor(
    public readonly lcEntryId: string,
    public readonly parent: string,
    public readonly children: string[],
    public readonly color: string,
    public readonly name: string,
    public readonly icon: string,
    public readonly type: GeometryType
  ) {}

}
