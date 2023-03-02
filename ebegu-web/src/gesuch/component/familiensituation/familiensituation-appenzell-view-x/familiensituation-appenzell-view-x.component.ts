import {Component, OnInit} from '@angular/core';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';



@Component({
    selector: 'dv-familiensituation-appenzell-view-x',
    templateUrl: './familiensituation-appenzell-view-x.component.html',
    styleUrls: ['./familiensituation-appenzell-view-x.component.less']
})
export class FamiliensituationAppenzellViewXComponent extends AbstractFamiliensitutaionView implements OnInit {

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager);
    }

    public ngOnInit(): void {
    }

}
