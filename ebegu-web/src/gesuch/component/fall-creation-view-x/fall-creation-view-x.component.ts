import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';

@Component({
    selector: 'dv-fall-creation-view-x',
    templateUrl: './fall-creation-view-x.component.html',
    styleUrls: ['./fall-creation-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FallCreationViewXComponent extends AbstractGesuchViewX<TSGesuch> implements OnInit {

    public gesuchsperiodeId: string;
    @ViewChild(NgForm) private form: NgForm;

    // showError ist ein Hack damit, die Fehlermeldung fuer die Checkboxes nicht direkt beim Laden der Seite angezeigt
    // wird sondern erst nachdem man auf ein checkbox oder auf speichern geklickt hat
    public showError: boolean = false;
    private yetUnusedGesuchsperiodenListe: Array<TSGesuchsperiode>;

    public constructor(
        public readonly gesuchModelManager: GesuchModelManager,
        private readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        private readonly $translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
    ) {
        super(gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.GESUCH_ERSTELLEN);
    }

    public ngOnInit(): void {
        // TODO: do we need super.onInit? See abstractGesuchView.$onInit

        this.readStateParams();
        this.initViewModel();
    }

    private readStateParams(): void {
        //if (this.$stateParams.gesuchsperiodeId && this.$stateParams.gesuchsperiodeId !== '') {
        //  this.gesuchsperiodeId = this.$stateParams.gesuchsperiodeId;
        // }
    }

    public setShowError(showError: boolean): void {
        this.showError = showError;
    }

    private initViewModel(): void {
        // gesuch should already have been initialized in resolve function
        if ((EbeguUtil.isNullOrUndefined(this.gesuchsperiodeId) || this.gesuchsperiodeId === '')
            && this.gesuchModelManager.getGesuchsperiode()) {
            this.gesuchsperiodeId = this.gesuchModelManager.getGesuchsperiode().id;
        }

        const dossier = this.gesuchModelManager.getDossier();
        if (!dossier) {
            return;
        }
        this.gesuchsperiodeRS.getAllPeriodenForGemeinde(dossier.gemeinde.id, dossier.id)
            .then((response: TSGesuchsperiode[]) => {
                this.yetUnusedGesuchsperiodenListe = angular.copy(response);
            });
    }

    // tslint:disable-next-line:cognitive-complexity
    public save(): Promise<TSGesuch> {
        this.showError = true;
        if (!this.isGesuchValid()) {
            return undefined;
        }
        if (!this.isSavingNecessary()) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return Promise.resolve(this.gesuchModelManager.getGesuch());
        }
        this.errorService.clearAll();
        return this.gesuchModelManager.saveGesuchAndFall().then(
            gesuch => {
                // if sozialdienst Fall Step muss be updated
                if (EbeguUtil.isNotNullOrUndefined(gesuch.dossier.fall.sozialdienstFall)) {
                    this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                }
                return gesuch;
            },
        ) as Promise<TSGesuch>;
    }

    private isSavingNecessary(): boolean {
        // if form is dirty or gesuch is new => save
        if (this.form.dirty || this.gesuchModelManager.getGesuch().isNew()) {
            return true;
        }
        // user is sozialdienst and step Sozialdienst is ok => don't save
        if (this.isSozialdienstAndOk()) {
            return false;
        }
        // if user is Gemeinde, Kanton or Superadmin => don't save
        return !this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchstellerSozialdienst());
    }

    public getGesuchModel(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getAllActiveGesuchsperioden(): Array<TSGesuchsperiode> {
        return this.yetUnusedGesuchsperiodenListe;
    }

    public setSelectedGesuchsperiode(): void {
        const gesuchsperiodeList = this.getAllActiveGesuchsperioden();
        const found = gesuchsperiodeList.find(gp => gp.id === this.gesuchsperiodeId);
        if (found) {
            this.getGesuchModel().gesuchsperiode = found;
        }
    }

    public isGesuchsperiodeActive(): boolean {
        if (this.gesuchModelManager.getGesuchsperiode()) {
            return TSGesuchsperiodeStatus.AKTIV === this.gesuchModelManager.getGesuchsperiode().status
                || TSGesuchsperiodeStatus.INAKTIV === this.gesuchModelManager.getGesuchsperiode().status;
        }
        return true;
    }

    public getTitle(): string {
        if (!this.gesuchModelManager.getGesuch() || !this.gesuchModelManager.isGesuch()) {
            return this.$translate.instant('ART_DER_MUTATION');
        }
        if (this.gesuchModelManager.isGesuchSaved() && this.gesuchModelManager.getGesuchsperiode()) {
            const k = this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH ?
                'KITAX_ERNEUERUNGSGESUCH_PERIODE' :
                'KITAX_ERSTGESUCH_PERIODE';
            return this.$translate.instant(k, {
                periode: this.gesuchModelManager.getGesuchsperiode().gesuchsperiodeString,
            });
        }
        const key = this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH ?
            'KITAX_ERNEUERUNGSGESUCH' :
            'KITAX_ERSTGESUCH';
        return this.$translate.instant(key);

    }

    public getNextButtonText(): string {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getGesuch().isNew()) {
                return this.$translate.instant('ERSTELLEN');
            }
            if (this.gesuchModelManager.isGesuchReadonly()
                || this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())
                || this.isSozialdienstAndOk()) {
                return this.$translate.instant('WEITER_ONLY');
            }
        }
        return this.$translate.instant('WEITER');
    }

    /**
     * Checks if logged in benutzer is in Sozialdienst role and if yes, check if status of GESUCH_ERSTELLEN
     * step is ok (e.g. document is uploaded).
     */
    private isSozialdienstAndOk(): boolean {
        const sozialdienstRole = this.authServiceRS.isOneOfRoles(TSRoleUtil.getSozialdienstRolle());
        return sozialdienstRole
            && this.wizardStepManager.isStepStatusOk(
                TSWizardStepName.GESUCH_ERSTELLEN);
    }

    public isSelectedGesuchsperiodeInaktiv(): boolean {
        return this.getGesuchModel() && this.getGesuchModel().gesuchsperiode
            && this.getGesuchModel().gesuchsperiode.status === TSGesuchsperiodeStatus.INAKTIV
            && this.getGesuchModel().isNew();
    }

    public canChangeGesuchsperiode(): boolean {
        return this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.isGesuch()
            && this.isGesuchsperiodeActive() && this.gesuchModelManager.getGesuch().isNew();
    }

    public getGemeinde(): TSGemeinde {
        if (this.gesuchModelManager.getDossier()) {
            return this.gesuchModelManager.getDossier().gemeinde;
        }
        return undefined;
    }

    public getPeriodString(): string {
        if (this.getGemeinde()) {
            return DateUtil.calculatePeriodenStartdatumString(this.getGemeinde().betreuungsgutscheineStartdatum);
        }
        return undefined;
    }

    /**
     * There could be Gesuchsperiode in the list so we can chose it, or the gesuch has already a
     * gesuchsperiode set
     */
    public isThereAnyGesuchsperiode(): boolean {
        return (this.yetUnusedGesuchsperiodenListe && this.yetUnusedGesuchsperiodenListe.length > 0)
            || (this.gesuchModelManager.getGesuch() && !!this.gesuchModelManager.getGesuch().gesuchsperiode);
    }

    public showGesuchsperiodeReadonly(): boolean {
        return !this.canChangeGesuchsperiode()
            // do not show readonly gesuchsperioden for sozialdienst
            && !TSRoleUtil.isSozialdienstRole(this.authServiceRS.getPrincipalRole());
    }

    /**
     * Diese Methode prueft ob das Form valid ist. Sollte es nicht valid sein wird das erste fehlende Element gesucht
     * und fokusiert, damit der Benutzer nicht scrollen muss, um den Fehler zu finden.
     * Am Ende wird this.form.$valid zurueckgegeben
     */
    public isGesuchValid(): boolean {
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
        }

        return this.form.valid;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

}
