import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';
import {TSGesuchstellerKardinalitaet} from '../../../../models/enums/TSGesuchstellerKardinalitaet';
import {
    DvNgGsRemovalConfirmationDialogComponent,
    GSRemovalConfirmationDialogData,
} from '../dv-ng-gs-removal-confirmation-dialog/dv-ng-gs-removal-confirmation-dialog.component';
import {FamiliensituationUtil} from '../FamiliensituationUtil';

@Component({
    selector: 'dv-familiensituation-schwyz',
    templateUrl: './familiensituation-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FamiliensituationSchwyzComponent extends AbstractFamiliensitutaionView {

    private readonly initialFamiliensituation: TSFamiliensituation;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
    ) {
        super(gesuchModelManager, errorService, wizardStepManager, familiensituationRS, authService);
        this.getFamiliensituation().familienstatus = TSFamilienstatus.SCHWYZ;
        this.initialFamiliensituation = this.gesuchModelManager.getFamiliensituation();
    }

    protected async confirm(onResult: (arg: any) => void): Promise<void> {
        if (this.changeResetsGS2()
        ) {
            this.dialog.open<DvNgGsRemovalConfirmationDialogComponent, GSRemovalConfirmationDialogData>(
                DvNgGsRemovalConfirmationDialogComponent,
                {
                    data: {
                        gsFullName: this.getGesuch().gesuchsteller2
                            ? this.getGesuch().gesuchsteller2.extractFullName() : '',
                    },
                }).afterClosed().toPromise().then(async hasConfirmed => {
                if (hasConfirmed) {
                    onResult(await this.save());
                } else {
                    onResult(undefined);
                }
            });
            return;
        }
        onResult(await this.save());
    }

    private changeResetsGS2(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2) &&
            (this.isMutation() ||
                FamiliensituationUtil.isChangeFrom2GSTo1GS(this.initialFamiliensituation,
                    this.getFamiliensituation(),
                    this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis));
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
