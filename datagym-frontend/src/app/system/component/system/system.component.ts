import { Component, OnInit } from '@angular/core';
import {SystemService} from "../../service/system.service";
import {InfoTO} from "../../model/InfoTO";

@Component({
  selector: 'app-system',
  templateUrl: './system.component.html',
  styleUrls: ['./system.component.css']
})
export class SystemComponent implements OnInit {

  public systemInfo: InfoTO = new InfoTO();

  constructor(private client: SystemService) { }

  ngOnInit() {
    this.client.systemInfo().subscribe((systemInfo: InfoTO) => {
      this.systemInfo = systemInfo;
    });
  }

}
