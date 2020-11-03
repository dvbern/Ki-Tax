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
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';
import {TSAbstractEntity} from '../models/TSAbstractEntity';
import {TSFall} from '../models/TSFall';
import {TSGemeinde} from '../models/TSGemeinde';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSDateRange} from '../models/types/TSDateRange';
import {EbeguUtil} from './EbeguUtil';
import {TestDataUtil} from './TestDataUtil.spec';
import IProvideService = angular.auto.IProvideService;

// tslint:disable:no-magic-numbers
describe('EbeguUtil', () => {

    let ebeguUtil: EbeguUtil;
    const defaultFormat = 'DD.MM.YYYY';

    // Das wird nur fuer tests gebraucht in denen etwas uebersetzt wird. Leider muss man dieses erstellen
    // bevor man den Injector erstellt hat. Deshalb muss es fuer alle Tests definiert werden
    beforeEach(angular.mock.module(($provide: IProvideService) => {
        const mockTranslateFilter = (value: any) => {
            if (value === 'FIRST') {
                return 'Erster';
            }
            if (value === 'SECOND') {
                return 'Zweiter';
            }
            return value;
        };
        $provide.value('translateFilter', mockTranslateFilter);
    }));

    beforeEach(angular.mock.inject($injector => {
        ebeguUtil = new EbeguUtil($injector.get('$filter'), undefined, undefined);
    }));

    describe('translateStringList', () => {
        it('should translate the given list of words', () => {
            const list = ['FIRST', 'SECOND'];
            const returnedList = ebeguUtil.translateStringList(list);
            expect(returnedList.length).toEqual(2);
            expect(returnedList[0].key).toEqual('FIRST');
            expect(returnedList[0].value).toEqual('Erster');
            expect(returnedList[1].key).toEqual('SECOND');
            expect(returnedList[1].value).toEqual('Zweiter');
        });
    });
    describe('addZerosToNumber', () => {
        it('returns a string with 6 chars starting with 0s and ending with the given number', () => {
            expect(ebeguUtil.addZerosToNumber(0, 2)).toEqual('00');
            expect(ebeguUtil.addZerosToNumber(1, 2)).toEqual('01');
            expect(ebeguUtil.addZerosToNumber(12, 2)).toEqual('12');
        });
        it('returns undefined if the number is undefined', () => {
            expect(ebeguUtil.addZerosToNumber(undefined, 2)).toBeUndefined();
            expect(ebeguUtil.addZerosToNumber(null, 2)).toBeUndefined();
        });
        it('returns the given number as string if its length is greather than 6', () => {
            expect(ebeguUtil.addZerosToNumber(1234567, 6)).toEqual('1234567');
        });
    });
    describe('calculateBetreuungsId', () => {
        it('it returns empty string for undefined objects', () => {
            expect(ebeguUtil.calculateBetreuungsId(undefined, undefined, undefined, 0, 0)).toBe('');
        });
        it('it returns empty string for undefined kindContainer', () => {
            const fall = new TSFall();
            const gemeinde = new TSGemeinde();
            expect(ebeguUtil.calculateBetreuungsId(undefined, fall, gemeinde, 0, 0)).toBe('');
        });
        it('it returns empty string for undefined betreuung', () => {
            const gesuchsperiode = new TSGesuchsperiode();
            expect(ebeguUtil.calculateBetreuungsId(gesuchsperiode, undefined, undefined, 0, 0)).toBe('');
        });
        it('it returns the right ID: YY(gesuchsperiodeBegin).fallNummer.gemeindeNummer.Kind.Betreuung', () => {
            const fall = new TSFall(254);
            const gemeinde = new TSGemeinde();
            gemeinde.gemeindeNummer = 99;
            const gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
            expect(ebeguUtil.calculateBetreuungsId(gesuchsperiode, fall, gemeinde, 1, 1)).toBe('16.000254.099.1.1');
        });
    });
    describe('getFirstDayGesuchsperiodeAsString', () => {
        it('it returns empty string for undefined Gesuchsperiode', () => {
            expect(ebeguUtil.getFirstDayGesuchsperiodeAsString(undefined)).toBe('');
        });
        it('it returns empty string for undefined daterange in the Gesuchsperiode', () => {
            const gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, undefined);
            gesuchsperiode.gueltigkeit = undefined;
            expect(ebeguUtil.getFirstDayGesuchsperiodeAsString(gesuchsperiode)).toBe('');
        });
        it('it returns empty string for undefined gueltigAb', () => {
            const daterange = new TSDateRange(undefined, moment('31.07.2017', defaultFormat));
            const gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, daterange);
            expect(ebeguUtil.getFirstDayGesuchsperiodeAsString(gesuchsperiode)).toBe('');
        });
        it('it returns 01.08.2016', () => {
            const daterange = new TSDateRange(moment('01.08.2016', defaultFormat),
                moment('31.07.2017', defaultFormat));
            const gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, daterange);
            expect(ebeguUtil.getFirstDayGesuchsperiodeAsString(gesuchsperiode)).toBe('01.08.2016');
        });
    });
    describe('generateRandomName', () => {
        it('it returns a string with 5 characters', () => {
            expect(EbeguUtil.generateRandomName(5).length).toBe(5);
        });
        it('it returns a string with 0 characters', () => {
            expect(EbeguUtil.generateRandomName(0).length).toBe(0);
        });
        it('it returns a string with 52 characters', () => {
            expect(EbeguUtil.generateRandomName(52).length).toBe(52);
        });
        it('it returns a string with 0 characters, for negative sizes', () => {
            expect(EbeguUtil.generateRandomName(-1).length).toBe(0);
        });
    });

    describe('isSameById', () => {
        class Entity extends TSAbstractEntity {
            public constructor(id: string) {
                super();
                this.id = id;
            }

            public static of(id: string): Entity {
                return new Entity(id);
            }
        }

        it('should not allow duplicates', () => {
            expect(EbeguUtil.isSameById([Entity.of('1'), Entity.of('1')], [Entity.of('1')])).toBe(false);
        });

        it('should allow identity', () => {
            const a = [Entity.of('1'), Entity.of('1')];
            expect(EbeguUtil.isSameById(a, a)).toBe(true);
        });

        it('should allow similar arrays', () => {
            expect(EbeguUtil.isSameById([Entity.of('1'), Entity.of('2')], [Entity.of('1'), Entity.of('2')]))
                .toBe(true);
        });

        it('should allow similar arrays - regardless of order', () => {
            expect(EbeguUtil.isSameById([Entity.of('1'), Entity.of('2')], [Entity.of('2'), Entity.of('1')]))
                .toBe(true);
        });

        it('should not require distinct ids', () => {
            expect(EbeguUtil.isSameById([Entity.of('1'), Entity.of('1')], [Entity.of('1'), Entity.of('2')]))
                .toBe(false);
        });
    });
    describe('ZemisNummerToStandardZemisNummer', () => {
        it('Formate sollten in Standard Format konvertiert werden können', () => {
            // tslint:disable-next-line:no-duplicate-string
                const zemis1 = '12345678.9';
                const zemis2 = '012345678.9';
                const zemis3 = '012.345.678.9';
                const zemis4 = '012.345.678-9';
                expect(EbeguUtil.zemisNummerToStandardZemisNummer(zemis1)).toBe('12345678.9');
                expect(EbeguUtil.zemisNummerToStandardZemisNummer(zemis2)).toBe('12345678.9');
                expect(EbeguUtil.zemisNummerToStandardZemisNummer(zemis3)).toBe('12345678.9');
                expect(EbeguUtil.zemisNummerToStandardZemisNummer(zemis4)).toBe('12345678.9');
        });
        it('Formate sollen nicht akzeptiert werden und Fehler werfen', () => {
            const zemis1 = '123.45678.9';
            const zemis2 = '12.345.678.9';
            const zemis3 = '0012.345.678.9';
            const zemis4 = '0012345678-9';
            expect(() => { EbeguUtil.zemisNummerToStandardZemisNummer(zemis1); })
                .toThrow(new Error(`Wrong Format for ZEMIS-Nummer ${zemis1}`));
            expect(() => { EbeguUtil.zemisNummerToStandardZemisNummer(zemis2); })
                .toThrow(new Error(`Wrong Format for ZEMIS-Nummer ${zemis2}`));
            expect(() => { EbeguUtil.zemisNummerToStandardZemisNummer(zemis3); })
                .toThrow(new Error(`Wrong Format for ZEMIS-Nummer ${zemis3}`));
            expect(() => { EbeguUtil.zemisNummerToStandardZemisNummer(zemis4); })
                .toThrow(new Error(`Wrong Format for ZEMIS-Nummer ${zemis4}`));
        });
    });
});
