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

import * as moment from 'moment';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {quicksearchFilter} from './quicksearchFilter';
import IProvideService = angular.auto.IProvideService;

describe('quicksearchFilter', () => {
    let quicksearchArray: Array<TSAntragDTO>;
    let antrag1: TSAntragDTO;
    let antrag2: TSAntragDTO;
    let antrag3: TSAntragDTO;
    let gesuchsperiode: TSGesuchsperiode;

    const abStr = '31.08.2016';
    let filter: any;

    beforeEach(
        angular.mock.module(($provide: IProvideService) => {
            $provide.service('quicksearchFilterFilter', quicksearchFilter);
        })
    );

    beforeEach(
        angular.mock.inject($injector => {
            filter = $injector.get('$filter')('quicksearchFilter');

            const ab = moment(abStr, 'DD.MM.YYYY');
            const bis = moment('01.07.2017', 'DD.MM.YYYY');
            gesuchsperiode = new TSGesuchsperiode(
                TSGesuchsperiodeStatus.AKTIV,
                new TSDateRange(ab, bis)
            );

            quicksearchArray = [];
            createAntrag1(ab);
            quicksearchArray.push(antrag1);

            createAntrag2(ab);

            quicksearchArray.push(antrag2);

            createAntrag3(ab);
            quicksearchArray.push(antrag3);
        })
    );

    describe('API usage', () => {
        it('should return an array with only the element with the given Fallnummer', () => {
            expect(filter(quicksearchArray, {fallNummer: '1'})).toEqual([
                antrag1
            ]);
            expect(filter(quicksearchArray, {fallNummer: '01'})).toEqual([
                antrag1
            ]);
            expect(filter(quicksearchArray, {fallNummer: '0002'})).toEqual([
                antrag2
            ]);
            // the fallnummer doesn't exist
            expect(filter(quicksearchArray, {fallNummer: '4'})).toEqual([]);
        });
        it('should return an array with only the elements with the given Familienname or containing the given string', () => {
            expect(
                filter(quicksearchArray, {familienName: 'Hernandez'})
            ).toEqual([antrag1]);
            expect(filter(quicksearchArray, {familienName: 'ez'})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
            // empty string returns all elements
            expect(filter(quicksearchArray, {familienName: ''})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
            // no familienname with this pattern
            expect(filter(quicksearchArray, {familienName: 'rrr'})).toEqual([]);
        });
        it('should return an array with only the elements of the given antragTyp', () => {
            expect(
                filter(quicksearchArray, {antragTyp: TSAntragTyp.ERSTGESUCH})
            ).toEqual([antrag1, antrag2]);
            expect(
                filter(quicksearchArray, {antragTyp: TSAntragTyp.MUTATION})
            ).toEqual([antrag3]);
            // empty string returns all elements
            expect(filter(quicksearchArray, {antragTyp: ''})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
            expect(filter(quicksearchArray, {antragTyp: 'error'})).toEqual([]);
        });
        it('should return an array with only the elements of the given gesuchsperiodeGueltigAb', () => {
            expect(
                filter(quicksearchArray, {gesuchsperiodeGueltigAb: abStr})
            ).toEqual([antrag1, antrag2, antrag3]);
            expect(
                filter(quicksearchArray, {gesuchsperiodeGueltigAb: ''})
            ).toEqual([antrag1, antrag2, antrag3]);
            expect(
                filter(quicksearchArray, {gesuchsperiodeGueltigAb: '2020/2021'})
            ).toEqual([]);
        });
        it('should return an array with only the elements of the given eingangsdatum', () => {
            expect(filter(quicksearchArray, {eingangsdatum: abStr})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
            expect(filter(quicksearchArray, {eingangsdatum: ''})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
            expect(
                filter(quicksearchArray, {eingangsdatum: '31.08.2017'})
            ).toEqual([]);
        });
        it('should return an array with only the elements of the given angebotstyp', () => {
            expect(
                filter(quicksearchArray, {
                    angebote: TSBetreuungsangebotTyp.KITA
                })
            ).toEqual([antrag1, antrag3]);
            expect(
                filter(quicksearchArray, {
                    angebote: TSBetreuungsangebotTyp.TAGESFAMILIEN
                })
            ).toEqual([antrag2, antrag3]);
            expect(
                filter(quicksearchArray, {
                    angebote: TSBetreuungsangebotTyp.TAGESSCHULE
                })
            ).toEqual([]);
            expect(filter(quicksearchArray, {angebote: ''})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
        });
        it('should return an array with only the elements of the given institutionen', () => {
            expect(
                filter(quicksearchArray, {institutionen: 'Instit1'})
            ).toEqual([antrag1, antrag3]);
            expect(
                filter(quicksearchArray, {institutionen: 'Instit2'})
            ).toEqual([antrag2, antrag3]);
            expect(filter(quicksearchArray, {institutionen: ''})).toEqual([
                antrag1,
                antrag2,
                antrag3
            ]);
        });
        it('should return the elements containing all given params, for a multiple filtering', () => {
            expect(
                filter(quicksearchArray, {
                    familienName: 'Hernandez',
                    institutionen: 'Instit1'
                })
            ).toEqual([antrag1]);
        });
    });

    function createAntrag1(ab: moment.Moment): void {
        antrag1 = new TSAntragDTO();
        antrag1.antragId = 'id1';
        antrag1.fallNummer = 1;
        antrag1.familienName = 'Hernandez';
        antrag1.antragTyp = TSAntragTyp.ERSTGESUCH;
        antrag1.eingangsdatum = ab;
        antrag1.eingangsdatumSTV = ab;
        antrag1.angebote = [TSBetreuungsangebotTyp.KITA];
        antrag1.institutionen = ['Instit1'];
        antrag1.verantwortlicherBG = 'Juan Arbolado';
        antrag1.verantwortlicherTS = 'Juan Arbolado';
        antrag1.status = TSAntragStatus.IN_BEARBEITUNG_JA;
        antrag1.gesuchsperiodeGueltigAb = gesuchsperiode.gueltigkeit.gueltigAb;
        antrag1.gesuchsperiodeGueltigBis =
            gesuchsperiode.gueltigkeit.gueltigBis;
    }

    function createAntrag2(ab: moment.Moment): void {
        antrag2 = new TSAntragDTO();
        antrag2.antragId = 'id2';
        antrag2.fallNummer = 2;
        antrag2.familienName = 'Perez';
        antrag2.antragTyp = TSAntragTyp.ERSTGESUCH;
        antrag2.eingangsdatum = ab;
        antrag2.eingangsdatumSTV = ab;
        antrag2.angebote = [TSBetreuungsangebotTyp.TAGESFAMILIEN];
        antrag2.institutionen = ['Instit2'];
        antrag2.verantwortlicherBG = 'Antonio Jimenez';
        antrag2.verantwortlicherTS = 'Antonio Jimenez';
        antrag2.status = TSAntragStatus.IN_BEARBEITUNG_JA;
        antrag2.gesuchsperiodeGueltigAb = gesuchsperiode.gueltigkeit.gueltigAb;
        antrag2.gesuchsperiodeGueltigBis =
            gesuchsperiode.gueltigkeit.gueltigBis;
    }

    function createAntrag3(ab: moment.Moment): void {
        antrag3 = new TSAntragDTO();
        antrag3.antragId = 'id3';
        antrag3.fallNummer = 3;
        antrag3.familienName = 'Dominguez';
        antrag3.antragTyp = TSAntragTyp.MUTATION;
        antrag3.eingangsdatum = ab;
        antrag3.eingangsdatumSTV = ab;
        antrag3.angebote = [
            TSBetreuungsangebotTyp.KITA,
            TSBetreuungsangebotTyp.TAGESFAMILIEN
        ];
        antrag3.institutionen = ['Instit1', 'Instit2'];
        antrag3.verantwortlicherBG = 'Eustaquio Romualdo';
        antrag3.verantwortlicherTS = 'Eustaquio Romualdo';
        antrag3.status = TSAntragStatus.IN_BEARBEITUNG_JA;
        antrag3.gesuchsperiodeGueltigAb = gesuchsperiode.gueltigkeit.gueltigAb;
        antrag3.gesuchsperiodeGueltigBis =
            gesuchsperiode.gueltigkeit.gueltigBis;
    }
});
