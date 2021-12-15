import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loader',
  templateUrl: './loading-bar.component.html',
  styleUrls: ['./loading-bar.component.css']
})
export class LoadingBarComponent {
  public loadingText: string = 'LOADING_BAR.LOADER_TEXT';
  @Input()
  public isLoading: boolean = false;
}
