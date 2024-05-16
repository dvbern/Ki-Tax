import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {
    FinanzielleSituationSchwyzService,
    MassgebendesEinkommenResultate,
} from '../../../finanzielleSituation/schwyz/finanzielle-situation-schwyz.service';

const LOG = LogFactory.createLog('EinkommensverschlechterungSchwyzResultateComponent');

@Component({
    selector: 'dv-einkommensverschlechterung-schwyz-resultate',
    templateUrl: './einkommensverschlechterung-schwyz-resultate.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungSchwyzResultateComponent extends AbstractGesuchViewX<TSFinanzModel> implements OnInit {
    public resultate?: MassgebendesEinkommenResultate;

    private readonly BASISJAHR = 1;

    public constructor(
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardstepManager: WizardStepManager,
        private readonly finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService,
        private readonly cd: ChangeDetectorRef,
    ) {
        super(gesuchmodelManager, wizardstepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ);
    }

    public ngOnInit(): void {
        this.finanzielleSituationSchwyzService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.cd.detectChanges();
            }, error => LOG.error(error),
        );
        this.initModel();
        this.finanzielleSituationSchwyzService.calculateEinkommensverschlechterung(this.model, this.BASISJAHR);
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.model);
    }

    private initModel() {
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null, this.BASISJAHR);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(this.BASISJAHR);
    }

    public save(onResult: (arg: any) => any): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            this.wizardStepManager.getCurrentStepName(),
            TSWizardStepStatus.OK).then(() => {
            onResult(true);
        });
    }

    public hasMultipleFinSits(): boolean {
        return this.gesuchmodelManager.isGesuchsteller2Required()
            && EbeguUtil.isNotNullAndFalse(this.getGesuch().extractFamiliensituation().gemeinsameSteuererklaerung);
    }
}
