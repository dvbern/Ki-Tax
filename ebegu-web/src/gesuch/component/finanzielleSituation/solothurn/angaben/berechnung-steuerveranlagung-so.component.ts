import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-berechnung-steuerveranlagung-so',
  templateUrl: './berechnung-steuerveranlagung-so.component.html',
  styleUrls: ['./berechnung-steuerveranlagung-so.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BerechnungSteuerveranlagungSoComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
