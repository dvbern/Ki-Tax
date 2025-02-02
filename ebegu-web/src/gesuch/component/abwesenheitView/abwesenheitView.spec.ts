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

import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbwesenheitViewController, KindBetreuungUI} from './abwesenheitView';

describe('abwesenheitView', () => {
    let abwesenheitController: AbwesenheitViewController;
    let gesuchModelManager: GesuchModelManager;
    let wizardStepManager: WizardStepManager;
    let berechnungsManager: BerechnungsManager;
    let errorService: ErrorService;
    let $translate: angular.translate.ITranslateService;
    let dialog: DvDialog;
    let $q: angular.IQService;
    let $scope: angular.IScope;
    let $timeout: angular.ITimeoutService;
    let einstellungRS: EinstellungRS;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            wizardStepManager = $injector.get('WizardStepManager');
            berechnungsManager = $injector.get('BerechnungsManager');
            errorService = $injector.get('ErrorService');
            $translate = $injector.get('$translate');
            dialog = $injector.get('DvDialog');
            $q = $injector.get('$q');
            $scope = $injector.get('$rootScope');
            $timeout = $injector.get('$timeout');
            einstellungRS = $injector.get('EinstellungRS');
        })
    );

    beforeEach(() => {
        spyOn(einstellungRS, 'findEinstellung').and.returnValue(
            of(new TSEinstellung())
        );
        spyOn(gesuchModelManager, 'getGemeinde').and.returnValue(
            new TSGemeinde()
        );
        spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
            new TSGesuchsperiode()
        );
        abwesenheitController = new AbwesenheitViewController(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            dialog,
            $translate,
            $q,
            errorService,
            $scope,
            $timeout,
            einstellungRS
        );
    });

    describe('getNameFromBetroffene', () => {
        it('should return empty string for undefined kindBetreuung', () => {
            const kindBetreuung = new KindBetreuungUI();
            expect(
                abwesenheitController.getTextForBetreuungDDL(kindBetreuung)
            ).toBe('');
        });
        it('should return empty string for empty data', () => {
            const kindBetreuung = new KindBetreuungUI();
            expect(
                abwesenheitController.getTextForBetreuungDDL(kindBetreuung)
            ).toBe('');
        });
        it('should return Name of KindBetreuung', () => {
            const kindBetreuung = new KindBetreuungUI();
            const betreuung = new TSBetreuung();
            const institutionStammdaten = new TSInstitutionStammdaten();
            const ins = new TSInstitution();
            ins.name = 'InstitutionTest';
            institutionStammdaten.institution = ins;
            betreuung.institutionStammdaten = institutionStammdaten;
            kindBetreuung.betreuung = betreuung;

            const kind = new TSKindContainer();
            const kindJA = new TSKind();
            kindJA.vorname = 'Pedrito';
            kindJA.nachname = 'Contreras';
            kind.kindJA = kindJA;
            kindBetreuung.kind = kind;

            expect(
                abwesenheitController.getTextForBetreuungDDL(kindBetreuung)
            ).toBe('Pedrito Contreras - InstitutionTest');
        });
    });

    describe('createAbwesenheit', () => {
        it('should return empty array for empty data', () => {
            expect(abwesenheitController.getAbwesenheiten().length).toBe(0);
            abwesenheitController.createAbwesenheit();
            expect(abwesenheitController.getAbwesenheiten().length).toBe(1);
            expect(abwesenheitController.getAbwesenheiten()[0]).toBeDefined();
            expect(
                abwesenheitController.getAbwesenheiten()[0].kindBetreuung
            ).toBeUndefined();
            expect(
                abwesenheitController.getAbwesenheiten()[0].abwesenheit
            ).toBeDefined();
        });
    });
});
