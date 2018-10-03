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
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import TSBenutzer from '../../../models/TSBenutzer';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import TSMitteilung from '../../../models/TSMitteilung';
import {POSTEINGANG_JS_MODULE} from '../posteingang.module';

// tslint:disable:no-magic-numbers no-duplicate-string
describe('posteingangFilter', () => {

    let posteingangFilter: any;
    let mitteilungArray: Array<TSMitteilung> = [];
    let mitteilung1: TSMitteilung;
    let mitteilung2: TSMitteilung;
    let mitteilung3: TSMitteilung;
    let mitteilung4: TSMitteilung;
    let mitteilung5: TSMitteilung;

    beforeEach(angular.mock.module(POSTEINGANG_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        posteingangFilter = $injector.get('$filter')('posteingangFilter');

        const ja1 = new TSBenutzer();
        ja1.nachname = 'Blaser';
        ja1.vorname = 'Kurt';

        const ja2 = new TSBenutzer();
        ja2.nachname = 'Becker';
        ja2.vorname = 'Julian';

        const gesuchsteller1 = new TSBenutzer();
        gesuchsteller1.nachname = 'Berger';
        gesuchsteller1.vorname = 'Michael';
        const fall1 = new TSFall();
        fall1.fallNummer = 112;
        fall1.besitzer = gesuchsteller1;
        const dossier1 = new TSDossier();
        dossier1.fall = fall1;

        const gesuchsteller2 = new TSBenutzer();
        gesuchsteller2.nachname = 'Gerber';
        gesuchsteller2.vorname = 'Emma';
        const fall2 = new TSFall();
        fall2.fallNummer = 108;
        fall2.besitzer = gesuchsteller2;
        const dossier2 = new TSDossier();
        dossier2.fall = fall2;

        const fallNoBesitzer = new TSFall();
        fallNoBesitzer.fallNummer = 1010;
        fallNoBesitzer.besitzer = undefined;
        const dossierNoBesitzer = new TSDossier();
        dossierNoBesitzer.fall = fallNoBesitzer;

        mitteilungArray = [];

        mitteilung1 = new TSMitteilung(dossier1,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller1,
            ja1,
            'Frage zum IAM',
            'Warum ist die Banane krumm?',
            TSMitteilungStatus.NEU,
            moment('2016-01-01'));
        mitteilungArray.push(mitteilung1);

        mitteilung2 = new TSMitteilung(dossier1,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller1,
            ja1,
            'Adressänderung',
            'Unsere neue Adresse lautet...',
            TSMitteilungStatus.NEU,
            moment('2016-02-02'));
        mitteilungArray.push(mitteilung2);

        mitteilung3 = new TSMitteilung(dossier2,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller2,
            ja2,
            'Frage zu Dokumentupload',
            'Welche Dokumente kann ich...',
            TSMitteilungStatus.NEU,
            moment('2016-03-03'));
        mitteilungArray.push(mitteilung3);

        mitteilung4 = new TSMitteilung(dossier2,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller2,
            ja2,
            'Gesuch freigegeben',
            'Was nun?',
            TSMitteilungStatus.NEU,
            moment('2016-02-02'));
        mitteilungArray.push(mitteilung4);

        mitteilung5 = new TSMitteilung(dossierNoBesitzer,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller2,
            ja2,
            'Gesuch freigegeben',
            'Was nun?',
            TSMitteilungStatus.NEU,
            moment('2016-02-02'));
        mitteilungArray.push(mitteilung5);

    }));

    describe('API usage', () => {
        it('should return an array with only the elements with the given Sender', () => {
            expect(posteingangFilter(mitteilungArray, {sender: 'berger'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {sender: 'er'})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sender: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sender: 'rrr'})).toEqual([]); // no familienname with this
                                                                                     // pattern
        });
        it('should return an array with only the element with the given Fallnummer', () => {
            expect(posteingangFilter(mitteilungArray,
                {dossier: {fall: {fallNummer: '000'}}})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4]);
            expect(posteingangFilter(mitteilungArray,
                {dossier: {fall: {fallNummer: '0001'}}})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4]);
            expect(posteingangFilter(mitteilungArray, {dossier: {fall: {fallNummer: '1'}}})).toEqual([mitteilung1,
                mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray,
                {dossier: {fall: {fallNummer: '12'}}})).toEqual([mitteilung1, mitteilung2]);
        });
        it('should return an array with only the elements with the given Familie (Besitzer)', () => {
            expect(posteingangFilter(mitteilungArray,
                {dossier: {fall: {besitzer: 'berger'}}})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {dossier: {fall: {besitzer: 'er'}}})).toEqual([mitteilung1,
                mitteilung2, mitteilung3, mitteilung4]);
            expect(posteingangFilter(mitteilungArray, {dossier: {fall: {besitzer: ''}}})).toEqual([mitteilung1,
                mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {dossier: {fall: {besitzer: 'rrr'}}})).toEqual([]);
        });
        it('should return an array with only the elements with the given subject', () => {
            expect(posteingangFilter(mitteilungArray, {subject: 'frage'})).toEqual([mitteilung1, mitteilung3]);
            expect(posteingangFilter(mitteilungArray, {subject: 'Dok'})).toEqual([mitteilung3]);
            expect(posteingangFilter(mitteilungArray, {subject: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {subject: 'rrr'})).toEqual([]); // no familienname with this
                                                                                      // pattern
        });
        it('should return an array with only the elements of the given sentDatum', () => {
            expect(posteingangFilter(mitteilungArray, {sentDatum: '02.02.2016'})).toEqual([mitteilung2, mitteilung4,
                mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sentDatum: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sentDatum: '2016-05-05'})).toEqual([]);
        });
        it('should return an array with only the elements of the given verantwortlicherBG/empfaenger', () => {
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'Julian Becker'})).toEqual([mitteilung3, mitteilung4,
                mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'Blaser'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'ser'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3,
                mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'rrr'})).toEqual([]);
        });
    });
});
