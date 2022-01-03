import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-steuerveranlagung-gemeinsam',
  templateUrl: './steuerveranlagung-gemeinsam.component.html',
  styleUrls: ['./steuerveranlagung-gemeinsam.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SteuerveranlagungGemeinsamComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
