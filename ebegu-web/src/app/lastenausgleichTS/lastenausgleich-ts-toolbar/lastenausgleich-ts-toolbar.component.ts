import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-lastenausgleich-ts-toolbar',
  templateUrl: './lastenausgleich-ts-toolbar.component.html',
  styleUrls: ['./lastenausgleich-ts-toolbar.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsToolbarComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
