import {Observable} from 'rxjs';

/**
 * A generic configuration object consists of
 * key<-->value pairs with unique keys only.
 */
export type DoubleEventConfiguration = {
  eventName: string,
  [key: string]: number | string | boolean
};

/**
 * Register an double event listener on the given (svg) element.
 *
 * The observable emits the next() with the last emitted event as argument,
 * when the <element>.on(<eventName>) fires within a <timeout>.
 *
 * The element is required. It also *must* implement the 'on()' method.
 * All other settings can be overridden with an configuration object. In addition
 * to only override the eventName pass just the new name as second argument.
 *
 * The default settings are:
 * - eventName: 'click'
 * - timeout: 300,
 * - finish: false
 *
 * @deprecated Use 'dblclick' event instead of 'click'.
 *
 * @param element
 */
export function onDoubleEvent(element: any): Observable<any>;
export function onDoubleEvent(element: any, eventName: string): Observable<any>;
export function onDoubleEvent(element: any, config: DoubleEventConfiguration): Observable<any>;
export function onDoubleEvent(element: any, args?: DoubleEventConfiguration | string): Observable<any> {

  const defaultConfig = {
    eventName: 'click',
    timeout: 300,
    finish: false
  };

  // Handle all possible types of 'args' (undefined, string & object (doubleEventConfiguration)).
  const params = typeof args === 'string' ? {eventName: args} : typeof args === 'object' ? args : {};
  const config = Object.assign({}, defaultConfig, params);

  let watchDoubleClick: number = 0;
  return new Observable<any>((observer) => {
    element.on(config.eventName, (event: Event) => {
      watchDoubleClick++;
      setTimeout(() => {
        watchDoubleClick = 0;
      }, config.timeout);

      if (watchDoubleClick > 1) {
        observer.next(event);

        if (config.finish) {
          observer.complete();
        }
      }
    });
  });
}
