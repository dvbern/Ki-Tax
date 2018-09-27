/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Injectable} from '@angular/core';
import {IPromise} from 'angular';
import {Log, LogFactory} from '../../app/core/logging/LogFactory';
import AntragStatusHistoryRS from '../../app/core/service/antragStatusHistoryRS.rest';
import GesuchsperiodeRS from '../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {getStartAntragStatusFromEingangsart} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {isNewDossierNeeded, isNewFallNeeded, TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import TSDossier from '../../models/TSDossier';
import TSFall from '../../models/TSFall';
import TSGesuch from '../../models/TSGesuch';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import DossierRS from './dossierRS.rest';
import FallRS from './fallRS.rest';
import GemeindeRS from './gemeindeRS.rest';
import GesuchRS from './gesuchRS.rest';
import WizardStepManager from './wizardStepManager';

/**
 * This class presents methods to init and create new Fall/Dossier/Gesuch objects.
 * All init-methods will create a clientside copy of the required object. This copy will be returned by the method but it won't be saved in the DB
 * The create-methods will take the given object and save it in the DB.
 */
@Injectable({
    providedIn: 'root'
})
export class GesuchGenerator {

    private readonly LOG: Log = LogFactory.createLog(GesuchGenerator.name);

    constructor(
        private readonly gesuchRS: GesuchRS,
        private readonly antragStatusHistoryRS: AntragStatusHistoryRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dossierRS: DossierRS,
        private readonly wizardStepManager: WizardStepManager,
        private readonly authServiceRS: AuthServiceRS,
        private readonly fallRS: FallRS
    ) {}


    /**
     * Erstellt ein neues Gesuch mit der angegebenen Eingangsart und Gesuchsperiode. Damit dies im resolve des
     * routing gemacht werden kann, wird das ganze als promise gehandhabt
     * @return a void promise that is resolved once all subpromises are done
     */
    public initFall(eingangsart: TSEingangsart,
                    gemeindeId: string): IPromise<TSGesuch> {

        return this.initDossier(eingangsart, gemeindeId, TSCreationAction.CREATE_NEW_FALL, undefined, undefined);
    }

    /**
     * Creates a new Dossier for the current Fall. Also a new Gesuch will be created.
     */
    public initDossierForCurrentFall(eingangsart: TSEingangsart,
                                     gemeindeId: string,
                                     currentFall: TSFall): IPromise<TSGesuch> {

        return this.initDossier(eingangsart, gemeindeId, TSCreationAction.CREATE_NEW_DOSSIER, currentFall, null);
    }

    /**
     * Creates a complete new Dossier. Depending on the value of creationAction the new dossier will be added to the existing Fall
     * or a complete new Fall will be created instead.
     */
    private initDossier(eingangsart: TSEingangsart,
                        gemeindeId: string,
                        creationAction: TSCreationAction,
                        currentFall: TSFall,
                        currentDossier: TSDossier): IPromise<TSGesuch> {

        return this.initGesuch(eingangsart, creationAction, undefined, currentFall, currentDossier)
            .then(gesuch => {
                return this.gemeindeRS.findGemeinde(gemeindeId).then(foundGemeinde => {
                    gesuch.dossier.gemeinde = foundGemeinde;
                    this.LOG.debug('initialized new dossier for Current fall', gesuch);
                    return gesuch;
                });
            });
    }

