

export enum InterpolationType {

  NONE = 'NONE',

  LINEAR = 'LINEAR',
}

export namespace InterpolationType {
  /**
   * Get the values of the enum.
   */
  export function values(): InterpolationType[] {
    return Object.keys(InterpolationType)
      // filter keys
      .filter(key => typeof InterpolationType[key] === 'string')
      // map to enum types
      .map(key => InterpolationType[key]);
  }

  export function inEnum(value2test: string|unknown): boolean {
    return typeof value2test === 'string' &&
      Object.keys(InterpolationType)
        .filter(type => typeof type === 'string')
        .includes(value2test);
  }
}
