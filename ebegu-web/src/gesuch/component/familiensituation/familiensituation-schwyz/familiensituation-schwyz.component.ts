import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSEinstellung} from '../../../../models/TSEinstellung';
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

const LOG = LogFactory.createLog('FamiliensituationSchwyzComponent');

@Component({
    selector: 'dv-familiensituation-schwyz',
    templateUrl: './familiensituation-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FamiliensituationSchwyzComponent extends AbstractFamiliensitutaionView implements OnInit {

    private readonly initialFamiliensituation: TSFamiliensituation;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS,
        private readonly einstellungRS: EinstellungRS,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
    ) {
        super(gesuchModelManager, errorService, wizardStepManager, familiensituationRS, authService);
        this.getFamiliensituation().familienstatus = TSFamilienstatus.SCHWYZ;
        this.initialFamiliensituation = this.gesuchModelManager.getFamiliensituation();
    }

    public ngOnInit(): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id,
        ).subscribe((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.MINIMALDAUER_KONKUBINAT)
                .forEach(value => {
                    this.getFamiliensituation().minDauerKonkubinat = Number(value.value);
                });
        }, error => LOG.error(error));
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
