import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './button/button.component';
import { TextfieldComponent } from './textfield/textfield.component';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LoadingBarComponent } from './loader/loading-bar.component';
import { TabGroupComponent } from './tab-group/tab-group.component';
import { TabComponent } from './tab-group/tab/tab.component';
import { RouterModule } from '@angular/router';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { LengthFilterPipe } from './service/length-filter.pipe';
import { DialogueModalComponent } from './dialogue-modal/dialogue-modal.component';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { GoBackComponent } from './go-back/go-back.component';
import { SearchFieldComponent } from './search-field/search-field.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { ActivatedDotComponent } from './activated-dot/activated-dot.component';
import { DgSelectOwnerComponent } from './dg-select-owner/dg-select-owner.component';
import { DgOwnerLabelComponent } from './dg-owner-label/dg-owner-label.component';
import { DynamicFilterComponent } from './dynamic-filter/dynamic-filter.component';
import { DynamicFilterElement } from './dynamic-filter/dynamic-filter-element.component';
import { DgSliderComponent } from './dg-slider/dg-slider.component';
import { DgLegendComponent } from './dg-legend/dg-legend.component';
import { HelpComponent } from './help/help.component';
import { DgInputErrorsComponent } from './dg-input-errors/dg-input-errors.component';
import { DgSelectModal } from './dg-select-modal/dg-select-modal';
import { BrowserSupportComponent } from './browser-support/browser-support.component';
import { DgInfoComponent } from './dg-info/dg-info.component';
import { DgExpandCollapseComponent } from './dg-expand-collapse/dg-expand-collapse.component';
import { DgActionIconComponent } from './dg-action-icon/dg-action-icon.component';
import { DgNotSupportedComponent } from './dg-not-supported/dg-not-supported.component';
import { DgSelectComponent } from './dg-select/dg-select.component';
import { DgSelectModalHelperComponent } from './dg-select-modal-helper/dg-select-modal-helper.component';

@NgModule({
  declarations: [
    ButtonComponent,
    TextfieldComponent,
    LoadingBarComponent,
    TabGroupComponent,
    TabComponent,
    FileUploadComponent,
    LengthFilterPipe,
    DialogueModalComponent,
    GoBackComponent,
    SearchFieldComponent,
    ActivatedDotComponent,
    DgSelectOwnerComponent,
    DgOwnerLabelComponent,
    DynamicFilterComponent,
    DynamicFilterElement,
    DgSliderComponent,
    DgLegendComponent,
    HelpComponent,
    DgInputErrorsComponent,
    DgSelectModal,
    BrowserSupportComponent,
    DgInfoComponent,
    DgExpandCollapseComponent,
    DgNotSupportedComponent,
    DgSelectComponent,
    DgSelectModalHelperComponent,
    DgActionIconComponent
  ],
  exports: [
    ButtonComponent,
    TextfieldComponent,
    LoadingBarComponent,
    TabGroupComponent,
    LengthFilterPipe,
    DialogueModalComponent,
    GoBackComponent,
    FileUploadComponent,
    SearchFieldComponent,
    ActivatedDotComponent,
    DgSelectOwnerComponent,
    DgOwnerLabelComponent,
    DynamicFilterComponent,
    DgSliderComponent,
    DgLegendComponent,
    HelpComponent,
    DgInputErrorsComponent,
    DgSelectModal,
    BrowserSupportComponent,
    DgInfoComponent,
    DgExpandCollapseComponent,
    DgNotSupportedComponent,
    DgSelectComponent,
    DgActionIconComponent,
    DgSelectModalHelperComponent
  ],
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    NgxSmartModalModule.forChild(),
    NgSelectModule
  ]
})
export class SharedModule {
}
