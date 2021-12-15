import {Injectable} from '@angular/core';
import {DomSanitizer, SafeStyle} from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class MediaStyleFilterService {

  /**
   * Private media filters with public setter methods for casting or use 1 as default
   */
  private currentMediaContrast: number = 1;
  private currentMediaBrightness: number = 1;
  private currentMediaSaturation: number = 1;

  constructor(private sanitizer: DomSanitizer) {
  }

  get contrast() {
    return this.currentMediaContrast;
  }

  set contrast(value: string | number) {
    this.currentMediaContrast = Number(value) || 0;
  }

  get brightness() {
    return this.currentMediaBrightness;
  }

  set brightness(value: string | number) {
    this.currentMediaBrightness = Number(value) || 0;
  }

  get saturation() {
    return this.currentMediaSaturation;
  }

  set saturation(value: string | number) {
    this.currentMediaSaturation = Number(value) || 0;
  }

  /**
   * Template:
   * [style.filter]="workspace.mediaStyleFilter()"
   */
  public getFilter(): SafeStyle {

    const css = `
      contrast(${this.currentMediaContrast})
      saturate(${this.currentMediaSaturation})
      brightness(${this.currentMediaBrightness})
    `;

    return this.sanitizer.bypassSecurityTrustStyle(css);
  }

  /**
   * Reset the media filter settings to their default values
   */
  public resetFilters(): void {
    this.currentMediaContrast = 1;
    this.currentMediaBrightness = 1;
    this.currentMediaSaturation = 1;
  }
}