    /**
     * Erstellt ein neues Gesuch und einen neuen Fall. Wenn !forced sie werden nur erstellt wenn das Gesuch noch nicht erstellt wurde i.e. es null/undefined ist
     * Wenn force werden Gesuch und Fall immer erstellt.
     */
    public initGesuch(eingangsart: TSEingangsart,
                      creationAction: TSCreationAction,
                      gesuchsperiodeId: string,
                      currentFall: TSFall,
                      currentDossier: TSDossier): IPromise<TSGesuch> {

        const gesuch: TSGesuch = this.initAntrag(TSAntragTyp.ERSTGESUCH, eingangsart, creationAction, currentFall, currentDossier);
        gesuch.status = getStartAntragStatusFromEingangsart(eingangsart);

        if (gesuchsperiodeId) {
            return this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
                gesuch.gesuchsperiode = periode;
                return this.antragStatusHistoryRS.loadLastStatusChange(gesuch).then(() => {
                    return gesuch;
                });
            });
        } else {
            return this.antragStatusHistoryRS.loadLastStatusChange(gesuch).then(() => {
                return gesuch;
            });
        }
    }

    /**
     * Diese Methode erstellt eine Fake-Mutation als gesuch fuer das GesuchModelManager. Die Mutation ist noch leer und hat
     * das ID des Gesuchs aus dem sie erstellt wurde. Wenn der Benutzer auf speichern klickt, wird der Service "antragMutieren"
     * mit dem ID des alten Gesuchs aufgerufen. Das Objekt das man zurueckbekommt, wird dann diese Fake-Mutation mit den richtigen
     * Daten ueberschreiben
     */
    public initMutation(gesuchID: string,
                        eingangsart: TSEingangsart,
                        gesuchsperiodeId: string,
                        dossierId: string,
                        currentFall: TSFall,
                        currentDossier: TSDossier): IPromise<TSGesuch> {

        return this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId,
            TSAntragTyp.MUTATION, TSCreationAction.CREATE_NEW_MUTATION,
            currentFall, currentDossier);
    }

    /**
     * Diese Methode erstellt ein Fake-Erneuerungsgesuch als gesuch fuer das GesuchModelManager. Das Gesuch ist noch leer und hat
     * das ID des Gesuchs aus dem es erstellt wurde.
     */
    public initErneuerungsgesuch(gesuchID: string,
                                 eingangsart: TSEingangsart,
                                 gesuchsperiodeId: string,
                                 dossierId: string,
                                 currentFall: TSFall,
                                 currentDossier: TSDossier): IPromise<TSGesuch> {

        return this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId,
            TSAntragTyp.ERNEUERUNGSGESUCH, TSCreationAction.CREATE_NEW_FOLGEGESUCH,
            currentFall, currentDossier);
    }

    private initCopyOfGesuch(gesuchID: string,
                             eingangsart: TSEingangsart,
                             gesuchsperiodeId: string,
                             dossierId: string,
                             antragTyp: TSAntragTyp,
                             creationAction: TSCreationAction,
                             currentFall: TSFall,
                             currentDossier: TSDossier): IPromise<TSGesuch> {

        const gesuch: TSGesuch = this.initAntrag(antragTyp, eingangsart, creationAction, currentFall, currentDossier);

        return this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
            gesuch.gesuchsperiode = periode;
            return this.dossierRS.findDossier(dossierId).then(foundDossier => {
                gesuch.dossier = foundDossier;
                gesuch.id = gesuchID; //setzen wir das alte gesuchID, um danach im Server die Mutation erstellen zu koennen
                gesuch.status = getStartAntragStatusFromEingangsart(eingangsart);
                gesuch.emptyCopy = true;
                return gesuch;
            });
        });
    }

    private initAntrag(antragTyp: TSAntragTyp,
                       eingangsart: TSEingangsart,
                       creationAction: TSCreationAction,
                       currentFall: TSFall,
                       currentDossier: TSDossier): TSGesuch {

        const gesuch = new TSGesuch();
        gesuch.dossier = isNewDossierNeeded(creationAction) ? new TSDossier() : currentDossier;
        gesuch.dossier.fall = isNewFallNeeded(creationAction) ? new TSFall() : currentFall;
        gesuch.typ = antragTyp; // by default ist es ein Erstgesuch
        gesuch.eingangsart = eingangsart;

        this.wizardStepManager.setHiddenSteps(gesuch);
        this.wizardStepManager.initWizardSteps();
        this.setCurrentUserAsFallVerantwortlicher(gesuch);

        return gesuch;
    }

    /**
     * Will create the given fall as a new fall in the DB and return the object as a promise
     */
    public createNewFall(fall: TSFall): IPromise<TSFall> {
        return this.fallRS.createFall(fall);
    }

    /**
     * Will create the given dossier as a new dossier in the DB and return the object as a promise
     */
    public createNewDossier(dossier: TSDossier): IPromise<TSDossier> {
        return this.dossierRS.createDossier(dossier);
    }

    /**
     * Will create the given gesuch as a new gesuch in the DB and return the object as a promise
     */
    public createNewGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        return this.gesuchRS.createGesuch(gesuch);
    }

    /**
     * Takes current user and sets him as the verantwortlicherBG of Fall. Depending on the role it sets him as
     * verantwortlicherBG or verantworlicherSCH
     */
    private setCurrentUserAsFallVerantwortlicher(gesuch: TSGesuch) {
        if (this.authServiceRS) {
            this.authServiceRS.principal$.subscribe(currentUser => {
                if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtRole())) {
                    gesuch.dossier.verantwortlicherBG = currentUser;

                } else if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSchulamtOnlyRoles())) {
                    gesuch.dossier.verantwortlicherTS = currentUser;
                }
            });
        }
    }
}
