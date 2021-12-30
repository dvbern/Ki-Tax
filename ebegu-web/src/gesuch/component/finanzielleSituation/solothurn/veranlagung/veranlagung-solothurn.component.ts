import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';

@Component({
  selector: 'dv-veranlagung-solothurn',
  templateUrl: './veranlagung-solothurn.component.html',
  styleUrls: ['./veranlagung-solothurn.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VeranlagungSolothurnComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;
  @Input() public readOnly: boolean;

  @Output() public readonly massgebendesEinkommenChange: EventEmitter<number> = new EventEmitter<number>();

  public constructor() { }

  public ngOnInit(): void {
  }

}
