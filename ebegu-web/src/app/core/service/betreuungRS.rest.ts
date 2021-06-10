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
import {WizardStepManager} from '../../../gesuch/service/wizardStepManager';
import {TSAnmeldungDTO} from '../../../models/TSAnmeldungDTO';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungspensumAbweichung} from '../../../models/TSBetreuungspensumAbweichung';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class BetreuungRS {

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
        gesuchId: string,
        abwesenheit: boolean,
    ): IPromise<TSBetreuung> {

        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        const url = `${this.serviceURL}/betreuung/${abwesenheit}`;
        return this.http.put(url, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public betreuungsPlatzAbweisen(betreuung: TSBetreuung, gesuchId: string): IPromise<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        return this.http.put(`${this.serviceURL}/abweisen`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public betreuungsPlatzBestaetigen(betreuung: TSBetreuung, gesuchId: string): IPromise<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        return this.http.put(`${this.serviceURL}/bestaetigen`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public anmeldungSchulamtAblehnen(betreuung: TSBetreuung, gesuchId: string): IPromise<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        return this.http.put(`${this.serviceURL}/schulamt/ablehnen/`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    private parseBetreuung(response: any, gesuchId: string): IPromise<TSBetreuung> {
        return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
            return this.ebeguRestUtil.parseBetreuung(new TSBetreuung(), response.data);
        });
    }

    public anmeldungSchulamtFalscheInstitution(
        betreuung: TSBetreuung,
        gesuchId: string,
    ): IPromise<TSBetreuung> {

        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        const url = `${this.serviceURL}/schulamt/falscheInstitution/`;
        return this.http.put(url, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }

    public anmeldungSchulamtStorniert(
        betreuung: TSBetreuung,
        gesuchId: string,
    ): IPromise<TSBetreuung> {

        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        const url = `${this.serviceURL}/schulamt/stornieren/`;
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

    public saveAbweichungen(
        betreuung: TSBetreuung,
    ): IPromise<Array<TSBetreuungspensumAbweichung>> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        const url = `${this.serviceURL}/betreuung/abweichungen/${encodeURIComponent(betreuung.id)}/`;
        return this.http.put(url, restBetreuung)
            .then(response => this.ebeguRestUtil.parseBetreuungspensumAbweichungen(response.data));
    }

    public loadAbweichungen(
        betreuungId: string,
    ): IPromise<Array<TSBetreuungspensumAbweichung>> {
        const url = `${this.serviceURL}/betreuung/abweichungen/${encodeURIComponent(betreuungId)}/`;
        return this.http.get(url)
            .then(response => this.ebeguRestUtil.parseBetreuungspensumAbweichungen(response.data));
    }

    public createAngebot(anmeldungDTO: TSAnmeldungDTO): IPromise<any> {
        const restAnmeldung = this.ebeguRestUtil.anmeldungDTOToRestObject({}, anmeldungDTO);

        return this.http.put(`${this.serviceURL}/anmeldung/create/`, restAnmeldung);
    }

    public anmeldungSchulamtModuleAkzeptiert(
        betreuung: TSBetreuung,
        gesuchId: string,
    ): IPromise<TSBetreuung> {

        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        return this.http.put(`${this.serviceURL}/schulamt/akzeptieren`, restBetreuung)
            .then(response => this.parseBetreuung(response, gesuchId));
    }
}
