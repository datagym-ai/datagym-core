import { Injectable } from '@angular/core';


interface Scripts {
  name: string;
  src: string;
}

// Define the script locations
export const ScriptStore: Scripts[] = [
  {name: 'svg.js', src: '/assets/svg/svg.js'},
  {name: 'svg.draw.js', src: '/assets/svg/svg.draw.js'},
  {name: 'svg.draggable.js', src: '/assets/svg/svg.draggable.js'},
  {name: 'svg.select.js', src: '/assets/svg/svg.select.js'},
  {name: 'svg.resize.js', src: '/assets/svg/svg.resize.js'},
  {name: 'svg.panzoom.js', src: '/assets/svg/svg.panzoom.js'},
  {name: 'svg.intersections.js', src: '/assets/svg/svg.intersections.js'},
  {name: 'svg.segment.js', src: '/assets/svg/svg.segment.js'},
  {name: 'svg.poly_extend.js', src: '/assets/svg/svg.poly_extend.js'},
];

/**
 * With this service you can load scripts dynamically. You use this script if you doesnt want to make the script global available
 * by adding it into the angular.json file in the scripts section.
 */
@Injectable({
  providedIn: 'root'
})
export class DynamicScriptLoaderService {

  private scripts: any = {};

  constructor() {
    ScriptStore.forEach((script: any) => {
      this.scripts[script.name] = {
        loaded: false,
        src: script.src
      };
    });
  }

  load(...scripts: string[]): Promise<any> {
    const promises: any[] = [];
    scripts.forEach((script) => promises.push(this.loadScript(script)));
    return Promise.all(promises);
  }

  loadScript(name: string): Promise<any> {
    return new Promise((resolve, reject) => {
      if (!this.scripts[name].loaded) {
        //load script
        const script: HTMLScriptElement = document.createElement('script');
        script.type = 'text/javascript';
        script.src = this.scripts[name].src;
        script.onload = () => {
          this.scripts[name].loaded = true;
          resolve({script: name, loaded: true, status: 'Loaded'});
        };
        script.onerror = (error: any) => resolve({script: name, loaded: false, status: 'Loaded'});
        document.getElementsByTagName('head')[0].appendChild(script);
      } else {
        resolve({script: name, loaded: true, status: 'Already Loaded'});
      }
    });
  }
}
