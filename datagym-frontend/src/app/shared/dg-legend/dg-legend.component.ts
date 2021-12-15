import {AfterViewInit, Component, Input, OnDestroy} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from "ngx-smart-modal";
import {Subscription} from "rxjs";
import {LegendConfiguration} from "./LegendConfiguration";

@Component({
  selector: 'app-dg-legend',
  templateUrl: './dg-legend.component.html',
  styleUrls: ['./dg-legend.component.css']
})
export class DgLegendComponent implements AfterViewInit, OnDestroy  {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public modalId: string = "LegendModal";

  private dataSub: Subscription;
  private modal: NgxSmartModalComponent;

  @Input()
  public customClass: string = '';

  @Input('config')
  private defaultConfiguration: LegendConfiguration;

  private configuration: LegendConfiguration;

  /**
   * OnInit is not possible here, so we use AfterViewInit to set the data
   * with the legendService. To not raise some errors, display the modal &
   * it's content only if the configuration is set.
   */
  get initialised(): boolean {
    return !!this.configuration;
  }

  constructor(
    private modalService: NgxSmartModalService,
  ) { }

  get conf(): LegendConfiguration {
    return this.configuration;
  }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);

    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    this.dataSub = this.modal.onDataAdded.subscribe((configuration: LegendConfiguration | boolean) => {
      if (configuration === true || configuration === false) {
        configuration = this.defaultConfiguration;
      }
      this.configuration = configuration;
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
    this.modal.removeData();
  }
}
