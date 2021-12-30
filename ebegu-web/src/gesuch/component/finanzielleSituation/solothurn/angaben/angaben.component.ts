import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-angaben',
  templateUrl: './angaben.component.html',
  styleUrls: ['./angaben.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AngabenComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;
  @Input() public isGemeinsam: boolean;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
