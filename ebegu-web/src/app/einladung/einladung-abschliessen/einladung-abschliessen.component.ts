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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TargetState, Transition} from '@uirouter/core';
import {TSEinladungTyp} from '../../../models/enums/TSEinladungTyp';
import TSBenutzer from '../../../models/TSBenutzer';
import {getEntityTargetState} from '../einladung-routing/einladung-helpers';

@Component({
    selector: 'dv-einladung-abschliessen',
    templateUrl: './einladung-abschliessen.component.html',
    styleUrls: ['./einladung-abschliessen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinladungAbschliessenComponent {

    @Input() public principal: TSBenutzer;

    public constructor(
        private readonly transition: Transition,
    ) {
    }

    public next(): void {
        const target = this.setParamIsRegistering(
            getEntityTargetState(this.transition, this.principal)
        );
        this.transition.router.stateService.go(target.state(), target.params(), target.options());
    }

    /**
     * For the typ GEMEINDE and INSTITUTION we must set the param isRegistering to true, so we know that
     * we are registering the institution for the first time. All other "typ" won't need this param.
     */
    private setParamIsRegistering(target: TargetState): TargetState {
        const typ = this.transition.params().typ;
        if (typ === TSEinladungTyp.GEMEINDE || typ === TSEinladungTyp.INSTITUTION) {
            return target.withParams({
                isRegistering: true,
            });
        }
        return target;
    }
}
