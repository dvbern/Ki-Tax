import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'interne-pendenzen-view',
  templateUrl: './interne-pendenzen.component.html',
  styleUrls: ['./interne-pendenzen.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InternePendenzenComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
