import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-lastenausgleich-ts',
  templateUrl: './lastenausgleich-ts.component.html',
  styleUrls: ['./lastenausgleich-ts.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTSComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
