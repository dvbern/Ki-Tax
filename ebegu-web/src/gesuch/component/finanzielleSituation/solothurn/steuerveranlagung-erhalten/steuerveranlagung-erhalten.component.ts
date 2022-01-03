import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-steuerveranlagung-erhalten',
  templateUrl: './steuerveranlagung-erhalten.component.html',
  styleUrls: ['./steuerveranlagung-erhalten.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SteuerveranlagungErhaltenComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
