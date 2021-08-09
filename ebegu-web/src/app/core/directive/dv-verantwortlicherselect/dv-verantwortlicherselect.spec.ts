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
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {TSDossier} from '../../../../models/TSDossier';
import {TSGesuch} from '../../../../models/TSGesuch';
import {CORE_JS_MODULE} from '../../core.angularjs.module';
import {BenutzerRSX} from '../../service/benutzerRSX.rest';
import {VerantwortlicherselectController} from './dv-verantwortlicherselect';
import ITranslateService = angular.translate.ITranslateService;

describe('dvVerantwortlicherSelect', () => {

    let gesuchModelManager: GesuchModelManager;
    let verantwortlicherselectController: VerantwortlicherselectController;
    let benutzerRS: BenutzerRSX;
    let benutzer: TSBenutzerNoDetails;
    let $translate: ITranslateService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        benutzerRS = $injector.get('BenutzerRS');
        benutzer = new TSBenutzerNoDetails('Emiliano', 'Camacho');
        $translate = $injector.get('$translate');

        verantwortlicherselectController = new VerantwortlicherselectController(benutzerRS,
            gesuchModelManager,
            $translate);
    }));

    describe('getVerantwortlicherFullName', () => {
        it('returns empty string for empty verantwortlicherBG', () => {
            expect(verantwortlicherselectController.getVerantwortlicherFullName()).toEqual('kein Verant.');
        });

        it('returns the fullname of the verantwortlicherBG', () => {
            const verantwortlicher = new TSBenutzerNoDetails('Emiliano', 'Camacho');
            const gesuch = new TSGesuch();
            gesuch.dossier = new TSDossier();
            gesuch.dossier.verantwortlicherBG = verantwortlicher;
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(verantwortlicherselectController.getVerantwortlicherFullName()).toEqual('Emiliano Camacho');
        });
    });
    describe('setVerantwortlicher()', () => {
        it('does nothing if the passed user is empty, verantwortlicherBG remains as it was before', () => {
            createGesuch();
            spyOn(gesuchModelManager, 'setUserAsFallVerantwortlicherBG');

            verantwortlicherselectController.setVerantwortlicher(undefined);
            expect(gesuchModelManager.getGesuch().dossier.verantwortlicherBG).toBe(benutzer);
        });
        it('sets the user as the verantwortlicherBG of the current fall', () => {
            createGesuch();
            spyOn(gesuchModelManager, 'setUserAsFallVerantwortlicherBG');

            const newUser = new TSBenutzerNoDetails('Adolfo', 'Contreras');
            verantwortlicherselectController.setVerantwortlicher(newUser);
            expect(gesuchModelManager.getGesuch().dossier.verantwortlicherBG).toBe(newUser);
        });
    });

    function createGesuch(): void {
        const gesuch = new TSGesuch();
        const dossier = new TSDossier();
        dossier.verantwortlicherBG = benutzer;
        gesuch.dossier = dossier;
        gesuchModelManager.setGesuch(gesuch);
    }

});
