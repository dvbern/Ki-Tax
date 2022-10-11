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
import {Observable, ReplaySubject} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAntragStatusHistory} from '../../../models/TSAntragStatusHistory';
import {TSDossier} from '../../../models/TSDossier';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class AntragStatusHistoryRS {

    public get lastChange$(): Observable<TSAntragStatusHistory | null> {
        return this._lastChange$;
    }

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'AuthServiceRS'];

    public serviceURL: string;

    private readonly lastChangeSubject$ = new ReplaySubject<TSAntragStatusHistory | null>(1);
    private readonly _lastChange$: Observable<TSAntragStatusHistory | null> = this.lastChangeSubject$.asObservable();

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly authServiceRS: AuthServiceRS
    ) {
        this.serviceURL = `${REST_API}antragStatusHistory`;
    }

    /**
     * laedt und merkt sich den letzten Statusuebergang, kann mit #lastChange() ausgelesen werden
     */
    public loadLastStatusChange(gesuch: TSGesuch): IPromise<TSAntragStatusHistory> {
        if (gesuch && gesuch.id) {
            return this.http.get(`${this.serviceURL}/${encodeURIComponent(gesuch.id)}`)
                .then((response: any) => {
                    this.log.debug('PARSING AntragStatusHistory REST object ', response.data);
                    const history = new TSAntragStatusHistory();
                    const lastChange = this.ebeguRestUtil.parseAntragStatusHistory(history, response.data);
                    this.lastChangeSubject$.next(lastChange);

                    return lastChange;
                });
        }
        return Promise.resolve(undefined);
    }

    public loadAllAntragStatusHistoryByGesuchsperiode(
        dossier: TSDossier,
        gesuchsperiode: TSGesuchsperiode
    ): IPromise<Array<TSAntragStatusHistory>> {

        if (gesuchsperiode && gesuchsperiode.id && dossier && dossier.id) {
            const baseUrl = `${this.serviceURL}/verlauf/`;

            return this.http.get(`${baseUrl + encodeURIComponent(gesuchsperiode.id)}/${encodeURIComponent(dossier.id)}`)
                .then((response: any) => {
                    this.log.debug('PARSING AntragStatusHistory REST object ', response.data);
                    return this.ebeguRestUtil.parseAntragStatusHistoryCollection(response.data);
                });
        }
        return Promise.resolve(undefined);
    }

    /**
     * Gibt den FullName des Benutzers zurueck, der den Gesuchsstatus am letzten geaendert hat. Sollte das Gesuch noch
     * nicht gespeichert sein (fallCreation), wird der FullName des eingeloggten Benutzers zurueckgegeben
     */
    public getUserFullname(lastChange: TSAntragStatusHistory): string {
        if (lastChange && lastChange.benutzer) {
            return lastChange.benutzer.getFullName();
        }

        const principal = this.authServiceRS.getPrincipal();

        return principal ? principal.getFullName() : '';
    }

}
