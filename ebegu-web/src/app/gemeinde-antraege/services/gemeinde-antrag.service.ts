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
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSPaginationResultDTO} from '../../../models/TSPaginationResultDTO';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';
import {PaginationDTO} from '../../shared/interfaces/PaginationDTO';

const LOG = LogFactory.createLog('GemeindeAntragService');

@Injectable({
    providedIn: 'root'
})
export class GemeindeAntragService {
    private readonly API_BASE_URL = `${CONSTANTS.REST_API}gemeindeantrag`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        private readonly http: HttpClient,
        private readonly authServiceRS: AuthServiceRS
    ) {}

    public getGemeindeAntraege(
        filter: DVAntragListFilter,
        sort: {
            predicate?: string;
            reverse?: boolean;
        },
        paginationDTO: PaginationDTO
    ): Observable<TSPaginationResultDTO<TSGemeindeAntrag>> {
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
        if (filter.verantwortlicherGemeindeantraege) {
            params = params.append(
                'verantwortlicher',
                filter.verantwortlicherGemeindeantraege.username
            );
        }
        if (sort.predicate) {
            params = params.append('sortPredicate', sort.predicate);
        }
        if (sort.reverse) {
            params = params.append('sortReverse', `${sort.reverse}`);
        }
        params = params.append(
            'paginationStart',
            paginationDTO.start.toFixed(0)
        );
        params = params.append(
            'paginationNumber',
            paginationDTO.number.toFixed(0)
        );

        return this.http
            .get<any>(this.API_BASE_URL, {
                params
            })
            .pipe(
                map(result => {
                    const dto = new TSPaginationResultDTO<TSGemeindeAntrag>();
                    dto.resultList = this.ebeguRestUtil.parseGemeindeAntragList(
                        result.resultList
                    );
                    dto.totalResultSize = result.totalCount;
                    return dto;
                })
            );
    }

    public getFilterableTypesForRole(): TSGemeindeAntragTyp[] {
        if (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) ||
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGemeindeOrBGOrTSRoles()
            )
        ) {
            return [
                TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN,
                TSGemeindeAntragTyp.FERIENBETREUUNG,
                TSGemeindeAntragTyp.GEMEINDE_KENNZAHLEN
            ];
        }
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getFerienbetreuungGemeindeRolesOnly()
            )
        ) {
            return [TSGemeindeAntragTyp.FERIENBETREUUNG];
        }
        return [TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN];
    }

    public getCreatableTypesForRole(): TSGemeindeAntragTyp[] {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())) {
            return [
                TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN,
                TSGemeindeAntragTyp.FERIENBETREUUNG,
                TSGemeindeAntragTyp.GEMEINDE_KENNZAHLEN
            ];
        }
        return [TSGemeindeAntragTyp.FERIENBETREUUNG];
    }

    public createAllAntrage(
        toCreate: {periode: string; antragTyp: string},
        gemeinden: TSGemeinde[]
    ): Observable<TSGemeindeAntrag[]> {
        return this.http
            .post<TSGemeindeAntrag[]>(
                `${this.API_BASE_URL}/createAllAntraege/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}`,
                gemeinden.map(gemeinde =>
                    this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde)
                )
            )
            .pipe(
                map(jaxAntrag =>
                    this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)
                )
            );
    }

    public deleteAllAntrage(
        gesuchsperiode: string,
        antragTyp: TSGemeindeAntragTyp
    ): Observable<void> {
        return this.http.delete<void>(
            `${this.API_BASE_URL}/deleteAntraege/${antragTyp}/gesuchsperiode/${gesuchsperiode}`
        );
    }

    public deleteGemeindeAntrag(
        gesuchsperiode: TSGesuchsperiode,
        gemeindeName: string,
        antragTyp: string
    ): Observable<void> {
        return this.http.delete<void>(
            `${this.API_BASE_URL}/deleteAntrag/${antragTyp}/gesuchsperiode/${encodeURIComponent(gesuchsperiode.id)}/gemeinde/${gemeindeName}`
        );
    }

    public createAntrag(toCreate: {
        periode: string;
        antragTyp: string;
        gemeinde: string;
    }): Observable<TSGemeindeAntrag[]> {
        return this.http
            .post<
                TSGemeindeAntrag[]
            >(`${this.API_BASE_URL}/create/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}/gemeinde/${toCreate.gemeinde}`, toCreate)
            .pipe(
                map(jaxAntrag =>
                    this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)
                )
            );
    }

    public gemeindeAntragTypStringToWizardStepTyp(
        wizardTypStr: string
    ): TSWizardStepXTyp | undefined {
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
        if (wizardTypStr === 'GEMEINDE_KENNZAHLEN') {
            return TSWizardStepXTyp.GEMEINDE_KENNZAHLEN;
        }
        LOG.error('wrong wizardTypStr provided');
        return undefined;
    }

    public getAllVisibleTagesschulenAngabenForTSLastenausgleich(
        lastenausgleichId: string
    ): Observable<TSLastenausgleichTagesschuleAngabenInstitutionContainer[]> {
        return this.http
            .get<
                TSLastenausgleichTagesschuleAngabenInstitution[]
            >(`${this.API_BASE_URL}/${lastenausgleichId}/tagesschulenantraege`)
            .pipe(
                map(lastenausgleichAngabenList =>
                    this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenInstitutionContainerList(
                        lastenausgleichAngabenList
                    )
                )
            );
    }
}
