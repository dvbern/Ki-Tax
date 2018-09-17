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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzerStatus} from '../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import {LogFactory} from '../../core/logging/LogFactory';
import BenutzerRS from '../../core/service/benutzerRS.rest';

const LOG = LogFactory.createLog('BenutzerEinladenComponent');

@Component({
    selector: 'dv-benutzer-einladen',
    templateUrl: './benutzer-einladen.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerEinladenComponent {

    public readonly benutzer = new TSBenutzer();
    public readonly excludedRoles: ReadonlyArray<TSRole> = [TSRole.GESUCHSTELLER];

    constructor(
        private readonly benutzerRS: BenutzerRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly stateService: StateService,
    ) {
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }

        this.benutzer.status = TSBenutzerStatus.EINGELADEN;
        this.benutzer.username = this.benutzer.email;
        this.benutzer.mandant = this.authServiceRS.getPrincipal().mandant;

        this.benutzerRS.einladen(this.benutzer)
            .then(() => this.stateService.go('admin.benutzerlist'));
    }

}
