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

import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {TSDokumentGrundPersonType} from '../../../../models/enums/TSDokumentGrundPersonType';
import {TSDokumentGrund} from '../../../../models/TSDokumentGrund';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../models/TSGesuchstellerContainer';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {CORE_JS_MODULE} from '../../core.angularjs.module';
import {DVDokumenteListController} from './dv-dokumente-list';

/* eslint-disable */
describe('dvDokumenteList', () => {
    let controller: DVDokumenteListController;
    let gesuchModelManager: GesuchModelManager;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');

            controller = new DVDokumenteListController(
                undefined,
                gesuchModelManager,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined,
                undefined
            );
        })
    );

    describe('extractFullName', () => {
        it('should return the fullName of GS1 for GESUCHSTELLER and personNumber=1', () => {
            const dokumentGrund = new TSDokumentGrund();
            dokumentGrund.personType = TSDokumentGrundPersonType.GESUCHSTELLER;
            dokumentGrund.personNumber = 1;
            dokumentGrund.fullName = 'Leonardo Dantes'; // even though this is set it shouldn't take it
            mockGesuch();
            expect(controller.extractFullName(dokumentGrund)).toBe(
                'Leonardo Primero'
            );
        });
        it('should return empty string for GESUCHSTELLER and personNumber=null', () => {
            const dokumentGrund = new TSDokumentGrund();
            dokumentGrund.personType = TSDokumentGrundPersonType.GESUCHSTELLER;
            dokumentGrund.personNumber = undefined;
            dokumentGrund.fullName = 'Leonardo Dantes'; // even though this is set it shouldn't take it
            mockGesuch();
            expect(controller.extractFullName(dokumentGrund)).toBe('');
        });
        it('should return the fullName of GS1 for GESUCHSTELLER and personNumber=2', () => {
            const dokumentGrund = new TSDokumentGrund();
            dokumentGrund.personType = TSDokumentGrundPersonType.GESUCHSTELLER;
            dokumentGrund.personNumber = 2;
            dokumentGrund.fullName = 'Leonardo Dantes'; // even though this is set it shouldn't take it
            mockGesuch();
            expect(controller.extractFullName(dokumentGrund)).toBe(
                'Leonardo Segundo'
            );
        });
        it('should return the fullName of KIND3 for KIND and personNumber=3', () => {
            const dokumentGrund = new TSDokumentGrund();
            dokumentGrund.personType = TSDokumentGrundPersonType.KIND;
            dokumentGrund.personNumber = 3;
            dokumentGrund.fullName = 'Leonardo Dantes'; // even though this is set it shouldn't take it
            mockGesuch();
            expect(controller.extractFullName(dokumentGrund)).toBe(
                'Leonardo Hijo'
            );
        });
        it('should return emptz string for a not existing KIND', () => {
            const dokumentGrund = new TSDokumentGrund();
            dokumentGrund.personType = TSDokumentGrundPersonType.KIND;
            dokumentGrund.personNumber = 6;
            dokumentGrund.fullName = 'Leonardo Dantes'; // even though this is set it shouldn't take it
            mockGesuch();
            expect(controller.extractFullName(dokumentGrund)).toBe('');
        });
    });

    function mockGesuch(): void {
        const gesuch = new TSGesuch();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        spyOn(gesuch.gesuchsteller1, 'extractFullName').and.returnValue(
            'Leonardo Primero'
        );
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        spyOn(gesuch.gesuchsteller2, 'extractFullName').and.returnValue(
            'Leonardo Segundo'
        );

        const kind = new TSKindContainer();
        kind.kindJA = new TSKind();
        spyOn(kind.kindJA, 'getFullName').and.returnValue('Leonardo Hijo');
        kind.kindNummer = 3;
        gesuch.kindContainers = [kind];

        spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
    }
});
