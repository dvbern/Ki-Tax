/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSWizardStepXTyp} from '../../../models/enums/TSWizardStepXTyp';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';

const LOG = LogFactory.createLog('GemeindeAntragService');

@Injectable({
    providedIn: 'root',
})
export class GemeindeAntragService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}gemeindeantrag`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        private readonly http: HttpClient,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public getGemeindeAntraege(filter: DVAntragListFilter, sort: {
        predicate?: string,
        reverse?: boolean
    }): Observable<TSGemeindeAntrag[]> {
        let params = new HttpParams();
        if (filter.gemeinde) {
            params = params.append('gemeinde', filter.gemeinde);
        }
        if (filter.gesuchsperiodeString) {
            params = params.append('periode', filter.gesuchsperiodeString);
        }
        if (filter.antragTyp) {
            params = params.append('typ', filter.antragTyp);
        }
        if (filter.status) {
            params = params.append('status', filter.status);
        }
        if (filter.aenderungsdatum) {
            params = params.append('timestampMutiert', filter.aenderungsdatum);
        }
        return this.http.get<TSGemeindeAntrag[]>(this.API_BASE_URL, {
            params,
        }).pipe(
            map(antraege => this.ebeguRestUtil.parseGemeindeAntragList(antraege)),
            map(antraege => this.sortAntraege(antraege, sort)),
        );
    }

    public getTypesForRole(): TSGemeindeAntragTyp[] {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())) {
            return [TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN, TSGemeindeAntragTyp.FERIENBETREUUNG];
        }
        return [TSGemeindeAntragTyp.FERIENBETREUUNG];
    }

    public createAllAntrage(
        toCreate: { periode: string, antragTyp: string, gemeinde?: TSGemeinde }
        ): Observable<TSGemeindeAntrag[]> {
        return this.http.post<TSGemeindeAntrag[]>(
            `${this.API_BASE_URL}/createAllAntraege/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}`,
            toCreate)
            .pipe(map(jaxAntrag => this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)));
    }

    public deleteAntrage(
        toDelete: { periode: string, antragTyp: string, gemeinde?: TSGemeinde }
        ): Observable<void> {
        return this.http.delete<void>(
            `${this.API_BASE_URL}/deleteAntraege/${toDelete.antragTyp}/gesuchsperiode/${toDelete.periode}`);
    }

    public createAntrag(
        toCreate: { periode: string, antragTyp: string, gemeinde: string }
        ): Observable<TSGemeindeAntrag[]> {
        return this.http.post<TSGemeindeAntrag[]>(
            `${this.API_BASE_URL}/create/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}/gemeinde/${toCreate.gemeinde}`,
            toCreate)
            .pipe(map(jaxAntrag => this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)));
    }

    private sortAntraege(
        antraege: TSGemeindeAntrag[],
        sort: { predicate?: string; reverse?: boolean },
    ): TSGemeindeAntrag[] {
        switch (sort.predicate) {
            case 'status':
                return sort.reverse ?
                    antraege.sort((a, b) => a.statusString.localeCompare(b.statusString)) :
                    antraege.sort((a, b) => b.statusString.localeCompare(a.statusString));
            case 'gemeinde':
                return sort.reverse ?
                    antraege.sort((a, b) => a.gemeinde.name.localeCompare(b.gemeinde.name)) :
                    antraege.sort((a, b) => b.gemeinde.name.localeCompare(a.gemeinde.name));
            case 'antragTyp':
                return sort.reverse ?
                    antraege.sort((a, b) => a.gemeindeAntragTyp.localeCompare(b.gemeindeAntragTyp)) :
                    antraege.sort((a, b) => b.gemeindeAntragTyp.localeCompare(a.gemeindeAntragTyp));
            case 'gesuchsperiodeString':
                return sort.reverse ?
                    antraege.sort((a, b) =>
                        a.gesuchsperiode.gesuchsperiodeString.localeCompare(b.gesuchsperiode.gesuchsperiodeString)) :
                    antraege.sort((a, b) =>
                        b.gesuchsperiode.gesuchsperiodeString.localeCompare(a.gesuchsperiode.gesuchsperiodeString));
            case 'aenderungsdatum':
                return sort.reverse ?
                    antraege.sort((a, b) =>
                        b.timestampMutiert.diff(a.timestampMutiert)) :
                    antraege.sort((a, b) =>
                        a.timestampMutiert.diff(b.timestampMutiert));
            default:
                return antraege;
        }
    }

    public gemeindeAntragTypStringToWizardStepTyp(wizardTypStr: string): TSWizardStepXTyp | undefined {
        if (!wizardTypStr) {
            LOG.error('no wizardTypStr provided');
            return undefined;
        }
        if (wizardTypStr === 'LASTENAUSGLEICH_TAGESSCHULEN') {
            return TSWizardStepXTyp.LASTENAUSGLEICH_TAGESSCHULEN;
        }
        if (wizardTypStr === 'FERIENBETREUUNG') {
            return TSWizardStepXTyp.FERIENBETREUUNG;
        }
        LOG.error('wrong wizardTypStr provided');
        return undefined;
    }

    public getAllVisibleTagesschulenAngabenForTSLastenausgleich(lastenausgleichId: string): Observable<TSLastenausgleichTagesschuleAngabenInstitutionContainer[]> {
        return this.http.get<TSLastenausgleichTagesschuleAngabenInstitution[]>(`${this.API_BASE_URL}/${lastenausgleichId}/tagesschulenantraege`)
            .pipe(
                map(lastenausgleichAngabenList => this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenInstitutionContainerList(
                    lastenausgleichAngabenList)),
            );
    }
}
