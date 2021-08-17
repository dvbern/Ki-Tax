import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'dv-dummy-mandant-selection',
  templateUrl: './dummy-mandant-selection.component.html',
  styleUrls: ['./dummy-mandant-selection.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DummyMandantSelectionComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
