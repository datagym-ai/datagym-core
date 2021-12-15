import { EventEmitter, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NavBarService {
  public isFolded: EventEmitter<boolean> = new EventEmitter<boolean>();
  private navIsFolded: boolean = false;

  get folded(): boolean {
    return this.navIsFolded;
  }
  get hidden(): boolean {
    return this.navIsHidden;
  }

  public isHidden: EventEmitter<boolean> = new EventEmitter<boolean>();
  private navIsHidden: boolean = false;

  constructor() {
    this.isFolded.emit(false);
  }

  public toggleFoldedMode(): void {
    this.navIsFolded = !this.navIsFolded;
    this.isFolded.emit(this.navIsFolded);
  }

  public fold(): void {
    this.navIsFolded = true;
    this.isFolded.emit(this.navIsFolded);
  }

  public unFold(): void {
    this.navIsFolded = false;
    this.isFolded.emit(this.navIsFolded);
  }

  public toggleHiddenMode(): void {
    this.navIsHidden = !this.navIsHidden;
    this.isHidden.emit(this.navIsHidden);
  }

  public hide(): void {
    this.navIsHidden = true;
    this.isHidden.emit(true);
  }

  public show(): void {
    this.navIsHidden = false;
    this.isHidden.emit(false);
  }
}
