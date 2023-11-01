import {mergeMap} from 'rxjs/operators';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {isAtLeastFreigegeben} from '../../../models/enums/TSAntragStatus';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {FamiliensituationRS} from '../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';

export abstract class AbstractFamiliensitutaionView extends AbstractGesuchViewX<TSFamiliensituationContainer> {

    public allowedRoles: ReadonlyArray<TSRole>;

    public savedClicked: boolean = false;

    protected constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS
    ) {

        super(gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.FAMILIENSITUATION);
        this.initViewModel();
        this.gesuchModelManager.initFamiliensituation();
        this.model = this.getGesuch().familiensituationContainer.deepCopyTo(new TSFamiliensituationContainer());
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FAMILIENSITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.allowedRoles = TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    public showError(): boolean {
        return this.hasError() && this.savedClicked;
    }

    public hasError(): boolean {
        return this.isMutation()
            && this.getFamiliensituation()?.aenderungPer
            && this.getFamiliensituationErstgesuch()?.isSameFamiliensituation(this.getFamiliensituation());
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.model.familiensituationJA;
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        return this.model.familiensituationErstgesuch;
    }

    public getFamiliensituationGS(): TSFamiliensituation {
        return this.model.familiensituationGS;
    }

    public showBisher(): boolean {
        return this.gesuchModelManager.getGesuch()
            && isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status)
            && (TSEingangsart.ONLINE === this.gesuchModelManager.getGesuch().eingangsart);
    }

    public onDatumBlur(): void {
        if (this.hasEmptyAenderungPer()) {
            this.resetFamsit();
        }
    }

    public hasEmptyAenderungPer(): boolean {
        return this.isMutation()
            && !this.getFamiliensituation()?.aenderungPer
            && !this.getFamiliensituationErstgesuch()?.isSameFamiliensituation(this.getFamiliensituation());
    }

    public resetFamsit(): void {
        this.getFamiliensituation().revertFamiliensituation(this.getFamiliensituationErstgesuch());
    }

    public getAllRolesButTraegerschaftInstitutionSteueramt(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt();
    }

    public getTraegerschaftInstitutionSteueramtOnlyRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getTraegerschaftInstitutionSteueramtOnlyRoles();
    }

    public async confirmAndSave(onResult: (arg: any) => void): Promise<void> {
        this.savedClicked = true;
        if (this.isGesuchValid() && !this.hasEmptyAenderungPer() && !this.hasError()) {
            if (!this.form.dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                // Update wizardStepStatus also if the form is empty and not dirty
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                onResult(this.getGesuch().familiensituationContainer);
                return;
            }
            await this.confirm(onResult);

        } else {
            onResult(undefined);
        }
    }

    protected save(): Promise<TSFamiliensituationContainer> {
        this.errorService.clearAll();
        return this.familiensituationRS.saveFamiliensituation(
            this.model,
            this.getGesuch().id
        ).pipe(mergeMap((familienContainerResponse: any) => {
            this.model = familienContainerResponse;
            this.getGesuch().familiensituationContainer = familienContainerResponse;
            // Gesuchsteller may changed...
            return this.gesuchModelManager.reloadGesuch().then(() => this.model);
        })).toPromise();
    }

    protected abstract confirm(onResult: (arg: any) => void): Promise<void>;

    public isFamiliensituationEnabled(): boolean {
        return this.isMutationAndDateSet() && !this.isGesuchReadonly();
    }

    public isMutationAndDateSet(): boolean {
        if (!this.isMutation()) {
            return true;
        }

        return EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation()) &&
            EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation().aenderungPer);
    }

    public isOneOfRoles(allowedRoles: ReadonlyArray<TSRole>) {
        return this.authService.isOneOfRoles(allowedRoles);
    }
}
