import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-tagesschulen-angaben',
  templateUrl: './tagesschulen-angaben.component.html',
  styleUrls: ['./tagesschulen-angaben.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagesschulenAngabenComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
