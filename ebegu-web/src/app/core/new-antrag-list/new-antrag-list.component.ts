import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'dv-new-antrag-list',
  templateUrl: './new-antrag-list.component.html',
  styleUrls: ['./new-antrag-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NewAntragListComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
