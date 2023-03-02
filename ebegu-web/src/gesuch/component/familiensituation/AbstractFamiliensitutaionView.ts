import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';

export class AbstractFamiliensitutaionView extends AbstractGesuchViewX<TSFamiliensituationContainer> {

    public allowedRoles: ReadonlyArray<TSRole>;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly wizardStepManager: WizardStepManager,
    ) {

        super(gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.FAMILIENSITUATION);
        this.model = this.getGesuch().familiensituationContainer;
        this.gesuchModelManager.initFamiliensituation();
    }

    protected initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FAMILIENSITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.allowedRoles = TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

}
