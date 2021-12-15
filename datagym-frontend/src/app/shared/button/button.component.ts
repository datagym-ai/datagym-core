import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

export interface AppButtonInput {
  label: string,
  type?: string,
  styling?: string,
  disabled?: boolean,
  args?: {[key: string]: string | number}
}

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.css']
})
export class ButtonComponent implements OnInit, AfterViewInit {
  @Input('label')
  public buttonLabel: string = '';

  @Input('type')
  public buttonType: string = 'button';

  @Input('styling')
  public buttonStyling: string = 'primary';

  @Input('disabled')
  public disabled: boolean = false;

  @Input('args') // some arguments to translate
  public args: {[key: string]: string | number} = {};

  @Input('css')
  public style: { [klass: string]: any; } = {};

  @Input()
  public title: string = '';

  @Output()
  public onClick: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  @Input('app-button-input')
  private buttonInput: AppButtonInput;

  @Input()
  public icon: string;

  ngOnInit(): void {
    if (this.buttonInput) {
      if (typeof this.buttonInput === 'string') {
        this.buttonLabel = this.buttonInput;
      } else {
        this.buttonLabel = this.buttonInput.label;
        this.buttonType = this.buttonInput.type;
        this.buttonStyling = this.buttonInput.styling;
        this.disabled = this.buttonInput.disabled;
        this.args = this.buttonInput.args || {};
      }
    }
  }

  ngAfterViewInit(): void {
    // 'text-align' from 'outside'
    const textAlign = 'text-align';
    this.style[textAlign] = !!this.style[textAlign] ? this.style[textAlign]
      // left / center depending on the icon state as default:
      : this.icon !== undefined ?  'left' : 'center';
  }

  public getNgClass() {
    switch (this.buttonStyling) {
      case 'primary':
        return 'app-button';
      case 'secondary':
        return 'app-button-filled';
      case 'secondary-full':
        return 'app-button-filled app-button-full';
      case 'warn':
        return 'app-button-warn';
      case 'warn-full':
        return 'app-button-warn app-button-full';
      case 'success':
        return 'app-button-success';
      case 'full':
      case 'button-full':
      case 'app-button-full':
        return 'app-button app-button-full';
      default:
        return 'app-button';
    }
  }

  public onClickButton(event: MouseEvent) {
    this.onClick.emit(event);
  }
}
