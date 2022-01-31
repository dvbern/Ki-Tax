import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-steuerveranlagung-gemeinsam',
  templateUrl: './steuerveranlagung-gemeinsam.component.html',
  changeDetection: ChangeDetectionStrategy.Default,
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagungGemeinsamComponent implements OnInit {

  @Input() public model: TSFinanzModel;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
