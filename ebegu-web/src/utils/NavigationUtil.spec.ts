/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {TSRole} from '../models/enums/TSRole';
import {NavigationUtil} from './NavigationUtil';

describe('NavigationUtil', () => {

    const gesuchId = '1111-1111';

    describe('navigateToStartsiteOfGesuchForRole', () => {
        it('should navigate to Betreuungen for Institution', () => {
            const stateServiceSpy = jasmine.createSpyObj('StateService', ['go']);
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                TSRole.SACHBEARBEITER_INSTITUTION,
                stateServiceSpy,
                gesuchId);

            expect(stateServiceSpy.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId});
        });
        it('should navigate to Betreuungen for Traegerschaft', () => {
            const stateServiceSpy = jasmine.createSpyObj('StateService', ['go']);
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
                stateServiceSpy,
                gesuchId);

            expect(stateServiceSpy.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId});
        });
        it('should navigate to familiensituation for Steueramt', () => {
            const stateServiceSpy = jasmine.createSpyObj('StateService', ['go']);
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                TSRole.STEUERAMT,
                stateServiceSpy,
                gesuchId);

            expect(stateServiceSpy.go).toHaveBeenCalledWith('gesuch.familiensituation', {gesuchId});
        });
        it('should navigate to fallcreation for other roles', () => {
            const stateServiceSpy = jasmine.createSpyObj('StateService', ['go']);
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                TSRole.SACHBEARBEITER_BG,
                stateServiceSpy,
                gesuchId);

            expect(stateServiceSpy.go).toHaveBeenCalledWith('gesuch.fallcreation', {gesuchId});
        });
        it('should navigate to gemeindeantraege for ferienbetreuung roles', () => {
            const stateServiceSpy = jasmine.createSpyObj('StateService', ['go']);
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                TSRole.ADMIN_FERIENBETREUUNG,
                stateServiceSpy,
                gesuchId);

            expect(stateServiceSpy.go).toHaveBeenCalledWith('gemeindeantrage.view');
        });
    });
});
