import { Component, Input, OnInit } from '@angular/core';
import { NgxSmartModalComponent, NgxSmartModalService } from 'ngx-smart-modal';
import { LabelConfigService } from '../../service/label-config.service';
import { LabelConfiguration } from '../../model/LabelConfiguration';

@Component({
  selector: 'app-import-configuration',
  templateUrl: './import-configuration.component.html',
  styleUrls: ['./import-configuration.component.css']
})
export class ImportConfigurationComponent implements OnInit {

  @Input()
  public configId: string = '';

  @Input()
  public isEmpty: boolean = false;

  @Input()
  public isDirty: boolean = false;

  @Input()
  public title: string = '';

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   */
  public readonly modalId = 'ImportLabelConfiguration';

  get uploadUrl(): string {
    return `/api/lconfig/${this.configId}/import`;
  }

  private get modal(): NgxSmartModalComponent {
    return this.modalService.get(this.modalId);
  }

  constructor(private modalService: NgxSmartModalService, private lcService: LabelConfigService) { }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
  }

  onOpenDialog() {
    // Don't allow the import while a configuration exists.
    if (!this.configId || !this.isEmpty || this.isDirty) {
      return;
    }
    const modal = this.modal;
    modal.setData(true);
    if (modal.hasData()) {
      modal.open();
    }
  }

  onClose() {
  }

  onUploadCompleted() {
    this.modal.close();
  }

  onFileCompleted(config: LabelConfiguration) {
    this.lcService.init(config);
  }
}
