import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-freigabe',
  templateUrl: './freigabe.component.html',
  styleUrls: ['./freigabe.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FreigabeComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
