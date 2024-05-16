import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';
import {TSGesuchstellerKardinalitaet} from '../../../../models/enums/TSGesuchstellerKardinalitaet';

@Component({
    selector: 'dv-familiensituation-schwyz',
    templateUrl: './familiensituation-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FamiliensituationSchwyzComponent extends AbstractFamiliensitutaionView {

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS,
        private readonly translate: TranslateService,
    ) {
        super(gesuchModelManager, errorService, wizardStepManager, familiensituationRS, authService);
        this.getFamiliensituation().familienstatus = TSFamilienstatus.SCHWYZ;
    }

    protected async confirm(onResult: (arg: any) => void): Promise<void> {
        const savedContainer = await this.save();
        onResult(savedContainer);
    }

    protected readonly TSGesuchstellerKardinalitaet = TSGesuchstellerKardinalitaet;

    public getBisherText(): string {
        return this.translate.instant(
            this.getFamiliensituationGS()?.gesuchstellerKardinalitaet === TSGesuchstellerKardinalitaet.ALLEINE ?
                'LABEL_NEIN' :
                'LABEL_JA');
    }

    public hasError(): boolean {
        return false;
    }
}
