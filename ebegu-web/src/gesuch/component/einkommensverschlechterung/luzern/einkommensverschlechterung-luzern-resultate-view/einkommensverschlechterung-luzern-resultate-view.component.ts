import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {Transition} from '@uirouter/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';

const LOG = LogFactory.createLog('EinkommensverschlechterungLuzernResultateViewComponent');

@Component({
    selector: 'dv-einkommensverschlechterung-luzern-resultate-view',
    templateUrl: './einkommensverschlechterung-luzern-resultate-view.component.html',
    styleUrls: ['./einkommensverschlechterung-luzern-resultate-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungLuzernResultateViewComponent extends AbstractGesuchViewX<TSFinanzModel> {

    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;

    public resultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected ref: ChangeDetectorRef,
        private readonly $transition$: Transition,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.finSitLuService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.resultatProzent = this.calculateVeraenderung();
                this.ref.markForCheck();
            }, error => LOG.error(error),
        );
        this.calculate();
        this.resultatBasisjahr = null;
        this.calculateResultateVorjahr();
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }
        this.finSitLuService.calculateEinkommensverschlechterung(this.model, this.model.getBasisJahrPlus());
    }

    public calculateVeraenderung(): string {
        if (this.resultatBasisjahr) {
            const resultatJahrPlus1 = this.finSitLuService.getResultate(this.model);
            if (resultatJahrPlus1) {
                this.finSitLuService.calculateProzentualeDifferenz(
                    this.resultatBasisjahr.massgebendesEinkVorAbzFamGr, resultatJahrPlus1.massgebendesEinkVorAbzFamGr)
                    .then(abweichungInProzentZumVorjahr => {
                        this.resultatProzent = abweichungInProzentZumVorjahr;
                        this.ref.markForCheck();
                        return abweichungInProzentZumVorjahr;
                    });
            }
        }
        return '';
    }

    public calculateResultateVorjahr(): void {
        this.finSitLuService.calculateResultateVorjahr(this.model).then(resultatVorjahr => {
            this.resultatBasisjahr = resultatVorjahr;
            this.resultatProzent = this.calculateVeraenderung();
            this.ref.markForCheck();
        });
    }

    public isGemeinsam(): boolean {
        // if we don't need two separate antragsteller for gesuch, this is the component for both antragsteller together
        // or only for the single antragsteller
        return !FinanzielleSituationLuzernService.finSitNeedsTwoSeparateAntragsteller(this.gesuchModelManager)
            && EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
    }

    public getAntragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
    }

    public getAntragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }
}
