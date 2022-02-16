import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-finanzielle-situation-aufteilung',
  templateUrl: './finanzielle-situation-aufteilung.component.html',
  styleUrls: ['./finanzielle-situation-aufteilung.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinanzielleSituationAufteilungComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
