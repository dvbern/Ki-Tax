import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../AbstractFinSitsolothurnView';
import {FinanzielleSituationSolothurnService} from '../finanzielle-situation-solothurn.service';

@Component({
    selector: 'dv-finanzielle-situation-start-solothurn',
    templateUrl: './finanzielle-situation-solothurn.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartSolothurnComponent extends AbstractFinSitsolothurnView {

    @ViewChild(NgForm) private readonly form: NgForm;

    public sozialhilfeBezueger: boolean;
    public finanzielleSituationRequired: boolean = true;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected readonly finSitSoService: FinanzielleSituationSolothurnService,
        protected wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, finSitSoService, 1);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public ngOnInit(): void {
        // verguenstigungGewunscht ist alway true for Solothurn, expect when sozialhilfeempfaenger is true
        this.model.verguenstigungGewuenscht = true;

        if (EbeguUtil.isNotNullAndTrue(this.model.sozialhilfeBezueger)) {
            this.model.verguenstigungGewuenscht = false;
            this.finanzielleSituationRequired = false;
        }
    }

    public getAntragstellerNummer(): number {
        return 1;
    }

    public getSubStepIndex(): number {
        return 0;
    }

    public getSubStepName(): string {
        return TSFinanzielleSituationSubStepName.SOLOTHURN_START;
    }

    public isGemeinsam(): boolean {
        return FinanzielleSituationSolothurnService.finSitIsGemeinsam(this.gesuchModelManager);
    }

    public notify(): void {
    }

    public prepareSave(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid(this.form)) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    protected save(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then(async () => {
                if (!this.isGemeinsam() || this.getAntragstellerNummer() === 2) {
                    await this.updateWizardStepStatus();
                }
                onResult(this.getModel());
                return this.getModel();
            }).catch(error => {
                throw(error);
            }) as Promise<TSFinanzielleSituationContainer>;
    }

    public getFamilienSitutation(): TSFamiliensituation {
        return this.getGesuch().familiensituationContainer.familiensituationJA;
    }

    public onSozialhilfeBezuegerChange(isSozialhilfebezueger: boolean): void {
        this.model.verguenstigungGewuenscht = !isSozialhilfebezueger;
        this.finanzielleSituationRequired = !isSozialhilfebezueger;

        if (EbeguUtil.isNotNullAndFalse(isSozialhilfebezueger)) {
            this.resetVeranlagungSolothurn();
            this.resetBruttoLohn();
        }
    }

    public steuerveranlagungErhaltenChange(steuerveranlagungErhalten: boolean): void {
        if (EbeguUtil.isNotNullAndTrue(steuerveranlagungErhalten)) {
            this.resetBruttoLohn();
            if (this.isGemeinsam()) {
                this.resetBruttoLohnGS2();
            }
        }
        // tslint:disable-next-line:early-exit
        if (EbeguUtil.isNotNullAndFalse(steuerveranlagungErhalten)) {
            this.resetVeranlagungSolothurn();
            if (this.isGemeinsam()) {
                this.resetVeranlagungSolothurnGS2();
            }
        }
    }

}
