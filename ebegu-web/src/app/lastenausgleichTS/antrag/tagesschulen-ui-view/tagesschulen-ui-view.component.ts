/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService, TransitionService} from '@uirouter/core';

@Component({
    selector: 'dv-tagesschulen-ui-view',
    templateUrl: './tagesschulen-ui-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagesschulenUiViewComponent implements OnInit {

    public constructor(
        private readonly $state: StateService,
        private readonly $transition: TransitionService,
    ) {
    }

    public ngOnInit(): void {
        this.$transition.onFinish({to: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN'}, () => {
            this.$state.go('LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.LIST');
        });

        if (this.$state.is('LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN')) {
            this.$state.go('LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.LIST');
        }

    }

}
