import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'dv-gemeinde-antraege',
  templateUrl: './gemeinde-antraege.component.html',
  styleUrls: ['./gemeinde-antraege.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GemeindeAntraegeComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
