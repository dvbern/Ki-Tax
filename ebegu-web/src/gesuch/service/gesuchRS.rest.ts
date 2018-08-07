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

import {IHttpPromise, IHttpService, ILogService, IPromise} from 'angular';
import * as moment from 'moment';
import {IEntityRS} from '../../core/service/iEntityRS.rest';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSGesuchBetreuungenStatus} from '../../models/enums/TSGesuchBetreuungenStatus';
import {TSMitteilungEvent} from '../../models/enums/TSMitteilungEvent';
import TSAntragDTO from '../../models/TSAntragDTO';
import TSGesuch from '../../models/TSGesuch';
import DateUtil from '../../utils/DateUtil';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import WizardStepManager from './wizardStepManager';
import IRootScopeService = angular.IRootScopeService;
import {TSFinSitStatus} from '../../models/enums/TSFinSitStatus';

export default class GesuchRS implements IEntityRS {
    serviceURL: string;
    http: IHttpService;
    ebeguRestUtil: EbeguRestUtil;

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager', '$rootScope'];
    /* @ngInject */
    constructor($http: IHttpService, REST_API: string, ebeguRestUtil: EbeguRestUtil, private $log: ILogService,
                private wizardStepManager: WizardStepManager, private $rootScope: IRootScopeService) {
        this.serviceURL = REST_API + 'gesuche';
        this.http = $http;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    public createGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        let sentGesuch = {};
        sentGesuch = this.ebeguRestUtil.gesuchToRestObject(sentGesuch, gesuch);
        return this.http.post(this.serviceURL, sentGesuch, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response: any) => {
            this.$log.debug('PARSING gesuch REST object ', response.data);
            let convertedGesuch: TSGesuch = this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            return this.wizardStepManager.updateFirstWizardStep(convertedGesuch.id).then(() => {
                return convertedGesuch;
            });
        });
    }

    public updateGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        let sentGesuch = {};
        sentGesuch = this.ebeguRestUtil.gesuchToRestObject(sentGesuch, gesuch);
        return this.http.put(this.serviceURL, sentGesuch, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            return this.wizardStepManager.findStepsFromGesuch(gesuch.id).then(() => {
                this.$log.debug('PARSING gesuch REST object ', response.data);
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
        });
    }

    public findGesuch(gesuchID: string): IPromise<TSGesuch> {
        return this.http.get(this.serviceURL + '/' + encodeURIComponent(gesuchID))
            .then((response: any) => {
                this.$log.debug('PARSING gesuch REST object ', response.data);
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public findGesuchForFreigabe(gesuchID: string): IPromise<TSAntragDTO> {
        return this.http.get(this.serviceURL + '/freigabe/' + encodeURIComponent(gesuchID))
            .then((response: any) => {
                this.$log.debug('PARSING antragDTO REST object ', response.data);
                return this.ebeguRestUtil.parseAntragDTO(new TSAntragDTO(), response.data);
            });
    }


    public findGesuchForInstitution(gesuchID: string): IPromise<TSGesuch> {
        return this.http.get(this.serviceURL + '/institution/' + encodeURIComponent(gesuchID))
            .then((response: any) => {
                this.$log.debug('PARSING gesuch (fuer Institutionen) REST object ', response.data);
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
    }

    public updateBemerkung(gesuchID: string, bemerkung: string): IHttpPromise<any> {
        return this.http.put(this.serviceURL + '/bemerkung/' + encodeURIComponent(gesuchID), bemerkung, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }

    public updateBemerkungPruefungSTV(gesuchID: string, bemerkungPruefungSTV: string): IHttpPromise<any> {
        return this.http.put(this.serviceURL + '/bemerkungPruefungSTV/' + encodeURIComponent(gesuchID), bemerkungPruefungSTV, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }

    public updateGesuchStatus(gesuchID: string, status: TSAntragStatus): IHttpPromise<any> {
        return this.http.put(this.serviceURL + '/status/' + encodeURIComponent(gesuchID) + '/' + status, null);
    }

    public getAllAntragDTOForFall(fallId: string): IPromise<TSAntragDTO[]> {
        return this.http.get(this.serviceURL + '/fall/' + encodeURIComponent(fallId)).then((response: any) => {
            return this.ebeguRestUtil.parseAntragDTOs(response.data);
        });
    }

    public antragMutieren(antragId: string, dateParam: moment.Moment): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/mutieren/' + encodeURIComponent(antragId), null,
            {params: {date: DateUtil.momentToLocalDate(dateParam)}}).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public antragErneuern(gesuchsperiodeId: string, antragId: string, dateParam: moment.Moment): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/erneuern/' + encodeURIComponent(gesuchsperiodeId) + '/' + encodeURIComponent(antragId), null,
            {params: {date: DateUtil.momentToLocalDate(dateParam)}}).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public antragFreigeben(antragId: string, usernameJA: string, usernameSCH: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/freigeben/' + encodeURIComponent(antragId) + '/JA/' + usernameJA + '/SCH/' + usernameSCH,
            null, {
            headers: {
                'Content-Type': 'text/plain'
            }
        }).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public setBeschwerdeHaengig(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/setBeschwerde/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public setAbschliessen(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/setAbschliessen/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public sendGesuchToSTV(antragId: string, bemerkungen: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/sendToSTV/' + encodeURIComponent(antragId), bemerkungen, null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public gesuchBySTVFreigeben(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/freigebenSTV/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public stvPruefungAbschliessen(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/stvPruefungAbschliessen/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public removeBeschwerdeHaengig(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/removeBeschwerde/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public removeOnlineMutation(dossierID: string, gesuchsperiodeId: string): IPromise<boolean> {
        return this.http.delete(this.serviceURL + '/removeOnlineMutation/' + encodeURIComponent(dossierID)
            + '/' + encodeURIComponent(gesuchsperiodeId))
            .then((response: any) => {
                this.$rootScope.$broadcast(TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_MUTATION_REMOVED], response);
                return response.data;
            });
    }

    public removeOnlineFolgegesuch(dossierID: string, gesuchsperiodeId: string): IPromise<boolean> {
        return this.http.delete(this.serviceURL + '/removeOnlineFolgegesuch/' + encodeURIComponent(dossierID)
            + '/' + encodeURIComponent(gesuchsperiodeId))
            .then((response: any) => {
                return response.data;
            });
    }

    public removePapiergesuch(gesuchId: string): IPromise<boolean> {
        return this.http.delete(this.serviceURL + '/removePapiergesuch/' + encodeURIComponent(gesuchId))
            .then((response: any) => {
                return response.data;
            });
    }

    public removeGesuchstellerAntrag(gesuchId: string): IPromise<boolean> {
        return this.http.delete(this.serviceURL + '/removeGesuchstellerAntrag/' + encodeURIComponent(gesuchId))
            .then((response: any) => {
                return response.data;
            });
    }

    public closeWithoutAngebot(antragId: string): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/closeWithoutAngebot/' + encodeURIComponent(antragId), null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public verfuegenStarten(antragId: string, hasFSDocument: boolean): IPromise<TSGesuch> {
        return this.http.post(this.serviceURL + '/verfuegenStarten/' + encodeURIComponent(antragId) + '/' + hasFSDocument, null).then((response) => {
            return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
        });
    }

    public getGesuchBetreuungenStatus(gesuchId: string): IPromise<TSGesuchBetreuungenStatus> {
        return this.http.get(this.serviceURL + '/gesuchBetreuungenStatus/' + encodeURIComponent(gesuchId))
            .then((response: any) => {
                return response.data;
            });
    }

    public gesuchVerfuegen(antragId: string): IHttpPromise<any> {
        return this.http.post(this.serviceURL + '/gesuchVerfuegen/' + encodeURIComponent(antragId), null);
    }

    public changeFinSitStatus(antragId: string, finSitStatus: TSFinSitStatus): IPromise<any> {
        return this.http.post(this.serviceURL + '/changeFinSitStatus/' + encodeURIComponent(antragId) + '/' + finSitStatus, null);
    }

    public isNeuestesGesuch(gesuchID: string): IPromise<boolean> {
        return this.http.get(this.serviceURL + '/newest/' + encodeURIComponent(gesuchID))
            .then((response: any) => {
                return response.data;
            });
    }

    public getIdOfNewestGesuchForGesuchsperiode(gesuchsperiodeId: string, dossierId: string): IPromise<string> {
        return this.http.get(this.serviceURL + '/newestid/gesuchsperiode/' + encodeURIComponent(gesuchsperiodeId)
            + '/dossier/' + encodeURIComponent(dossierId))
            .then((response: any) => {
                return response.data;
            });
    }

    public getIdOfNewestGesuchForDossier(dossierId: string): IPromise<string> {
        return this.http.get(this.serviceURL + '/newestid/fall/' + encodeURIComponent(dossierId))
            .then((response: any) => {
                return response.data;
            });
    }
}
