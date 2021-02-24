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

import {waitForAsync, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {AntragStatusHistoryRS} from '../../app/core/service/antragStatusHistoryRS.rest';
import {GesuchsperiodeRS} from '../../app/core/service/gesuchsperiodeRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSDossier} from '../../models/TSDossier';
import {TSFall} from '../../models/TSFall';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {DossierRS} from './dossierRS.rest';
import {FallRS} from './fallRS.rest';
import {GemeindeRS} from './gemeindeRS.rest';
import {GesuchGenerator} from './gesuchGenerator';
import {GesuchRS} from './gesuchRS.rest';
import {WizardStepManager} from './wizardStepManager';

describe('gesuchGenerator', () => {

    const gpId = '2222-1111';
    const gesuchId = '33333-1111';
    const dossierId = '44444-1111';
    let dossier: TSDossier;
    let fall: TSFall;
    let gesuchGenerator: GesuchGenerator;
    let gesuchsperiode: TSGesuchsperiode;
    let user: TSBenutzer;
    const gemeinde = new TSGemeinde();

    beforeEach(waitForAsync(() => {

        initValues();

        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, {
            getAllGemeinden: Promise.resolve([gemeinde]),
        });

        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, {
            isOneOfRoles: true,
        });
        authServiceSpy.principal$ = of(user) as any;

        const dossierServiceSpy = jasmine.createSpyObj<DossierRS>(DossierRS.name, {
            findDossier: Promise.resolve(dossier),
        });

        const antragStatusHistoryServiceSpy = jasmine.createSpyObj<AntragStatusHistoryRS>(AntragStatusHistoryRS.name,
            ['loadLastStatusChange']);
        antragStatusHistoryServiceSpy.loadLastStatusChange.and.resolveTo();

        const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name, {
            findGesuchsperiode: Promise.resolve(gesuchsperiode),
        });

        const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(WizardStepManager.name, [
            'setHiddenSteps',
            'initWizardSteps',
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

        gesuchGenerator = TestBed.inject<GesuchGenerator>(GesuchGenerator);
    }));

    describe('initGesuch', () => {
        it('creates a new papier fall, dossier and gesuch. The given fall and dossier should be ignored', waitForAsync(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_FALL, gpId, fall, dossier, null)
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
                    expect(gesuch.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
        it('creates a new online fall, dossier and gesuch. The given fall and dossier should be ignored', waitForAsync(() => {
            gesuchGenerator.initGesuch(TSEingangsart.ONLINE, TSCreationAction.CREATE_NEW_FALL, gpId, fall, dossier, null)
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
                    expect(gesuch.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
        it('creates a new Gesuch and Dossier linked to the existing fall', waitForAsync(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_DOSSIER, gpId, fall, dossier, null)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).not.toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
        it('creates a new Gesuch linked to the existing fall and Dossier', waitForAsync(() => {
            gesuchGenerator.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_GESUCH, gpId, fall, dossier, null)
                .then(gesuch => {
                    expect(gesuch).toBeDefined();
                    expect(gesuch.gesuchsperiode).toBe(gesuchsperiode);
                    expect(gesuch.dossier).toBeDefined();
                    expect(gesuch.dossier).toBe(dossier);
                    expect(gesuch.dossier.fall).toBeDefined();
                    expect(gesuch.dossier.fall).toBe(fall);
                    expect(gesuch.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
    });

    describe('initMutation', () => {
        it('creates a new mutation', waitForAsync(() => {
            gesuchGenerator.initMutation(gesuchId, TSEingangsart.PAPIER, gpId, dossierId, fall, dossier)
                .then(mutation => {
                    expect(mutation).toBeDefined();
                    expect(mutation.id).toBe(gesuchId);
                    expect(mutation.isMutation()).toBe(true);
                    expect(mutation.eingangsart).toBe(TSEingangsart.PAPIER);
                    expect(mutation.status).toBe(TSAntragStatus.IN_BEARBEITUNG_JA);
                    expect(mutation.isNew()).toBe(true);
                    expect(mutation.dossier).toBeDefined();
                    expect(mutation.dossier).toBe(dossier);
                    expect(mutation.dossier.fall).toBeDefined();
                    expect(mutation.dossier.fall).toBe(fall);
                    expect(mutation.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
    });

    describe('initErneuerungsgesuch', () => {
        it('creates a new Erneuerungsgesuch', waitForAsync(() => {
            gesuchGenerator.initErneuerungsgesuch(gesuchId, TSEingangsart.PAPIER, gpId, dossierId, fall, dossier)
                .then(mutation => {
                    expect(mutation).toBeDefined();
                    expect(mutation.id).toBe(gesuchId);
                    expect(mutation.isFolgegesuch()).toBe(true);
                    expect(mutation.eingangsart).toBe(TSEingangsart.PAPIER);
                    expect(mutation.status).toBe(TSAntragStatus.IN_BEARBEITUNG_JA);
                    expect(mutation.isNew()).toBe(true);
                    expect(mutation.dossier).toBeDefined();
                    expect(mutation.dossier).toBe(dossier);
                    expect(mutation.dossier.fall).toBeDefined();
                    expect(mutation.dossier.fall).toBe(fall);
                    expect(mutation.dossier.verantwortlicherBG).toEqual(user.toBenutzerNoDetails());
                });
        }));
    });

    function initValues(): void {
        gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
        gesuchsperiode.id = gpId;
        user = new TSBenutzer();
        fall = new TSFall();
        dossier = new TSDossier();
        dossier.id = dossierId;
        dossier.fall = fall;
    }
});
