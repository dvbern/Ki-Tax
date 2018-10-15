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
import {Transition} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {ignoreNullAndUndefined} from '../../../utils/rxjs-operators';
import {LogFactory} from '../../core/logging/LogFactory';
import {getEntityTargetState} from '../einladung-routing/einladung-helpers';

const LOG = LogFactory.createLog('EinladungAbschliessenComponent');

@Component({
    selector: 'dv-einladung-abschliessen',
    templateUrl: './einladung-abschliessen.component.html',
    styleUrls: ['./einladung-abschliessen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinladungAbschliessenComponent {

    public constructor(
        private readonly transition: Transition,
        private readonly authService: AuthServiceRS,
    ) {
    }

    public next(): void {
        this.authService.principal$
            .pipe(
                ignoreNullAndUndefined(),
                take(1),
                map(principal => {
                        // we are logged: redirect to the new entity
                        return getEntityTargetState(this.transition, principal);
                    },
                ),
            )
            .subscribe(
                target => this.transition.router.stateService.go(target.state(), target.params(), target.options()),
                err => LOG.error(err),
            );
    }
}
