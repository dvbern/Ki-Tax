import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-steuerveranlagung-gemeinsam',
  templateUrl: './steuerveranlagung-gemeinsam.component.html',
  changeDetection: ChangeDetectionStrategy.Default,
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagungGemeinsamComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
