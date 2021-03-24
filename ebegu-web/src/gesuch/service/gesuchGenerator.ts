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
import {LogFactory} from '../../app/core/logging/LogFactory';
import {AntragStatusHistoryRS} from '../../app/core/service/antragStatusHistoryRS.rest';
import {GesuchsperiodeRS} from '../../app/core/service/gesuchsperiodeRS.rest';
import {SozialdienstRS} from '../../app/core/service/SozialdienstRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {getStartAntragStatusFromEingangsart} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {isNewDossierNeeded, isNewFallNeeded, TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSSozialdienst} from '../../models/sozialdienst/TSSozialdienst';
import {TSSozialdienstFall} from '../../models/sozialdienst/TSSozialdienstFall';
import {TSDossier} from '../../models/TSDossier';
import {TSFall} from '../../models/TSFall';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {DossierRS} from './dossierRS.rest';
import {FallRS} from './fallRS.rest';
import {GemeindeRS} from './gemeindeRS.rest';
import {GesuchRS} from './gesuchRS.rest';
import {WizardStepManager} from './wizardStepManager';

const LOG = LogFactory.createLog('GesuchGenerator');

/**
 * This class presents methods to init and create new Fall/Dossier/Gesuch objects.
 * All init-methods will create a clientside copy of the required object. This copy will be returned by the method but
 * it won't be saved in the DB The create-methods will take the given object and save it in the DB.
 */
@Injectable({
    providedIn: 'root',
})
export class GesuchGenerator {

    public constructor(
        private readonly gesuchRS: GesuchRS,
        private readonly antragStatusHistoryRS: AntragStatusHistoryRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dossierRS: DossierRS,
        private readonly wizardStepManager: WizardStepManager,
        private readonly authServiceRS: AuthServiceRS,
        private readonly fallRS: FallRS,
        private readonly sozialdienstRS: SozialdienstRS,
    ) {
    }

    /**
     * Erstellt ein neues Gesuch mit der angegebenen Eingangsart und Gesuchsperiode. Damit dies im resolve des
     * routing gemacht werden kann, wird das ganze als promise gehandhabt
     * @return a void promise that is resolved once all subpromises are done
     */
    public initFall(
        eingangsart: TSEingangsart,
        gemeindeId: string,
        sozialdienstId: string,
        gesuchsperiodeId: string,
    ): IPromise<TSGesuch> {
        if (EbeguUtil.isNotNullOrUndefined(sozialdienstId)) {
            return this.sozialdienstRS.getSozialdienstStammdaten(sozialdienstId).toPromise().then(
                sozialdienstStammdaten => {
                    return this.initDossier(eingangsart,
                        gemeindeId,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined,
                        undefined,
                        sozialdienstStammdaten.sozialdienst,
                        gesuchsperiodeId);
                },
            );
        }
        return this.initDossier(eingangsart,
            gemeindeId,
            TSCreationAction.CREATE_NEW_FALL,
            undefined,
            undefined,
            undefined,
            undefined);
    }

    /**
     * Creates a new Dossier for the current Fall. Also a new Gesuch will be created.
     */
    public initDossierForCurrentFall(
        eingangsart: TSEingangsart,
        gemeindeId: string,
        currentFall: TSFall,
        sozialdienst: TSSozialdienst,
    ): IPromise<TSGesuch> {

        return this.initDossier(eingangsart,
            gemeindeId,
            TSCreationAction.CREATE_NEW_DOSSIER,
            currentFall,
            null,
            sozialdienst,
            undefined);
    }

    /**
     * Creates a complete new Dossier. Depending on the value of creationAction the new dossier will be added to the
     * existing Fall or a complete new Fall will be created instead.
     */
    private initDossier(
        eingangsart: TSEingangsart,
        gemeindeId: string,
        creationAction: TSCreationAction,
        currentFall: TSFall,
        currentDossier: TSDossier,
        sozialdienst: TSSozialdienst,
        gesuchsperiodeId: string,
    ): IPromise<TSGesuch> {

        return this.initGesuch(eingangsart, creationAction, gesuchsperiodeId, currentFall, currentDossier, sozialdienst)
            .then(gesuch => {
                return this.gemeindeRS.findGemeinde(gemeindeId).then(foundGemeinde => {
                    gesuch.dossier.gemeinde = foundGemeinde;
                    LOG.debug('initialized new dossier for Current fall', gesuch);

                    return gesuch;
                });
            });
    }

