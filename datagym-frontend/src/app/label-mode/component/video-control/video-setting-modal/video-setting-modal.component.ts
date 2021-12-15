import {Component, OnInit} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Subject} from 'rxjs';
import {FormControl, FormGroup} from '@angular/forms';
import {VideoControlService} from '../../../service/video-control.service';
import {InterpolationService} from '../../../service/interpolation.service';
import {InterpolationType} from '../../../model/InterpolationType';


@Component({
  selector: 'app-video-setting-modal',
  templateUrl: './video-setting-modal.component.html',
  styleUrls: ['./video-setting-modal.component.css']
})
export class VideoSettingModalComponent implements OnInit {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public readonly modalId: string = 'VideoSettingModal';

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  public readonly formGroup = new FormGroup({
    'playbackRate': new FormControl(),
    'interpolationType': new FormControl(),
  });
  public hasOptions: boolean = true;

  public readonly playbackRates: {id: number, label: string}[]= [];

  /**
   * All possible InterpolationTypes are listed here.
   */
  public readonly interpolationTypes: string[] = InterpolationType.values();

  constructor(
    private modalService: NgxSmartModalService,
    private videoControl: VideoControlService,
    private interpolationService: InterpolationService
  ) {
    this.playbackRates = videoControl.playbackRates;
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Opens the video control menu via cog.
   */
  onOpenMenu() {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.setData(true);
    if (modal.hasData()) {
      this.preselectElements();
      modal.open();
    }
  }

  onClose() {
    const modal: NgxSmartModalComponent = this.modalService.get(this.modalId);
    modal.removeData();
    modal.close();
  }

  onCancel() {
    this.onClose();
  }

  onSave() {
    this.videoControl.playbackRate = this.formGroup.controls['playbackRate'].value as number;
    // Interpolation change is disabled until the BE supports changes (per geometry?).
    // this.formGroup.controls['interpolationType'].value as InterpolationType;
    this.interpolationService.interpolationType = InterpolationType.LINEAR;

    this.onClose();
  }

  /**
   * Preselect the settings.
   *
   * @private
   */
  private preselectElements(): void {
    /*
     * Find the index of the current speed on the fly. So we don't
     * depend on the number / order of options.
     * Not found means index -1 and is not allowed
     */
    const playbackRate = this.videoControl.playbackRate;
    const playbackRateIndex = this.playbackRates.findIndex(option => option.id === playbackRate);

    if (playbackRateIndex !== -1) {
      this.formGroup.controls['playbackRate'].reset(this.playbackRates[playbackRateIndex].id);
    }

    // Preselect LINEAR as it's the default value and the only one supported
    // until the BE is ready for different types.
    // const interpolationType = this.interpolationService.interpolationType;
    const interpolationType = InterpolationType.LINEAR;
    if (!!interpolationType) {
      this.formGroup.controls['interpolationType'].reset(interpolationType);
      this.formGroup.controls['interpolationType'].disable();
    }
  }
}
