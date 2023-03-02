import {Component, OnInit} from '@angular/core';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';



@Component({
    selector: 'dv-familiensituation-appenzell-view-x',
    templateUrl: './familiensituation-appenzell-view-x.component.html',
    styleUrls: ['./familiensituation-appenzell-view-x.component.less']
})
export class FamiliensituationAppenzellViewXComponent extends AbstractFamiliensitutaionView implements OnInit {

    public ngOnInit(): void {
    }

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS,
    ) {
        super(gesuchModelManager, errorService, wizardStepManager, familiensituationRS, authService);
    }

    public async confirm(onResult: (arg: any) => void): Promise<void> {
        const savedContaier = await this.save();
        onResult(savedContaier);
    }

}
