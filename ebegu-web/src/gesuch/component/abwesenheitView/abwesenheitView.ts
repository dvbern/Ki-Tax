/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IPromise} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSAbwesenheitContainer} from '../../../models/TSAbwesenheitContainer';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class AbwesenheitViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./abwesenheitView.html');
    public controller = AbwesenheitViewController;
    public controllerAs = 'vm';
}

export class KindBetreuungUI {
    public betreuung: TSBetreuung;
    public kind: TSKindContainer;
}

export class AbwesenheitUI {
    public kindBetreuung: KindBetreuungUI;
    public abwesenheit: TSAbwesenheitContainer;

    public constructor(kindBetreuung: KindBetreuungUI, abwesenheit: TSAbwesenheitContainer) {
        this.kindBetreuung = kindBetreuung;
        this.abwesenheit = abwesenheit;
    }
}

export class AbwesenheitViewController extends AbstractGesuchViewController<Array<AbwesenheitUI>> {

    public static $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'WizardStepManager',
        'DvDialog',
        '$translate',
        '$q',
        'ErrorService',
        '$scope',
        '$timeout',
        'EinstellungRS'
    ];

    public betreuungList: Array<KindBetreuungUI>;
    public maxTageAbwesenheit: number;
    private removed: boolean;
    private readonly changedBetreuungen: Array<TSBetreuung> = [];

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
        private readonly $q: IQService,
        private readonly errorService: ErrorService,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly einstellungRS: EinstellungRS
    ) {

        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.ABWESENHEIT,
            $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.removed = false;
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.ABWESENHEIT,
            TSWizardStepStatus.OK);
        this.setBetreuungList();
        this.initAbwesenheitList();
        this.einstellungRS.findEinstellung(
            TSEinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id
        ).subscribe(einstellung => {
            this.maxTageAbwesenheit = parseInt(einstellung.value, 10);
        });
    }

    /**
     * Aus der Liste mit den gesamten Kindern wird rausgefunden, welche Betreuungen KITA sind. Mit diesen
     * wird eine neue Liste gemacht, die ein Object fuer jedes Kind und Betreuung hat
     */
    private setBetreuungList(): void {
        const kinderList = this.gesuchModelManager.getKinderWithBetreuungList();
        this.betreuungList = [];
        kinderList.forEach(kind => {
            const betreuungenFromKind = kind.betreuungen;
            betreuungenFromKind.forEach(betreuung => {
                if (betreuung.institutionStammdaten
                    && betreuung.institutionStammdaten.betreuungsangebotTyp
                    && (
                        betreuung.institutionStammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA
                        || betreuung.institutionStammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN
                    )) {
                    this.betreuungList.push({betreuung, kind});
                }
            });
        });
    }

    private initAbwesenheitList(): void {
        this.model = [];
        this.betreuungList.forEach(kindBetreuung => {
            if (!kindBetreuung.betreuung.abwesenheitContainers) {
                return;
            }

            kindBetreuung.betreuung.abwesenheitContainers.forEach((abwesenheitCont: TSAbwesenheitContainer) => {
                this.model.push(new AbwesenheitUI(kindBetreuung, abwesenheitCont));
            });
        });
    }

    public getBetreuungList(): Array<KindBetreuungUI> {
        return this.betreuungList;
    }

    public save(): IPromise<Array<TSBetreuung>> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        this.errorService.clearAll();
        if (!this.form.$dirty && !this.removed) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            // Update wizardStepStatus also if the form is empty and not dirty
            this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
            return this.$q.when([]);
        }

        // Zuerst loeschen wir alle Abwesenheiten jeder Betreuung
        const kinderList = this.gesuchModelManager.getKinderWithBetreuungList();
        kinderList.forEach((kindContainer: TSKindContainer) => {
            kindContainer.betreuungen.forEach((betreuung: TSBetreuung) => {
                betreuung.abwesenheitContainers.length = 0;
            });
        });
        // Jetzt koennen wir alle geaenderten Abwesenheiten nochmal hinzufuegen
        this.model.forEach((abwesenheit: AbwesenheitUI) => {
            if (!abwesenheit.kindBetreuung.betreuung.abwesenheitContainers) {
                abwesenheit.kindBetreuung.betreuung.abwesenheitContainers = [];
            }
            abwesenheit.kindBetreuung.betreuung.abwesenheitContainers.push(abwesenheit.abwesenheit);
            this.addChangedBetreuungToList(abwesenheit.kindBetreuung.betreuung);
        });

        return this.gesuchModelManager.updateBetreuungen(this.changedBetreuungen, true);
    }

    /**
     * Anhand des IDs schaut es ob die gegebene Betreuung bereits in der Liste changedBetreuungen ist.
     * Nur wenn sie noch nicht da ist, wird sie hinzugefuegt
     */
    private addChangedBetreuungToList(betreuung: TSBetreuung): void {
        let betreuungAlreadyChanged = false;
        this.changedBetreuungen.forEach(changedBetreuung => {
            if (changedBetreuung.id === betreuung.id) {
                betreuungAlreadyChanged = true;
            }
        });
        if (!betreuungAlreadyChanged) {
            this.changedBetreuungen.push(betreuung);
        }
    }

    /**
     * Nur wenn die Abwesenheit bereits existiert (in der DB) wird es nach Confirmation gefragt.
     * Sonst wird sie einfach geloescht
     */
    public removeAbwesenheitConfirm(abwesenheit: AbwesenheitUI): void {
        if (!abwesenheit.abwesenheit.id) {
            this.removeAbwesenheit(abwesenheit);

            return;
        }

        const remTitleText = this.$translate.instant('ABWESENHEIT_LOESCHEN');
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: '',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.removeAbwesenheit(abwesenheit);
        });
    }

    private removeAbwesenheit(abwesenheit: AbwesenheitUI): void {
        const indexOf = this.model.lastIndexOf(abwesenheit);
        if (indexOf < 0) {
            return;
        }

        if (abwesenheit.kindBetreuung) {
            this.removed = true;
            this.addChangedBetreuungToList(abwesenheit.kindBetreuung.betreuung);
        }
        this.model.splice(indexOf, 1);
        this.$timeout(() => EbeguUtil.selectFirst(), 100);
    }

    public createAbwesenheit(): void {
        if (!this.model) {
            this.model = [];
        }
        this.model.push(new AbwesenheitUI(undefined, new TSAbwesenheitContainer()));
        this.$postLink();
        // todo focus on specific id, so the newly added abwesenheit will be selected not the first in the DOM
    }

    public getAbwesenheiten(): Array<AbwesenheitUI> {
        return this.model;
    }

    /**
     * Gibt ein string zurueck mit der Form
     * "Kindname - InstitutionName"
     * Leerer String wieder zurueckgeliefert wenn die Daten nicht richtig sind
     */
    public getTextForBetreuungDDL(kindBetreuung: KindBetreuungUI): string {
        if (kindBetreuung
            && kindBetreuung.kind
            && kindBetreuung.kind.kindJA
            && kindBetreuung.betreuung
            && kindBetreuung.betreuung.institutionStammdaten
            && kindBetreuung.betreuung.institutionStammdaten.institution) {

            const institution = kindBetreuung.betreuung.institutionStammdaten.institution;

            return `${kindBetreuung.kind.kindJA.getFullName()} - ${institution.name}`;
        }

        return '';
    }

    /**
     * Diese Methode macht es moeglich, dass in einer Abwesenheit, das Betreuungsangebot geaendert werden kann. Damit
     * fuegen wir die Betreuung der Liste changedBetreuungen hinzu, damit sie danach aktualisiert wird
     */
    public changedAngebot(oldKindID: string, oldBetreuungID: string): void {
        // In case the Abwesenheit didn't exist before, the old IDs will be empty and there is no need to change
        // anything
        if (!oldKindID || oldKindID === '' || !oldBetreuungID || oldBetreuungID === '') {
            return;
        }

        this.gesuchModelManager.findKindById(oldKindID);
        this.gesuchModelManager.findBetreuungById(oldBetreuungID);
        const betreuungToWorkWith = this.gesuchModelManager.getBetreuungToWorkWith();
        if (betreuungToWorkWith && betreuungToWorkWith.id) {
            this.addChangedBetreuungToList(betreuungToWorkWith);
        }
    }

    public getPreviousButtonText(): string {
        return this.getAbwesenheiten().length === 0 ? 'ZURUECK_ONLY' : 'ZURUECK';
    }

    public getNextButtonText(): string {
        return this.getAbwesenheiten().length === 0 ? 'WEITER_ONLY' : 'WEITER';
    }
}
