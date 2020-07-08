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

import {IHttpPromise, IHttpService, IPromise} from 'angular';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {TSKitaxResponse} from '../../models/dto/TSKitaxResponse';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSFinSitStatus} from '../../models/enums/TSFinSitStatus';
import {TSGesuchBetreuungenStatus} from '../../models/enums/TSGesuchBetreuungenStatus';
import {TSMitteilungEvent} from '../../models/enums/TSMitteilungEvent';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {WizardStepManager} from './wizardStepManager';
import IRootScopeService = angular.IRootScopeService;

export class GesuchRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', 'WizardStepManager', '$rootScope'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly wizardStepManager: WizardStepManager,
        private readonly $rootScope: IRootScopeService,
    ) {
        this.serviceURL = `${REST_API}gesuche`;
    }

    public createGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        let sentGesuch = {};
        sentGesuch = this.ebeguRestUtil.gesuchToRestObject(sentGesuch, gesuch);
        return this.$http.post(this.serviceURL, sentGesuch).then((response: any) => {
            const convertedGesuch = this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            return this.wizardStepManager.updateFirstWizardStep(convertedGesuch.id).then(() => {
                return convertedGesuch;
            });
        });
    }

    public updateGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        let sentGesuch = {};
        sentGesuch = this.ebeguRestUtil.gesuchToRestObject(sentGesuch, gesuch);
        return this.$http.put(this.serviceURL, sentGesuch).then(response => {
            return this.wizardStepManager.findStepsFromGesuch(gesuch.id).then(() => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
        });
    }

    public findGesuch(gesuchID: string): IPromise<TSGesuch> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(gesuchID)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public findGesuchForFreigabe(gesuchID: string, anzZurueckgezogen: string): IPromise<TSAntragDTO> {
        return this.$http.get(`${this.serviceURL}/freigabe/${encodeURIComponent(gesuchID)}/${anzZurueckgezogen}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseAntragDTO(new TSAntragDTO(), response.data);
            });
    }

    public findGesuchForInstitution(gesuchID: string): IPromise<TSGesuch> {
        return this.$http.get(`${this.serviceURL}/institution/${encodeURIComponent(gesuchID)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public updateBemerkung(gesuchID: string, bemerkung: string): IHttpPromise<any> {
        return this.$http.put(`${this.serviceURL}/bemerkung/${encodeURIComponent(gesuchID)}`, bemerkung);
    }

    public updateBemerkungPruefungSTV(gesuchID: string, bemerkungPruefungSTV: string): IHttpPromise<any> {
        return this.$http.put(`${this.serviceURL}/bemerkungPruefungSTV/${encodeURIComponent(gesuchID)}`,
            bemerkungPruefungSTV);
    }

    public updateGesuchStatus(gesuchID: string, status: TSAntragStatus): IHttpPromise<any> {
        return this.$http.put(`${this.serviceURL}/status/${encodeURIComponent(gesuchID)}/${status}`, null);
    }

    public getAllAntragDTOForDossier(dossierId: string): IPromise<TSAntragDTO[]> {
        return this.$http.get(`${this.serviceURL}/dossier/${encodeURIComponent(dossierId)}`).then((response: any) => {
            return this.ebeguRestUtil.parseAntragDTOs(response.data);
        });
    }

    public antragFreigeben(antragId: string, usernameJA: string, usernameSCH: string): IPromise<TSGesuch> {
        const url = `${this.serviceURL}/freigeben/${encodeURIComponent(antragId)}/JA/${usernameJA}/SCH/${usernameSCH}`;
        return this.$http.post(url, null, {
            headers: {'Content-Type': 'text/plain'},
        }).then(response => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public antragZurueckziehen(antragId: string): IPromise<TSGesuch> {
        const url = `${this.serviceURL}/zurueckziehen/${encodeURIComponent(antragId)}`;
        return this.$http.post(url, null, {
            headers: {'Content-Type': 'text/plain'},
        }).then(response => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public setBeschwerdeHaengig(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/setBeschwerde/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public setAbschliessen(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/setAbschliessen/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public sendGesuchToSTV(antragId: string, bemerkungen: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/sendToSTV/${encodeURIComponent(antragId)}`, bemerkungen, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public gesuchBySTVFreigeben(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/freigebenSTV/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public stvPruefungAbschliessen(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/stvPruefungAbschliessen/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public removeBeschwerdeHaengig(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/removeBeschwerde/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public removeOnlineMutation(dossierID: string, gesuchsperiodeId: string): IPromise<boolean> {
        const url = `${this.serviceURL}/removeOnlineMutation/${encodeURIComponent(dossierID)}/${encodeURIComponent(
            gesuchsperiodeId)}`;

        return this.$http.delete(url)
            .then((response: any) => {
                this.$rootScope.$broadcast(TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_MUTATION_REMOVED],
                    response);
                return response.data;
            });
    }

    public removeOnlineFolgegesuch(dossierID: string, gesuchsperiodeId: string): IPromise<boolean> {
        const url = `${this.serviceURL}/removeOnlineFolgegesuch/${encodeURIComponent(dossierID)}/${encodeURIComponent(
            gesuchsperiodeId)}`;

        return this.$http.delete(url)
            .then((response: any) => {
                return response.data;
            });
    }

    public removeAntrag(gesuchId: string): IPromise<boolean> {
        return this.$http.delete(`${this.serviceURL}/removeAntrag/${encodeURIComponent(gesuchId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public removeAntragForced(gesuchId: string): IPromise<boolean> {
        return this.$http.delete(`${this.serviceURL}/removeAntragForced/${encodeURIComponent(gesuchId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public closeWithoutAngebot(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/closeWithoutAngebot/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public verfuegenStarten(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/verfuegenStarten/${encodeURIComponent(antragId)}`,
            null).then(response => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public getGesuchBetreuungenStatus(gesuchId: string): IPromise<TSGesuchBetreuungenStatus> {
        return this.$http.get(`${this.serviceURL}/gesuchBetreuungenStatus/${encodeURIComponent(gesuchId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public changeFinSitStatus(antragId: string, finSitStatus: TSFinSitStatus): IPromise<any> {
        return this.$http.post(`${this.serviceURL}/changeFinSitStatus/${encodeURIComponent(antragId)}/${finSitStatus}`,
            null);
    }

    public isNeuestesGesuch(gesuchID: string): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/newest/${encodeURIComponent(gesuchID)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getIdOfNewestGesuchForGesuchsperiode(gesuchsperiodeId: string, dossierId: string): IPromise<string> {
        const gesuchsperiodeIdEnc = encodeURIComponent(gesuchsperiodeId);
        const url = `${this.serviceURL}/newestid/gesuchsperiode/${gesuchsperiodeIdEnc}/dossier/${encodeURIComponent(
            dossierId)}`;

        return this.$http.get(url)
            .then((response: any) => {
                return response.data;
            });
    }

    public getIdOfNewestGesuchForDossier(dossierId: string): IPromise<string> {
        return this.$http.get(`${this.serviceURL}/newestid/fall/${encodeURIComponent(dossierId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public isAusserordentlicherAnspruchPossible(antragId: string): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/ausserordentlicheranspruchpossible/${encodeURIComponent(antragId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getMassenversandTexteForGesuch(gesuchID: string): IPromise<string[]> {
        return this.$http.get(`${this.serviceURL}/massenversand/${encodeURIComponent(gesuchID)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public setKeinKontingent(antragId: string): IPromise<TSGesuch> {
        return this.$http.post(`${this.serviceURL}/setKeinKontingent/${encodeURIComponent(antragId)}`, null).then(
            response => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public updateAlwaysEditableProperties(properties: any): IPromise<TSGesuch> {
        return this.$http.put(this.serviceURL + '/updateAlwaysEditableProperties', properties).then(response => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public lookupKitax(url: string, userUuid: string): IPromise<TSKitaxResponse> {
        return this.$http.get(`${url}/${userUuid}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseKitaxResponse(response.data);
            }).catch( () => {
                return undefined;
            });
    }
}
