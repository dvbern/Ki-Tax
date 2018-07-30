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

import {CONSTANTS} from '../../core/constants/CONSTANTS';
import TSAntragDTO from '../../models/TSAntragDTO';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import {TSBetreuungsangebotTyp} from '../../models/enums/TSBetreuungsangebotTyp';
import {TSDateRange} from '../../models/types/TSDateRange';
import * as moment from 'moment';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import EbeguUtil from '../../utils/EbeguUtil';
import {QuicksearchFilter} from './quicksearchFilter';

describe('quicksearchFilter', () => {

    let quicksearchFilter: any;
    let quicksearchArray: Array<TSAntragDTO>;
    let antrag1: TSAntragDTO;
    let antrag2: TSAntragDTO;
    let antrag3: TSAntragDTO;
    let gesuchsperiode: TSGesuchsperiode;


    beforeEach(angular.mock.module('pascalprecht.translate'));

    beforeEach(angular.mock.module(($provide: angular.auto.IProvideService) => {
        $provide.value('CONSTANTS', CONSTANTS);
        $provide.service('EbeguUtil', EbeguUtil);
        $provide.service('quicksearchFilterFilter', QuicksearchFilter);
    }));

    beforeEach(angular.mock.inject($injector => {
        quicksearchFilter = $injector.get('$filter')('quicksearchFilter');

        const ab = moment('31.08.2016', 'DD.MM.YYYY');
        const bis = moment('01.07.2017', 'DD.MM.YYYY');
        gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, new TSDateRange(ab, bis));

        quicksearchArray = [];
        antrag1 = new TSAntragDTO('id1', 1, 'Hernandez', TSAntragTyp.ERSTGESUCH, ab, ab, undefined,
            [TSBetreuungsangebotTyp.KITA], ['Instit1'], 'Juan Arbolado', 'Juan Arbolado', TSAntragStatus.IN_BEARBEITUNG_JA,
            gesuchsperiode.gueltigkeit.gueltigAb, gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag1);

        antrag2 = new TSAntragDTO('id2', 2, 'Perez', TSAntragTyp.ERSTGESUCH, ab, ab, undefined,
            [TSBetreuungsangebotTyp.TAGESFAMILIEN], ['Instit2'], 'Antonio Jimenez', 'Antonio Jimenez', TSAntragStatus.IN_BEARBEITUNG_JA,
            gesuchsperiode.gueltigkeit.gueltigAb, gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag2);

        antrag3 = new TSAntragDTO('id3', 3, 'Dominguez', TSAntragTyp.MUTATION, ab, ab, undefined,
            [TSBetreuungsangebotTyp.KITA, TSBetreuungsangebotTyp.TAGESFAMILIEN], ['Instit1', 'Instit2'],
            'Eustaquio Romualdo', 'Eustaquio Romualdo', TSAntragStatus.IN_BEARBEITUNG_JA,
            gesuchsperiode.gueltigkeit.gueltigAb, gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag3);

    }));

    describe('API usage', () => {
        it('should return an array with only the element with the given Fallnummer', () => {
            expect(quicksearchFilter(quicksearchArray, {fallNummer: '1'})).toEqual([antrag1]);
            expect(quicksearchFilter(quicksearchArray, {fallNummer: '01'})).toEqual([antrag1]);
            expect(quicksearchFilter(quicksearchArray, {fallNummer: '0002'})).toEqual([antrag2]);
            expect(quicksearchFilter(quicksearchArray, {fallNummer: '4'})).toEqual([]); // the fallnummer doesn't exist
        });
        it('should return an array with only the elements with the given Familienname or containing the given string', () => {
            expect(quicksearchFilter(quicksearchArray, {familienName: 'Hernandez'})).toEqual([antrag1]);
            expect(quicksearchFilter(quicksearchArray, {familienName: 'ez'})).toEqual([antrag1, antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {familienName: ''})).toEqual([antrag1, antrag2, antrag3]); // empty string returns all elements
            expect(quicksearchFilter(quicksearchArray, {familienName: 'rrr'})).toEqual([]); // no familienname with this pattern
        });
        it('should return an array with only the elements of the given antragTyp', () => {
            expect(quicksearchFilter(quicksearchArray, {antragTyp: TSAntragTyp.ERSTGESUCH})).toEqual([antrag1, antrag2]);
            expect(quicksearchFilter(quicksearchArray, {antragTyp: TSAntragTyp.MUTATION})).toEqual([antrag3]);
            expect(quicksearchFilter(quicksearchArray, {antragTyp: ''})).toEqual([antrag1, antrag2, antrag3]); // empty string returns all elements
            expect(quicksearchFilter(quicksearchArray, {antragTyp: 'error'})).toEqual([]);
        });
        it('should return an array with only the elements of the given gesuchsperiodeGueltigAb', () => {
            expect(quicksearchFilter(quicksearchArray, {gesuchsperiodeGueltigAb: '31.08.2016'})).toEqual([antrag1, antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {gesuchsperiodeGueltigAb: ''})).toEqual([antrag1, antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {gesuchsperiodeGueltigAb: '2020/2021'})).toEqual([]);
        });
        it('should return an array with only the elements of the given eingangsdatum', () => {
            expect(quicksearchFilter(quicksearchArray, {eingangsdatum: '31.08.2016'})).toEqual([antrag1, antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {eingangsdatum: ''})).toEqual([antrag1, antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {eingangsdatum: '31.08.2017'})).toEqual([]);
        });
        it('should return an array with only the elements of the given angebotstyp', () => {
            expect(quicksearchFilter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.KITA})).toEqual([antrag1, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.TAGESFAMILIEN})).toEqual([antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.TAGESSCHULE})).toEqual([]);
            expect(quicksearchFilter(quicksearchArray, {angebote: ''})).toEqual([antrag1, antrag2, antrag3]);
        });
        it('should return an array with only the elements of the given institutionen', () => {
            expect(quicksearchFilter(quicksearchArray, {institutionen: 'Instit1'})).toEqual([antrag1, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {institutionen: 'Instit2'})).toEqual([antrag2, antrag3]);
            expect(quicksearchFilter(quicksearchArray, {institutionen: ''})).toEqual([antrag1, antrag2, antrag3]);
        });
        it('should return the elements containing all given params, for a multiple filtering', () => {
            expect(quicksearchFilter(quicksearchArray, {
                familienName: 'Hernandez',
                institutionen: 'Instit1'
            })).toEqual([antrag1]);
        });
    });

});
