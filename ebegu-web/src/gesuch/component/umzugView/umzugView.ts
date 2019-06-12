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
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {TSAdressetyp} from '../../../models/enums/TSAdressetyp';
import {TSBetroffene} from '../../../models/enums/TSBetroffene';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSAdresse from '../../../models/TSAdresse';
import TSAdresseContainer from '../../../models/TSAdresseContainer';
import TSGesuchstellerContainer from '../../../models/TSGesuchstellerContainer';
import TSUmzugAdresse from '../../../models/TSUmzugAdresse';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class UmzugViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./umzugView.html');
    public controller = UmzugViewController;
    public controllerAs = 'vm';
}

export class UmzugViewController extends AbstractGesuchViewController<Array<TSUmzugAdresse>> {

    public static $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'WizardStepManager',
        'ErrorService',
        '$translate',
        'DvDialog',
        '$q',
        '$scope',
        '$timeout',
    ];

    public dirty = false;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        private readonly errorService: ErrorService,
        private readonly $translate: ITranslateService,
        private readonly dvDialog: DvDialog,
        private readonly $q: IQService,
        $scope: IScope,
        $timeout: ITimeoutService,
    ) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.UMZUG, $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.model = [];
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.UMZUG, TSWizardStepStatus.OK);
        this.extractAdressenListFromBothGS();
    }

    public getUmzugAdressenList(): Array<TSUmzugAdresse> {
        return this.model;
    }

    public save(): IPromise<TSGesuchstellerContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (!this.form.$dirty && !this.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.gesuchModelManager.getStammdatenToWorkWith());
        }

        this.errorService.clearAll();
        this.saveAdresseInGS();
        this.gesuchModelManager.setGesuchstellerNumber(1);
        return this.gesuchModelManager.updateGesuchsteller(true).then(() => {
            if (this.gesuchModelManager.getGesuch().gesuchsteller2) {
                this.gesuchModelManager.setGesuchstellerNumber(2);
                return this.gesuchModelManager.updateGesuchsteller(true);
            }
            return this.gesuchModelManager.getStammdatenToWorkWith();
        });
    }

    /**
     * Hier schauen wir wie viele GS es gibt und dementsprechen fuellen wir die Liste aus.
     * Bei Mutationen wird es nur geschaut ob der GS existiert (!=null), da die Familiensituation nicht relevant ist.
     * Es koennte einen GS2 geben obwohl die neue Familiensituation "ledig" sagt
     */
    public getBetroffenenList(): Array<TSBetroffene> {
        const betroffenenList: Array<TSBetroffene> = [];
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getGesuch().gesuchsteller1) {
                betroffenenList.push(TSBetroffene.GESUCHSTELLER_1);
            }
            const gesuchsteller2 = this.gesuchModelManager.getGesuch().gesuchsteller2;
            if (gesuchsteller2) {
                betroffenenList.push(TSBetroffene.GESUCHSTELLER_2);

                if (this.gesuchModelManager.getGesuch().gesuchsteller1) {
                    // Dies koennte auch direkt beim Push des GS2 gemacht werden, da es keinen GS2 geben darf wenn es
                    // keinen GS1 gibt. Allerdings sind wir mit diesem IF sicher dass GS1 und GS2 wirklich existieren.
                    betroffenenList.push(TSBetroffene.BEIDE_GESUCHSTELLER);
                }
            }
        }
        return betroffenenList; // empty list wenn die Daten nicht richtig sind
    }

    public getNameFromBetroffene(betroffene: TSBetroffene): string {
        if (TSBetroffene.GESUCHSTELLER_1 === betroffene && this.gesuchModelManager.getGesuch().gesuchsteller1) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
        }
        if (TSBetroffene.GESUCHSTELLER_2 === betroffene && this.gesuchModelManager.getGesuch().gesuchsteller2) {
            return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
        }
        if (TSBetroffene.BEIDE_GESUCHSTELLER === betroffene) {
            return this.$translate.instant(TSBetroffene[betroffene]);
        }

        return '';
    }

    private extractAdressenListFromBothGS(): void {
        this.getAdressenListFromGS1();
        this.getAdressenListFromGS2();
    }

    private getAdressenListFromGS1(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!(gesuch && gesuch.gesuchsteller1)) {
            return;
        }

        gesuch.gesuchsteller1.getUmzugAdressen().forEach(umzugAdresse => {
            umzugAdresse.showDatumVon = true; // wird benoetigt weil es vom Server nicht kommt
            this.model.push(new TSUmzugAdresse(TSBetroffene.GESUCHSTELLER_1, umzugAdresse));
        });
    }

    /**
     * Geht durch die Adressenliste des GS2 durch. Wenn eine Adresse von GS2
     */
    private getAdressenListFromGS2(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!gesuch || !gesuch.gesuchsteller2) {
            return;
        }
        gesuch.gesuchsteller2.getUmzugAdressen().forEach(umzugAdresse => {
            umzugAdresse.showDatumVon = true; // wird benoetigt weil es vom Server nicht kommt
            let foundPosition = -1;
            for (let i = 0; i < this.model.length; i++) {
                if (this.model[i].adresse.isSameWohnAdresse(umzugAdresse)) {
                    foundPosition = i;
                }
            }
            if (foundPosition < 0) {
                this.model.push(new TSUmzugAdresse(TSBetroffene.GESUCHSTELLER_2, umzugAdresse));
                return;
            }
            this.model[foundPosition].betroffene = TSBetroffene.BEIDE_GESUCHSTELLER;

            // speichern der AdressContainer vom Gs2 damit wir sie später wieder finden
            this.model[foundPosition].adresseGS2 = umzugAdresse;
        });
    }

    public removeUmzugAdresse(adresse: TSUmzugAdresse): void {
        const remTitleText = this.$translate.instant('UMZUG_LOESCHEN');
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: '',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.dirty = true;
            const indexOf = this.model.lastIndexOf(adresse);
            if (indexOf >= 0) {
                this.model.splice(indexOf, 1);
            }
            this.$timeout(() => EbeguUtil.selectFirst(), 100);
        });
    }

    /**
     * Erstellt eine neue leere Adresse vom Typ WOHNADRESSE
     */
    public createUmzugAdresse(): void {
        const adresseContainer = this.createAdressContainer();
        const umzugAdresse = new TSUmzugAdresse(undefined, adresseContainer);

        this.model.push(umzugAdresse);
        this.dirty = true;
        this.$postLink();
        // todo focus on specific id, so the newly added umzug will be selected not the first in the DOM
    }

    private createAdressContainer(): TSAdresseContainer {
        const adresseContainer = new TSAdresseContainer();
        const adresse = new TSAdresse();
        adresse.adresseTyp = TSAdressetyp.WOHNADRESSE;
        adresseContainer.showDatumVon = true;
        adresseContainer.adresseJA = adresse;
        return adresseContainer;
    }

    /**
     * Zuerst entfernt alle Elemente der Arrays von adressen vom GS1 und GS2, ausser dem ersten Element (Wohnadresse).
     * Danach fuellt diese mit den Adressen die hier geblieben sind bzw. nicht entfernt wurden, dafuer
     * nimmt es aus der Liste von umzugAdressen alle eingegebenen Adressen und speichert sie in dem entsprechenden GS
     */
    private saveAdresseInGS(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        const gesuchsteller1 = gesuch.gesuchsteller1;
        if (gesuchsteller1 && gesuchsteller1.adressen && gesuchsteller1.adressen.length > 0) {
            gesuchsteller1.adressen.length = 1;
        }
        const gesuchsteller2 = gesuch.gesuchsteller2;
        if (gesuchsteller2 && gesuchsteller2.adressen && gesuchsteller2.adressen.length > 0) {
            gesuchsteller2.adressen.length = 1;
        }
        this.model.forEach(umzugAdresse => {

            if (TSBetroffene.GESUCHSTELLER_1 === umzugAdresse.betroffene) {
                this.addAdresseToGS(this.gesuchModelManager.getGesuch().gesuchsteller1, umzugAdresse.adresse);

            } else if (TSBetroffene.GESUCHSTELLER_2 === umzugAdresse.betroffene) {
                this.addAdresseToGS(this.gesuchModelManager.getGesuch().gesuchsteller2, umzugAdresse.adresse);

            } else if (TSBetroffene.BEIDE_GESUCHSTELLER === umzugAdresse.betroffene) {
                this.addAdresseToGS(this.gesuchModelManager.getGesuch().gesuchsteller1, umzugAdresse.adresse);

                if (!umzugAdresse.adresseGS2) {
                    umzugAdresse.adresseGS2 = this.createAdressContainer();
                }
                umzugAdresse.adresseGS2.adresseJA.copy(umzugAdresse.adresse.adresseJA);
                this.addAdresseToGS(this.gesuchModelManager.getGesuch().gesuchsteller2, umzugAdresse.adresseGS2);
            }
        });
    }

    private addAdresseToGS(gesuchsteller: TSGesuchstellerContainer, adresse: TSAdresseContainer): void {
        if (!gesuchsteller) {
            return;
        }
        if (gesuchsteller.adressen.indexOf(adresse) < 0) {
            gesuchsteller.addAdresse(adresse);
        } else {
            // update old adresse
        }
    }

    public getPreviousButtonText(): string {
        return this.getUmzugAdressenList().length === 0 ? 'ZURUECK_ONLY' : 'ZURUECK';
    }

    public getNextButtonText(): string {
        return this.getUmzugAdressenList().length === 0 ? 'WEITER_ONLY' : 'WEITER';
    }
}
