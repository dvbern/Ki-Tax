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
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('BenutzerEinladenComponent');

@Component({
    selector: 'dv-benutzer-einladen',
    templateUrl: './benutzer-einladen.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerEinladenComponent {

    public readonly benutzer = new TSBenutzer();
    public readonly excludedRoles: ReadonlyArray<TSRole> = [TSRole.GESUCHSTELLER];

    public onSubmit(form: NgForm): void {
        LOG.info('is valid', form.valid);
        LOG.info('errors', form.errors);
        LOG.info('values', form.value);
    }

}
