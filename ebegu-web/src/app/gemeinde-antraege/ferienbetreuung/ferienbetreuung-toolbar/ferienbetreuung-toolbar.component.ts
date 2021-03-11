import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung-toolbar',
  templateUrl: './ferienbetreuung-toolbar.component.html',
  styleUrls: ['./ferienbetreuung-toolbar.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungToolbarComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
