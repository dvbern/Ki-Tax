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

import {StateService} from '@uirouter/core';
import {IHttpBackendService, IQService, IScope} from 'angular';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import TSBetreuung from '../../../models/TSBetreuung';
import TSGesuch from '../../../models/TSGesuch';
import TSKindContainer from '../../../models/TSKindContainer';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import {VerfuegenListViewController} from './verfuegenListView';

describe('verfuegenListViewTest', () => {

    const verfuegenView = 'gesuch.verfuegenView';

    let verfuegenListView: VerfuegenListViewController;
    let gesuchModelManager: GesuchModelManager;
    let $state: StateService;
    let tsKindContainer: TSKindContainer;
    let berechnungsManager: BerechnungsManager;
    let $q: IQService;
    let $rootScope: IScope;
    let $httpBackend: IHttpBackendService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));
    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        $state = $injector.get('$state');
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');
        $httpBackend = $injector.get('$httpBackend');
        tsKindContainer = new TSKindContainer();
        tsKindContainer.kindNummer = 1;
        const wizardStepManager: WizardStepManager = $injector.get('WizardStepManager');
        spyOn(wizardStepManager, 'updateWizardStepStatus').and.returnValue({});
        spyOn(gesuchModelManager, 'getKinderWithBetreuungList').and.returnValue([tsKindContainer]);
        spyOn(gesuchModelManager, 'calculateVerfuegungen').and.returnValue($q.when({}));

        spyOn(gesuchModelManager, 'getGesuch').and.returnValue(new TSGesuch());

        berechnungsManager = $injector.get('BerechnungsManager');
        spyOn(berechnungsManager, 'calculateFinanzielleSituation').and.returnValue({});
        spyOn(berechnungsManager, 'calculateEinkommensverschlechterung').and.returnValue({});

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        verfuegenListView = new VerfuegenListViewController($state,
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            null,
            $injector.get('DownloadRS'),
            $injector.get(
                'MahnungRS'),
            $injector.get('AuthServiceRS'),
            $rootScope,
            $injector.get('GesuchRS'),
            $injector.get('$timeout'));
        $rootScope.$apply();
    }));

    describe('Usage API', () => {
        it('should call gesuchModelManager.getBetreuungenList() and return it back', () => {
            const kinderWithetreuungList = verfuegenListView.getKinderWithBetreuungList();
            expect(kinderWithetreuungList).toBeDefined();
            expect(kinderWithetreuungList.length).toBe(1);
            expect(kinderWithetreuungList[0]).toBe(tsKindContainer);
        });
        describe('openVerfuegen', () => {
            it('does not open the betreuung because it is not BESTAETIGT', () => {
                spyOn(gesuchModelManager, 'findKind').and.returnValue(1);
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.ABGEWIESEN;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                // tslint:disable-next-line:no-unbound-method
                expect(gesuchModelManager.findKind).not.toHaveBeenCalled();
            });
            it('does not find the Kind, so it stops loading and does not move to the next page', () => {
                spyOn(gesuchModelManager, 'convertKindNumberToKindIndex').and.returnValue(-1);
                spyOn(gesuchModelManager, 'setKindIndex');
                spyOn($state, 'go');
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                // tslint:disable-next-line:no-unbound-method
                expect(gesuchModelManager.convertKindNumberToKindIndex)
                    .toHaveBeenCalledWith(tsKindContainer.kindNummer);
                // tslint:disable-next-line:no-unbound-method
                expect(gesuchModelManager.setKindIndex).not.toHaveBeenCalled();

                // tslint:disable-next-line:no-unbound-method
                expect($state.go).not.toHaveBeenCalledWith(verfuegenView, {gesuchId: ''});
            });
            it('does find the Kind but does not find the Betreuung, so it stops loading and does not move to the next page',
                () => {
                    spyOn(gesuchModelManager, 'convertKindNumberToKindIndex').and.returnValue(0);
                    spyOn(gesuchModelManager, 'convertBetreuungNumberToBetreuungIndex').and.returnValue(-1);
                    spyOn(gesuchModelManager, 'setKindIndex');
                    spyOn(gesuchModelManager, 'setBetreuungIndex');
                    spyOn($state, 'go');
                    const betreuung = new TSBetreuung();
                    betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;

                    verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                    // tslint:disable-next-line:no-unbound-method
                    expect(gesuchModelManager.convertKindNumberToKindIndex)
                        .toHaveBeenCalledWith(tsKindContainer.kindNummer);
                    // tslint:disable-next-line:no-unbound-method
                    expect(gesuchModelManager.setKindIndex).toHaveBeenCalledWith(0);
                    // tslint:disable-next-line:no-unbound-method
                    expect(gesuchModelManager.setBetreuungIndex).not.toHaveBeenCalled();
                    // tslint:disable-next-line:no-unbound-method
                    expect($state.go).not.toHaveBeenCalledWith(verfuegenView, {gesuchId: ''});
                });
            it('does find the Kind but does not find the Betreuung, so it stops loading and does not move to the next page',
                () => {
                    spyOn(gesuchModelManager, 'convertKindNumberToKindIndex').and.returnValue(0);
                    spyOn(gesuchModelManager, 'convertBetreuungNumberToBetreuungIndex').and.returnValue(1);
                    spyOn(gesuchModelManager, 'setKindIndex');
                    spyOn(gesuchModelManager, 'setBetreuungIndex');
                    spyOn($state, 'go');
                    const betreuung = new TSBetreuung();
                    betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;
                    betreuung.betreuungNummer = 2;

                    verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                    // tslint:disable-next-line:no-unbound-method
                    expect(gesuchModelManager.convertKindNumberToKindIndex)
                        .toHaveBeenCalledWith(tsKindContainer.kindNummer);
                    // tslint:disable-next-line:no-unbound-method
                    expect(gesuchModelManager.setKindIndex).toHaveBeenCalledWith(0);
                    // tslint:disable-next-line:no-unbound-method
                    expect($state.go).toHaveBeenCalledWith(verfuegenView, {
                        betreuungNumber: 2,
                        kindNumber: 1,
                        gesuchId: undefined,
                    });
                });
        });
    });

});
