import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-bruttolohn',
  templateUrl: './bruttolohn.component.html',
  styleUrls: ['./bruttolohn.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BruttolohnComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

}
