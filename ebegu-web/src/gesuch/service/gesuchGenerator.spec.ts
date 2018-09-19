/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {async, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import AntragStatusHistoryRS from '../../app/core/service/antragStatusHistoryRS.rest';
import GesuchsperiodeRS from '../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import TSDossier from '../../models/TSDossier';
import TSFall from '../../models/TSFall';
import TSGesuch from '../../models/TSGesuch';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import TSBenutzer from '../../models/TSBenutzer';
import TestDataUtil from '../../utils/TestDataUtil.spec';
import DossierRS from './dossierRS.rest';
import FallRS from './fallRS.rest';
import GemeindeRS from './gemeindeRS.rest';
import {GesuchGenerator} from './gesuchGenerator';
import GesuchRS from './gesuchRS.rest';
import WizardStepManager from './wizardStepManager';

describe('gesuchGenerator', () => {

    const GP_ID = '2222-1111';
    const GESUCH_ID = '33333-1111';
    const DOSSIER_ID = '44444-1111';
    let dossier: TSDossier;
    let fall: TSFall;
    let gesuchGenerator: GesuchGenerator;
    let gesuchsperiode: TSGesuchsperiode;
    let user: TSBenutzer;

    beforeEach(async(() => {

        initValues();

        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, {
            'getAllGemeinden': Promise.resolve(['findGemeinde']),
        });

        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, {
            'isOneOfRoles': true,
        });
        authServiceSpy.principal$ = of(user) as any;

        const dossierServiceSpy = jasmine.createSpyObj<DossierRS>(DossierRS.name, {
            'findDossier': Promise.resolve(dossier)
        });

        const antragStatusHistoryServiceSpy = jasmine.createSpyObj<AntragStatusHistoryRS>(AntragStatusHistoryRS.name,
            ['loadLastStatusChange']);
        antragStatusHistoryServiceSpy.loadLastStatusChange.and.callFake((gesuch: TSGesuch) => Promise.resolve(gesuch));

        const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name, {
            'findGesuchsperiode': Promise.resolve(gesuchsperiode)
        });

        const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(WizardStepManager.name, [
            'setHiddenSteps',
            'initWizardSteps'
        ]);

        const fallServiceSpy = jasmine.createSpyObj<FallRS>(FallRS.name, ['createFall']);

        const gesuchServiceSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, ['createGesuch']);

        TestBed.configureTestingModule({
            imports: [],
            providers: [
                {provide: GesuchRS, useValue: gesuchServiceSpy},
                {provide: AntragStatusHistoryRS, useValue: antragStatusHistoryServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: DossierRS, useValue: dossierServiceSpy},
                {provide: WizardStepManager, useValue: wizardStepManagerSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: FallRS, useValue: fallServiceSpy},
                GesuchGenerator,
            ],
        });

        gesuchGenerator = TestBed.get(GesuchGenerator);
    }));

    describe('initGesuch', () => {
        it('creates a new papier fall, dossier and gesuch. The given fall and dossier should be ignored', async(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_FALL, GP_ID, fall, dossier)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.eingangsart).toBe(TSEingangsart.PAPIER);
                    expect(gesuch.status).toBe(TSAntragStatus.IN_BEARBEITUNG_JA);
                    expect(gesuch.isNew()).toBe(true);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).not.toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).not.toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toBe(user);
                });
        }));
        it('creates a new online fall, dossier and gesuch. The given fall and dossier should be ignored', async(() => {
            gesuchGenerator.initGesuch(TSEingangsart.ONLINE, TSCreationAction.CREATE_NEW_FALL, GP_ID, fall, dossier)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.eingangsart).toBe(TSEingangsart.ONLINE);
                    expect(gesuch.status).toBe(TSAntragStatus.IN_BEARBEITUNG_GS);
                    expect(gesuch.isNew()).toBe(true);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).not.toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).not.toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toBe(user);
                });
        }));
        it('creates a new Gesuch and Dossier linked to the existing fall', async(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_DOSSIER, GP_ID, fall, dossier)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).not.toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toBe(user);
                });
        }));
        it('creates a new Gesuch linked to the existing fall and Dossier', async(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_GESUCH, GP_ID, fall, dossier)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toBe(user);
                });
        }));
    });

    describe('initMutation', () => {
        it('creates a new mutation', async(() => {
            gesuchGenerator.initMutation(GESUCH_ID, TSEingangsart.PAPIER, GP_ID, DOSSIER_ID, fall, dossier)
                .then(mutation => {
                    expect(mutation).toBeDefined();
                    expect(mutation.id).toBe(GESUCH_ID);
                    expect(mutation.isMutation()).toBe(true);
                    expect(mutation.eingangsart).toBe(TSEingangsart.PAPIER);
                    expect(mutation.status).toBe(TSAntragStatus.IN_BEARBEITUNG_JA);
                    expect(mutation.isNew()).toBe(true);
                    expect(mutation.dossier).toBeDefined();
                    expect(mutation.dossier).toBe(dossier);
                    expect(mutation.dossier.fall).toBeDefined();
                    expect(mutation.dossier.fall).toBe(fall);
                    expect(mutation.dossier.verantwortlicherBG).toBe(user);
                });
        }));
    });

    describe('initErneuerungsgesuch', () => {
        it('creates a new Erneuerungsgesuch', async(() => {
            gesuchGenerator.initErneuerungsgesuch(GESUCH_ID, TSEingangsart.PAPIER, GP_ID, DOSSIER_ID, fall, dossier)
                .then(mutation => {
                    expect(mutation).toBeDefined();
                    expect(mutation.id).toBe(GESUCH_ID);
                    expect(mutation.isFolgegesuch()).toBe(true);
                    expect(mutation.eingangsart).toBe(TSEingangsart.PAPIER);
                    expect(mutation.status).toBe(TSAntragStatus.IN_BEARBEITUNG_JA);
                    expect(mutation.isNew()).toBe(true);
                    expect(mutation.dossier).toBeDefined();
                    expect(mutation.dossier).toBe(dossier);
                    expect(mutation.dossier.fall).toBeDefined();
                    expect(mutation.dossier.fall).toBe(fall);
                    expect(mutation.dossier.verantwortlicherBG).toBe(user);
                });
        }));
    });

    function initValues(): void {
        gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
        gesuchsperiode.id = GP_ID;
        user = new TSBenutzer();
        fall = new TSFall();
        dossier = new TSDossier();
        dossier.id = DOSSIER_ID;
        dossier.fall = fall;
    }
});
