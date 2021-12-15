import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, Output} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Subscription} from 'rxjs';
import {FormControl, FormGroup} from '@angular/forms';
import {DgSelectOptionModel} from './DgSelectOptionModel';

@Component({
  selector: 'app-dg-select-modal',
  templateUrl: './dg-select-modal.html',
  styleUrls: ['./dg-select-modal.css']
})
/**
 * @deprecated Use a custom implementation without service instead of this generic modal with an service.
 */
export class DgSelectModal implements AfterViewInit, OnDestroy {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  @Input('id')
  public modalId: string = 'DgSelectModal';

  @Output('left.btn.click')
  public leftClicked: EventEmitter<string> = new EventEmitter<string>();
  @Output('right.btn.click')
  public rightClicked: EventEmitter<string> = new EventEmitter<string>();

  @Input()
  public title: string = '';
  @Input()
  public label: string = '';
  @Input()
  public preselectFirstValue: boolean = true;

  @Input('left.btn.label')
  public leftButtonLabel: string = 'GLOBAL.CANCEL';
  @Input('left.btn.style')
  public leftButtonStyle: string = 'warn-full';

  @Input('right.btn.label')
  public rightButtonLabel: string = 'GLOBAL.CANCEL';
  @Input('right.btn.style')
  public rightButtonStyle: string = 'secondary-full';

  public options: DgSelectOptionModel[] = [];

  public formGroup = DgSelectModal.createForm();

  get hasOptions(): boolean {
    return !!this.options && this.options.length > 0;
  }

  get hasValue(): boolean {
    return !!this.value;
  }

  get value(): string {
    return this.formGroup.controls['options'].value || '';
  }

  private static createForm(): FormGroup {
    return new FormGroup({
      'options': new FormControl()
    });
  }

  private dataSub: Subscription;
  private modal: NgxSmartModalComponent;

  constructor(private modalService: NgxSmartModalService) { }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);

    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    this.dataSub = this.modal.onDataAdded.subscribe((options: DgSelectOptionModel[]) => {
      // recreate the form to clear it's state.
      this.formGroup = DgSelectModal.createForm();
      this.options = !!options ? options : [];

      if (this.preselectFirstValue) {
        this.preselectElementByIndex(0);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    if (this.modal) {
      this.modal.close();
    }
  }

  public onClose(): void {
    this.options = [];
    this.modal.removeData();
    this.modal.close();
  }

  public onLeftClick(): void {
    const value = this.value;
    this.onClose();
    this.leftClicked.emit(value);
  }

  public onRightClick(): void {
    const value = this.value;
    this.onClose();
    this.rightClicked.emit(value);
  }

  /**
   * Preselect the element at the given index.
   * @param index
   */
  private preselectElementByIndex(index: number) : void {
    if (this.options.length > index) { // length = max.index + 1
      this.formGroup.controls['options'].reset(this.options[index].id);
    }
  }
}
