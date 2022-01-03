import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';

@Component({
  selector: 'dv-veranlagung-solothurn',
  templateUrl: './veranlagung-solothurn.component.html',
  styleUrls: ['./veranlagung-solothurn.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class VeranlagungSolothurnComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;
  @Input() public readOnly: boolean;

  @Output() public readonly massgebendesEinkommenChange: EventEmitter<number> = new EventEmitter<number>();

  public constructor() { }

  public ngOnInit(): void {
  }

}
