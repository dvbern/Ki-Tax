import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-verlauf',
  templateUrl: './verlauf.component.html',
  styleUrls: ['./verlauf.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerlaufComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
