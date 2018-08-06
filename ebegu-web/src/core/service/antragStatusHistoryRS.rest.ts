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
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import TSAntragStatusHistory from '../../models/TSAntragStatusHistory';
import TSDossier from '../../models/TSDossier';
import TSGesuch from '../../models/TSGesuch';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import EbeguRestUtil from '../../utils/EbeguRestUtil';

export default class AntragStatusHistoryRS {

    get lastChange(): TSAntragStatusHistory {
        return this._lastChange;
    }


    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'AuthServiceRS'];

    serviceURL: string;

    private _lastChange: TSAntragStatusHistory;

    constructor(public http: IHttpService, REST_API: string, public ebeguRestUtil: EbeguRestUtil, public log: ILogService,
                private readonly authServiceRS: AuthServiceRS) {
        this.serviceURL = REST_API + 'antragStatusHistory';
    }

    public getServiceName(): string {
        return 'AntragStatusHistoryRS';
    }

    /**
     * laedt und merkt sich den letzten Statusuebergang, kann mit #lastChange() ausgelesen werden
     */
    public loadLastStatusChange(gesuch: TSGesuch): IPromise<TSAntragStatusHistory> {
        if (gesuch && gesuch.id) {
            return this.http.get(this.serviceURL + '/' + encodeURIComponent(gesuch.id))
                .then((response: any) => {
                    this.log.debug('PARSING AntragStatusHistory REST object ', response.data);
                    this._lastChange = this.ebeguRestUtil.parseAntragStatusHistory(new TSAntragStatusHistory(), response.data);
                    return this._lastChange;
                });
        } else {
            this._lastChange = undefined;
        }
        return undefined;
    }

    public loadAllAntragStatusHistoryByGesuchsperiode(dossier: TSDossier, gesuchsperiode: TSGesuchsperiode): IPromise<Array<TSAntragStatusHistory>> {
        if (gesuchsperiode && gesuchsperiode.id && dossier && dossier.id) {
            return this.http.get(this.serviceURL + '/verlauf/' + encodeURIComponent(gesuchsperiode.id) + '/' + encodeURIComponent(dossier.id))
                .then((response: any) => {
                    this.log.debug('PARSING AntragStatusHistory REST object ', response.data);
                    return this.ebeguRestUtil.parseAntragStatusHistoryCollection(response.data);
                });
        }
        return undefined;
    }

    /**
     * Gibt den FullName des Benutzers zurueck, der den Gesuchsstatus am letzten geaendert hat. Sollte das Gesuch noch nicht
     * gespeichert sein (fallCreation), wird der FullName des eingeloggten Benutzers zurueckgegeben
     * @returns {string}
     */
    public getUserFullname(): string {
        if (this.lastChange && this.lastChange.benutzer) {
            return this.lastChange.benutzer.getFullName();
        } else {
            if (this.authServiceRS && this.authServiceRS.getPrincipal()) {
                return this.authServiceRS.getPrincipal().getFullName();
            }
        }
        return '';
    }

}
