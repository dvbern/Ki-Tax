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

import {StateService} from '@uirouter/core';
import {IComponentOptions, ILogService, IPromise} from 'angular';
import {Permission} from '../../../app/authorisation/Permission';
import {PERMISSIONS} from '../../../app/authorisation/Permissions';
import {AngularXBenutzerRS} from '../../../app/core/service/angularXBenutzerRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSUserSearchresultDTO} from '../../../models/TSUserSearchresultDTO';
import {AbstractAdminViewController} from '../../abstractAdminView';

export class BenutzerListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        benutzer: '<',
    };
    public template = require('./benutzerListView.html');
    public controller = BenutzerListViewController;
    public controllerAs = 'vm';
}

export class BenutzerListViewController extends AbstractAdminViewController {

    public static $inject: string[] = ['$state', '$log', 'AuthServiceRS', 'BenutzerRS'];

    public totalResultCount: string = '0';
    public readonly PERMISSION_BENUTZER_EINLADEN = PERMISSIONS[Permission.BENUTZER_EINLADEN];

    public constructor(
        private readonly $state: StateService,
        private readonly $log: ILogService,
        authServiceRS: AuthServiceRS,
        private readonly benutzerRS: AngularXBenutzerRS,
    ) {
        super(authServiceRS);
    }

    public passFilterToServer = (tableFilterState: any): IPromise<TSUserSearchresultDTO> => {
        this.$log.debug('Triggering ServerFiltering with Filter Object', tableFilterState);

        return this.benutzerRS.searchUsers(tableFilterState).then((response: TSUserSearchresultDTO) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize.toString() : '0';

            return response;
        });
    }

    /**
     * Fuer Benutzer mit der Rolle SACHBEARBEITER_INSTITUTION oder SACHBEARBEITER_TRAEGERSCHAFT oeffnet es das Gesuch
     * mit beschraenkten Daten Fuer anderen Benutzer wird das Gesuch mit allen Daten geoeffnet
     */
    public editBenutzer(user?: TSBenutzer): void {
        if (user) {
            this.$state.go('admin.benutzer', {benutzerId: user.username});
        }
    }

    public onBenutzerEinladen(): void {
        this.$state.go('benutzer.einladen');
    }
}
