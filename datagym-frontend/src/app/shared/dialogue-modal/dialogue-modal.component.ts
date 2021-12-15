import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core';
import { NgxSmartModalComponent, NgxSmartModalService } from 'ngx-smart-modal';
import { DialogueModel } from './DialogueModel';
import { DialogueService } from '../service/dialogue.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dialogue-modal',
  templateUrl: './dialogue-modal.component.html',
  styleUrls: ['./dialogue-modal.component.css']
})
export class DialogueModalComponent implements AfterViewInit, OnDestroy {
  public inputs: DialogueModel;
  public modalId: string = 'dialogueModal';
  private modal: NgxSmartModalComponent;
  private dataSub: Subscription;

  constructor(private modalService: NgxSmartModalService, private dialogService: DialogueService) {
  }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);
    this.dataSub = this.modal.onDataAdded.subscribe((data: DialogueModel) => {
      this.inputs = data;
    });
  }

  ngOnDestroy(): void {
    this.dataSub.unsubscribe();
  }

  public onClose() {
    this.dialogService.onCloseAction(false);
    this.modal.removeData();
  }

  public onAccept() {
    this.dialogService.onCloseAction(true);
    this.modal.close();
  }

  @HostListener('document:keydown', ['$event'])
  public handleKeyDownEvent(event: KeyboardEvent) {
    if (!this.modal.hasData()) {
      return;
    }
    if (event.key === 'Enter') {
      this.onAccept();
    }
  }
}
