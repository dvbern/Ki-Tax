import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung',
  templateUrl: './ferienbetreuung.component.html',
  styleUrls: ['./ferienbetreuung.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
