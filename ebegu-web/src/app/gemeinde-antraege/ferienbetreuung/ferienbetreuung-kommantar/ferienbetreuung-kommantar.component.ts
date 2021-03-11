import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung-kommantar',
  templateUrl: './ferienbetreuung-kommantar.component.html',
  styleUrls: ['./ferienbetreuung-kommantar.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungKommantarComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