    /**
     * Erstellt ein neues Gesuch und einen neuen Fall. Wenn !forced sie werden nur erstellt wenn das Gesuch noch nicht
     * erstellt wurde i.e. es null/undefined ist Wenn force werden Gesuch und Fall immer erstellt.
     */
    public initGesuch(
        eingangsart: TSEingangsart,
        creationAction: TSCreationAction,
        gesuchsperiodeId: string,
        currentFall: TSFall,
        currentDossier: TSDossier,
        sozialdienst: TSSozialdienst,
    ): IPromise<TSGesuch> {

        const gesuch = this.initAntrag(TSAntragTyp.ERSTGESUCH,
            eingangsart,
            creationAction,
            currentFall,
            currentDossier,
            sozialdienst);
        gesuch.status = getStartAntragStatusFromEingangsart(eingangsart, EbeguUtil.isNotNullOrUndefined(sozialdienst));

        if (gesuchsperiodeId) {
            return this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
                gesuch.gesuchsperiode = periode;
                return this.antragStatusHistoryRS.loadLastStatusChange(gesuch).then(() => {
                    return gesuch;
                });
            });
        }

        return this.antragStatusHistoryRS.loadLastStatusChange(gesuch).then(() => {
            return gesuch;
        });
    }

    /**
     * Diese Methode erstellt eine Fake-Mutation als gesuch fuer das GesuchModelManager. Die Mutation ist noch leer und
     * hat das ID des Gesuchs aus dem sie erstellt wurde. Wenn der Benutzer auf speichern klickt, wird der Service
     * "antragMutieren" mit dem ID des alten Gesuchs aufgerufen. Das Objekt das man zurueckbekommt, wird dann diese
     * Fake-Mutation mit den richtigen Daten ueberschreiben
     */
    public initMutation(
        gesuchID: string,
        eingangsart: TSEingangsart,
        gesuchsperiodeId: string,
        dossierId: string,
        currentFall: TSFall,
        currentDossier: TSDossier,
    ): IPromise<TSGesuch> {

        return this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId,
            TSAntragTyp.MUTATION, TSCreationAction.CREATE_NEW_MUTATION,
            currentFall, currentDossier);
    }

    /**
     * Diese Methode erstellt ein Fake-Erneuerungsgesuch als gesuch fuer das GesuchModelManager. Das Gesuch ist noch
     * leer und hat das ID des Gesuchs aus dem es erstellt wurde.
     */
    public initErneuerungsgesuch(
        gesuchID: string,
        eingangsart: TSEingangsart,
        gesuchsperiodeId: string,
        dossierId: string,
        currentFall: TSFall,
        currentDossier: TSDossier,
    ): IPromise<TSGesuch> {

        return this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId,
            TSAntragTyp.ERNEUERUNGSGESUCH, TSCreationAction.CREATE_NEW_FOLGEGESUCH,
            currentFall, currentDossier);
    }

    private initCopyOfGesuch(
        gesuchID: string,
        eingangsart: TSEingangsart,
        gesuchsperiodeId: string,
        dossierId: string,
        antragTyp: TSAntragTyp,
        creationAction: TSCreationAction,
        currentFall: TSFall,
        currentDossier: TSDossier,
    ): IPromise<TSGesuch> {

        const gesuch = this.initAntrag(antragTyp, eingangsart, creationAction, currentFall, currentDossier, null);

        return this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
            gesuch.gesuchsperiode = periode;
            return this.dossierRS.findDossier(dossierId).then(foundDossier => {
                gesuch.dossier = foundDossier;
                gesuch.id = gesuchID; // setzen wir das alte gesuchID, um danach im Server die Mutation erstellen zu
                                      // koennen
                gesuch.status = getStartAntragStatusFromEingangsart(eingangsart,
                    EbeguUtil.isNotNullOrUndefined(currentFall.sozialdienstFall));
                gesuch.emptyCopy = true;
                return gesuch;
            });
        });
    }

    private initAntrag(
        antragTyp: TSAntragTyp,
        eingangsart: TSEingangsart,
        creationAction: TSCreationAction,
        currentFall: TSFall,
        currentDossier: TSDossier,
        sozialdienst: TSSozialdienst,
    ): TSGesuch {

        const gesuch = new TSGesuch();
        gesuch.dossier = isNewDossierNeeded(creationAction) ? new TSDossier() : currentDossier;
        gesuch.dossier.fall = isNewFallNeeded(creationAction) ? new TSFall() : currentFall;
        gesuch.typ = antragTyp; // by default ist es ein Erstgesuch
        gesuch.eingangsart = eingangsart;

        if (EbeguUtil.isNotNullOrUndefined(sozialdienst) && isNewFallNeeded(creationAction)) {
            gesuch.dossier.fall.sozialdienstFall = new TSSozialdienstFall();
            gesuch.dossier.fall.sozialdienstFall.sozialdienst = sozialdienst;
        }
        this.wizardStepManager.setHiddenSteps(gesuch);
        this.wizardStepManager.initWizardSteps(isNewFallNeeded(creationAction));
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
    private setCurrentUserAsFallVerantwortlicher(gesuch: TSGesuch): void {
        if (!this.authServiceRS) {
            return;
        }

        this.authServiceRS.principal$.subscribe(currentUser => {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtRole())) {
                gesuch.dossier.verantwortlicherBG = currentUser.toBenutzerNoDetails();
            } else if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSchulamtOnlyRoles())) {
                gesuch.dossier.verantwortlicherTS = currentUser.toBenutzerNoDetails();
            }
        }, err => LOG.error(err));
    }

    public initSozialdienstFall(
        fallId: string,
        gemeindeId: string,
        gesuchId: string,
        gesuchsperiodeId: string,
    ): IPromise<TSGesuch> {
        return this.fallRS.findFall(fallId).then(
            fall => {
                if (!EbeguUtil.isEmptyStringNullOrUndefined(gesuchId)) {
                    return this.gesuchRS.findGesuch(gesuchId);
                }

                return this.initDossier(TSEingangsart.PAPIER,
                    gemeindeId,
                    undefined,
                    fall,
                    new TSDossier(),
                    undefined,
                    gesuchsperiodeId);
            },
        );
    }
}
