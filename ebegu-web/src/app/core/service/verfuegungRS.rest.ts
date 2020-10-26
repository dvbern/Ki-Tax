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
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSVerfuegung} from '../../../models/TSVerfuegung';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class VerfuegungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
    ) {
        this.serviceURL = `${REST_API}verfuegung`;
    }

    public getServiceName(): string {
        return 'VerfuegungRS';
    }

    public calculateVerfuegung(gesuchID: string): IPromise<TSKindContainer[]> {
        return this.http.get(`${this.serviceURL}/calculate/${encodeURIComponent(gesuchID)}`)
            .then((response: any) => {
                this.log.debug('PARSING KindContainers REST object ', response.data);
                return this.ebeguRestUtil.parseKindContainerList(response.data);
            });
    }

    public saveVerfuegung(
        verfuegungManuelleBemerkungen: string,
        gesuchId: string,
        betreuungId: string,
        ignorieren: boolean,
        ignorierenMahlzeiten: boolean
    ): IPromise<TSVerfuegung> {
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const betreuungIdEnc = encodeURIComponent(betreuungId);
        const url = `${this.serviceURL}/verfuegen/${gesuchIdEnc}/${betreuungIdEnc}/${ignorieren}/${ignorierenMahlzeiten}`;

        return this.http.put(url, verfuegungManuelleBemerkungen).then((response: any) => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                return this.ebeguRestUtil.parseVerfuegung(new TSVerfuegung(), response.data);
            });
        });
    }

    public verfuegungSchliessenOhneVerfuegen(gesuchId: string, betreuungId: string): IPromise<void> {
        return this.http.post(`${this.serviceURL}/schliessenOhneVerfuegen/${encodeURIComponent(betreuungId)}`, {})
            .then(() => {
                return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                    return;
                });
            });
    }

    public nichtEintreten(gesuchId: string, betreuungId: string): IPromise<TSVerfuegung> {
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const betreuungIdEnc = encodeURIComponent(betreuungId);
        const url = `${this.serviceURL}/nichtEintreten/${gesuchIdEnc}/${betreuungIdEnc}`;

        return this.http.get(url)
            .then((response: any) => {
                return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                    this.log.debug('PARSING Verfuegung REST object ', response.data);
                    return this.ebeguRestUtil.parseVerfuegung(new TSVerfuegung(), response.data);
                });
            });
    }

    public anmeldungUebernehmen(
        betreuung: TSBetreuung
    ): IPromise<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject({}, betreuung);
        return this.http.put(`${this.serviceURL}/anmeldung/uebernehmen`, restBetreuung)
            .then(response => this.ebeguRestUtil.parseBetreuung(new TSBetreuung(), response.data));
    }
}
