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

import {IHttpService, ILogService, IPromise} from 'angular';
import WizardStepManager from '../../../gesuch/service/wizardStepManager';
import TSAnmeldungDTO from '../../../models/TSAnmeldungDTO';
import TSBetreuung from '../../../models/TSBetreuung';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';

export default class BetreuungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
    ) {
        this.serviceURL = `${REST_API}betreuungen`;
    }

    public getServiceName(): string {
        return 'BetreuungRS';
    }

    public findBetreuung(betreuungID: string): IPromise<TSBetreuung> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(betreuungID)}`)
            .then((response: any) => {
                this.log.debug('PARSING betreuung REST object ', response.data);
                return this.ebeguRestUtil.parseBetreuung(new TSBetreuung(), response.data);
            });
    }

    public findAllBetreuungenWithVerfuegungForDossier(dossierId: string): IPromise<TSBetreuung[]> {
        return this.http.get(`${this.serviceURL}/alleBetreuungen/${encodeURIComponent(dossierId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseBetreuungList(response.data);
            });
    }

    public saveBetreuung(
        betreuung: TSBetreuung,
        kindId: string,
        gesuchId: string,
        abwesenheit: boolean,
    ): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        const url = `${this.serviceURL}/betreuung/${encodeURIComponent(kindId)}/${abwesenheit}`;
        return this.http.put(url, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public betreuungsPlatzAbweisen(betreuung: TSBetreuung, kindId: string, gesuchId: string): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        return this.http.put(`${this.serviceURL}/abweisen/${encodeURIComponent(kindId)}/`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public betreuungsPlatzBestaetigen(betreuung: TSBetreuung, kindId: string, gesuchId: string): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        return this.http.put(`${this.serviceURL}/bestaetigen/${encodeURIComponent(kindId)}/`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public anmeldungSchulamtUebernehmen(
        betreuung: TSBetreuung,
        kindId: string,
        gesuchId: string,
    ): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        return this.http.put(`${this.serviceURL}/schulamt/uebernehmen/${encodeURIComponent(kindId)}/`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public anmeldungSchulamtAblehnen(betreuung: TSBetreuung, kindId: string, gesuchId: string): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        return this.http.put(`${this.serviceURL}/schulamt/ablehnen/${encodeURIComponent(kindId)}/`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    private parseBetreuung(response: any, gesuchId: string): IPromise<TSBetreuung> {
        return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
            return this.ebeguRestUtil.parseBetreuung(new TSBetreuung(), response.data);
        });
    }

    public anmeldungSchulamtFalscheInstitution(
        betreuung: TSBetreuung,
        kindId: string,
        gesuchId: string,
    ): IPromise<TSBetreuung> {
        let restBetreuung = {};
        restBetreuung = this.ebeguRestUtil.betreuungToRestObject(restBetreuung, betreuung);
        const url = `${this.serviceURL}/schulamt/falscheInstitution/${encodeURIComponent(kindId)}/`;
        return this.http.put(url, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public removeBetreuung(betreuungId: string, gesuchId: string): IPromise<any> {
        return this.http.delete(`${this.serviceURL}/${encodeURIComponent(betreuungId)}`)
            .then(responseDeletion => {
                return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                    return responseDeletion;
                });
            });
    }

    /**
     * Diese Methode ruft den Service um alle uebergebenen Betreuungen zu speichern.
     * Dies wird empfohlen wenn mehrere Betreuungen gleichzeitig gespeichert werden muessen,
     * damit alles in einer Transaction passiert. Z.B. fuer Abwesenheiten
     */
    public saveBetreuungen(
        betreuungenToUpdate: Array<TSBetreuung>,
        gesuchId: string,
        saveForAbwesenheit: boolean,
    ): IPromise<Array<TSBetreuung>> {
        const restBetreuungen: Array<any> = [];
        betreuungenToUpdate.forEach((betreuungToUpdate: TSBetreuung) => {
            restBetreuungen.push(this.ebeguRestUtil.betreuungToRestObject({}, betreuungToUpdate));
        });
        return this.http.put(`${this.serviceURL}/all/${saveForAbwesenheit}`, restBetreuungen)
            .then((response: any) => {
                return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                    this.log.debug('PARSING Betreuung REST object ', response.data);
                    const convertedBetreuungen: Array<TSBetreuung> = [];
                    response.data.forEach((returnedBetreuung: any) => {
                        convertedBetreuungen.push(this.ebeguRestUtil.parseBetreuung(new TSBetreuung(),
                            returnedBetreuung));
                    });
                    return convertedBetreuungen;
                });
            });
    }

    public createAngebot(anmeldungDTO: TSAnmeldungDTO): IPromise<any> {
        let restAnmeldung = {};
        restAnmeldung = this.ebeguRestUtil.anmeldungDTOToRestObject(restAnmeldung, anmeldungDTO);

        return this.http.put(`${this.serviceURL}/anmeldung/create/`, restAnmeldung);
    }
}
