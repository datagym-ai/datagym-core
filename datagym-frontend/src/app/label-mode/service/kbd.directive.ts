import {AfterContentInit, Directive, ElementRef} from '@angular/core';

/**
 * Use this directive to wrap the elements inner html into '<kbd>' tag
 * where useful to print them out as keyboard keys. Translates also some
 * strings like 'ARROW-UP' into the sign '↑'. For more translations see
 * the translation property. Only the words listed in the keys array are
 * wrapped in the kbd tag.
 *
 * E.g. `<span dgkbd>CTRL + ARROW-UP</span>` would be transformed
 * into `<span><kbd>CTRL</kbd> + <kbd>↑</kbd></span>`.
 */
@Directive({
  selector: '[dgkbd]'
})
export class KbdDirective implements AfterContentInit {

  /**
   * Some special chars to 'translate' before printing out.
   * @private
   */
  private readonly translations: {[key: string]: string} = {
    'arrow-keys': '↑ ↓ ← →',
    'arrow-left': '←',
    'arrow-right': '→',
    'arrow-up': '↑',
    'arrow-down': '↓',
  };

  /**
   * Wrapp only this words / signs in kbd tag.
   * @private
   */
  private readonly keys: string[] = ['ctrl', 'esc', 'del', 'shift', 'enter', '←', '→', '↑', '↓',];

  constructor(private el: ElementRef) {}

  ngOnInit(): void {
  }

  ngAfterContentInit(){

    const content = this.el.nativeElement.innerHTML
      .split('+').join(' + ')
      .split(' ');

    const translationKeys = Object.keys(this.translations);
    const translatedContent = content.map(
      (word: string) => translationKeys.includes(word.toLowerCase()) ? this.translations[word.toLowerCase()] : word
    ).join(' ');

    const newContent = translatedContent.split(' ').map(
      (word: string) => this.keys.includes(word.toLowerCase()) ? `<kbd>${ word }</kbd>` : word
    );

    this.el.nativeElement.innerHTML = newContent.join(' ');
  }
}
