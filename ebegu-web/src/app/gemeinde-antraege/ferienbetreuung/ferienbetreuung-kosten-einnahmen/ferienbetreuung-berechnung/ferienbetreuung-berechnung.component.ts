import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung-berechnung',
  templateUrl: './ferienbetreuung-berechnung.component.html',
  styleUrls: ['./ferienbetreuung-berechnung.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungBerechnungComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
