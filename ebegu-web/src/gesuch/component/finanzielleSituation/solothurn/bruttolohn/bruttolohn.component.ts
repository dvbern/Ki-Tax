import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-bruttolohn',
  templateUrl: './bruttolohn.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class BruttolohnComponent implements OnInit {

  @Input() public model: TSFinanzielleSituationContainer;
  @Input() public onValueChange: () => void;

  public constructor(
      public gesuchModelManager: GesuchModelManager
  ) { }

  public ngOnInit(): void {
  }

  public isNotNullOrUndefined(toCheck: any): boolean {
    return EbeguUtil.isNotNullOrUndefined(toCheck);
  }
}
