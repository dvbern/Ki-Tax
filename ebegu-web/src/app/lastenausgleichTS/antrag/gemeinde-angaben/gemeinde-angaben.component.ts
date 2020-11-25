import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-gemeinde-angaben',
  templateUrl: './gemeinde-angaben.component.html',
  styleUrls: ['./gemeinde-angaben.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GemeindeAngabenComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
