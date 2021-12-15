import {Injectable, Renderer2} from '@angular/core';

export interface EventHandler {
  element: HTMLElement;
  name: string;
  callback: (event?: any) => boolean | void;
  dispose: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class DomEventServiceService {

  constructor() { }

  addEvents(renderer: Renderer2, events: EventHandler[]): void {
    for (const event of events) {
      event.dispose = renderer.listen(event.element, event.name, newEvent => event.callback(newEvent));
    }
  }

  removeEvents(events: EventHandler[]): void {
    for (const event of events) {
      if (event.dispose) {
        event.dispose();
      }
    }
  }
}
