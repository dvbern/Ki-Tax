import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung-ui-view',
  templateUrl: './ferienbetreuung-ui-view.component.html',
  styleUrls: ['./ferienbetreuung-ui-view.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungUiViewComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
