import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-activated-dot',
  templateUrl: './activated-dot.component.html',
  styleUrls: ['./activated-dot.component.css']
})
export class ActivatedDotComponent {
  @Input('active')
  public active: boolean;
  /**
   * May add some additional css classes
   */
  @Input('class')
  public class: string | string[];
}
